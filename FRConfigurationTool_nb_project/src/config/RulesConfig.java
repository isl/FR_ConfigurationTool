package config;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * (New class) This class loads (from configuration file) all the related to sparql constant parameters
 * @author axaridou
 *
 */

public class RulesConfig {
	//next declaration order matters
	static String confpath = System.getProperty("user.dir") + System.getProperty("file.separator");
	private static Config conf = new Config(confpath +"Quest.conf");
	private static String rulePreficesProp = "rulePrefix";
	
	public static String[] rulePrefices = loadprefices();

	
	private static String[] loadprefices (){
		
		String[] prefices = {};
		
		try {
			List<Object> list = conf.getList(rulePreficesProp);
			prefices = new String[list.size()];
			int i = 0;
			
			ListIterator<Object> it = list.listIterator();
			
			while(it.hasNext()){
				String propname = (String) it.next();
				prefices[i++] = propname;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prefices;
	}
	
	//for debug
	public static void main(String[] args){
		System.out.println(""+rulePrefices[0]);
	}
}
