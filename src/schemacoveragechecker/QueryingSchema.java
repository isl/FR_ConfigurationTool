package schemacoveragechecker;

import RepoEdit.MRepositoryManager;
import config.FCFRConfig;
import config.SparqlConfig;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import tools.FileReader;
import tools.FileReader.*;
import tools.VariableFileReader;

/**
 * Used to find (super) properties from the CIDOC CRM in FR schema 
 * @author Katerina
 */
public class QueryingSchema {
    
    HashSet<String> propertiesSet = new HashSet<String>();
    static ArrayList<String> fileContent = new ArrayList<String>();
    
    public HashSet<String> ListProperties(File dir, MRepositoryManager mrm) {
        HashSet<String> propertiesWithExtra = new HashSet<String>();
        ArrayList<String> paths = new ArrayList<String>();
        paths = readDirectoryToPaths(dir);

        findPropertiesFromPaths(paths);
        for (String prop : propertiesSet) {
            if (prop.startsWith("" + FCFRConfig.fcStart)) { //updated by Anastasia
                ArrayList<String> extraProps = rulesReplacement(prop);
                for (String property : extraProps) {
                    property = clear_whiteChar(property);
                    propertiesWithExtra.add(property);
                    
                }
                
            }
            propertiesWithExtra.add(prop);
        }
        
        //now add the inverse properties 
        ArrayList<String> InverseList = new ArrayList<String>();
        InverseList = addReverseProperties(propertiesWithExtra, mrm);
        for (String inverse : InverseList) {
            propertiesWithExtra.add(inverse);
            
        }
   
        return propertiesWithExtra;
    }

    /**
     * Takes as input a list with the paths and returns only the included properties
     * @param pathsList
     * @return a set containing the properties
     */
    private void findPropertiesFromPaths(ArrayList<String> pathsList) {


        for (String path : pathsList) {
         
            Stack<String> pathsStack = new Stack<String>();
            path = clear_whiteChar(path);
            pathsStack = paths_seperator(path);
            Hashtable<Integer, ArrayList<String[]>> listsTable = new Hashtable<Integer, ArrayList<String[]>>();
            listsTable = seperatePathTriples(pathsStack);

            for (int i = 0; i < listsTable.size(); i++) {

                //for all the elemnts of the arraylist
                for (String[] subpath : listsTable.get(i)) {

                    /* in currentTriple i store the path i am checking and finally
                    the first path in the arraylist that has classes and no
                     * query format with $varNo
                     */
                    
                    for (String currentTriple : subpath) {
                        
                        tripleHandling(currentTriple);
                        
                    }
                    
                    
                }
            }
            
        }
        
        
        
    }

