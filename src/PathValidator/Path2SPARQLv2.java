package PathValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.PathAndDomainVariables;
import tools.VariableFileReader;
import tools.WriteFile;
import tools.TextUtilities;

/**
 * Turns the path to SPARQL
 * @author Katerina
 */
public class Path2SPARQLv2 {

    static String eol = System.getProperty("line.separator");
    static PathAndDomainVariables pathDomainVar = new PathAndDomainVariables();
    static Integer varNo = 0;

    /**
     * This method takes as input parameter a stack with all the complete paths
     * of which the original path provided by the user consists. As a result it
     * returns a string with the SPARQL query.
     * @param inputStr 
     * @return SPARQL query statement
     * 
     */
    public static Hashtable<String, String> SPARQLgen(String inputStr) {

        /* Here I will store as arraylist elements the paths that are elements of the stack.
         * The difference is that i will store them as string arrays that will contain for each path
         * the triples that are generated.
         */

        //re-initialize the varNo everytime the button is pushed
        varNo = 0;
        inputStr = Syntax4.clear_whiteChar(inputStr);
        Stack<String> pathStack = Syntax4.paths_seperator(inputStr);
        Hashtable<Integer, ArrayList<String[]>> listsTable = new Hashtable<Integer, ArrayList<String[]>>();
        listsTable = seperatePathTriples(pathStack);


        /* Here I want to change the format of the paths, so that they include
         * instead of classes, variables
         */
        PathAndDomainVariables pathDom = queryFormatTable(listsTable);


        /* Here I create the Sparql statement from the separeted paths in the qFormatTable
         *
         */


        String query = createSPARQLFromPathsTable(pathDom);

        //write the query in the SPARQL.txt file

        WriteFile.write_general(query, "./src/results/SPARQL.txt", true);

        Hashtable<String, String> results = new Hashtable<String, String>();
        results.put("query", query);
        results.put("domain", pathDomainVar.getStartVarClass());
        results.put("range", pathDomainVar.getEndVarClass());

        return results;
    }

