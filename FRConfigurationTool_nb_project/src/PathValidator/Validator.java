/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import RepoEdit.MRepositoryManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import org.openrdf.repository.RepositoryException;
import subrelationshipsfinder.FundamentalCategories.Category;
import subrelationshipsfinder.NewRelationship;

/**
 * Invokes the necessary steps in order to validate syntactically and semantically the path
 * @author Christos, Katerina
 */
public class Validator {

   /**
     * Checks if the user provided path is valid according to the schema and the 
     * paths' syntax. Also takes into consideration the mulitinstantiation and disjoint cases
     * set by the user
     * @param inputStr
     * @param mrm
     * @param multiList
     * @param disjointList
     * @return
     * @throws RepositoryException
     * @throws IOException
     * @throws Exception 
     */
    public static ArrayList validate(String inputStr, MRepositoryManager mrm,
                ArrayList<Class_MultiInstantiation> multiList,
                ArrayList<Class_Disjointness> disjointList)
                throws RepositoryException, IOException, Exception {

        boolean successValidation = false;
        //this is an arrayList containing some objects that are necessary for the 
        //general functionality of the tool, like if the validation was successfull
        //or what the domain and range FCs are
        ArrayList<Object> resultSet = new ArrayList<Object>();
        inputStr = Syntax4.clear_whiteChar(inputStr);
        Category domain = null;
        Category range = null;


        //========= STEP 0 =========//
        /* Check the hooks
         */
        boolean errorhook = Syntax4.check_open_close(inputStr);
        if (errorhook == true) {
            //return;
            resultSet.add(successValidation);
            resultSet.add(domain);
            resultSet.add(range);
            return resultSet;
        }

        System.out.println("============== step 0 - DONE! ==============\n");

        //========= STEP 1 =======//
        // Seperate the paths from the user input
        Stack<String> stackPathSeperatorExceptFirstTriple = Syntax4.paths_seperator(inputStr);

        System.out.println("============== step 1 - DONE! ==============\n");

        //========= STEP 2 =======//
        // Check if the first triple is more than one (has OR term)
        Stack<String> stackPathSeperator = Syntax4.paths_seperator_firstTripleCheck(stackPathSeperatorExceptFirstTriple);
        System.out.println("============== step 2 - DONE! ==============\n");

        //========= STEP 3 =======//
        // Validate the connections between the triples in every path
        boolean errorPathConValid = Quest.triplet_connection_validation(stackPathSeperator, mrm);
        if (errorPathConValid == false) {
            //return;
            resultSet.add(successValidation);
            resultSet.add(domain);
            resultSet.add(range);
            return resultSet;
        }
        System.out.println("============== step 3 - DONE! ==============\n");

        //========= STEP 4 =======//
        // Seperate each path to triples
        Stack<String> stackAbsolutePathSeperatorWithWeights = Syntax4.absolute_path_seperator(stackPathSeperator);
        for (int i=0;i<stackAbsolutePathSeperatorWithWeights.size();i++){
//           System.out.println(stackAbsolutePathSeperatorWithWeights.elementAt(i));
        }
        System.out.println("============== step 4 - DONE! ==============\n");

        //========= STEP 5 =======//
        // Clean the triples from their weights
        Stack<String> stackAbsolutePathSeperatorWithOUTWeights = Syntax4.remove_weights_from_triples(stackAbsolutePathSeperatorWithWeights);
//        for (int i = 0; i < stackAbsolutePathSeperatorWithOUTWeights.size(); i++) {
//            System.out.println(stackAbsolutePathSeperatorWithOUTWeights.elementAt(i));
//        }
        System.out.println("============== step 5 - DONE! ==============\n");

        //========= STEP 6 =======//
        // Validate the triples 

        successValidation = Quest.triplet_validation(stackAbsolutePathSeperatorWithOUTWeights, mrm);
        System.out.println("------------\n"+successValidation+"\n------------------");
        System.out.println("============== step 6 - DONE! ==============\n");
//
      
//        
       
//        /**/
        resultSet.add(successValidation);
        
        if (successValidation) {
              //========= STEP 7 =======//
            // Create and print an intented list
              OutputToIndentedList.createIndentedList(stackPathSeperator);
           System.out.println("============== step 7 - DONE! ==============\n");
            ArrayList<Category> dom_rangeList = findDomainAndRange(inputStr, mrm);
         //   System.out.println("dom_rangeList.get(0) "+dom_rangeList.get(0));
            resultSet.add(dom_rangeList.get(0));
            resultSet.add(dom_rangeList.get(1));
        }
        
        return resultSet;

    }

    private static ArrayList<Category> findDomainAndRange(String input, MRepositoryManager mrm) {
        ArrayList<Category> dom_rangeList = new ArrayList<Category>();
        String domain = input.substring(0, input.indexOf("--"));
        //  System.out.println("domain "+domain);
        String range = "";
        int semicolon = input.lastIndexOf(":");
        int arrow = input.lastIndexOf("->");
        //  System.out.println("semi "+semicolon+" arrow "+arrow);
        if (semicolon > arrow) {
            range = input.substring(semicolon, input.length());
        } else {
            range = input.substring(arrow + 2, input.length());
        }
        domain = domain.replaceAll("\\s", "");
        domain = domain.replaceAll("\\{", "");
        range = range.replaceAll("\\s", "").replaceAll("\\}", "");

        //  System.out.println("range "+range);
        //  System.out.println("domain = " + domain);
        NewRelationship newRel = new NewRelationship();
        newRel.setMrm(mrm);
        Category domainCat = newRel.findCategoryFromVar(domain, mrm);
        Category rangeCat = newRel.findCategoryFromVar(range, mrm);

        dom_rangeList.add(domainCat);
        dom_rangeList.add(rangeCat);

        return dom_rangeList;
    }
}
