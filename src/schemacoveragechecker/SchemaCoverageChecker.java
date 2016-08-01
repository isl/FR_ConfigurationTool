package schemacoveragechecker;

import RepoEdit.MRepositoryManager;
import RepoEdit.RepositoryManager.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.repository.RepositoryException;
import tools.FileReader;

/**
 * Used in order to find which of the super properties in CIDOC crm are not covered
 * by properties in the FR schema
 * @author Katerina
 */
public class SchemaCoverageChecker {


    /**
     * Checks what properties of the schemata of the repository are not included in the FRs
     * @param mrm
     * @return the uncovered properties (only super-properties i.e. properties that don't have other sub-properties rather than them)
     */
    public static String checkShemaCoverage(MRepositoryManager mrm) {
        ArrayList<String> coformSuperProperties = new ArrayList<String>();
        String result = "";
       
            COFORMSchema cof = new COFORMSchema();
         
            coformSuperProperties = cof.ListSuperPredicates(mrm);
            String fundamentalFolder = System.getProperty("user.dir") + System.getProperty("file.separator")
             +FileReader.readPathConfigFile("fundamentalFolder");

            File f = new File(fundamentalFolder);
            QueryingSchema qs = new QueryingSchema();
            HashSet<String> queryingProps = qs.ListProperties(f, mrm);


            for (String fundamentalProperty : queryingProps) {
                if (coformSuperProperties.contains(fundamentalProperty)) {
                    coformSuperProperties.remove(fundamentalProperty);
                }

            }

            result = "Number of not covered properties in CIDOC: " + coformSuperProperties.size() + "\n";
            result += "Uncovered Properties: \n";
            result += "---------------------\n";
            for (String uncoveredProperty : coformSuperProperties) {
             
                result += uncoveredProperty+"\n";
            }

        
        return result;



    }
}
