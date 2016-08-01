package config;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * (New class) This class loads (from configuration file) all the related to sparql constant parameters
 * @author axaridou
 *
 */

public class FCFRConfig {
	//next declaration order matters
	static String confpath = System.getProperty("user.dir") + System.getProperty("file.separator");
	private static Config conf = new Config(confpath +"Quest.conf");
	private static String fcProp = "fc";
	
	public static HashMap<String,String> fcList = loadFCs();
	public static String fcStart = conf.getString("fc.start");

	
	private static HashMap<String,String> loadFCs (){
		
		HashMap<String,String> tmp = new HashMap<String,String>();
		
		try {

			List<Object> list = conf.getList(fcProp) ;
			ListIterator<Object> it = list.listIterator();
			
			while(it.hasNext()){
				String propname = (String) it.next();
	
				String fcName = propname.split(";")[0].trim();
				String fcClass = propname.split(";")[1].trim();
			    tmp.put(fcName, fcClass);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tmp;
	}
	
	//for debug
	public static void main(String[] args){

		//System.out.println(Quest.PREFICES);
		//loadprefices ();
		
		//System.out.println( preficesList.get("crm") + "$$$" + nsList.get("http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CRMdig.rdfs#"));
	}
}
