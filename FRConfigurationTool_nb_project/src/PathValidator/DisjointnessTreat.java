/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import GUI.ToolInterface;
import java.util.ArrayList;
import tools.FileReader;
import tools.WriteFile;

/**
 * This class checks whether a disjointess case occurs and treats it accordingly.
 * @author Katerina
 */
public class DisjointnessTreat {

    private boolean exist;


    /**
     * Checks if one class is disjoint with another
     * @param firstClass the class to be checked
     * @param secondClass the class to be found in the disjoint table of the first class
     * @param all the user-typed expression (e.g. A:B) that will be used in order to be saved or removed
     * @param type indicates if the action will be to remove or add the case
     * @return 
     */
    public String checkExistenceInDisjoint(String firstClass, String secondClass, String all, String type) {

        if (ToolInterface.disjointClassList == null) {
            ToolInterface.disjointClassList = new All_Classes_Disjointness_List();
            ToolInterface.disjointClassList.createList();
        }

        checkDijointClassList(firstClass, secondClass, all, type);

        String result = "fail";
       //exist boolean is set accordingly in the checkDijointClassList method
        if (exist) {
            result = "success";
        }
        if (!exist && type.equalsIgnoreCase("remove")) {

            result = "The disjointness case you provided does not exist!";
        }
        return result;
    }

    private void checkDijointClassList(String firstClass, String secondClass, String all, String type) {


        boolean existsDisjoint = false;
        String[] disjList = null;
        Class_Disjointness foundClass = null;
        if (ToolInterface.disjointClassList != null) {
            ArrayList<Class_Disjointness> list = ToolInterface.disjointClassList.list;
            for (Class_Disjointness disj : list) {

                if (disj.getGivenClass().equalsIgnoreCase(firstClass)) {
                    exist = true;
                    disjList = disj.getDisjointList();
                    foundClass = disj;
                    break;
                }

            }
            //if there are already disjoint cases defined for the first class
            if (exist == true) {
                for (String disjoint : disjList) {
                    if (disjoint.equalsIgnoreCase(secondClass)) {
                        existsDisjoint = true;
                        break;
                    }
                }
                //if the secondclass is not in the disjointList, add it

                if (!existsDisjoint) {

                    if (type.equalsIgnoreCase("remove")) {
                        exist = false;
                        return;
                    }
                    String newDisjointContext = FileReader.changeDisjointORMultiFile(ToolInterface.disjointPath, firstClass, secondClass, type);
                    String res = WriteFile.write_general(newDisjointContext, ToolInterface.disjointPath, true);
                    if (!res.equalsIgnoreCase("success")) {
                        exist = false;
                    }

                }
                if (type.equalsIgnoreCase("remove")) {

                    String newDisjointContext = FileReader.changeDisjointORMultiFile(ToolInterface.disjointPath, firstClass, secondClass, type);
                    String res = WriteFile.write_general(newDisjointContext, ToolInterface.disjointPath, true);

                    if (!res.equalsIgnoreCase("success")) {
                        exist = false;
                    }
                }


                return;
            } else {
                if (type.equalsIgnoreCase("add")) {
                    String append = WriteFile.appendFile(all.replaceAll(" ", ""), ToolInterface.disjointPath);
                    if (append.equalsIgnoreCase("success")) {
                        exist = true;
                    }
                } else {
                    exist = false;
                }
            }
        }



    }
}
