/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Contains for a class its multiple-instantiation classes
 * @author katetzob
 */
public class Class_MultiInstantiation {
    
   
    private  String givenClass="";
    private String[] multiList;

    public void setGivenClass(String givenClass) {
        this.givenClass = givenClass;
    }
    public String getGivenClass() {
        return givenClass;
    }
    
    public String[] getMultiList() {
        return multiList;
    }

    public void setMultiList(String[] multiList) {
        this.multiList = multiList;
    }
    
    
    /**
     * 
     * @param givenClass
     * @param multiList
     */
    public Class_MultiInstantiation(String givenClass,String[] multiList){
    setGivenClass(givenClass);
    setMultiList(multiList);
    
    
    }


    
    
}
