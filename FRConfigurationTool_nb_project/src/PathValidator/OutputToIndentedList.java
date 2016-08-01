/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

import org.springframework.util.StringUtils;

import tools.WriteFile;

/**
 * Creates an intended list format of the input path
 * @author Christos
 */
public class OutputToIndentedList {
    
    public static void OutputToIntendedList (Stack<String> paths) throws IOException {
        
    	String IntendedList = "";
        boolean frstTime = true;
        
        for (int i=0;i<paths.size();i++){
            String temp = "";
            String [] tempTab = paths.elementAt(i).split("[|]");
                
            if (frstTime){
                
                for (int j=0;j<tempTab.length;j++){
                    
                    String tmp = "";
                    
                    for (int k=0;k<j;k++){
                        tmp = tmp + "\t";
                    }
                  
                    temp = temp + tmp + tempTab[j] + "\n";
                }
                
                frstTime = false;
            }
            else {
                for (int j=1;j<tempTab.length;j++){
                    
                    String tmp = "";
                    
                    for (int k=0;k<j;k++){
                        tmp = tmp + "\t";
                    }
                   
                    temp = temp + tmp + tempTab[j] + "\n";
                }
            }
            
            
            
            System.out.println(temp);
            
            
            IntendedList = IntendedList + temp;
        }
        
        WriteFile.write_general(IntendedList, "./src/results/IntendedList.txt",true);
        
    }
    
    
    
    /*
     * WORK
     */
    public static void createIndentedList (Stack<String> paths) throws IOException {
    	
    	/* Find the largest path (we are going to use this information in future)
    	 */
    	int max = 0;
    	for (int i=0;i<paths.size();i++){
    		// Count the occurrences of a character
    		int cnt = StringUtils.countOccurrencesOf(paths.elementAt(i), "|");
    		    		
    		// Compare with previous to find the string with the most occurrences
    		if (max < cnt) {
    			max = cnt;
    		}
    	}
    	
    	
    	/* Create a table where we will save each value of our path. The 
    	 * table row is equal to number of paths and the columns are equal 
    	 * to each node of our path.  
    	 */
    	max = max+1; // plus one because we count the connectors "|" not the nodes
    	String [][] indListTab = new String[paths.size()][max];
    	
    	
    	for (int i=0;i<paths.size();i++){
    		
    		// Split each path 
    		String[] tmpStr = paths.elementAt(i).split("[|]");
    		
    		/* and save it into the table. The table is not going to be 
    		 * out of order because we have already count the largest path 
    		 * and we create the table according that dimensions. So, the
    		 * smaller paths are going to leave empty slots at the end of
    		 * each row.  
    		 */
    		for (int j=0;j<tmpStr.length;j++){
    			indListTab[i][j] = tmpStr[j];
    		}
    	}
    	
    	
    	/* Scan all the table and delete duplicated values in each column
    	 * according to specific criteria 
    	 */
    	for (int i=0;i<paths.size();i++){
    		for (int j=0;j<max;j++){
    			
    			// Parse each value and check the column which belongs to
    			for (int g=0;g<paths.size();g++){
    				
    				/* Check if:
    				 *   i. the slot in array isn't empty
    				 *  ii. the column IS the first one
    				 * iii. the slot we exam has the same value with the each specific slot
    				 *  iv. the slot we exam IS NOT the same
    				 */
    				if (indListTab[i][j]!=null && j==0 && indListTab[i][j].equals(indListTab[g][j]) && g!=i){
    					indListTab[g][j]="_E_M_P_T_Y_";
    				}
    				
    				/* Check if:
    				 *   i. the slot in array isn't empty
    				 *  ii. the column IS NOT the first one
    				 * iii. the slot in previous column and same row is _E_M_P_T_Y_ which mean that
    				 *      we are at the same subpath and we have a duplicate value and not in another
    				 *      subpath with just the same value
    				 *  iv. the slot we exam has the same value with the each specific slot
    				 *   v. the slot we exam IS NOT the same
    				 */
    				if (indListTab[i][j]!=null && j>0 && indListTab[g][j-1]=="_E_M_P_T_Y_" && indListTab[i][j].equals(indListTab[g][j]) && g!=i){
    					indListTab[g][j]="_E_M_P_T_Y_";
    				}
    				
    				/* Previously, when we fill the array we left a few Null slot when the path is smaller than
    				 * the biggest one. Thats slots we are going to give the value _E_M_P_T_Y_ because is going 
    				 * to help us to manipulate the array at final stage of this process 
    				 */
    				if (indListTab[i][j] == null){
    					indListTab[g][j]="_E_M_P_T_Y_";
    				}
    			}// End of for with "g"
    			
    		}// End of for with "j"
    	}// End of for with "i"
    	
    	
    	
    	
    	String IndenntedListStr = "";
    	
    	for (int i=0;i<paths.size();i++){
    		for (int j=0;j<max;j++){
    			
    			String tmpStr = "";
    			
    			
    			
    			if(indListTab[i][j]!= "_E_M_P_T_Y_" ){
    				
    				for (int k=0;k<j;k++){
        				tmpStr = tmpStr + "\t";
        			}
    				tmpStr = tmpStr + indListTab[i][j] + "\n";
    			}
    			
    			
    			if (indListTab[i][j]=="_E_M_P_T_Y_" && j<max-1 && indListTab[i][j+1]!="_E_M_P_T_Y_"){
    				
    				for (int k=0;k<=j;k++){
        				tmpStr = tmpStr + "\t";
        			}
    				tmpStr = tmpStr + "OR" + "\n";
    			}
	    		
    			IndenntedListStr = IndenntedListStr + tmpStr;
    		}
    	}
		
    	WriteFile.write_general(IndenntedListStr, "./src/results/IntendedList.txt",true);
        //WriteFile.write_general(IndenntedListStr, "./src/results/OUTPUT");
    	
    	
    	/*TESTER
         * 
        for (int i=0;i<paths.size();i++){
    		String tmp ="";
    		for (int j=0;j<max;j++){
    			tmp = tmp +"     "+ indListTab[i][j];
    		}
    		//System.out.println(tmp);
    	}
         * 
         */
    	
    	
    	
    	
    }
    
    
    
}
