/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.File;

/**
 * Create a directory, provided a filename
 * @author katetzob
 */
public class CreateDir {

    /**
     *  Creates a directory at the specified filename
     * @param path
     * @return success or failure 
     */
    public static boolean createDir(String path) {
        File f = new File(path);
        try {
            if (f.mkdir()) {
                System.out.println("Directory Created");
            } else {
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
        }
        return f.mkdir();
    }
}
