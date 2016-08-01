/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package schemacoveragechecker;

import RepoEdit.MRepositoryManager;
import config.SparqlConfig;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * Used to find the super-properties in the COFORM schema
 * @author Katerina
 */
public class COFORMSchema {

    private String queryForAllPredicates = " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
            + "select distinct $prop{"
            + "$prop rdfs:subPropertyOf $higherProperty."
            + "FILTER(REGEX(STR($prop), \"^http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CIDOC-CRM.rdfs#\"))} ";
    private String queryForChildPredicates = " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
            + "select distinct $prop{"
            + "$prop rdfs:subPropertyOf $higherProperty."
            + "FILTER(REGEX(STR($prop), \"^http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CIDOC-CRM.rdfs#\")) "
            + "FILTER($higherProperty!=$prop)}";

    public String getQueryForChildPredicates() {
        return queryForChildPredicates;
    }

    public void setQueryForChildPredicates(String queryForChildPredicates) {
        this.queryForChildPredicates = queryForChildPredicates;
    }

    public String getQueryForAllPredicates() {
        return queryForAllPredicates;
    }

    public void setQueryForAllPredicates(String queryForAllPredicates) {
        this.queryForAllPredicates = queryForAllPredicates;
    }

    /**
     * This method lists only the predicates of the CIDOC-CRM and CIDOC-CRMdig schema
     * that are not children of any other predicate
     * @param mrm the connection to the database
     * @return 
     */
    public ArrayList<String> ListSuperPredicates(MRepositoryManager mrm) {
      
        ArrayList<String> childrenPredicatesList = new ArrayList<String>();
        ArrayList<String> allPredicatesList = new ArrayList<String>();
        //first find all predicates
        String sparql = this.getQueryForAllPredicates();
        allPredicatesList = ListPredicates(mrm, sparql);
 
        //then find all children predicates
        sparql = this.getQueryForChildPredicates();
        childrenPredicatesList = ListPredicates(mrm, sparql);
    
        //now find the dis-joint of the two sets

        for (String predicate : childrenPredicatesList) {
            if (allPredicatesList.contains(predicate)) {
                allPredicatesList.remove(predicate);
            }

        }
        ArrayList<String> propertiesWithPrefixList = new ArrayList<String>();
        propertiesWithPrefixList = addPrefixInsteadOfFullNameSpace(allPredicatesList);

        
        //the list containing the super properties is now the eliminated allPredicatesList
        return propertiesWithPrefixList;
    }

    /**
     * This method lists all predicates of the CIDOC-CRM CIDOC-CRMdig schema, according to the sparql
     * @param mrm the connection object to the repository
     * @param sparql the sparql to be executed
     * @return the list with the predicates
     */
    public ArrayList<String> ListPredicates(MRepositoryManager mrm, String sparql) {


        String xmlResult = "";
        try {
            xmlResult = mrm.runSPARQL2XMLString(sparql);
        } catch (RepositoryException ex) {
            Logger.getLogger(COFORMSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(COFORMSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(COFORMSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TupleQueryResultHandlerException ex) {
            Logger.getLogger(COFORMSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(COFORMSchema.class.getName()).log(Level.SEVERE, null, ex);
        }

        //   System.out.println("xmlResult = " + xmlResult);

        ArrayList<String> predicatesList = new ArrayList<String>();
        predicatesList = XMLtoArrayListResults(xmlResult);
        return predicatesList;


    }

    /**
     * XML handling of the sparql results to take the results in a list
     * @param xmlResults the xml formatted string with the results
     * @return  the arraylist with the results
     */
    public ArrayList<String> XMLtoArrayListResults(String xmlResults) {
        ArrayList<String> predicatesList = new ArrayList<String>();
        try {
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlResults));

            org.w3c.dom.Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("result");

            // iterate the uris
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);

                NodeList name = element.getElementsByTagName("uri");
                Element line = (Element) name.item(0);

             
                predicatesList.add(getCharacterDataFromElement(line));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        return predicatesList;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }
    /**
     * This method replaces the full namespace from the sparql result 
     * with the respective prefix
     * @param propertiesList
     * @return 
     */

    public ArrayList<String> addPrefixInsteadOfFullNameSpace(ArrayList<String> propertiesList) {
        ArrayList<String> newList = new ArrayList<String>();

        for (String prop : propertiesList) {
        	
        	 //updated by Anastasia
            Iterator<String> nsIt = SparqlConfig.nsList.keySet().iterator();
            while(nsIt.hasNext()){
            	String ns = nsIt.next();
            	 if (prop.startsWith(ns)) {
                     prop = prop.replace(ns, SparqlConfig.nsList.get(ns)+":");
                 }
            }

            newList.add(prop);
        }



        return newList;
    }
}
