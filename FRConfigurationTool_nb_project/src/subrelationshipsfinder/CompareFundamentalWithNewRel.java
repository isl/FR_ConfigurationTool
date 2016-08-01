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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.repository.RepositoryException;
import tools.FileReader;

/**
 * Compares a Fundamental Relationship with the provided path
 * @author katetzob
 */
public class CompareFundamentalWithNewRel {

    public void setMrm(MRepositoryManager mrm) {
        this.mrm = mrm;
    }
    private MRepositoryManager mrm;
    private Hashtable<String, Integer> freqOfFRs = new Hashtable<String, Integer>();
    private Hashtable<String, ArrayList<String>> rulesTable;

    /**
     * Takes the paths of the new relationship and compares it with the paths of the 
     * fundamental Relationships of which it can be subrelationship
     * To be a subrelationship means that it must be the same or contain subproperties
     * of one of the different paths a fundamental relationship is divided into. 
     * The different paths are created by split at ORs at subjects, not ORs at properties.
     * @param newRelTable a table with the list of the triples of each subpath of the new relationship
     * @param AllFundamentalTable a table with the list of the triples of each subpath for each fundamental relationship
     * @param mrmb 
     * @return the filename of the fundamental relationship to which the new relationship is subrelationship,
     *         or null if it is not a subrelationship of any
     */
    public ArrayList<String> findSubRelationinFundamental(Hashtable<Integer, ArrayList<String[]>> newRelTable,
            Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> AllFundamentalTable, MRepositoryManager mrmb) {
        setMrm(mrmb);

        //we iterate through the triples of the new relationship 
        ArrayList<String> FoundFRs = new ArrayList<String>();
        //create object that contains rules 
        rulesTable = FileReader.readRulesFile();
        //and find all the keys representing the rules in the database
        boolean AllFound = false;
        //this is used to build the table for keeping track of the paths in interest
        Enumeration FundRelkeys = AllFundamentalTable.keys();
        //initalization of the freqOfFRs table to 0 values all the FRs
        while (FundRelkeys.hasMoreElements()) {
            String relName = (String) FundRelkeys.nextElement();
            freqOfFRs.put(relName, 0);
        }
        Enumeration e3 = newRelTable.keys();
        int numberOfNEWsubpaths = newRelTable.size();

        while (e3.hasMoreElements()) {
            ArrayList<String[]> newRelList = new ArrayList<String[]>();

            newRelList = newRelTable.get(e3.nextElement());
            Enumeration e = AllFundamentalTable.keys();

            /*here we want to check if the current path exists in the paths 
             * of any fundamental relationship, so here we add the code for it
             * 
             */
            FundamantalPaths:
            while (e.hasMoreElements()) {
                //found shows us if the path is found as subpath in the fundamental relationship
                boolean found = false;
                String relName = (String) e.nextElement();
                Hashtable<Integer, ArrayList<String[]>> pathTable = new Hashtable<Integer, ArrayList<String[]>>();
                pathTable = AllFundamentalTable.get(relName);
                int numberOfFundsubpaths = pathTable.size();

                //if and only if the current fundamental relationship has more or equal subpaths 
                //then go inside to compare
                if (numberOfFundsubpaths >= numberOfNEWsubpaths) {
                    Enumeration e2 = pathTable.keys();
                    while (e2.hasMoreElements()) {
                        ArrayList<String[]> listFund = new ArrayList<String[]>();
                        listFund = pathTable.get(e2.nextElement());
                        //for every subpath of the new relationship check the subpaths 
                        //of the fundamental one
                        //this for iterates only once
                        for (String[] newReltable : newRelList) {

                            found = false;

                            //check in all subpaths
                            for (String[] tableFund : listFund) {
                                found = false;
                                int i = 0;
                                if (newReltable.length <= tableFund.length) {

                                    // counter for the rel triple position 

                                    for (String newRelTriple : newReltable) {
                                        newRelTriple = clear_whiteChar(newRelTriple);
                                        //now I want to check one by one the triples 
                                        //in the two string tables
                                        if (newRelTriple.contains("--")) {
                                            String tripleFund = tableFund[i];
                                            tripleFund = clear_whiteChar(tripleFund);

                                            if (tripleFund.equals(newRelTriple)) {
                                                found = true;

                                            } else if (tripleFund.contains("OR")) {
                                               
                                                String result = checkInOR(tripleFund, newRelTriple);

                                                if (result.equalsIgnoreCase("false")) {

                                                    found = false;
                                                } else if (result.equalsIgnoreCase("true")) {

                                                    found = true;
                                                } else if (result.equalsIgnoreCase("Check next")) {
                                                    i++;
                                                    ArrayList<Object> resultList = checkNextTriple(tableFund, newRelTriple, i);
                                                    i = (Integer) resultList.get(0);
                                                    found = (Boolean) resultList.get(1);
                                                }

                                            } else if (tripleFund.contains("[0,n]") && !tripleFund.contains("OR")) {
                                                
                                                String result = checkInTransitive(tripleFund, newRelTriple);

                                                if (result.equalsIgnoreCase("false")) {

                                                    found = false;

                                                } else if (result.equalsIgnoreCase("true")) {

                                                    found = true;
                                                } else if (result.equalsIgnoreCase("Check next")) {

                                                    ArrayList<Object> resultList = checkNextTriple(tableFund, newRelTriple, i);
                                                    i = (Integer) resultList.get(0);
                                                    found = (Boolean) resultList.get(1);

                                                }

                                            } //check if the fundamental id a rule
                                            else if (rulesTable.containsKey(tripleFund)) {
                                                if (rulesTable.get(tripleFund).contains(newRelTriple)) {
                                                    found = true;
                                                } else {
                                                    found = false;
                                                }

                                            } else {
                                                found = checkSubProperty(tripleFund, newRelTriple);

                                            }

                                            if (!found) {

                                                break;
                                            }
                                            i++;
                                        }//the end of all newreltriples 
                                    }

                                }//end of if new triples are less than fund triples 
                                else {
                                    found = false;
                                }

                                int realTableSize = tableFund.length;
                                if (!tableFund[tableFund.length - 1].contains("--")) {
                                    realTableSize = realTableSize - 1;
                                }

                                if (found && i != realTableSize) {
                                    found = false;
                                }

                            }//at this point we have checked all subpaths of fundamental relationship
                            //if one subpath is not found in the subpaths of the fundamental relationship
                            //do not search any more in this relationship 
                            if (!found) {
                                break;
                            }
                        }
                        if (found) {
                            AllFound = found;
                            //since we found the subpath in this FR
                            //increase the occurances in the respective FR in the
                            increaseFreqInTable(relName);
                        }

                    }//the end of interation on triples of subpath of fundamental

                } //this is the end of check on number of subpaths
                else {
                    found = false;
                }
            }//ends the iteration on the fundamental relationships



            if (!AllFound) {
                return FoundFRs;
            } else if (AllFound && !e3.hasMoreElements()) {

                Enumeration finalKeys = freqOfFRs.keys();

                while (finalKeys.hasMoreElements()) {
                    String relName = (String) finalKeys.nextElement();
                    int freq = freqOfFRs.get(relName);
                    if (freq == numberOfNEWsubpaths) {
                        FoundFRs.add(relName);
                    }
                }
                return FoundFRs;
            }
        }
        return FoundFRs;

    }

