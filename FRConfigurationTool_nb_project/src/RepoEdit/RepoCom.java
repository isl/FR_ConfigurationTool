package RepoEdit;

import java.io.IOException;
import java.util.Properties;

import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import COFORM.DataTypes.RDFResourceID;
import COFORM.DataTypes.SPARQLStatement;

import java.util.List;
import org.w3c.dom.NodeList;
import tools.FileReader;
import tools.WriteFile;


public class RepoCom {

	/**
	 * @param args
	 * @throws RepositoryException 
	 */
	public static void Repository_Select_Query(String query,MRepositoryManager mrm) throws QueryEvaluationException,RepositoryException, IOException, Exception {
		
      
            try {
             
                WriteFile.write(mrm.runSPARQL2XMLString(query));
            
            } catch (MalformedQueryException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } 
             catch (TupleQueryResultHandlerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            
	}
        
        /**
	 * @param args
	 * @throws RepositoryException 
         * @return boolean
	 */
	public static boolean Repository_Ask_Query(String query,MRepositoryManager mrm) throws QueryEvaluationException,RepositoryException, IOException, Exception {
		
          
            boolean response = false;
            
            try {
                response = mrm.runSPARQL2AskBoolean(query);
                
            } catch (MalformedQueryException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }  catch (TupleQueryResultHandlerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            
            return response;
            
	}
	
	

}
