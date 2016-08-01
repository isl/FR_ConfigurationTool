/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rulesUpdate;

import RepoEdit.MRepositoryManager;
import subrelationshipsfinder.FundamentalCategories;

import java.util.ArrayList;
import org.openrdf.repository.RepositoryException;
import tools.FileReader;

/**
 * Testing purposes
 * @author katetzob
 */
public class Main {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {



        try {
            MRepositoryManager mrm = new MRepositoryManager("http://localhost:8080/openrdf-sesame", "reposioryName");
            String pie = FileReader.readPathConfigFile("repositoryPieFile");
            RulesSet rulesSet = FileReader.readCustomCRMRulesFromPieFile(pie);
            String result="";
      
            for (Rule rule : rulesSet.rulesSet) {
                String rulePath = "";
                String consPath = "";
                String res = "";
 
                if (rule.getPremises() != null) {

                    for (int i = 0; i < rule.getPremises().size(); i++) {

                        String triple = rule.getPremises().get(i);
                        //if it is the starting triple and it is not the only one
                       
                        if (rule.getPremises().size() == 1) {
                            rulePath = triple;
                        } //if there are more than one triples and this is the first one
                        else if (i == 0) {
                            rulePath = triple + ":";
                        } //if there  are more than one triples and this is not the last one
                        else if (i != rule.getPremises().size() - 1) {
                            rulePath += "{" + triple + ":";
                        } else if (i == rule.getPremises().size() - 1) {
                            rulePath += "{" + triple;
                            for (int j = 0; j < rule.getPremises().size() - 1; j++) {
                                rulePath += "}";
                            }
                        }

                    }
                    for (int i = 0; i < rule.getConsequences().size(); i++) {

                        String triple = rule.getConsequences().get(i);
                        consPath += triple;
                    }


                    res = FindsubpathsEqualToRules.subRelFind(rulePath, mrm);
                    if (!res.equalsIgnoreCase("Given Path is not subpath of any Fundamental Relationship")) {
                    
                    
                    result+="The rule with ID: "+rule.getName()+" which corresponds to the path: \n"
                            +rulePath+ " can be applied to the following FRs\n"+
                            res+"To replace it, you can use one of the following consequences: \n";
                         for (int i = 0; i < rule.getConsequences().size(); i++) {

                        String triple = rule.getConsequences().get(i);
                        result += triple+"\n";
                    }
                    }
                }
            }
        } catch (RepositoryException ex) {
            ex.printStackTrace();
        }
    }
}