    /*
     * Seperates every single path (NOT the ORs in predicates)
     */
    private Stack<String> paths_seperator(String str) {
      
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
                // System.out.println("secondPart "+secondPart);

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
    
    private String[] colon_seperator(String str) {

        /*
         * Split the overall string into markets
         */
        String[] overallStr = null;
        overallStr = str.split("[:]", 2);


        return overallStr;
        
    }

    /*
     * Seperate the ORs
     */
    private String[] or_seperator(String str) {

        /* Remove all special characters
         */
        //String clearStr = str.replaceAll("[{}\t]", "");

        /* Seperate string 
         */
        String[] orSep = null;
      
        orSep = str.split(" OR ");
        
        return orSep;
        
    }

    /*******************Private functions******************************************/
    /**
     * In this method I create an arraylist for each path, with elements the 
     * triples it is constructed of. 
     * @param pathStack
     * @return A hashtable containing the arraylists of all the paths the fundamental relation
     * is constructed of
     */
    private Hashtable<Integer, ArrayList<String[]>> seperatePathTriples(Stack<String> pathStack) {
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
     * Finds the properties in the provided triple
     * @param currentTriple 
     */
    private void tripleHandling(String currentTriple) {
        
     
        currentTriple = clear_whiteChar(currentTriple);
        if (currentTriple.contains("--")) {
            String[] split = currentTriple.split("--");
            String[] split2 = split[1].split("->");
            String predicate = split2[0];
            
            String reg = ".*\\sOR\\s.*";

            //if there exists OR
            if (predicate.matches(reg)) {
                String[] predicates1 = or_seperator(predicate);
                if (predicates1 != null) {
                    for (String pre : predicates1) {

                        pre = clearFromExtraCharacters(pre);
                        pre = addPrefixToPredicates(pre);
                        propertiesSet.add(pre);
                      
                    }
                }
            } //else if OR does not exist
            else {
                String pre = split2[0];
                pre = clearFromExtraCharacters(pre);
                pre = addPrefixToPredicates(pre);
                propertiesSet.add(pre);
              

            }
        }
        
        
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
    
    private String addPrefixToPredicates(String predicateWithoutPrefix) {
        
        String qrSelectNamespace = "";
        String predicateWithPrefix = "";
        String AssociationTable[][] = VariableFileReader.startLeterAs_read_variables_of("StartLetterAssociation");
        
        for (int g = 0; g < AssociationTable.length; g++) {
            boolean exist = false;
            String fromFile = AssociationTable[g][0];
            String fromString = predicateWithoutPrefix.substring(0, 1);
            exist = fromString.equalsIgnoreCase(fromFile);
            qrSelectNamespace = AssociationTable[g][1];
            if (exist) {
                predicateWithPrefix = qrSelectNamespace + ":" + predicateWithoutPrefix;
            }
        }
        return predicateWithPrefix;
    }
    /**
     * Finds from the respective doc which properties are implied by using a rule-relationship
     * @param prop
     * @return 
     */
    private ArrayList<String> rulesReplacement(String prop) {
      
        ArrayList<String> newProps = new ArrayList<String>();
        String rulesPath=System.getProperty("user.dir") + System.getProperty("file.separator")
             +FileReader.readPathConfigFile("rulesPath");
        try {
            // Open the file that is the first 
            // command line parameter
            
            FileInputStream fstream = new FileInputStream(rulesPath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
               
                String rule = strLine.substring(0, strLine.indexOf("->"));
                
                if (rule.equals(prop)) {
                    String restProperties = strLine.substring(strLine.indexOf("->") + 2);
                  
                    String[] props = restProperties.split("&&");
                    for (String property : props) {
                    
                        newProps.add(property);
                    }

            
                    break;
                }
                
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return newProps;
    }
    /**
     * Finds the inverse properties of the ones in the input list
     * @param propertiesWithExtra
     * @param mrm
     * @return 
     */
    private ArrayList<String> addReverseProperties(HashSet<String> propertiesWithExtra, MRepositoryManager mrm) {
        COFORMSchema cof = new COFORMSchema();
        ArrayList<String> list = new ArrayList<String>();
        for (String prop : propertiesWithExtra) {
           
            String sparqlForInverse = SparqlConfig.prefices
                    + "select distinct $prop{"
                    + "$prop owl:inverseOf " + prop + ".}";
            
            ArrayList<String> list1 = new ArrayList<String>();
            list1 = cof.ListPredicates(mrm, sparqlForInverse);
            
         
            if (list1.size() > 0) {
                list.add(list1.get(0));
            }
            
        }
    
        list=cof.addPrefixInsteadOfFullNameSpace(list);
        return list;
        
    }
    
    /**
     * This method reads a Directory containing folders with files containing paths,
     * and returns the paths 
     * @param dir  the directory containing the folders with the paths
     * @return arraylist with the paths 
     * @author Katerina
     */
    private static ArrayList<String> readDirectoryToPaths(File dir) {

      
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
           
                readDirectoryToPaths(f);
            } else {
                String filename = f.getAbsolutePath();
                if (filename.endsWith("Path.txt")) {
                
                    String content = FileReader.read_from_file(filename);
                
                    if (content != null) {
                        fileContent.add(content);
                  
                    }
                } else {
             
                }
            }
        }
      
        return fileContent;
    }
}
