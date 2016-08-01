/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import GUI.ToolInterface;
import RepoEdit.MRepositoryManager;
import RepoEdit.RepoCom;
import config.SparqlConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.NodeList;

import tools.VariableFileReader;
import tools.WriteFile;

/**
 *
 * @author Christos, Katerina
 */
public class Quest {
	
 
    /**
     * This function take as input the path in Sting and isolate each pair of 
     * classes and subclasses
     * @param strPath
     * @return
     */
    public static String[] class_subclass_isolator(String strPath) {

        //Stack<String> ClassSubclass = new Stack();
        
        String tmp = strPath;

        // Split the string
        strPath = strPath.replace("--", "@");
        strPath = strPath.replace("->", "@");
        String[] tempTab = strPath.split("@");
        
        if (tempTab.length < 3) {
            System.out.println(tempTab.length);
            System.out.println(tmp);
            String output = tmp;
            WriteFile.write_general(output, "./src/results/OUTPUT", true);
        }
        
      
        return tempTab;
    }

    /*
     * 
     */
    /**
     *  Checks if the triples in the path are connected
     * @param paths
     * @param mrm
     * @return
     * @throws RepositoryException
     * @throws IOException
     * @throws Exception
     */
    public static boolean triplet_connection_validation(Stack<String> paths, MRepositoryManager mrm) throws RepositoryException, IOException, Exception {
        boolean askRangeResponse = false;
      

        /* Check if we have only one path
         */
        if (paths.contains("|")) {

            Stack<String> tempStack = new Stack();
            Stack<String> tempPathConnectionStack = new Stack();

            /* Split the paths at connections arrows
             */
            for (int i = 0; i < paths.size(); i++) {
                //System.out.println(paths.elementAt(i));
                String tempStr = paths.elementAt(i);

                tempStr = tempStr.replace("--", "@");
                tempStr = tempStr.replace("->", "@");
                String[] tempTab = tempStr.split("@");

                for (int j = 0; j < tempTab.length; j++) {
                    tempStack.push(tempTab[j]);
                }

            }


            /* Check if the stack elements contains the path connection character,
             *  if there is the "|"
             */
            for (int i = 0; i < tempStack.size(); i++) {
                if (tempStack.elementAt(i).contains("|")) {
                    tempPathConnectionStack.push(tempStack.elementAt(i));
                }
            }

            /* Create the query and send it to the server
             */
            for (int i = 0; i < tempPathConnectionStack.size(); i++) {
                String[] tempConTab = tempPathConnectionStack.elementAt(i).split("[|]");

                String tempCl = tempConTab[0];
                String tempSubCl = tempConTab[1];

                String qrAskNamespaceCl = "";
                String qrAskNamespaceSbCl = "";


                /* Parse the variables from file
                 */
                String AssociationTable[][] = VariableFileReader.startLeterAs_read_variables_of("StartLetterAssociation");

                /* Making the associations
                 */
                for (int g = 0; g < AssociationTable.length; g++) {
                    /* Associate the predicate
                     */
                    if (AssociationTable[g][0].equals(Character.toString(tempCl.charAt(0)))) {
                        qrAskNamespaceCl = AssociationTable[g][1];
                    }

                    /* Associate the subject
                     */
                    if (AssociationTable[g][0].equals(Character.toString(tempSubCl.charAt(0)))) {
                        qrAskNamespaceSbCl = AssociationTable[g][1];
                    }
                }


                if (!tempCl.equals(tempSubCl)) {
                    /* Crete the ASK query
                     */
                    String qrAsk = SparqlConfig.prefices
                            + "ASK {\n"
                            + qrAskNamespaceSbCl + ":" + tempSubCl + " rdfs:subClassOf " + qrAskNamespaceCl + ":" + tempCl + ".\n"
                            + "}";
                    askRangeResponse = RepoCom.Repository_Ask_Query(qrAsk, mrm);

                } else {
                    askRangeResponse = true;
                }

                /* Check the response of ask query
                 */
                if (!askRangeResponse) {

                    /* Examine the case of being the reverse tmpCl subclass of tempSubCl
                     */
                    String qrAsk = SparqlConfig.prefices
                            + "ASK {\n"
                            + qrAskNamespaceCl + ":" + tempCl + " rdfs:subClassOf " + qrAskNamespaceSbCl + ":" + tempSubCl + ".\n"
                            + "}";
                    askRangeResponse = RepoCom.Repository_Ask_Query(qrAsk, mrm);

                    if (!askRangeResponse) {
                      String   output = "Path Connection Error: The " + tempSubCl + " is NOT subclass of " + tempCl + " or the inverce!";
                        // System.out.println(output);
                        WriteFile.write_general(output, "./src/results/OUTPUT", true);
                        return askRangeResponse;
                    }

                }


            }
        } else {
            askRangeResponse = true;
        }

        return askRangeResponse;

    }


   
    /**
     *  Validate each one triplet of the path
     * @param paths
     * @param mrm
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static boolean triplet_validation(Stack<String> paths, MRepositoryManager mrm) throws IOException, Exception {
        //System.out.println("TRIPLET VALIDATION!");
        /* Clear the stack from useless for this task elements
         */
       String output="";
        for (int i = 0; i < paths.size(); i++) {
            if (paths.elementAt(i).contentEquals("==")) {
                paths.removeElementAt(i);
            }
        }