    /**
     * This method changes the format of the query's path style into the format
     * of SPARQL
     * @param oldFormatTable Table that contains the paths in path style
     * @return Table that contains the paths in SPARQL style
     */
    private static PathAndDomainVariables queryFormatTable(Hashtable<Integer, ArrayList<String[]>> oldFormatTable) {

        int iteration = 0;

        Hashtable<Integer, ArrayList<String>> newListsTable = new Hashtable<Integer, ArrayList<String>>();
        ArrayList<String> domainVars = new ArrayList<String>();
        Hashtable<Integer, ArrayList<String[]>> extraPaths = new Hashtable<Integer, ArrayList<String[]>>();




        for (int i = 0; i < oldFormatTable.size(); i++) {


            //for all the elemnts of the arraylist
            for (String[] path : oldFormatTable.get(i)) {

                /* in currentTriple i store the path i am checking and finally
                the first path in the arraylist that has classes and no
                 * query format with $varNo
                 */
                String currentTriple = "";


                /* in queryTriple I store the path in its queryformat with $varNo
                 *
                 */
                String queryTriple = "";
                String withoutTrans = "";
                ArrayList<String> newPaths = new ArrayList<String>();

                for (int j = 0; j < path.length; j++) {
                    currentTriple = path[j];
                    currentTriple = Syntax4.clear_whiteChar(currentTriple);
                    /*
                     * if currentTriple contains more than one elements
                     */
                    if (currentTriple.contains("--")) {
                        String[] split = currentTriple.split("--");
                        String[] split2 = split[1].split("->");
                        String predicate = split2[0];


                        /*
                         * Here i split the predicate at OR if it exists so that
                         * i can add prefix to all predicates
                         */
                        String reg = ".*\\sOR\\s.*";

                        ArrayList<String> predicates = new ArrayList<String>();
                        //if there exists OR
                        if (predicate.matches(reg)) {
                            predicates = splitAtOr(predicate);
                        } //else if OR does not exist
                        else {

                            predicates.add(split2[0]);

                        }

                        /*
                         * Here I add the prefixes to all the predicates and
                         * construct a new string that contains the prefix:predicates
                         * connected with OR. 
                         */


                        String allPredicates = addPrefixToPredicates(predicates);

                        //add the domain variables for the path (only for 2)
                        if (path.length > 0 && j == 1 && path[0].matches(reg)) {
                            domainVars.add("$var" + varNo.toString(varNo));


                        }
                        if (path.length > 1 && j == 2 && path[0].matches(reg) && path[1].matches(reg)) {
                            domainVars.add("$var" + varNo.toString(varNo));


                        }

                        //check for transitivity in predicates
                        withoutTrans = removeTransitivity(allPredicates);
                        boolean trans = false;
                        if (!withoutTrans.equalsIgnoreCase(allPredicates)) {
                            trans = true;
                        }
                        //for the first triple
                        if (j == 0) {

                            //check for transitivity in the first triple
                            if (trans) {

                                ArrayList<String[]> firstList = new ArrayList<String[]>();
                                String[] newfirstpaths = new String[path.length - 1];
                                //the first list is the old list without the first triple

                                for (int size = 1; size < path.length; size++) {
                                    newfirstpaths[size - 1] = path[size];
                                }
                                firstList.add(newfirstpaths);
                                extraPaths.put(iteration, firstList);
                                iteration++;
                                ArrayList<String> newTriples = new ArrayList<String>();
                                if (withoutTrans.matches(reg)) {
                                    newTriples = splitAtOr(withoutTrans);
                                } else {
                                    String triple = "";
                                    triple = withoutTrans;
                                    newTriples.add(triple);
                                }

                                //here I recreate the path format for the first triple that had transitivity
                                for (Iterator<String> it = newTriples.iterator(); it.hasNext();) {
                                    String triple = it.next();
                                    triple = split[0] + "--" + triple + "->" + split2[1];
                                    String[] newpaths = new String[path.length];
                                    newpaths[0] = triple;
                                    for (int size = 1; size < path.length; size++) {
                                        newpaths[size] = path[size];

                                    }
                                    //the newList contains for each predicate of the first transitive
                                    //triple as a starting triple the triple with one of the maybe 
                                    //multiple transitive predicates and then the rest of the triples.
                                    ArrayList<String[]> newList = new ArrayList<String[]>();
                                    newList.add(newpaths);
                                    extraPaths.put(iteration, newList);
                                    iteration++;
                                }
                                //   System.out.println("BREAK FOR Recursion");
                                break;
                            }
                          
                            pathDomainVar.setStartVarClass(path[j].substring(0, path[j].indexOf("--")));
                            //check if the first triple has one or more transitive properties
                            //check if next triple has more than one elements
                        
                            if (path.length == 1 || !path[j + 1].contains("--")) {
                             
                                System.out.println("HERE2");
                                pathDomainVar.setEndVarClass(path[j].substring(path[j].lastIndexOf(">") + 1, path[j].length()));
                              

                                queryTriple = "$StartVar --" + withoutTrans + "->$Endvar";

                            } //if there is no endvar in the end
                            else {
//                             
                                queryTriple = "$StartVar --" + withoutTrans + "->$var" + varNo.toString(varNo);
                            }
                        } //for the rest triples except for the last
                        else if (j < (path.length - 1)) {

                            //if the next triple has one element then put endvar to the range of this one
                            if (!path[j + 1].contains("--")) {
                                //if there is transitivity
                                if (trans) {
                                    if (withoutTrans.matches(reg)) {
                                        ArrayList parameters = new ArrayList();
                                        parameters.add(withoutTrans);
                                        parameters.add(j);
                                        parameters.add(newPaths);
                                        parameters.add(varNo);
                                        parameters.add(false);
                                        parameters.add(true);


                                        //the result of the handling     
                                        ArrayList res = treatMultipleTransPredicates(parameters);
                                        queryTriple = (String) res.get(0);
                                        varNo = (Integer) res.get(1);
                                        newPaths = (ArrayList<String>) res.get(2);
//                                      
                                    } else {

                                        String previousTriple = newPaths.get(j - 1);
                                        if (previousTriple.matches(reg)) {
                                            ArrayList<String> temp = splitAtOr(previousTriple);
                                            previousTriple = formulateIntermediateWithoutEndvar("", "", temp);
                                        }
                                        queryTriple = "UNION{" + previousTriple;
                                        if (!previousTriple.contains("UNION")) {
                                            queryTriple += ". ";
                                        }
                                        queryTriple += " $var" + varNo.toString(varNo) + " " + withoutTrans + " $Endvar.}";

                                        //the previous triple treatment 
                                        String newPreviousTriple = "{" + newPaths.get(j - 1).substring(0, newPaths.get(j - 1).lastIndexOf("$var"));
                                        newPreviousTriple += "$Endvar.}";
                                        if (newPreviousTriple.matches(reg)) {
                                            ArrayList<String> temp = splitAtOr(newPreviousTriple);
                                            newPreviousTriple = formulateIntermediateWithoutEndvar("", "", temp);
                                        }
                                        newPaths.remove(j - 1);
                                        newPaths.add(j - 1, newPreviousTriple);
                                    }
                                } else //if no transitive
                                {
                                    queryTriple = " $var" + varNo.toString(varNo) + "--" + withoutTrans + "->$Endvar ";
                                }

                            } //no endVar
                            else {
                                String previousTriple = newPaths.get(j - 1);
                                if (previousTriple.matches(reg)) {
                                    ArrayList<String> temp = splitAtOr(previousTriple);
                                    previousTriple = formulateIntermediateWithoutEndvar("", "", temp);
                                }
                                if (trans) {
                                    ArrayList parameters = new ArrayList();
                                    parameters.add(withoutTrans);
                                    parameters.add(j);
                                    parameters.add(newPaths);
                                    parameters.add(varNo);
                                    parameters.add(false);
                                    parameters.add(true);


                                    //the result of the handling     
                                    ArrayList res = treatMultipleTransPredicates(parameters);
                                    queryTriple = (String) res.get(0);
                                    varNo = (Integer) res.get(1);
                                    newPaths = (ArrayList<String>) res.get(2);

                                } else {
                                    queryTriple = " $var" + varNo.toString(varNo) + "--" + withoutTrans + "->$var" + varNo.toString(++varNo);
                                }
                            }

                        } //  last triple
                        else {
                            if (trans) {
                                if (withoutTrans.matches(reg)) {
                                    ArrayList parameters = new ArrayList();
                                    parameters.add(withoutTrans);
                                    parameters.add(j);
                                    parameters.add(newPaths);
                                    parameters.add(varNo);
                                    parameters.add(true);
                                    parameters.add(true);


                                    //the result of the handling     
                                    ArrayList res = treatMultipleTransPredicates(parameters);
                                    queryTriple = (String) res.get(0);
                                    varNo = (Integer) res.get(1);
                                    newPaths = (ArrayList<String>) res.get(2);
                                } //if we donnot have multiple transitiveness
                                else {
                                    String previousTriple = newPaths.get(j - 1);
                                    if (previousTriple.matches(reg)) {
                                        ArrayList<String> temp = splitAtOr(previousTriple);
                                        previousTriple = formulateIntermediateWithoutEndvar("", "", temp);
                                    }
                                    queryTriple = "UNION{" + previousTriple + " ";
                                    if (!previousTriple.contains("UNION")) {
                                        queryTriple += ". ";
                                    }
                                    queryTriple += "$var" + varNo.toString(varNo) + " " + withoutTrans + " $Endvar.}";
                                    String newPreviousTriple = "{" + newPaths.get(j - 1).substring(0, newPaths.get(j - 1).lastIndexOf("$var"));
                                    newPreviousTriple += "$Endvar.}";
                                    if (newPreviousTriple.matches(reg)) {
                                        ArrayList<String> temp = splitAtOr(newPreviousTriple);
                                        newPreviousTriple = formulateIntermediateWithoutEndvar("", "", temp);
                                    }
                                    newPaths.remove(j - 1);
                                    newPaths.add(j - 1, newPreviousTriple);
                                }
                            } else //if no transitive
                            {
                                queryTriple = " $var" + varNo.toString(varNo) + "--" + withoutTrans + "->$Endvar ";

                            }
                            //increase the variable counter because this is the last triple
                            varNo++;
                            pathDomainVar.setEndVarClass(path[j].substring(path[j].lastIndexOf(">") + 1, path[j].length()));
                        }

                    } else {
                        //if there is only one element in the path then increase j
                        //and do nothing more(the path contains no -- )
                        j++;
                        break;
                    }
                    //store the new triple
                    newPaths.add(queryTriple);
                }
                //add the new var-path listin the table
                if (newPaths.size() > 0) {
                    newListsTable.put(i, newPaths);
                }

            }
        }
        //check for extra paths so that to decide if recursion is needed
        if (extraPaths != null && extraPaths.size() > 0) {
            PathAndDomainVariables tempPathDomain = new PathAndDomainVariables();
            tempPathDomain = queryFormatTable(extraPaths);
            Hashtable<Integer, ArrayList<String>> tempTable = new Hashtable<Integer, ArrayList<String>>();
            tempTable = tempPathDomain.getQtable();
            int tableSize = tempTable.size();
            //iter is an iterator to move after the size of the table for merging the two tables into one
            int iter = 0;
            Enumeration e = newListsTable.keys();
            while (e.hasMoreElements()) {
                tempTable.put((tableSize + iter), newListsTable.get(e.nextElement()));
                iter++;
            }
            pathDomainVar.setQtable(tempTable);

        } else {
            pathDomainVar.setQtable(newListsTable);
        }
        return pathDomainVar;
    }