    /**
     * Clears the white spaces from the provided string
     * @param inputStr
     * @return 
     */
    private String clear_whiteChar(String inputStr) {

        String clearStr = inputStr.replaceAll("OR", "@@@OR@@@");
        clearStr = clearStr.replaceAll("\\s", "");
        clearStr = clearStr.replaceAll("@@@OR@@@", " OR ");
        return clearStr;

    }

    /**
     * Compares the two triples when there is OR in the new triple.
     * @param tripleFund
     * @param newRelTriple
     * @return 
     */
    private String checkInOR(String tripleFund, String newRelTriple) {


        String relProperty = keepProperty(newRelTriple);
        
        relProperty = clearFromExtraCharacters(relProperty);
        relProperty = clearTransitiveness(relProperty);
        
        String[] relProperties = relProperty.split("OR");


        String fundProperty = keepProperty(tripleFund);
        
        fundProperty = clearFromExtraCharacters(fundProperty);
        
        String[] fundProperties = fundProperty.split("OR");

        boolean exist = true;
        //check if all fund properties are transitive 
        boolean trans = true;
        for (String fundProp : fundProperties) {
            if (!fundProp.contains("[0,n]")) {
                trans = false;
            }
        }


        relLoop:
        for (String relProp : relProperties) {
          
            if (exist == false) {
                break;
            }
            for (String fundProp : fundProperties) {
                String fundNotrans = clearTransitiveness(fundProp);
        
                if (relProp.equalsIgnoreCase(fundNotrans)) {
                    continue relLoop;
                } else if (checkSubProperty(fundNotrans, relProp)) {
                    continue relLoop;
                } else if (rulesTable.containsKey(fundNotrans)) {

                    if (rulesTable.get(fundNotrans).contains(relProp)) {

                        continue relLoop;
                    }
                }
            }
            exist = false;
        }

        if (exist == false && trans == true) {
            return "Check next";
        } else if (exist == false && trans == false) {
            return "false";
        } else if (exist == true) {
            return "true";
        } else {
            return "error";
        }

    }

