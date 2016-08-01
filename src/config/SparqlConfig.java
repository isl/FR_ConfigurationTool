package config;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * (New class) This class loads (from configuration file) all the related to sparql constant parameters
 * @author axaridou
 *
 */

public class SparqlConfig {
	//next declaration order matters
	static String confpath = System.getProperty("user.dir") + System.getProperty("file.separator");
	private static Config conf = new Config(confpath +"Quest.conf");
	private static String prefixProp = "prefix";
	
	public static HashMap<String,String> preficesList = new HashMap<String,String>();
	public static HashMap<String,String> nsList = new HashMap<String,String>();
	public static String prefices = loadprefices();

	
	@SuppressWarnings("unchecked")
	private static String loadprefices (){
		
		String prefices = "";
		
		try {

			List<Object> list = conf.getList(prefixProp) ;
			ListIterator<Object> it = list.listIterator();
			
			while(it.hasNext()){
				String propname = (String) it.next();
				prefices += "PREFIX " + propname + "\n";
				String pre = propname.split(":")[0];
				String post = propname.replaceFirst(pre+":", "").trim();
				post = post.substring(1);//remove 1st char '<'
				post = post.substring(0,post.length()-1);//remove last char '>'
				
				preficesList.put(pre, post);
				nsList.put(post, pre);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(prefices);

		return prefices;
	}
	
	//for debug
	public static void main(String[] args){

		//System.out.println(Quest.PREFICES);
		//loadprefices ();
		
		//System.out.println( preficesList.get("crm") + "$$$" + nsList.get("http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CRMdig.rdfs#"));
	}
}
