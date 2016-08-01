/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rulesUpdate;

import java.util.ArrayList;
import java.util.Hashtable;

import config.RulesConfig;

/**
 * The consequences, premises and name of a rule
 * @author katetzob
 */
public class Rule {

    String name;
    ArrayList<String> consequences;
    ArrayList<String> premises;

    /**
     * 
     * @return
     */
    public ArrayList<String> getConsequences() {
        return consequences;
    }

    /**
     * 
     * @param consequences
     */
    public void setConsequences(ArrayList<String> consequences) {
     
        this.consequences = consequences;
    }

    /**
     * 
     * @return
     */
    public ArrayList<String> getPremises() {
        return premises;
    }

    /**
     * 
     * @param premises
     */
    public void setPremises(ArrayList<String> premises) {
        ArrayList<String> FRpremises = turnPremisesToFRFormat(premises);

        this.premises = FRpremises;
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * checks if the rule can be defined in the FRs (there must be continuity in the rule chain)
     * and turns them to Paths format
     * @param premises
     * @return 
     */
    private ArrayList<String> turnPremisesToFRFormat(ArrayList<String> premises) {

      
        ArrayList<String> newPremises = new ArrayList<String>();
        String previousObject = null;
        for (String premise : premises) {
//take only the first three parts, ignore any constraints put
        	

            String[] PremiseSet = premise.split(" ");

            String subject = PremiseSet[0].replaceAll("<", "").replaceAll(">", "");
            subject = subject.replaceAll("\\s", "");
            

            // updated by Anastasia
            String predicate = PremiseSet[1];
            for (String pref: RulesConfig.rulePrefices){
                predicate = predicate.replaceAll("<"+pref+":", "");
            }
            predicate = predicate.replaceAll(">", "").replaceAll("\\s", "");
            
            
            
            String object = PremiseSet[2].replaceAll("<", "").replaceAll(">", "");
            object = object.replaceAll("\\s", "");
            if (previousObject == null || previousObject.equalsIgnoreCase(subject)) {

                previousObject = object;
                String triple = subject + "--" + predicate + "->" + object;
                triple = triple.replaceAll(" ", "");
                //    System.out.println("triple " + triple);
                newPremises.add(triple);

            } //else we cannot replace the rule in the FR
            else {

                newPremises = null;
                break;

            }
        }
        if (newPremises != null) {
            if (newPremises.size() != premises.size()) {
        
                newPremises = null;
            }
        }
        
        return newPremises;
    }

    /**
     * turns the consequences to FR format (either or not continuous)
     *
     * @param premises
     * @return 
     */
    private Hashtable<String, ArrayList<String>> turnConsequenceToFRFormat(ArrayList<String> consequences) {

     
        Hashtable<String, ArrayList<String>> consequencesTable = new Hashtable<String, ArrayList<String>>();
        ArrayList<String> continuous = new ArrayList<String>();
        ArrayList<String> nonContinuous = new ArrayList<String>();
        String previousObject = null;
        for (String cons : consequences) {
//take only the first three parts, ignore any constraints put

            String[] ConsSet = cons.split(" ");

            String subject = ConsSet[0].replaceAll("<", "").replaceAll(">", "");
            subject = subject.replaceAll("\\s", "");
            
            
         // updated by Anastasia
            String predicate = ConsSet[1];
            for (String pref: RulesConfig.rulePrefices){
                predicate = predicate.replaceAll("<"+pref+":", "");
            }
            predicate = predicate.replaceAll(">", "").replaceAll("\\s", "");

            
            
            String object = ConsSet[2].replaceAll("<", "").replaceAll(">", "");
            object = object.replaceAll("\\s", "");
           
            if (previousObject == null || previousObject.equalsIgnoreCase(subject)) {

                previousObject = object;
                String triple = subject + "--" + predicate + "->" + object;
                triple = triple.replaceAll(" ", "");
       
                continuous.add(triple);

            } //else we cannot replace the rule in the FR
            else {

                previousObject = object;
                String triple = subject + "--" + predicate + "->" + object;
                triple = triple.replaceAll(" ", "");
              
                nonContinuous.add(triple);
            }


        }
        consequencesTable.put("continuous", continuous);
        consequencesTable.put("nonContinuous", nonContinuous);
       
        return consequencesTable;
    }
}