    /**
     * Compares the two triples when there is transitivity in the fundamental one.
     * This means that it removes transitivity and compares the triples
     * @param tripleFund
     * @param newRelTriple
     * @return 
     */
    private String checkInTransitive(String tripleFund, String newRelTriple) {
        String relProperty = keepProperty(newRelTriple);
        relProperty = clearFromExtraCharacters(relProperty);
        relProperty = clearTransitiveness(relProperty);
        String[] relProperties = relProperty.split("OR");


        String fundProperty = keepProperty(tripleFund);
        fundProperty = clearFromExtraCharacters(fundProperty);
        String[] fundProperties = fundProperty.split("OR");
        String fundNotrans = clearTransitiveness(fundProperty);
        
        String res = "Check next";
        for (String relProp : relProperties) {
            relProp = clearTransitiveness(relProp);
            relProp = clear_whiteChar(relProp);
        
            if (relProp.equalsIgnoreCase(fundNotrans)) {
                res = "true";
            } else if (checkSubProperty(fundNotrans, relProp)) {
                res = "true";

            } else if (rulesTable.containsKey(fundNotrans)) {
        
                if (rulesTable.get(fundNotrans).contains(relProp)) {

                    res = "true";
                } else {
                    res = "Check next";
                }
            } else {
                res = "Check next";
                break;
            }
        }
        return res;
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

    /**
     * clears the transitiveness characters
     * 
     */
    private String clearTransitiveness(String pre) {

        pre = pre.replace("(", "").replace(")", "").replace("[0,n]", "");
        return pre;

    }

    /**
     * returns only the property part of the triple
     * @param newRelTriple
     * @return 
     */
    private String keepProperty(String newRelTriple) {

        if (newRelTriple.contains("--")) {
            String tempStr = newRelTriple;
            tempStr = tempStr.replace("--", "@");
            tempStr = tempStr.replace("->", "@");
            String[] tempTab = tempStr.split("@");
            tempStr = tempTab[1];
            newRelTriple = tempStr;
        }
       
        return newRelTriple;
    }

    /**
     * Checks whether or not the new triple's property is subproperty of the respective
     * Fundamental triple's property
     * @param tripleFund
     * @param newRelTriple
     * @return 
     */
    private boolean checkSubProperty(String tripleFund, String newRelTriple) {

        String relProperty = keepProperty(newRelTriple);
        relProperty = clear_whiteChar(relProperty);
        relProperty = clearFromExtraCharacters(relProperty);
        String fundProperty = keepProperty(tripleFund);

        fundProperty = clear_whiteChar(fundProperty);
        fundProperty = clearFromExtraCharacters(fundProperty);
        NewRelationship newRel = new NewRelationship();
        newRel.setMrm(mrm);
        relProperty = newRel.addPrefixToClass(relProperty);
        fundProperty = newRel.addPrefixToClass(fundProperty);

        boolean isSubClass = false;
        String sparql = SparqlConfig.prefices
                + "ASK { "
                + relProperty + " rdfs:subPropertyOf " + fundProperty + "}";
        try {
           
            isSubClass = RepoCom.Repository_Ask_Query(sparql, mrm);

        } catch (RepositoryException ex) {
            Logger.getLogger(NewRelationship.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewRelationship.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(NewRelationship.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isSubClass;
    }

    /**
     * This method is called when we have transitivity in one fundamental property
     * so we have also to check the next Fundamental triple with the current new
     * @param tableFund
     * @param newRelTriple
     * @param i shows us at what place we are on the tableFund, so which triple to check
     * @return 
     */
    private ArrayList<Object> checkNextTriple(String[] tableFund, String newRelTriple, int i) {
        ArrayList<Object> resultsTable = new ArrayList<Object>();
        Boolean found = false;
        boolean goNext = false;
       

        do { 
            
            String tripleFund = tableFund[i];
            tripleFund = clear_whiteChar(tripleFund);
            goNext = false;
            if (tripleFund.equals(newRelTriple)) {
                found = true;
                goNext = false;
            } else if (tripleFund.contains("OR")) {

                String result = checkInOR(tripleFund, newRelTriple);
                if (result.equalsIgnoreCase("false")) {
                    goNext = false;
                    found = false;
                } else if (result.equalsIgnoreCase("true")) {
                    goNext = false;
                    found = true;
                } else if (result.equalsIgnoreCase("Check next")) {
                    i++;
                    goNext = true;
                }

            } else if (tripleFund.contains("[0,n]") && !tripleFund.contains("OR")) {

                String result = checkInTransitive(tripleFund, newRelTriple);

                if (result.equalsIgnoreCase("false")) {

                    found = false;
                } else if (result.equalsIgnoreCase("true")) {

                    found = true;
                } else if (result.equalsIgnoreCase("Check next")) {
                    i++;
                    found = false;
                    goNext = true;
                }

            } else {
                goNext = false;
              //if there are more predicates in the new relationship, then we return false
                if (!newRelTriple.contains("OR")){
                found = checkSubProperty(tripleFund, newRelTriple);}
                else found=false;
            }
        } while (i < tableFund.length && goNext );

        resultsTable.add(i);
        resultsTable.add(found);
        return resultsTable;

    }

    /**
     * This method increases the number that represents how many subpaths of the users path
     * are found in the respective Fundamental Relationship (the name of which is the key
     * of the respective record in the table)
     * @param relName 
     */
    private void increaseFreqInTable(String relName) {
        int freq = freqOfFRs.get(relName);
        freq++;
        freqOfFRs.put(relName, freq);

    }
}