        /* set Prefixes TODO
         */
        
  /* Anastasia: comment out
   * 
        String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
        String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        String xsd = "http://www.w3.org/2001/XMLSchema#";
        String crm = "http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CIDOC-CRM.rdfs#";
        String crmdig = "http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CRMdig.rdfs#";
        
*/
        
        

        Stack<String> d = new Stack();
        Stack<String> r = new Stack();


        //String response = null;

        for (int i = 0; i < paths.size(); i++) {
            /* Seperate the each path to Subject, Predicate and Object
             */
//            String[] tripleTab = class_subclass_isolator(paths.elementAt(i));
            // Split the string
               String strPathOriginal = paths.elementAt(i);
            String strPath = paths.elementAt(i);
            strPath = strPath.replace("--", "@");
            strPath = strPath.replace("->", "@");
            String[] tripleTab = strPath.split("@");
//            if (tripleTab.length > 1)
            
            
            

//            if (tripleTab.length > 1) {
            if (tripleTab.length == 3) {

                /* Take the Predicate
                 */
//                System.out.println("paths.elementAt(i) " + paths.elementAt(i));
                String subject = tripleTab[0];
                String predicate = tripleTab[1];
                String object = tripleTab[2];

                String qrSelectNamespace = "";
                String qrAskNamespaceSbj = "";
                String qrAskNamespaceObj = "";
                //    System.out.println("PREDICATE " + predicate);

                /* Parse the variables from file
                 */
                String AssociationTable[][] = VariableFileReader.startLeterAs_read_variables_of("StartLetterAssociation");

                /* Making the associations
                 */
                for (int g = 0; g < AssociationTable.length; g++) {
                    /* Associate the predicate
                     */
                    if (AssociationTable[g][0].equals(Character.toString(predicate.charAt(0)))) {
                        qrSelectNamespace = AssociationTable[g][1];
                    }

                    /* Associate the subject
                     */
                    if (AssociationTable[g][0].equals(Character.toString(subject.charAt(0)))) {
                        qrAskNamespaceSbj = AssociationTable[g][1];
                    }

                    /* Associate the object
                     */
                    if (AssociationTable[g][0].equals(Character.toString(object.charAt(0)))) {
                        qrAskNamespaceObj = AssociationTable[g][1];
                    }
                }

                //==================== Select Query about Domain ==================//
                /* Create the SELECT query for the Domain of each predicate
                 */

                String qrSelectDomain = SparqlConfig.prefices
                        + "SELECT DISTINCT ?q WHERE {\n"
                        + qrSelectNamespace + ":" + predicate + " rdfs:domain ?q.\n"
                        + "}";

                boolean askDomainResponse = false;
                String selectDomainResponse = null;

                /* Execute the query
                 */
            try{
                
                RepoCom.Repository_Select_Query(qrSelectDomain, mrm);
            }
            catch (QueryEvaluationException e) {
                   System.out.println("exception in quest"
                           + "");
                   output="The predicate \""+predicate+ "\" in the triple:\""+strPathOriginal+"\" is not correctly written! Please, check it again!";
                    //e.printStackTrace();
                  WriteFile.write_general(output, "./src/results/OUTPUT", true);
                   return false;
                    
            } 
                /* Create a NodeListe an take the value of query response
                 */
                NodeList selectDomainNodelist = tools.Exist.executeGetQuery("//*[local-name() = 'uri']", "./src/results/RESULTS.xml");

                /* Check if there are results to our query
                 */
                if (selectDomainNodelist.getLength() != 0) {

                    /* If there are, take the response and set the prefixes
                     */
                    selectDomainResponse = selectDomainNodelist.item(0).getTextContent().trim();

                    // A temporally string value for checking the return value without prefixes
                    String temp = null;
                    

                  //updated by Anastasia
                    Iterator<String> nsIt = SparqlConfig.nsList.keySet().iterator();
                    while(nsIt.hasNext()){
                    	String ns = nsIt.next();
                    	 if (selectDomainResponse.contains(ns)) {
                             temp = selectDomainResponse.replace(ns, "");
                             selectDomainResponse = selectDomainResponse.replace(ns, SparqlConfig.nsList.get(ns)+":");
                         }
                    }
                    

                    
                    /* Check if the subject is the same selectDomainResponse
                     */
                    if (!subject.equals(temp)) {
                        /* Create the ASK query
                         */
                        String qrAsk = SparqlConfig.prefices
                                + "ASK {\n"
                                + qrAskNamespaceSbj + ":" + subject + " rdfs:subClassOf " + selectDomainResponse + ".\n"
                                + "}";
                        
                        try{
                        askDomainResponse = RepoCom.Repository_Ask_Query(qrAsk, mrm);
                        }
                          catch (QueryEvaluationException e) {
                   System.out.println("exception in quest"
                           + "");
                   output="The subject \""+subject+ "\" in the triple:\""+strPathOriginal+"\" is not correctly written! Please, check it again!";
                    //e.printStackTrace();
                  WriteFile.write_general(output, "./src/results/OUTPUT", true);
                   return false;
                    
            } 
                        

                    } else {
                        askDomainResponse = true;
                    }

                    /* Check the response of ask query
                     */
                    if (!askDomainResponse) {


                        /* Create the ASK query
                         */
                        String qrAsk = SparqlConfig.prefices
                                + "ASK {\n"
                                + selectDomainResponse + " rdfs:subClassOf " + qrAskNamespaceSbj + ":" + subject + ".\n"
                                + "}";
                        askDomainResponse = RepoCom.Repository_Ask_Query(qrAsk, mrm);
                        //  System.out.println("ASKDOMAIN RESPONSE "+askDomainResponse);

                        if (!askDomainResponse) {
                            //      System.out.println("Problem ");

                            //Katerina: add here the disjoint and multi-instantiation case
                            //in this point the subject given is neither superclass nor subclass
                            //of the actual domain of the predicate. So check other possibilities
                             output = "You have used " + subject + " instead of " + temp + " as domain variable\n"
                                    + " for the predicate: " + predicate + " .\n";
                            boolean disjoint = checkDisjointness(temp, subject);
                            boolean multi = false;
                            if (disjoint) {
                                output = "You are not allowed to use " + subject + " instead of " + temp + " as domain variable\n"
                                        + " for the predicate: " + predicate + ".\n" + temp + " and " + subject + " are disjoint.\n ";

                                output += " If you really want to use " + subject + " instead of " + temp
                                        + "\n remove the disjointess case from the disjointness file, by using the"
                                        + " menu button: \"Remove Disjointness case\"";
                            } else {
                                multi = checkMultiInstantiation(temp, subject);

                                //           System.out.println("multi "+multi);
                                if (!multi) {

                                    output += " If you really want to use " + subject + " instead of " + temp
                                            + "\n add this case in the multi-instantiation file, by using the"
                                            + " menu button: \"Add Multi-Instantiation case\"";

                                }

                            }
                            //if multi is false then return the validation error message, else go on

                            if (!multi) {
                                //       String output = "Triples Validation Domain Error: The " + subject + " is NOT subclass of " + selectDomainResponse + " or the inverce!";
                                //     System.out.println(output);
                                WriteFile.write_general(output, "./src/results/OUTPUT", true);
                                return false;
                            }
                        }
                    }


                    //d.push(selectDomainResponse);
                } else {
                    //d.push("00000000000000000000000");
                     output = "The \" " + predicate + " \" is not correctly written! Please, check it again!";
                    // System.out.println(output);
                    WriteFile.write_general(output, "./src/results/OUTPUT", true);
                    return false;
                }
                //================================================================//


                //==================== Select Query about Range ==================//
                /* Create the SELECT query for the Range of each predicate
                 */
              //  System.out.println("OBJECT "+object);
                String qrSelectRange = SparqlConfig.prefices
                        + "SELECT DISTINCT ?q WHERE {\n"
                        + qrSelectNamespace + ":" + predicate + " rdfs:range ?q.\n"
                        + "}";
                
                boolean askRangeResponse = false;
                String selectRangeResponse = null;

                /* Execute the query
                 */
              try{
                RepoCom.Repository_Select_Query(qrSelectRange, mrm);
              }
                catch (QueryEvaluationException e) {
                   System.out.println("exception in quest"
                           + "");
                   output="The predicate \""+predicate+ "\" in the triple:\""+strPathOriginal+"\" is not correctly written! Please, check it again!";
                    //e.printStackTrace();
                  WriteFile.write_general(output, "./src/results/OUTPUT", true);
                   return false;
                    
            } 
              
                /* Create a NodeList and take the value of query response
                 */
                NodeList selectRangeNodelist = tools.Exist.executeGetQuery("//*[local-name() = 'uri']", "./src/results/RESULTS.xml");

                /* Check if there are results to our query
                 */
                if (selectRangeNodelist.getLength() != 0) {

                    /* If there are, take the response and set the prefixes
                     */
                    selectRangeResponse = selectRangeNodelist.item(0).getTextContent().trim();

                    // A temporally string value for checking the return value without prefixes
                    String temp = null;
                    

                    //updated by Anastasia
                    Iterator<String> nsIt = SparqlConfig.nsList.keySet().iterator();
                    while(nsIt.hasNext()){
                    	String ns = nsIt.next();
                    	 if (selectRangeResponse.contains(ns)) {
                             temp = selectRangeResponse.replace(ns, "");
                             selectRangeResponse = selectRangeResponse.replace(ns, SparqlConfig.nsList.get(ns)+":");
                         }
                    }

                    /* Check if the object is the same selectRangeResponse
                   
                    */
                  //  System.out.println("TEMP "+temp);
                    if (!object.equals(temp)) {
                        /* Create the ASK query
                         */
                        String qrAsk = SparqlConfig.prefices
                                + "ASK {\n"
                                + qrAskNamespaceObj + ":" + object + " rdfs:subClassOf " + selectRangeResponse + ".\n"
                                + "}";
                    
                        try{
                        askRangeResponse = RepoCom.Repository_Ask_Query(qrAsk, mrm);}
                          catch (QueryEvaluationException e) {
                   System.out.println("exception in quest"
                           + "");
                   output="The object \""+object+ "\" in the triple:\""+strPathOriginal+"\" is not correctly written! Please, check it again!";
                    //e.printStackTrace();
                  WriteFile.write_general(output, "./src/results/OUTPUT", true);
                   return false;
                    
            } 
                    } else {
                        askRangeResponse = true;
                    }

                    /* Check the response of ask query (the object is not subclass of the default)
                     */
                    if (!askRangeResponse) {


                        /* Crete the ASK query
                         */
                        String qrAsk = SparqlConfig.prefices
                                + "ASK {\n"
                                + selectRangeResponse + " rdfs:subClassOf " + qrAskNamespaceObj + ":" + object + ".\n"
                                + "}";
                        askRangeResponse = RepoCom.Repository_Ask_Query(qrAsk, mrm);


                           //if it is not sub or superclass
                        if (!askRangeResponse) {

                         

                            //Katerina: add here the disjoint and multi-instantiation case
                            //in this point the subject given is neither superclass nor subclass
                            //of the actual domain of the predicate. So check other possibilities
                             output = "You have used " + object + " instead of " + temp + " as range variable\n"
                                    + " for the predicate: " + predicate + " .\n";
                            boolean disjoint = checkDisjointness(temp, object);
                            boolean multi = false;
                            if (disjoint) {
                                output = "You are not allowed to use " + object + " instead of " + temp + " as range variable\n"
                                        + " for the predicate: " + predicate + ".\n" + temp + " and " + object + " are disjoint. \n";
                                output += " If you really want to use " + object + " instead of " + temp
                                        + "\n remove the disjointess case from the disjointness file, by using the"
                                        + " menu button: \"Remove Disjointness case\"";

                            } else {
                                multi = checkMultiInstantiation(temp, object);

                                //           System.out.println("multi "+multi);
                                if (!multi) {

                                    output += " If you really want to use " + object + " instead of " + temp
                                            + "\n add this case in the multi-instantiation file, by using the"
                                            + " menu button: \"Add Multi-Instantiation case\"";

                                }

                            }
                            //if multi is false then return the validation error message, else go on

                            if (!multi) {
                                //       String output = "Triples Validation Domain Error: The " + subject + " is NOT subclass of " + selectDomainResponse + " or the inverce!";
                                //     System.out.println(output);
                                WriteFile.write_general(output, "./src/results/OUTPUT", true);
                                return false;
                            }

//                            String output = "Triples Validation Range Error: The " + object + " is NOT subclass of " + selectRangeResponse + " or the inverce!";
//                      //      System.out.println(output);
//                            WriteFile.write_general(output, "./src/results/OUTPUT", true);
//                            return false;
                        }
                    }


                    //d.push(selectRangeResponse);
                } else {
                    //d.push("00000000000000000000000");
                     output = "The \" " + predicate + " \" in triple: \""+strPathOriginal+"\" is not correctly written! Please, check it again!";
                    WriteFile.write_general(output, "./src/results/OUTPUT", true);
                    //   System.out.println(output);

                    return false;
                }
                //================================================================//

            } //End of If about triple table length
            
            else if(tripleTab.length==1){
          
            }
            else{
                WriteFile.write_general("The triple \" " + paths.elementAt(i)+"\" that you provided is syntactically wrong!"
                        + "\n Please, check that your syntax includes the  signs -- and -> in a correct way! ", "./src/results/OUTPUT", true);
                System.out.println("Error at: " + paths.elementAt(i));
                return false;
            }

        } //End of for about paths stack

