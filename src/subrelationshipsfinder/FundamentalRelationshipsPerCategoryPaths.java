/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subrelationshipsfinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import subrelationshipsfinder.FundamentalCategories.Category;
import tools.*;

/**
 * Finds which are the FRs per FC
 * @author Katerina
 */
public class FundamentalRelationshipsPerCategoryPaths {

    private Category category;
    private Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> pathsTable;

    public Category getCategory() {
        return category;
    }

    public Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> getPathsTable() {
        return this.pathsTable;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPathsTable(Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> pathsTable) {

        this.pathsTable = pathsTable;
    }

 /**
     * Takes a file with Fundamental Relationships and the Category in interest 
     * and sets the class variable pathsTable with the subpaths of each category
     * @param dir
     * @param Category 
     */
    public void createCategoryPathsTable(File dir, FundamentalCategories.Category Category) {


        String category = Category.toString();
        FileReader fr = new FileReader();
        Hashtable<String, String> pathsOfCategoryList = new Hashtable<String, String>();
        pathsOfCategoryList = fr.readDirectoryToPaths(dir, category);
        //this is the table for the whole relationship
        Hashtable<String, Hashtable<Integer, ArrayList<String[]>>> pathsOfRelation = new Hashtable<String, Hashtable<Integer, ArrayList<String[]>>>();

        //take all the paths
        Enumeration e = pathsOfCategoryList.keys();
        while (e.hasMoreElements()) {
            String relName = (String) e.nextElement();
            String relPath = (String) pathsOfCategoryList.get(relName);
            //  Hashtable<Integer, ArrayList<String[]>> separatedPathsTable=new Hashtable<Integer, ArrayList<String[]>>();
            Stack<String> pathsStack = new Stack<String>();
            relPath = clear_whiteChar(relPath);
            pathsStack = paths_seperator(relPath);
            Hashtable<Integer, ArrayList<String[]>> listsTable = new Hashtable<Integer, ArrayList<String[]>>();
            listsTable = seperatePathTriples(pathsStack);
            pathsOfRelation.put(relName, listsTable);
        }

       
    
        setPathsTable(pathsOfRelation);

    }

    /**
     * In this method I create an arraylist for each path, with elements the 
     * triples it is constructed of. 
     * @param pathStack
     * @return A hashtable containing the arraylists of all the paths the fundamental relation
     * is constructed of
     */
    public Hashtable<Integer, ArrayList<String[]>> seperatePathTriples(Stack<String> pathStack) {
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

    /**
     * seperates each path
     * @param str
     * @return 
     */
    public Stack<String> paths_seperator(String str) {
        //   System.out.println("Input: " + str);
        final Stack<String> allPaths = new Stack<String>();
        allPaths.push(str);

        int j = 0;
         while (j < allPaths.size()) {

            /* Check if the given element contains more paths by checking if
             * there is the colon character
             */
            if (allPaths.elementAt(j).contains(":")) {

                /* Split the string in two parts at colon character
                 */
                String[] parts = colon_seperator(allPaths.elementAt(j));

                /* Take the 2nd part
                 */
                String secondPart = parts[1];
              /* Check if there is more paths inside the part
                 */
                if (secondPart.startsWith("{")) {


                    // We are going to keep only this level and the deeper ones
                    secondPart = secondPart.substring(1, (secondPart.length() - 1));

                    /* We are using a temp Sting variable because we don't want to 
                     * affect the deeper paths. So, we are locating all the subpaths 
                     * finding the '{' and '}' (if there is a subpath, it will be 
                     * inside hooks).
                     */


                    // Stacks to save the subpaths and the temp strings
                    Stack<String> subPaths = new Stack();
                    Stack<String> tempStrings = new Stack();
                    int tmpcnt = 0;



                    while (secondPart.contains("{")) {
                        int strPos = 0, endPos = 0;

                        // Convert string to char array
                        char[] ar = new char[secondPart.length()];
                        ar = secondPart.toCharArray();

                        // Hooks' counter
                        int cnt = 0;

                        // First hook flag
                        boolean frstFlag = false;

                        for (int i = 0; i < ar.length; i++) {

                            // Check if the hook is the first one and hold the position
                            if (ar[i] == '{' && cnt == 0) {
                                //startPositionStack.push(i);
                                strPos = i;
                            }

                            // Counting the opening hooks increasing the counter
                            if (ar[i] == '{') {
                                cnt++;
                                frstFlag = true;
                            }

                            // Counting the closing hooks decreasing the counter
                            if (ar[i] == '}') {
                                cnt--;
                            }

                            // Check if all the hooks has closed
                            if (cnt == 0 && frstFlag == true) {
                                // end point is i+1 because we want to take also the closing hook
                                //endPositionStack.push(i+1);
                                endPos = i + 1;

                                subPaths.push(secondPart.substring(strPos, endPos));
                                tempStrings.push("TEMP_STRING_" + tmpcnt);
                                tmpcnt++;

                                secondPart = secondPart.replace(subPaths.peek(), tempStrings.peek());

                                //frstFlag = false;
                                break;
                            }
                        }
                    }

                    // Seperate the OR values
                    String orSep[] = or_seperator(secondPart);

                    // Remove the specific element which hase the ':'
                    allPaths.remove(j);

                    for (int i = 0; i < orSep.length; i++) {

                        // Replace back the subpaths by the temporally strings (the replacement is reversal)
                        //for (int k=0;k<subPaths.size();k++) {
                        for (int k = subPaths.size() - 1; k >= 0; k--) {
                            if (orSep[i].contains(tempStrings.elementAt(k))) {
                                orSep[i] = orSep[i].replace(tempStrings.elementAt(k), subPaths.elementAt(k));
                            }
                        }
                        // Add subpaths to the stack
                        allPaths.push(parts[0] + "|" + orSep[i]);
                    }


                } else {
                    // If there is not subpaths...

                    // Clear the stack
                    allPaths.removeAllElements();
                    // Add the only one path
                    allPaths.push(parts[0] + "|" + parts[1]);
                }

            } else {
                // There is not colon so the check can move on
                j++;
            }

        }


        return allPaths;

    }

    /**
     * splits string at :
     * @param str
     * @return 
     */
    public String[] colon_seperator(String str) {

        /*
         * Split the overall string into markets
         */
        String[] overallStr = null;
        overallStr = str.split("[:]", 2);

        return overallStr;

    }

    /**
     * splits at OR the string in input
     * @param str
     * @return 
     */
    public String[] or_seperator(String str) {

        String[] orSep = null;
        orSep = str.split(" OR ");
        return orSep;

    }

    /**
     * Clears the white spaces from the provided string
     * @param inputStr
     * @return 
     */
    public String clear_whiteChar(String inputStr) {

        String clearStr = inputStr.replaceAll("OR", "@@@OR@@@");
        clearStr = clearStr.replaceAll("\\s", "");
        clearStr = clearStr.replaceAll("@@@OR@@@", " OR ");

        return clearStr;

    }
}
