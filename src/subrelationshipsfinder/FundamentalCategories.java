/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subrelationshipsfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import config.FCFRConfig;

/**
 * Enumerates the Fundamental Categories
 * @author Katerina
 */

public class FundamentalCategories {
	
	public static ArrayList<Category> values = load();
	
	private static ArrayList<Category> load(){
		ArrayList<Category> tmp = new ArrayList<Category>();
		Iterator<String> it = FCFRConfig.fcList.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			tmp.add(
					new Category(key, FCFRConfig.fcList.get(key))
					);
		}
		return tmp;
	}

	
	public static class Category{
		 String name;
		 String type;
		 
		 protected Category(String name, String type){
			 this.name = name;
			 this.type = type;
		 }
	}
	
/**
     * The existing Fundamental Categories
     */
    public static enum CategoryDEL {
    THING, ACTOR, PLACE, TIME,
    EVENT, CONCEPT
    
    
}

}