    /**
     * This method takes the variable formated paths and turn them into SPARQL 
     * statement
     * @param qTable the table with the var-formated paths
     * @return SPARQL statement
     */
    private static String createSPARQLFromPathsTable(PathAndDomainVariables pathDom) {

        Hashtable<Integer, ArrayList<String>> qTable = pathDom.getQtable();
        String startvarType = "$StartVar  rdf:type " + pathDom.getStartVarClass() + ".";
        String queryString = "select distinct $StartVar $Label { " + eol;
        queryString += startvarType + eol;
        queryString += "optional { $StartVar crmdig:L4F.has_preferred_label $Label. }";

        String intermediate = "";

        int i = 0;

        for (ArrayList<String> arrayL : qTable.values()) {

            //in cases where there is no triples just skip
            //like a--transitiveproperty->b:
            //     b
            if (arrayL != null) {
                if (i == 0) {

                    if (qTable.size() > 1) {
                        intermediate = "{" + eol;
                    }


                    String reg = ".*\\sOR\\s.*";

                    for (String triple : arrayL) {
                        ArrayList<String> tempPath = new ArrayList<String>();

                        triple.replaceAll(eol, " ");
                        if (triple.contains("{") && !triple.matches(reg)) {
                            triple = triple.replace("--", " ").replace("->", " ");
                            if (triple.lastIndexOf("}") == triple.length() - 1) {
                                intermediate += triple + eol;
                            } else {
                                intermediate += triple + "." + eol;
                            }
                            continue;
                        } else {
                            boolean existsOR = false;
                            if (triple.matches(reg)) {
                                existsOR = true;
                                int starthook = triple.lastIndexOf("{");
                                int endhook = triple.indexOf("}");

                                // Isolating the predicates with OR
                                String subStr = triple.substring(starthook, endhook + 1);
                                triple = triple.replace(subStr, "_T_E_M_P_");

                                // Clear the string from hooks and split to the OR
                                String[] sepOr = subStr.replaceAll("[{}]", "").split(" OR ");

                                // Putting the paths into the stack with paths
                                for (int l = 0; l < sepOr.length; l++) {
                                    tempPath.add(triple.replace("_T_E_M_P_", sepOr[l]));

                                }
                                intermediate = formulateIntermediateWithoutEndvar(intermediate, triple, tempPath);
                            }
                            if (!existsOR) {

                                triple = triple.replace("--", " ").replace("->", " ");
                                if (triple.lastIndexOf("}") == triple.length() - 1) {
                                    intermediate += triple + eol;
                                } else {
                                    intermediate += triple + "." + eol;
                                }
                                //add the restriction for endvar class
                                if (!triple.contains("$Endvar")) {

                                    intermediate = formulateIntermediateWithoutEndvar(intermediate, triple, tempPath);
                                }

                            }
                        }
                    }

                    if (qTable.size() > 1) {
                        intermediate += " }" + eol + eol;
                    }

                }//this is the else for the rest of triples
                else {
                    intermediate += " UNION {  " + eol;


                    String reg = ".*\\sOR\\s.*";

                    for (String triple : arrayL) {
                        ArrayList<String> tempPath = new ArrayList<String>();

                        if (triple.contains("{") && !triple.matches(reg)) {

                            triple = triple.replace("--", " ").replace("->", " ");
                            intermediate += triple + eol;
                            continue;
                        } else {

                            boolean existsOR = false;
                            if (triple.matches(reg)) {
                                existsOR = true;
                                tempPath = splitAtOr(triple);
                                intermediate = formulateIntermediateWithoutEndvar(intermediate, triple, tempPath);

                            }
                            if (!existsOR) {
                                triple = triple.replace("--", " ").replace("->", " ");

                                if (triple.lastIndexOf("}") == triple.length() - 1) {
                                    intermediate += triple + eol;
                                } else {
                                    intermediate += triple + "." + eol;
                                }
                                intermediate = formulateIntermediateWithoutEndvar(intermediate, triple, tempPath);
                            }
                        }
                    }
                    intermediate += " }" + eol + eol;
                }
            }
            i++;
        }
        queryString += intermediate;

        return queryString += "}";
    }

