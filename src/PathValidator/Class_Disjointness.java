/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

/** Contains for a class its disjointness classes
 *
 * @author katetzob
 */
public class Class_Disjointness {

    //the main class
    private String givenClass = "";
    //a table containing its disjointess classes
    private String[] disjointList;

    public String[] getDisjointList() {
        return disjointList;
    }

    public void setDisjointList(String[] disjointList) {
        this.disjointList = disjointList;
    }

    public String getGivenClass() {
        return givenClass;
    }

    public void setGivenClass(String givenClass) {
        this.givenClass = givenClass;
    }

    /**
     * 
     * @param givenClass
     * @param disjointList
     */
    public Class_Disjointness(String givenClass, String[] disjointList) {
        setGivenClass(givenClass);
        setDisjointList(disjointList);


    }
}
