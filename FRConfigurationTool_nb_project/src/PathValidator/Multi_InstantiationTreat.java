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
 *This class checks whether a multiple-instantiation case occurs and treats it accordingly.
 * @author Katerina
 */
public class Multi_InstantiationTreat {

    private boolean exist;

  
    /**
     * Checks if one class is multiple-instantiated with another
     * @param firstClass the class to be checked
     * @param secondClass the class to be found in the multiple-instantiation table of the first class
     * @param all the user-typed expression (e.g. A:B) that will be used in order to be saved or removed
     * @param type indicates if the action will be to remove or add the case
     * @return 
     */

    public String checkExistenceInMulti(String firstClass, String secondClass, String all, String type) {

        if (ToolInterface.multiClassList == null) {
            ToolInterface.multiClassList = new All_Classes_Multinstantiation_List();
            ToolInterface.multiClassList.createList();
        }

        checkMultiClassList(firstClass, secondClass, all, type);

        String result = "fail";
        if (exist) {
            result = "success";
        }
        if (!exist && type.equalsIgnoreCase("remove")) {

            result = "The multi-instantiation case you provided does not exist!";
        }
        return result;
    }

    private void checkMultiClassList(String firstClass, String secondClass, String all, String type) {


        boolean existsMulti = false;
        String[] multList = null;
        Class_MultiInstantiation foundClass = null;
        if (ToolInterface.multiClassList != null) {
   
            ArrayList<Class_MultiInstantiation> list = ToolInterface.multiClassList.list;
            for (Class_MultiInstantiation multi : list) {
              if (multi.getGivenClass().equalsIgnoreCase(firstClass)) {
                    exist = true;
                    multList = multi.getMultiList();
                    foundClass = multi;
                    break;
                }

            }
            //if there are already multi cases defined for the first class
            if (exist == true) {
              
                for (String multi : multList) {
                  
                    if (multi.equalsIgnoreCase(secondClass)) {
                        existsMulti = true;
                   
                        break;
                    }
                }
                //if the secondclass is not in the multiList, add it

                if (!existsMulti) {
             
                    if (type.equalsIgnoreCase("remove")) {
                        exist = false;
                        return;
                    }
                    String newMultiContext = FileReader.changeDisjointORMultiFile(ToolInterface.multiInstantPath, firstClass, secondClass, type);
                    String res = WriteFile.write_general(newMultiContext, ToolInterface.multiInstantPath, true);
                 
                    if (!res.equalsIgnoreCase("success")) {
                        exist = false;
                    }

                } else {
                    if (type.equalsIgnoreCase("remove")) {

                        String newMultiContext = FileReader.changeDisjointORMultiFile(ToolInterface.multiInstantPath, firstClass, secondClass, type);
                        String res = WriteFile.write_general(newMultiContext, ToolInterface.multiInstantPath, true);
                       
                        if (!res.equalsIgnoreCase("success")) {
                            exist = false;
                        }
                    }
                }


                return;
            } else {
                if (type.equalsIgnoreCase("add")) {
                    String append = WriteFile.appendFile(all.replaceAll(" ", ""), ToolInterface.multiInstantPath);
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
