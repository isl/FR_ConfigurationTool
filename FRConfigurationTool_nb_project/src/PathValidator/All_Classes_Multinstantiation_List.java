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
 *This is a list consisting of the defined multi-instantiation classes, created from
 * the respective file for multi-instantiation classes (in the set up folder).
 *The procedure is analogous to the disjointness classes. 
 * @author katetzob
 */
public class All_Classes_Multinstantiation_List {

    public static ArrayList<Class_MultiInstantiation> list;

    /**
     * 
     */
    public void createList() {
        list = readMultiInstFileToListOfClassMulitinst();



    }

    /**
     * Finds which classes can be multiple-instantiated with which classes and saves them together in a new object
     * All the new objects are returned in a list
     * 
     * @param prop
     * @return 
     */
    private ArrayList<Class_MultiInstantiation> readMultiInstFileToListOfClassMulitinst() {

        ArrayList<Class_MultiInstantiation> list = new ArrayList<Class_MultiInstantiation>();

        try {

            FileInputStream fstream = new FileInputStream(ToolInterface.multiInstantPath);
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
                Class_MultiInstantiation classMultiInst = new Class_MultiInstantiation(firstClass, table);
                list.add(classMultiInst);
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }
}
