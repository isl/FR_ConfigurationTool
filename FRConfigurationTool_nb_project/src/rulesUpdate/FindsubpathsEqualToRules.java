/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rulesUpdate;

import GUI.ToolInterface;
import RepoEdit.MRepositoryManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import org.openrdf.repository.RepositoryException;
import subrelationshipsfinder.CompareFundamentalWithNewRel;
import subrelationshipsfinder.FundamentalCategories;
import subrelationshipsfinder.FundamentalCategories.Category;
import subrelationshipsfinder.FundamentalRelationshipsPerCategoryPaths;
import subrelationshipsfinder.NewRelationship;
import tools.FileReader;

/**
 * 
 * @author katetzob
 */
public class FindsubpathsEqualToRules {

    private static void setMrm(MRepositoryManager mrm) {
        FindsubpathsEqualToRules.mrm = mrm;
    }
    private static MRepositoryManager mrm;

    /** Reads from the .pie file the custom crm rules and checks if they exist in the FRs.
     * The .pie filepath is read from the path.conf file under the variable repositoryPieFile
     * @param mrm 
     * @return  
     */
    public static String findRulesInFRs(MRepositoryManager mrm) {
        String result = "";
       
          //  MRepositoryManager mrm = new MRepositoryManager("http://139.91.183.87:8080/openrdf-sesame", "Katerina");
            String pie = FileReader.readPathConfigFile("repositoryPieFile");
            RulesSet rulesSet = FileReader.readCustomCRMRulesFromPieFile(pie);

            // ArrayList<Rule> rulesList = rulesSet.rulesSet;
            for (Rule rule : rulesSet.rulesSet) {
                String rulePath = "";
                String consPath = "";
                String res = "";

                if (rule.getPremises() != null) {

                    for (int i = 0; i < rule.getPremises().size(); i++) {

                        String triple = rule.getPremises().get(i);
                        //if it is the starting triple and it is not the only one
                      
                        if (rule.getPremises().size() == 1) {
                            rulePath = triple;
                        } //if there are more than one triples and this is the first one
                        else if (i == 0) {
                            rulePath = triple + ":";
                        } //if there  are more than one triples and this is not the last one
                        else if (i != rule.getPremises().size() - 1) {
                            rulePath += "{" + triple + ":";
                        } else if (i == rule.getPremises().size() - 1) {
                            rulePath += "{" + triple;
                            for (int j = 0; j < rule.getPremises().size() - 1; j++) {
                                rulePath += "}";
                            }
                        }

                    }
                    for (int i = 0; i < rule.getConsequences().size(); i++) {

                        String triple = rule.getConsequences().get(i);
                      
                        consPath += triple;


                    }
                
                    res = subRelFind(rulePath, mrm);
                    if (!res.equalsIgnoreCase("Given Path is not subpath of any Fundamental Relationship.")) {


                        result += "The rule with ID: " + rule.getName() + " which corresponds to the path: \n"
                                + rulePath + " \n can be applied to the following FRs.\n"
                                + res + ". \nTo replace it, you can use one of the following consequences: \n";
                        for (int i = 0; i < rule.getConsequences().size(); i++) {

                            String triple = rule.getConsequences().get(i);
                          
                        
                            result += triple + "\n";


                        }
                        result += "-------------------------------------\n";
                    }

                
                }


            }

   
        return result;
    }

    static String subRelFind(String pathFromUser, MRepositoryManager mrmb) {
        setMrm(mrmb);

        //FOR FUNDAMENTAL RELATIONSHIPS

        Hashtable<Category, FundamentalRelationshipsPerCategoryPaths> allCategoriesPaths = new Hashtable<Category, FundamentalRelationshipsPerCategoryPaths>();
        String fundamentalFolder = System.getProperty("user.dir") + System.getProperty("file.separator")
                + FileReader.readPathConfigFile("fundamentalFolder");
        File dir = new File(fundamentalFolder);
        //find for each category the subpaths table for each FR (represented by the respective dir)
        
        //for (Category categ : Category.values()) {
        for (Category categ : FundamentalCategories.values) {//updated by Anastasia
            FundamentalRelationshipsPerCategoryPaths CatPaths = new FundamentalRelationshipsPerCategoryPaths();
            CatPaths.setCategory(categ);
            CatPaths.createCategoryPathsTable(dir, categ);
            allCategoriesPaths.put(categ, CatPaths);
        }


        //here treat the given by the user new path
        NewRelationship newRel = new NewRelationship();
        newRel.setPathString(pathFromUser);

        //keep in newRelPathTable the list with all triples per subpath of the new rel
        Hashtable<Integer, ArrayList<String[]>> newRelPathTable = newRel.createPathsTable(mrm);

        ArrayList<String> res;

        ArrayList<String> allResults = new ArrayList<String>();
        //find which FundamentalRelationships to investigate according to categories 
        //in range and domain

        //select the relationships for domain
        
        //for (Category categ : Category.values()) {
        for (Category categ : FundamentalCategories.values) {//updated by Anastasia
            //   Category categ =Category.ACTOR;
            FundamentalRelationshipsPerCategoryPaths categFundRel = allCategoriesPaths.get(categ);
            Enumeration e1 = allCategoriesPaths.keys();
            Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> allPathsofCat = categFundRel.getPathsTable();
            //  Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> allPathsofCatRange = new Hashtable<String, Hashtable<Integer, ArrayList<String[]>>>();



            //now in allPathsofCatRange I have all the paths in interest which I want to
            //check with the newrelationship

            CompareRulePathWithFRpath compare = new CompareRulePathWithFRpath();

            res = compare.findSubRelationinFundamental(newRelPathTable, allPathsofCat, mrm);
            allResults.addAll(res);


        }

        String outputString = "";
        String fileSep = System.getProperty("file.separator");
        if (allResults != null && allResults.size() > 0) {

            for (String rel : allResults) {
                rel = rel.substring(rel.lastIndexOf(fileSep) + 1);
                rel = rel.substring(0, rel.indexOf("Path"));
                outputString += rel + "\n";
            }
        } else {
            outputString = "Given rule can not be found in any Fundamental Relationship.";
        }
        return outputString;
    }
}