    /*******************Private functions******************************************/
    /**
     * In this method I create an arraylist for each path, with elements the 
     * triples it is constructed of. 
     * @param pathStack
     * @return A hashtable containing the arraylists of all the paths the fundamental relation
     * is constructed of
     */
    private static Hashtable<Integer, ArrayList<String[]>> seperatePathTriples(Stack<String> pathStack) {
        Hashtable<Integer, ArrayList<String[]>> listsTable = new Hashtable<Integer, ArrayList<String[]>>();
        for (int i = 0; i < pathStack.size(); i++) {

            String strPath = pathStack.elementAt(i);

            /* If the path contains more than one triples
             *
             */
            ArrayList<String[]> pathsList = new ArrayList<String[]>();

            if (strPath.indexOf("|") > 0) {
                String[] sepTrip = strPath.split("[|]");
                pathsList.add(sepTrip);


            } /*
             * if it contains one
             */ else {
                String[] sepTrip = new String[1];
                sepTrip[0] = strPath;
                pathsList.add(sepTrip);
            }
            listsTable.put(i, pathsList);

        }
        return listsTable;
    }

    /**Formulates the intermediate paths in SPARQL.     * 
     * @param intermediate the path to which the path that will be created will be added
     * @param triple not used!
     * @param tempPath an arraylist containing the triples to be unified under SPARQL
     * @return  the intermediate string with its new part in the end
     */
    private static String formulateIntermediateWithoutEndvar(String intermediate, String triple, ArrayList<String> tempPath) {

        for (int z = 0; z < tempPath.size(); z++) {
            boolean proexistingHooks = false;
            String tempTriple = tempPath.get(z);

            boolean unionExisting = tempTriple.startsWith("UNION");
            if ((tempTriple.charAt(0) == '{' || unionExisting) && tempTriple.charAt(tempTriple.length() - 1) == '}') {

                proexistingHooks = true;
                if (unionExisting) {
                    tempTriple = tempTriple.substring(6, tempTriple.length() - 1);
                } else {
                    tempTriple = tempTriple.substring(1, tempTriple.length() - 1);
                }
            }
            if (z == 0) {

                tempTriple = tempTriple.replace("--", " ").replace("->", " ");
                if (proexistingHooks) {
                    if (unionExisting) {
                        intermediate += "UNION{";
                    } else {
                        intermediate += "{";
                    }
                }

                intermediate += "{ " + tempTriple + eol;
                //add the restriction for endvar class

                intermediate += "}";

            } else {
                tempTriple = tempTriple.replace("--", " ").replace("->", " ");
                intermediate += "UNION{ " + tempTriple + eol + "}";

                if ((z == tempPath.size() - 1) && proexistingHooks) {
                    intermediate += "}";
                }
            }

        }

        return intermediate;



    }

