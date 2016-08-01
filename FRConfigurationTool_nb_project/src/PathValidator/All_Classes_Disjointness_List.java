/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import GUI.ToolInterface;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *This is a list consisting of the defined disjointness classes, created from
 * the respective file for disjointness classes (in the set up folder)
 * 
 * @author katetzob
 */
public class All_Classes_Disjointness_List {

    public static ArrayList<Class_Disjointness> list;

    /**
     * 
     */
    public void createList() {
        list = readDisjointFileToListOfClassDisjoint();



    }

    /**
     * Finds which classes are disjoint with which classes and saves them together in a new object
     * All the new objects are returned in a list
     * 
     * @return a list with the disjointess classes
     */
    private ArrayList<Class_Disjointness> readDisjointFileToListOfClassDisjoint() {

        ArrayList<Class_Disjointness> list = new ArrayList<Class_Disjointness>();

        try {

            //read the disjointess file (location is indicated in the ToolInterface)
            FileInputStream fstream = new FileInputStream(ToolInterface.disjointPath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {

                String firstClass = strLine.substring(0, strLine.indexOf(":"));
                firstClass = firstClass.replaceAll(" ", "");
                String otherClasses = strLine.substring(strLine.indexOf(":") + 1);
                otherClasses = otherClasses.replaceAll(" ", "");
                String[] table = otherClasses.split(",");
                //Save each found class (first class) along with its disjoint classes (kept in a table)
                //in a new object
                Class_Disjointness classDisjoint = new Class_Disjointness(firstClass, table);
                list.add(classDisjoint);
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }
}
