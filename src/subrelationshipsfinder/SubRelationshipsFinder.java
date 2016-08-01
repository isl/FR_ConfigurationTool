/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subrelationshipsfinder;

import GUI.ToolInterface;
import RepoEdit.MRepositoryManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import subrelationshipsfinder.FundamentalCategories.Category;
import tools.FileReader;

/**
 * Finds if the provided path is a subrelationship of an existing FR
 * @author Katerina
 */
public class SubRelationshipsFinder {

    /**
     * 
     * @param mrm
     */
    public static void setMrm(MRepositoryManager mrm) {
        SubRelationshipsFinder.mrm = mrm;
    }
   
    private static MRepositoryManager mrm;

    /**
     * Given a user defined path, this method finds if it is a sub-relationship of
     * an FR. The FRs are read from the respective folder, specified in the path.conf
     * file at the variable 'fundamentalFolder'
     * @param pathFromUser  The user defined path
     * @param mrmb The repository manager (passed from the ToolInterface.java)
     * @return the found FRs  
     */
    public static String subRelFind(String pathFromUser,MRepositoryManager mrmb) {
        setMrm(mrmb);

        //FOR FUNDAMENTAL RELATIONSHIPS

        Hashtable<Category, FundamentalRelationshipsPerCategoryPaths> allCategoriesPaths = new Hashtable<Category, FundamentalRelationshipsPerCategoryPaths>();
        String fundamentalFolder = System.getProperty("user.dir") + System.getProperty("file.separator")
             +FileReader.readPathConfigFile("fundamentalFolder");
        File dir = new File(fundamentalFolder);
       //find for each category the subpaths table for each FR (represented by the respective dir)
       
        for (Category categ : FundamentalCategories.values) {//updated by Anastasia
        //for (Category categ : Category.values()) {
            FundamentalRelationshipsPerCategoryPaths CatPaths = new FundamentalRelationshipsPerCategoryPaths();
            CatPaths.setCategory(categ);
            CatPaths.createCategoryPathsTable(dir, categ);
            allCategoriesPaths.put(categ, CatPaths);
        }
        
        


     //here treat the given by the user new path
        NewRelationship newRel = new NewRelationship();
        newRel.setPathString(pathFromUser);
        newRel.setDomain(mrm);
        newRel.setRange(mrm);
        Category domain = newRel.getDomain();
        Category range = newRel.getRange();
        ToolInterface.range = range;
        ToolInterface.domain = domain;
        //keep in newRelPathTable the list with all triples per subpath of the new rel
        Hashtable<Integer, ArrayList<String[]>> newRelPathTable = newRel.createPathsTable(mrm);


        //find which FundamentalRelationships to investigate according to categories 
        //in range and domain

        //select the relationships for domain
        FundamentalRelationshipsPerCategoryPaths categFundRel = allCategoriesPaths.get(domain);
        Enumeration e1 = allCategoriesPaths.keys();
        Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> allPathsofCat = categFundRel.getPathsTable();
        Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> allPathsofCatRange = new Hashtable<String, Hashtable<Integer, ArrayList<String[]>>>();

        //keep only the records of the table that regard the range

        if (allPathsofCat != null) {
//keys here refer to the FR names
            Enumeration e = allPathsofCat.keys();
            while (e.hasMoreElements()) {
                String relName = (String) e.nextElement();
                //if the name of the path contains the range name then add it to 
                //the new hashtable
                if (!range.toString().equalsIgnoreCase(domain.toString())) {
                    if (relName.contains(range.toString())) {
                        Hashtable<Integer, ArrayList<String[]>> pathsTable = new Hashtable<Integer, ArrayList<String[]>>();
                        pathsTable = allPathsofCat.get(relName);
                        allPathsofCatRange.put(relName, pathsTable);
                    }
                } else if (range.toString().equalsIgnoreCase(domain.toString())) {
                    if (relName.contains(domain.toString() + "-" + range.toString())) {
                        Hashtable<Integer, ArrayList<String[]>> pathsTable = new Hashtable<Integer, ArrayList<String[]>>();
                        pathsTable = allPathsofCat.get(relName);
                        allPathsofCatRange.put(relName, pathsTable);
                    }
                }
            }

        } else {
            System.out.println("NO EXISTING PATHS FOR DOMAIN AND RANGE GIVEN!");
        }

        //now in allPathsofCatRange I have all the paths in interest which I want to
        //check with the newrelationship

        CompareFundamentalWithNewRel compare = new CompareFundamentalWithNewRel();
        
        ArrayList<String> res = compare.findSubRelationinFundamental(newRelPathTable, allPathsofCatRange,mrm);
        String outputString = "";
        String fileSep = System.getProperty("file.separator");
        if (res != null && res.size() > 0) {

            for (String rel : res) {
                rel = rel.substring(rel.lastIndexOf(fileSep) + 1);
                rel = rel.substring(0, rel.indexOf("Path"));
                outputString += rel + "\n";
            }
        } else {
            outputString = "Given Path is not subpath of any Fundamental Relationship";
        }
        return outputString;
    }
}