    /**
     * This method takes as input a list of predicates and adds the respective 
     * prefix to it. It returns a String with the predicates, united with an OR
     * if there are more than one
     * @param predicates
     * @return 
     */
    private static String addPrefixToPredicates(ArrayList<String> predicates) {
        String qrSelectNamespace = "";
        String allPredicates = "";
        String AssociationTable[][] = VariableFileReader.startLeterAs_read_variables_of("StartLetterAssociation");
        for (int k = 0; k < predicates.size(); k++) {
            boolean predicateExists = false;

            if (predicates.get(k).contains(":")) {
                predicateExists = true;

            }
            if (!predicateExists) {
                for (int g = 0; g < AssociationTable.length; g++) {

                    /* Assigning prefix to predicate according to the
                     * link's first letter
                     */

                    boolean exist = false;
                    boolean trans = false;


                    //if there is transitivity
                    if (predicates.get(k).contains("(")) {
                        String fromFile = AssociationTable[g][0];
                        String fromString = predicates.get(k).substring(1, 2);
                        exist = fromString.equalsIgnoreCase(fromFile);
                        qrSelectNamespace = AssociationTable[g][1];
                        trans = true;
                    } else {
                        String fromFile = AssociationTable[g][0];
                        String fromString = predicates.get(k).substring(0, 1);
                        exist = fromString.equalsIgnoreCase(fromFile);
                        qrSelectNamespace = AssociationTable[g][1];

                    }

                    if (exist) {
                        //re-construct the OR of predicates with the prefixes
                        //if it is the first we donnot add the OR
                        if (k != 0) {
                            //if there is no transitivity
                            if (!trans) {
                                allPredicates += " OR " + qrSelectNamespace + ":" + (predicates.get(k));
                            } //else we need to treat the opening (
                            else {
                                allPredicates += " OR " + "(" + qrSelectNamespace + ":" + (predicates.get(k).substring(1));
                            }

                            //if last we also add a closing }
                            if (k == predicates.size() - 1) {
                                allPredicates += "}";
                            }
                        } else {
                            //if there is only one predicate (none OR)
                            if (predicates.size() == 1) {
                                if (!trans) {
                                    allPredicates = qrSelectNamespace + ":" + (predicates.get(k));
                                } //else we need to treat the opening (
                                else {
                                    allPredicates = "(" + qrSelectNamespace + ":" + (predicates.get(k).substring(1));
                                }

                            } else {
                                //for the first predicate
                                if (!trans) {
                                    allPredicates = "{" + qrSelectNamespace + ":" + (predicates.get(k));
                                } //else we need to treat the opening (
                                else {
                                    allPredicates = "{ (" + qrSelectNamespace + ":" + (predicates.get(k).substring(1));
                                }

                            }
                        }
                    }
                }
            } else {
                if (predicates.size() == 1) {
                    allPredicates = predicates.get(k);
                } else {
                    if (k == 0) {
                        allPredicates = "{" + predicates.get(k);
                    } else {
                        allPredicates += " OR " + predicates.get(k);

                    }
                    if (k == predicates.size() - 1) {
                        allPredicates += "}";
                    }

                }


            }

        }
        return allPredicates;

    }