         output = "Validation is OK!";
        WriteFile.write_general(output, "./src/results/OUTPUT", true);
        System.out.println("=========================");
        System.out.println("|   Validation is OK!   |");
        System.out.println("=========================");

        //System.out.println(d);
        //System.out.println(r);

        return true;
    }
    
   

    /**
     * checks if subject cannot be used instead of temp
     * @param temp
     * @param subject
     * @return 
     */
    private static boolean checkDisjointness(String temp, String subject) {
        ArrayList<Class_Disjointness> disjointList = ToolInterface.disjointClassList.list;
        //System.out.println("subject |"+subject+"| temp |"+temp+"|");
        boolean disjoint = false;
        for (Class_Disjointness classDisj : disjointList) {
//System.out.println("classDisj.getGivenClass() = " + classDisj.getGivenClass());
            if (!classDisj.getGivenClass().equalsIgnoreCase(subject)) {

                continue;
            } else {
                String[] listOfDisj = classDisj.getDisjointList();
                for (String cls : listOfDisj) {
                    //  cls=cls.replaceAll("\n", "");
                    //   System.out.println("cls "+cls+"|");
                    if (cls.equalsIgnoreCase(temp)) {
                        disjoint = true;
                        break;
                    }
                }
            }
        }

        return disjoint;
    }

    /**
     * checks if temp can be used with the meaning of subject
     * @param temp
     * @param subject
     * @return 
     */
    private static boolean checkMultiInstantiation(String subject, String temp) {
        ArrayList<Class_MultiInstantiation> multiList = ToolInterface.multiClassList.list;
        // System.out.println("subject |"+subject+"| temp |"+temp+"|");
        boolean multi = false;
        for (Class_MultiInstantiation classMulti : multiList) {
            //    System.out.println("classMulti.getGivenClass() = " + classMulti.getGivenClass());
            if (!classMulti.getGivenClass().equalsIgnoreCase(subject)) {
                continue;

            } else {
                String[] listOfMulti = classMulti.getMultiList();
                for (String cls : listOfMulti) {
                    //         System.out.println("cls "+cls);
                    if (cls.equalsIgnoreCase(temp)) {
                        multi = true;
                        //          System.out.println("multi true!");
                        break;
                    }
                }
            }
        }

        return multi;
    }
}
