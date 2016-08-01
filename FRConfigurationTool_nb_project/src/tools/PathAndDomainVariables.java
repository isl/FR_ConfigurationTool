/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Keeps the first and last and domain class of a path
 * @author Katerina
 */
public class PathAndDomainVariables {

    public Hashtable<Integer, ArrayList<String>> qtable;
    public ArrayList<String> domainVar;
    public String EndVarClass;
    public String StartVarClass;

    public ArrayList<String> getDomainVar() {
        return domainVar;
    }

    public String getEndVarClass() {
      
        return EndVarClass;
    }

    public void setEndVarClass(String EndVarClass) {

        String withPrefix=crmPrefix(EndVarClass);
        this.EndVarClass = withPrefix;
    }

    public String getStartVarClass() {
  
        return StartVarClass;
    }

    public void setStartVarClass(String StartVarClass) {
        String withPrefix = crmPrefix(StartVarClass);
        this.StartVarClass = withPrefix;
    }

    public void setDomainVar(ArrayList<String> domainVar) {
        this.domainVar = domainVar;
    }

    public Hashtable<Integer, ArrayList<String>> getQtable() {
        return qtable;
    }

    public void setQtable(Hashtable<Integer, ArrayList<String>> qtable) {
        this.qtable = qtable;
    }
/**
     * adds the prefix to a predicate
     * @param withoutprefix
     * @return 
     */
    private static String crmPrefix(String withoutprefix) {

        String AssociationTable[][] = VariableFileReader.startLeterAs_read_variables_of("StartLetterAssociation");
        String selectNamespace = "";
        String prefixVar = "";


        for (int g = 0; g < AssociationTable.length; g++) {

            /* Assigning prefix to predicate according to the
             * link's first letter
             */

            if (AssociationTable[g][0].equals(Character.toString(withoutprefix.charAt(0)))) {
                selectNamespace = AssociationTable[g][1];
                prefixVar = selectNamespace + ":" + withoutprefix;
                //re-construct the OR of predicates with the prefixes
                //if it is the first we donnot add the OR

            }
        }

        return prefixVar;
    }
}