    /**This method takes as input a path and if it contains triples with predicates with transitivity
     * eg (forms part of)[0,n] it removes the extra characters ("(",")","[0,n]") and returns
     * the path "clean". In any other case it returns the path as it is.
     * 
     * @param pathWithTransitivity
     * @return 
     */
    private static String removeTransitivity(String pathWithTransitivity) {

        /* Remove the all the useless characters
         */

        pathWithTransitivity = pathWithTransitivity.replaceAll("[()]", "");

        /* Searching for the triples that contains weights and clear
         * them from weights
         */
        String reg = ".*\\[.*\\].*";

        if (pathWithTransitivity.matches(reg)) {

            pathWithTransitivity = pathWithTransitivity.replaceAll("\\[.*?\\]", "");

        }


        return pathWithTransitivity;
    }

    /**
     * This method takes an expression that more than one transitive predicates
     * concatenated with OR and returns the appropriate SPARQL for it. Also 
     * returns the necessary alteration of the used parameters
     * @param input it is an arrayList that contains:
     *              in position 0: the path
     *              in position 1: the number of the triple
     *              in position 2: the arraylist of the newly created paths
     *              in position 3: current VarNo
     *              in position 4: if the last var is Endvar (true )
     *              in position 5: if the first var is StartVar (true )
     * 
     * @return  an arraylist containing the querytriple, the new current VarNo
     *              and the altered arraylist with the paths
     * 
     */
    private static ArrayList treatMultipleTransPredicates(ArrayList input) {

        ArrayList<String> newPaths = (ArrayList<String>) input.get(2);
        ArrayList<String> predicates = new ArrayList<String>();
        String reg = ".*\\sOR\\s.*";

        if (!((String) input.get(0)).matches(reg)) {
            predicates.add((String) input.get(0));

        } else {
            predicates = splitAtOr((String) input.get(0));
        }

        String triple = "";
        Iterator itr = predicates.iterator();
        int i = 0;
        Integer j = (Integer) input.get(1);
        Integer varNo = (Integer) input.get(3);
        String firstvar = "";
        String lastvar = "";
        String rangeVar = "$var" + varNo.toString(varNo);
        if ((Boolean) input.get(5)) {
            firstvar = "$StartVar";
        } else {
            firstvar = "$var" + varNo.toString(varNo);
        }
        if ((Boolean) input.get(4)) {
            lastvar = "$Endvar";
            rangeVar = lastvar;
        } else {
            if ((Boolean) input.get(5)) {
                lastvar = "$var" + varNo.toString(varNo);
            } else {
                lastvar = "$var" + varNo.toString(++varNo);
            }

        }

        ArrayList<String> varsForFilter = new ArrayList<String>();
        while (itr.hasNext()) {
            String predicate = (String) itr.next();
            String previousTriple = newPaths.get(((Integer) input.get(1) - 1));
            //an eimaste sto prwto predicate
            if (i == 0) {

                String newPreviousTriple = previousTriple.substring(0, previousTriple.lastIndexOf("$var"));
                newPreviousTriple += "$tempvar" + varNo.toString(++varNo);
                //added on 25/4 to deal with OR in previous
                if (newPreviousTriple.matches(reg)) {

                    ArrayList<String> temp = splitAtOr(newPreviousTriple);
                    newPreviousTriple = formulateIntermediateWithoutEndvar("", "", temp);

                }
                if (newPreviousTriple.charAt(newPreviousTriple.length() - 1) == '}') {
                    triple = "UNION{" + newPreviousTriple;
                } else {
                    triple = "UNION{" + newPreviousTriple + ".";
                }

                if (predicates.size() == 1) {
                    triple += "$tempvar" + varNo.toString(varNo) + " " + predicate + " " + rangeVar + ".";
                } else {
                    triple += "{$tempvar" + varNo.toString(varNo) + " " + predicate + " " + rangeVar + ".}";
                }

            }//an den eimaste sto prwto predicate 
            else {
                triple += " UNION{ $tempvar" + varNo.toString(varNo) + " " + predicate + " " + rangeVar + ".}";
            }
            if (i == predicates.size() - 1) {
                triple += "}";
            }
            i++;
        }
        if (j != 0) {
            String previousTriple = newPaths.get(((Integer) input.get(1) - 1));
            //replace previous triple

            String newPreviousTriple = "{" + previousTriple.substring(0, previousTriple.lastIndexOf("$var"));
            newPreviousTriple += lastvar + ".}";

            //added on 25/4 to deal with OR in previous
            if (newPreviousTriple.matches(reg)) {

                ArrayList<String> temp = splitAtOr(newPreviousTriple);
                newPreviousTriple = formulateIntermediateWithoutEndvar("", "", temp);

            }
            newPaths.remove(j - 1);
            newPaths.add(j - 1, newPreviousTriple);

        }
        ArrayList res = new ArrayList();
        res.add(triple);
        res.add((Integer) input.get(3));
        res.add(newPaths);

        return res;

    }

