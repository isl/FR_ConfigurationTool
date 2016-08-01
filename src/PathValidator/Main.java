package PathValidator;



import RepoEdit.*;
import org.xml.sax.SAXException;
import tools.FileReader;
import tools.KeyboardInput;

import java.io.*;
import java.util.Scanner;
import java.util.Stack;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.NodeList;
import tools.DOM;
import tools.PathAndDomainVariables;
import tools.VariableFileReader;

public class Main {

	/** For testing reasons
	 * @param args
	 */
	public static void main(String[] args) throws IOException, RepositoryException, SAXException, Exception {

            
            String testText1 = "E70.Thing (--> (P67F.refers_to -> E53.Place ->])[{";
            String testText2 = "[[[]]]{}[]{{";
            String testText3 = "--> hasjhda -> adshfas --> asfdhasifh -> sdgadgf";
            String testText4 = "a --> b1 OR b2 OR b3 -> c Or d-->e->f oR g-->j->k";
            String testText4a = "E --> P1 OR P2 OR P3 -> E Or E-->P->E oR E-->P->E";
            String testText5 = "--> ->";
            String testText6 = "E70.Thing->(P67F.refers_to) OR (P62F.depicts)->E53.Place";
            String testText7 = "E***->P***->(E*** OR (E***->P***->(E*** OR E***->P***->E***)))";
            String testText8 = "E->P->(E OR (E->P->E) OR (E->P->E))";
            String testText9 = "E->P->(E OR (E->P->E) OR (E->P->E) OR (E***->P***->(E*** OR E***->P***->E***)))";
            String testText10 = "E->P OR p:(E OR (E->P:E) OR (E->P oR pp:E) OR (E***->P***->(E*** OR E***->P***->E***)))";
                        
            String testText11 = "C1-P1->C2:{C3-P2->C4 OR C5-P3->C6 OR null}";
            String testText12 = "C1-P1->C2:{C3-P2->C4:{as OR fa} OR C5-P3->C6:{C7-P4->C8 OR C9-P5->C10 OR null} OR null}";
            String testText13 = "C1-P1->C2:{C3-P2->C4 OR C5-P3->C6:{C7-P4->C8 OR C9-P5->C10 OR null} OR null}";
            String testText14 = "C1-P1->C2:C3-P2->C4";
            
            String testText15 = "C1.Object – P130F.shows_features_of -> C1.Object : { E24.Physical_Man-Made_Thing – P62F.depicts -> E73.Information_Object: { E73.Information Object – P67F.refers_to -> E53.Place OR  E73.Information Object – P129F.is_about -> E53.Place	OR  E89.Propositional_Object – P67F.refers_to -> E53.Place	OR  E89.Propositional_Object – P129F.is_about -> E53.Place } OR E24.Physical_Man-Made_Thing–P128F.carries -> E73.Information_Object: {	E73.Information Object – P67F.refers_to -> E53.Place	OR 	E73.Information Object –  P129F.is_about -> E53.Place	OR 	E89.Propositional_Object – P67F.refers_to -> E53.Place	OR  E89.Propositional_Object – P129F.is_about -> E53.Place } }";
            
            String testText16  = "E70.Thing-{P130F.shows_features_of OR P130B.features_are_also_found_on}->E70.Thing:{E24.Physical_Man-Made_Thing-P62F.depicts->E53.Place OR E89.Propositional_Object-{P67F.refers_to OR P129F.is_about}->E53.Place OR E24.Physical_Man-Made_Thing-P128F.carries->E73.InformationObject:{E73.InformationObject-{P67F.refers_to OR P129F.is_about}->E53.Place}}";
            String testText16b = "E70.Thing--{P130F.shows_features_of OR P130B.features_are_also_found_on}->E70.Thing:{E24.Physical_Man-Made_Thing--P62F.depicts->E53.Place OR E89.Propositional_Object--{P67F.refers_to OR P129F.is_about}->E53.Place OR E24.Physical_Man-Made_Thing--P128F.carries->E73.Information_Object:{E73.Information_Object--{P67F.refers_to OR P129F.is_about}->E53.Place}}";
            
            String testText16c = "E70.Thing--{P130F.shows_features_of OR P130B.features_are_also_found_on}->E70.Thing:{E24.Physical_Man-Made_Thing OR E89.Propositional_Object--{P67F.refers_to OR P129F.is_about}->E53.Place OR E24.Physical_Man-Made_Thing--P128F.carries->E73.Information_Object:{E73.Information_Object--{P67F.refers_to OR P129F.is_about}->E53.Place}}";
            
            
            String testText17 = "E24.Physical_Man-Made_Thing--P62F.depicts->E53.Place";
            
            String testText18 = "E70.Thing--{P130F.shows_features_of OR P130B.features_are_also_found_on}->E70.Thing:"
                                + "{"
                                + "E24.Physical_Man-Made_Thing--P62F.depicts->E53.Place"
                                + " OR "
                                + "E89.Propositional_Object--{P67F.refers_to OR P129F.is_about}->E53.Place"
                                + " OR "
                                + "E24.Physical_Man-Made_Thing--P128F.carries->E73.InformationObject:"
                                    + "{E73.InformationObject--{P67F.refers_to OR P129F.is_about}->E53.Place}"
                                + "}";
            
 
            String testText19 = "E70.Thing--{P130F.shows_features_of OR P130B.features_are_also_found_on}->E70.Thing:"
                                + "{"
	                                + "E24.Physical_Man-Made_Thing--P62F.depicts->E53.Place:"
	                                    + "{"
	                                        + "E73.InformationObject--{P67F.refers_to OR P129F.is_about}->E53.Place"
	                                    + "}"
	                                + " OR "
	                                + "E89.Propositional_Object--{P67F.refers_to OR P129F.is_about}->E53.Place:"
	                                    + "{"
	                                        + "E99.Propositional_Object--{P67F.refers_to OR P129F.is_about}->E53.Place"
	                                    + " OR "
	                                        + "E88.Propositional_Object--{P67F.refers_to OR P129F.is_about}->E53.Place"
	                                    + "}"
	                                + " OR "
	                                + "E24.Physical_Man-Made_Thing--P128F.carries->E73.InformationObject:"
	                                    + "{"
	                                        + "E73.InformationObject--{P67F.refers_to OR P129F.is_about}->E53.Place:"
	                                        	+"{"
	                                        		+ "E77773.InformationObject--{P67F.refers_to OR P129F.is_about}->E53.Place"
	                                        		+ " OR "
	                                        		+ "E73.InformationObject--{P67F.refers_to OR P129F.is_about}->E53.Place:"
	                                        		+ "{"
	    	                                        	+ "E88883.InformationObject--{P67F.refers_to OR P129F.is_about}->E53.Place"
	    	                                        + "}"
	                                        	+"}"
	                                    + "}"
                                + "}";

            
            
            String testText20 = "E70.Thing--{(P46F.is_composed_of)[0,n]  OR (P46B.forms_part_of)[0,n] } -> E18.Physical_Thing:{E18.Physical_Thing OR E18.Physical_Thing -- P12B.was_present_at -> E5.Event : {E5.Event -- P12F.occurred_in_the_presence_of -> E70.Thing}}";
            
            
            //String inputStr = testText18;
            
            
            
            String testText21 = " {E70.Thing -- { P130F.shows_features_of  OR  P130B.features_are_also_found_on }  -> E70.Thing OR E70.Thing -- { P130F.shows_features_of  OR  P130B.features_are_also_found_on }  -> E70.Thing } :{ D1.Digital_Object -- L11B.was_output_of -> D7.Digital_Machine_Event:{ D7.Digital_Machine_Event -- P9B.forms_part_of -> D2.Digitization_Process : {D2.Digitization_Process --  L1F.digitized ->    E18.Physical_Thing }} OR  E24.Physical_Man-Made_Thing -- P62F.depicts -> E70.Thing} ";
            
            
            
            
            
            
            
            //==============================================================================================================================
            //==============================================================================================================================
            
            
            
            /*
             * Uncomment the next line for console interface 
             */
            
            //Validator.demo(testText21);
            //Validator.demo("E21.Person -- {P97B.was_father_for  OR   P96B.gave_birth }->  E67.Birth:       { E5.Event--P98F.brought_into_life->          E21.Person       }");
            
            
            /*
             * Convert ans="" for console interface
             */
            
            String ans = "n";
            
            while (ans!="n" && ans!="N" && ans!="No" && ans!="nO" && ans!="NO" && ans!="no"){
	            
            	System.out.print("Give string: ");
                Scanner in = new Scanner(System.in);
                String inputStr = in.nextLine();
                in.close();


//                Validator.demo(inputStr);


                System.out.print("Run again?");
                Scanner inAns = new Scanner(System.in);
                ans = inAns.nextLine();
                in.close();
	            
            
            }
            
            
            
            
            
            /*
             * Uncomment the next line for graphical interface 
             */
            
        //    JFrame.main(args);
            
            
            
            
            
            //==============================================================================================================================
            //==============================================================================================================================
            
            
            
            
            
            
            
            
            /*
            boolean error = Syntax4.check_open_close(inputStr);
            if (error == true){
                return;
            }
            */
            
            //========= STEP 1 =======//
            /* Seperate the paths from the user input
             */
            //Stack<String> stackPathSeperator = Syntax4.paths_seperator(inputStr);
            //Quest.triplet_connection_validation(stackPathSeperator);
            
            //========= STEP 2 =======//
            /* Seperate each path to triples
             */
            //Stack<String> stackAbsolutePathSeperator = Syntax4.absolute_path_seperator(stackPathSeperator);
            
            //for (int i=0;i<stackAbsolutePathSeperator.size();i++){
                //System.out.println(stackAbsolutePathSeperator.elementAt(i));
            //}
            
            //========= STEP 3 =======//
            //Quest.triplet_validation(stackAbsolutePathSeperator);
            
            
            
            
            
            
            
            
            
            
            /*
            String qrAsk = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                                + "PREFIX crm: <http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CIDOC-CRM.rdfs#>\n"
                                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                                + "PREFIX crmdig: <http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CRMdig.rdfs#>\n"
                                + "ASK {\n"
                                + "crm:E53.Place rdfs:subClassOf crm:E1.CRM_Entity.\n"
                                + "}";
            
            String qrSelectDomain = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                    + "PREFIX crm: <http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CIDOC-CRM.rdfs#>\n"
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                    + "PREFIX crmdig: <http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CRMdig.rdfs#>\n"
                    + "SELECT DISTINCT ?q WHERE {\n"
                    + "crm:P62F.depicts rdfs:domain ?q.\n"
                    + "}";
            
            RepoCom.Repository_Ask_Query(qrAsk);
            RepoCom.Repository_Select_Query(qrSelectDomain);
            
             * 
             */
            
            
            //Syntax4.class_subclass_isolator(Syntax4.paths_seperator(testText16));
            
            //SyntaxCheck.check_open_close(testText18);
            //SyntaxCheck.check_class(testText10);
            //SyntaxCheck.check_class(testText4a);
            //SyntaxCheck
            //SyntaxCheck
            //SyntaxCheck
            
            
    //        NodeList er = tools.Exist.executeGetQuery("//*[local-name() = 'uri']", "./src/results/RESULTS.xml");
  //          String response = er.item(0).getTextContent().trim();
            
            //System.out.println("---------------------");
            //System.out.println("---------------------");
            //System.out.println("---------------------");
    //        System.out.println(response);
            
            
	}

}
