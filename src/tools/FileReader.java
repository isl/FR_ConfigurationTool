/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

/**
 * Reads a file in various ways
 * @author Christos,Katerina
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import rulesUpdate.Rule;
import rulesUpdate.RulesSet;

public class FileReader {
    
    Hashtable<String, String> fileContent = new Hashtable<String, String>();
   /**
     * the filename of the file where necessary paths are stored
     */
    final static String pathConf = System.getProperty("user.dir") + System.getProperty("file.separator")
            + "paths.conf";

    /**
     * Simply reads from a file provided a path to that file
     * @param path the filename
     * @return  the content of the file in a string
     * 
     */
    public static String read_from_file(String path) {
        
        String text_from_file = "";
        
        try {
            // Open the file that is the first 
            // command line parameter

            FileInputStream fstream = new FileInputStream(path);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
               
                text_from_file = text_from_file + strLine + "\n";
                
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
        	 System.err.println("Error in method read_from_file: " + e.getClass());
        }
        
        return text_from_file;
    }

    /**
     * This method reads a Directory containing folders with files containing paths,
     * and returns the paths 
     * @param dir  the directory containing the folders with the paths
     * @param Category the category for which we want the paths
     * @return arraylist with the paths 
     */
    public Hashtable<String, String> readDirectoryToPaths(File dir, String Category) {
        
        
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                if (f.getName().contains(Category)) {
                    System.out.println(f.getName());
                    readDirectoryToPaths(f, Category);
                }
            } else {
                String filename = f.getAbsolutePath();
                if (filename.endsWith("Path.txt")) {
                    String content = read_from_file(filename);
                    if (content != null) {
                        fileContent.put(filename, content);
                    }
                }
            }
        }
        
        return fileContent;
    }

    /**
     * creates a table that contains for each rule the respective properties that are implied
     * @return the created table
     */
    public static Hashtable<String, ArrayList<String>> readRulesFile() {
        
        Hashtable<String, ArrayList<String>> rulesTable = new Hashtable<String, ArrayList<String>>();
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(System.getProperty("user.dir") + "\\rules.conf");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                ArrayList<String> newProps = new ArrayList<String>();
                // Print the content on the console

                String rule = strLine.substring(0, strLine.indexOf("->"));
                rule = rule.replaceAll("crm:", "").replaceAll("crmdig:", "").replaceAll(" ", "");
                String restProperties = strLine.substring(strLine.indexOf("->") + 2);
                
                String[] props = restProperties.split("&&");
                for (String property : props) {
                    //clean from un-needed strings
                    property = property.replaceAll("crm:", "").replaceAll("crmdig:", "").replaceAll(" ", "");

                    newProps.add(property);
                }
                rulesTable.put(rule, newProps);
                
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error in method readRulesFile: " + e.getClass());
        }
        
        
        return rulesTable;
    }

    /**
     * Reads from the Disjointness file the lines (that contain string of the type A:B,M,N)
     * the line that starts with the classToFind variable and depending on the actionType
     * adds or removes the classToAdd
     * @param path where the file is 
     * @param classToFind
     * @param classToAdd 
     * @param actionType add or remove
     * @return the new altered file content as a string
     */
    public static String changeDisjointORMultiFile(String path, String classToFind, String classToAdd, String actionType) {
        
        String text_from_file = "";
        
        try {
            // Open the file that is the first 
            // command line parameter

            FileInputStream fstream = new FileInputStream(path);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
           
                if (strLine.startsWith(classToFind + ":")) {
                    if (actionType.equalsIgnoreCase("add")) {
                        strLine = strLine.replaceAll("\n", "");
                        strLine += "," + classToAdd;
                        //     System.out.println("H KAINOURIA "+strLine);
                    } else {
                        
                        String secondPart = strLine.substring(strLine.indexOf(":") + 1);
                        
                        int start = secondPart.indexOf(classToAdd);
                        int lengthOfClass = classToAdd.length();
                        int possibleCommaPosition = start + lengthOfClass;

                        //if the classToAdd is the first on the list
                        if (secondPart.startsWith(classToAdd)) {
                            //if there is only one class
                            if (secondPart.length() == classToAdd.length()) {
                                //then erase the whole line 
                                strLine = "";
                            } else {
                                secondPart = secondPart.replaceAll(classToAdd + ",", "");
                            }
                            
                        } //if the classToAdd is the last one in the list
                        else if (secondPart.length() == possibleCommaPosition) {
                            //then erase the comma before
                            secondPart = secondPart.replaceAll("," + classToAdd, "");
                            
                        } //in any other case the classToAdd will be a middle one
                        else {
                            secondPart = secondPart.replaceAll(classToAdd + ",", "");
                        }
                        
                        if (!strLine.equalsIgnoreCase("")) {
                            strLine = strLine.substring(0, strLine.indexOf(":") + 1) + secondPart;
                        }
                     

                    }
                    
                }
                if (!strLine.equalsIgnoreCase("")) {
                    text_from_file = text_from_file + strLine + "\n";
                }
                
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error in method changeDisjointORMultiFile: " + e.getClass());
        }
        
        return text_from_file;
    }

    /**
     * This method reads the paths.conf file placed in the user directory, and returns 
     * for the provided variable, the place of the respective file
     * @param variable the file sought for
     * @return the directory the file is placed at
     */
    public static String readPathConfigFile(String variable) {
        String variableValue = null;
        
        try {
            
            FileInputStream fstream = new FileInputStream(pathConf);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
      while ((strLine = br.readLine()) != null) {
      
                if (strLine.startsWith("$" + variable)) {
                  
                    variableValue = strLine.substring(strLine.indexOf("=") + 1, strLine.length());
                  
                }
                  
            }

            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error in method readPathConfigFile: " + e.getClass());
        }
        
        
        return variableValue;
    }
    /**
     * Reads the .pie file that contains the custom CRM rules among the lines
     * CUSTOM CRM RULES START and CUSTOM CRM RULES END.
     * @param repositoryPieFilename the filename of the rules file
     * @return the set containing the rules
     */
    public static RulesSet readCustomCRMRulesFromPieFile(String repositoryPieFilename) {
        
        RulesSet rulesSet = new RulesSet();
        try {
            
            FileInputStream fstream = new FileInputStream(repositoryPieFilename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line

            boolean startCRM = false;
            boolean premise = false;
            boolean consequence = false;
            boolean firstRule = true;
            ArrayList<String> premises = null;
            ArrayList<String> consequences = null;
            
            
            Rule rule = null;
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console

                if (strLine.contains("CUSTOM CRM RULES START")) {
            
                    startCRM = true;
                    continue;
                }
                //here I read the part inside the custom crm rules start and end 
                if (startCRM) {
                    
                    if (strLine.startsWith("Id:")) {
                      
                        //if this is not the first Id that I am finding
                        //then I need to store the previous rule!
                        if (!firstRule) {
                            rule.setConsequences(consequences);
                            rule.setPremises(premises);
                            rulesSet.rulesSet.add(rule);
                        }
                        rule = new Rule();
                        rule.setName(strLine.substring(strLine.indexOf("Id:") + 3));
                
                        premise = true;
                        premises = new ArrayList<String>();
                        consequences = new ArrayList<String>();
                        firstRule = false;
                    } else if (!strLine.contains("-----------") && premise) {

                        premises.add(strLine);
                        
                    } else if (strLine.contains("-----------")) {
           
                        premise = false;
                        consequence = true;
                        
                    } else if (!strLine.contains("CUSTOM CRM RULES END") && consequence) {
                        
                        
                        String strLine2 = strLine.replaceAll("\\s", "");
                        strLine2 = strLine2.replaceAll("\r\n", "");
                          
                        if (!strLine2.equalsIgnoreCase("")) {
                            consequences.add(strLine);
                    
                        }
                        
                    } else if (strLine.contains("CUSTOM CRM RULES END")) {
                         rule.setConsequences(consequences);
                            rule.setPremises(premises);
                            rulesSet.rulesSet.add(rule);
                     
                        consequence = false;
                        startCRM = false;
                    } //if the line is empty
                    else {
                        continue;
                    }
                }
                
            }

            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error in method readCustomCRMRulesFromPieFile: " + e.getClass());
        }

        //--------------- CUSTOM CRM RULES ----------


        
        
        return rulesSet;
    }
}
