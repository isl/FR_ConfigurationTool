/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subrelationshipsfinder;

import RepoEdit.MRepositoryManager;
import RepoEdit.RepoCom;
import config.SparqlConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.RepositoryException;
import subrelationshipsfinder.FundamentalCategories.Category;
import tools.VariableFileReader;

/**
 * Represents the path the user has provided, which we call here the new relationship.
 * Contains the path, the class domain and range.
 * @author katetzob
 */
public class NewRelationship {

    public static MRepositoryManager mrm;
    private String pathString = "";
    private Category domain;
    private Category range;

    public static void setMrm(MRepositoryManager mrm) {
        NewRelationship.mrm = mrm;
    }

    public Category getRange() {
        return range;
    }

    public void setPathString(String pathString) {
        this.pathString = pathString;
    }

    public String getPathString() {
        return pathString;
    }

    public Category getDomain() {
        return domain;
    }

    public void setDomain(MRepositoryManager mrmb) {

        String startVar = this.pathString.substring(0, this.pathString.indexOf("--"));
        FundamentalRelationshipsPerCategoryPaths FR = new FundamentalRelationshipsPerCategoryPaths();
        //the original path is set in the object of this class as a global variable
        startVar = FR.clear_whiteChar(startVar);
        startVar = clearFromExtraCharacters(startVar);
        Category startVarCat = findCategoryFromVar(startVar, mrmb);
        this.domain = startVarCat;
    }

    public void setRange(MRepositoryManager mrmb) {

        String endVar = this.pathString.substring(this.pathString.lastIndexOf("->") + 2, this.pathString.length());
        FundamentalRelationshipsPerCategoryPaths FR = new FundamentalRelationshipsPerCategoryPaths();
        //the original path is set in the object of this class as a global variable
        endVar = FR.clear_whiteChar(endVar);
        endVar = clearFromExtraCharacters(endVar);
        Category endVarCat = findCategoryFromVar(endVar, mrmb);
        this.range = endVarCat;
    }

    /**
     * This category will take the path provided by the user and split it in the
     * different paths it can be split (NOT the ORs in predicates). The different
     * paths will be stored in a hashtable. The original path must be set in the
     * object's variable pathString using the respective setter method.
     *  
     * @param mrmb
     * @return the paths table 
     */
    public Hashtable<Integer, ArrayList<String[]>> createPathsTable(MRepositoryManager mrmb) {
        mrm = mrmb;

        Hashtable<Integer, ArrayList<String[]>> listTable = new Hashtable<Integer, ArrayList<String[]>>();
        Stack<String> pathsStack = new Stack<String>();
        FundamentalRelationshipsPerCategoryPaths FR = new FundamentalRelationshipsPerCategoryPaths();
        //the original path is set in the object of this class as a global variable
        String path = FR.clear_whiteChar(pathString);
        pathsStack = FR.paths_seperator(path);
        listTable = FR.seperatePathTriples(pathsStack);
        return listTable;

    }

    /**
     * Finds the respective FC given a variable containing a class 
     * @param Var the class to be mapped to a Category
     * @param mrmb
     * @return the category
     */
    public Category findCategoryFromVar(String Var, MRepositoryManager mrmb) {
        setMrm(mrmb);
        if (!Var.contains(":")) {
            Var = addPrefixToClass(Var);
        }

        //String[] categoriesList = {"crm:E55.Type", "crm:E39.Actor", "crm:E70.Thing", "crm:E53.Place", "crm:E5.Event", "crm:E52.Time-Span"};
        boolean categoryFound = false;
        Category category = null;
        
        //for (String cat : categoriesList) {
        for (Category categ : FundamentalCategories.values) {//updated by Anastasia
        	String cat = categ.type;
         
            String sparql = SparqlConfig.prefices
                    + "ASK { "
                    + Var + " rdfs:subClassOf " + cat + "}";


            try {
                categoryFound = RepoCom.Repository_Ask_Query(sparql, mrm);


            } catch (RepositoryException ex) {
                Logger.getLogger(NewRelationship.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(NewRelationship.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(NewRelationship.class.getName()).log(Level.SEVERE, null, ex);
            }
            //because we ask in a certain order, if we get YES as answer then we can break
            //(because Type and Actor are asked before Thing)
            if (categoryFound) {
            	
            	for (Category categ2 : FundamentalCategories.values) {//updated by Anastasia
            		if (categ2.type.equals(cat)){
            			 category = categ2;
            			 break;
            		}
            	}
/*
                switch (cat) {
                    case "crm:E55.Type":
                        category = FundamentalCategories.Category.CONCEPT;
                        break;
                    case "crm:E39.Actor":
                        category = FundamentalCategories.Category.ACTOR;
                        break;
                    case "crm:E70.Thing":
                        category = FundamentalCategories.Category.THING;
                        break;
                    case "crm:E53.Place":
                        category = FundamentalCategories.Category.PLACE;
                        break;
                    case "crm:E52.Time-Span":
                        category = FundamentalCategories.Category.TIME;
                        break;
                    case "crm:E5.Event":
                        category = FundamentalCategories.Category.EVENT;
                        break;
                }//switch
                
                //<editor-fold defaultstate="collapsed" desc="comment">
//                switch (cat) {
//                    case "crm:E55.Type":
//                        category = FundamentalCategories.Category.CONCEPT;
//                        break;
//                        //</editor-fold>
//                }
                break;
*/
            }

        }
        
        if (!categoryFound) {
            System.out.println("THE CATEGORY IS NOT A FUNDAMENTAL ONE");
            return null;
        }

        return category;
    }

    /**
     *  Given a class, it adds to it the prefix (crm: or crmdig: mostly)
     * @param classWithoutPrefix the given class
     * @return the class with prefix
     */
    public String addPrefixToClass(String classWithoutPrefix) {

        String qrSelectNamespace = "";
        String classWithPrefix = "";
        String AssociationTable[][] = VariableFileReader.startLeterAs_read_variables_of("StartLetterAssociation");

        for (int g = 0; g < AssociationTable.length; g++) {
            boolean exist = false;
            String fromFile = AssociationTable[g][0];
            String fromString = classWithoutPrefix.substring(0, 1);
            exist = fromString.equalsIgnoreCase(fromFile);
            qrSelectNamespace = AssociationTable[g][1];
            if (exist) {
                classWithPrefix = qrSelectNamespace + ":" + classWithoutPrefix;
            }
        }
        return classWithPrefix;
    }

    /**
     * Clears the input string from characters that are unnecessary in the current phase
     * and come from the path format
     * @param pre
     * @return 
     */
    private String clearFromExtraCharacters(String pre) {

        pre = pre.replace("{", "").replace("}", "").replace("(", "").replace(")", "").replace("[0,n]", "");

        return pre;
    }
}