    /**
     * Takes a path formulated in string and splits it at OR between predicates
     * @param path the input path
     * @return  an arraylist that contains the new triples that occur from the split 
     */
    private static ArrayList<String> splitAtOr(String path) {

        String reg = ".*\\sOR\\s.*";
        ArrayList<String> predicates = new ArrayList<String>();
        //if there exists OR
        if (path.matches(reg)) {
            int starthook = path.indexOf("{");
            int endhook = path.indexOf("}");
            boolean specialhookcase1 = false;
            boolean specialhookcase5 = false;
            //starts with hook
            if (starthook == 0) {
                specialhookcase1 = true;
            }
            //starts with UNION
            if (starthook == 5) {
                specialhookcase5 = true;
            }
            boolean specialhookcase = (specialhookcase1 || specialhookcase5);

            // Isolating the predicates with OR

            int numberOfHook = TextUtilities.countCharsInPhrase(path, '{');

            if (path.contains("--")) {
                //BE AWARE: there may be extra { } in case of UNION so exlude them

                if (specialhookcase1 || specialhookcase5) {

                    if (starthook == 0) {
                        path = path.replaceFirst("[{]", "");
                    }

                    starthook = path.indexOf("{");
                    endhook = path.indexOf("}");
                    path = path.substring(0, path.length() - 1);

                }
                String subStr = path.substring(starthook + 1, endhook);
                String startVar = null;
                String endVar = null;
                startVar = path.substring(0, path.indexOf("--"));
                endVar = path.substring(path.indexOf(">") + 1, path.length());
                StringBuffer startVarBuff = new StringBuffer(startVar);
                // Clear the string from hooks and split to the OR
                String[] sepOr = subStr.replaceAll("[{}]", "").split(" OR ");
                // Putting the paths into the stack with paths
                for (int l = 0; l < sepOr.length; l++) {

                    if (specialhookcase && l == 0) {
                        if (specialhookcase1) {
                            predicates.add("{ " + Syntax4.clear_whiteChar(startVar) + " " + sepOr[l] + " " + Syntax4.clear_whiteChar(endVar));
                        } else {
                            startVarBuff.insert(5, "{");

                            startVar = startVarBuff.toString();
                            predicates.add(Syntax4.clear_whiteChar(startVar) + " " + sepOr[l] + " " + endVar);
                        }

                    } else if (specialhookcase && l == (sepOr.length - 1)) {
                        specialhookcase = false;
                        predicates.add(Syntax4.clear_whiteChar(startVar) + " " + sepOr[l] + " " + endVar + "}");

                    } else {
                        predicates.add(Syntax4.clear_whiteChar(startVar) + " " + sepOr[l] + " " + endVar);

                    }
                }
            } else {
                String[] sepOr = path.replaceAll("[{}]", "").split(" OR ");

                for (int l = 0; l < sepOr.length; l++) {
                    predicates.add(sepOr[l]);

                }
            }
        }

        return predicates;

    }

    /**NOT USED AFTERALL
     * 
     * @param TriplesTable
     * @return 
     */
    private static int findNumberOfSameStartingTriples(Hashtable<Integer, ArrayList<String[]>> TriplesTable) {

        //find the how many paths there are in the table

        int tableSize = TriplesTable.size();
        //find the length of the smallest path
        int minSize = TriplesTable.get(0).get(0).length;

        for (int i = 1; i < tableSize; i++) {


            int temp = TriplesTable.get(i).get(0).length;
            if (temp < minSize) {
                minSize = temp;
            }
        }
        //check for the number of the same start triples
        //there is only need to check maximum the minSize

        int sameTriplesNo = 0;
        String[] firstStringTable = TriplesTable.get(0).get(0);


        while (sameTriplesNo < minSize) {
            String value = (String) firstStringTable[sameTriplesNo];
            int arrayNo = 1;
            while (arrayNo < tableSize) {
                if (((String) TriplesTable.get(arrayNo).get(0)[sameTriplesNo]).equalsIgnoreCase(value)) {
                    arrayNo++;
                } else {
                    break;
                }
            }
            if (arrayNo == tableSize) {
                sameTriplesNo++;
            } else {
                break;
            }

        }

        return sameTriplesNo;
    }
}
