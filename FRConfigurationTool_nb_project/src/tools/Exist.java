package tools;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.NodeList;

/**
 *
 * @author Christos
 */
public class Exist {

    public static NodeList executeGetQuery(String query, String fileName) {
        try{

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(fileName);
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(query);

            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            return nodes;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void executeUpdateQuery(String query, String content, String fileName){
        try{
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(fileName);

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(query);
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            if(nodes.getLength() > 0){
                nodes.item(0).setTextContent(content);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            //StreamResult result = new StreamResult(System.out);
            StreamResult sresult = new StreamResult(fileName);
            transformer.transform(source, sresult);
        } catch(Exception e){
            e.printStackTrace();
        }
    }






}
