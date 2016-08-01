package tools;

import java.io.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.InputSource;
import javax.xml.transform.OutputKeys;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOM {

	public static NodeList getValue() throws SAXException, IOException{
            NodeList list = null;
            try{
                
                /* It is the DocumentBuilder instance that is the DOM parser. 
                 * Using this DOM parser you can parse XML files into DOM objects
                 */
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                
                /* Parsing an XML file into a DOM tree using the DocumentBuilder
                 */
                Document document = builder.parse(new FileInputStream("./src/results/RESULTS.xml"));
                
               
                NodeList nodeLst = document.getElementsByTagName("uri");
                Node receiver = nodeLst.item(0);
                list = receiver.getChildNodes();
      
                
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DOM.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DOM.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(DOM.class.getName()).log(Level.SEVERE, null, ex);
            } 
		
            
                return list;
	}
        
                
        /**
         * Converts a node element to string
         * @param node
         * @return
         *
         */
        public static String nodeToString(Node node) {
            StringWriter sw = new StringWriter();
            try {
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                t.transform(new DOMSource(node), new StreamResult(sw));
            } catch (TransformerException te) {
                System.out.println("nodeToString Transformer Exception");
            }
            return sw.toString();
        }
	
        
        
	
}
