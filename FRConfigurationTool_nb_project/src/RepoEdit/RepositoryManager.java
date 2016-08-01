package RepoEdit;

import org.openrdf.repository.Repository;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.RepositoryException;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import java.net.URL;
//import java.net.URI;

import org.openrdf.query.*;
import org.openrdf.rio.*;
import org.openrdf.model.*;

import java.net.URLConnection;
import java.util.*;
import java.io.*;

/**
 * 
 * @author FORTH-ICS
 *
 */
public class RepositoryManager {

    /**
     * 
     */
    public static String sesameServer;
    public static String repositoryID;
    private static Repository myRepository;
    private RepositoryConnection con;

    /**
     * 
     * @param sesameServer1
     * @param repositoryID1
     * @throws RepositoryException
     */
    public RepositoryManager(String sesameServer1, String repositoryID1) throws RepositoryException {
        if (myRepository == null) {
            init(sesameServer1, repositoryID1);
        }
    }

    /**
     * 
     * @param sesameServer1
     * @param repositoryID1
     * @throws RepositoryException
     */
    public static void init(String sesameServer1, String repositoryID1) throws RepositoryException {
        sesameServer = sesameServer1;//"http://localhost:8080/openrdf-sesame";
        repositoryID = repositoryID1; //"anast";

        System.out.println(sesameServer1);
        System.out.println(repositoryID1);

        myRepository = new HTTPRepository(sesameServer, repositoryID);
        myRepository.initialize();
        //this.con = this.myRepository.getConnection();
    }

    /**
     * 
     * @throws RepositoryException
     */
    public void startTransaction()
            throws RepositoryException {
        this.con.setAutoCommit(false);
    }

    /**
     * 
     * @throws RepositoryException
     */
    public void commitTransaction()
            throws RepositoryException {
        this.con.commit();
    }

    /**
     * 
     * @throws RepositoryException
     */
    public void rollbackTransaction()
            throws RepositoryException {
        this.con.rollback();
    }

    /**
     * 
     * @throws RepositoryException
     */
    public boolean isConnected()
            throws RepositoryException {
        return (this.con != null);//true means connection exists
    }

    /**
     * 
     * @throws RepositoryException
     */
    public void openConnection()
            throws RepositoryException {
        this.con = myRepository.getConnection();
    }

    /**
     * 
     * @throws RepositoryException
     */
    public void closeConnection()
            throws RepositoryException {
        this.con.close();
    }

    /** 
     * Inserts a triple into graph.
     * @param s The subject of the triple URIref.
     * @param p The predicate URIref.
     * @param o The value object (URIref or Literal).
     * @param context The context of the triple.
     */
    public void addTriple(Resource s, URI p, Value o, Resource context)
            throws RepositoryException {

        this.con.add(s, p, o, context);
    }

    /** 
     * Inserts a triple/statement into graph.
     * @param st A triple statement.
     * @param context The context of the data.
     */
    public void addTripleStatement(Statement st, Resource context)
            throws RepositoryException {
        this.con.add(st, context);
    }

    /** 
     * Imports RDF data from a string.
     * @param rdfstring The string with RDF data.
     * @param format The RDF format of the string (used to select parser).
     * @param context The context of the data.
     */
    public void addString(String rdfstring, String baseURI, RDFFormat format, Resource context)
            throws RepositoryException, RDFParseException, IOException {
        StringReader sr = new StringReader(rdfstring);
        this.con.add(sr, baseURI, format, context);
    }

    /**
     * Imports RDF data from a stream.
     * @param instream The input stream of RDF data.
     * @param baseURI The base URI to resolve any relative URIs that are in the data against.
     * @param format The RDF format of the string (used to select parser).
     * @param context The context of the data.
     */
    public void addStream(InputStream instream, String baseURI, RDFFormat format, Resource context)
            throws RepositoryException, RDFParseException, IOException {
        this.con.add(instream, baseURI, format, context);
    }

    /** 
     * Imports RDF data from a file.
     * @param filepath The (absolute) filepath of file (c:/path/file) with RDF data.
     * @param baseURI The base URI to resolve any relative URIs that are in the data against.
     * @param format The RDF format of the string (used to select parser).
     * @param context The context of the data.
     */
    public void addFile(String filepath, String baseURI, RDFFormat format, Resource context)
            throws RepositoryException, RDFParseException, IOException {
        this.con.add(new File(filepath), baseURI, format, context);
    }

    /** 
     * Imports data from URI source.
     * Request is made with proper HTTP ACCEPT header.
     * and will follow redirects for proper LOD source negotiation.
     * @param urlstring The (absolute) URI of the data source.
     * @param format The RDF format to request/parse from data source.
     * @param context The context of the data.
     */
    public void addURI(String urlstring, RDFFormat format, Resource context)
            throws RepositoryException, RDFParseException, IOException {
        URL url = new URL(urlstring);
        URLConnection uricon = (URLConnection) url.openConnection();
        uricon.addRequestProperty("accept", format.getDefaultMIMEType());
        InputStream instream = uricon.getInputStream();
        this.con.add(instream, urlstring, format, context);
    }

    /** 
     * Tuple pattern query - find all statements with the pattern, where null is a wildcard.
     * @param s The subject of the triple (null for wildcard).
     * @param p The predicate of the triple (null for wildcard).
     * @param o The object of the triple (null for wildcard).
     * @return A serialized graph of results.
     */
    public List tuplePattern(URI s, URI p, Value o)
            throws RepositoryException {
        RepositoryResult repres = this.con.getStatements(s, p, o, true);
        ArrayList reslist = new ArrayList();
        while (repres.hasNext()) {
            reslist.add(repres.next());
        }
        return reslist;
    }

    /** 
     * Executes a CONSTRUCT/DESCRIBE SPARQL query against the graph.
     * @param qs The CONSTRUCT or DESCRIBE SPARQL query.
     * @param format The serialization format for the returned graph.
     * @return A serialized graph of results.
     */
    public String runSPARQL2GRAPHString(String qs, RDFFormat format)
            throws RepositoryException, RDFParseException, MalformedQueryException, QueryEvaluationException, RDFHandlerException { //System.out.println("runSPARQL2GRAPHString>>>>>>>>>>>>>>>>>>>>>" + qs);
        GraphQuery query =
                this.con.prepareGraphQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);
        StringWriter stringout = new StringWriter();
        RDFWriter w = Rio.createWriter(format, stringout);
        query.evaluate(w);

        return stringout.toString();
    }

    /** 
     * Executes a CONSTRUCT/DESCRIBE SPARQL query against the graph.
     * @param qs The CONSTRUCT or DESCRIBE SPARQL query.
     * @param format The serialization format for the returned graph.
     * @return A serialized graph of results.
     */
    public List runSPARQL2GRAPHList(String qs, RDFFormat format)
            throws RepositoryException, RDFParseException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {
        GraphQuery query =
                this.con.prepareGraphQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);
        GraphQueryResult qres = query.evaluate();
        ArrayList reslist = new ArrayList();
        while (qres.hasNext()) {
            Statement st = qres.next();
            ArrayList triple = new ArrayList(4);
            triple.add(st.getSubject());
            triple.add(st.getPredicate());
            triple.add(st.getObject());
            triple.add(st.getContext());
            reslist.add(triple);
        }
        return reslist;
    }

    /** 
     * Executes a SELECT SPARQL query against the graph.
     * @param qs The SELECT SPARQL query.
     * @return A list of solutions, each containing a hashmap of bindings.
     */
    public List runSPARQL2List(String qs)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        //System.out.println("1 runSPARQL2List OK \n" + this.con);
        TupleQuery query =
                this.con.prepareTupleQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);
        //  System.out.println("2 runSPARQL2List OK " + query.toString());
        TupleQueryResult qres = query.evaluate();
        //   System.out.println("3 runSPARQL2List OK");
        ArrayList reslist = new ArrayList();
        while (qres.hasNext()) {
            BindingSet b = qres.next();
            Set names = b.getBindingNames();
            HashMap hm = new HashMap();
            for (Object n : names) {
                hm.put((String) n, b.getValue((String) n));
            }
            reslist.add(hm);
        }
        return reslist;

    }

    /** 
     * Executes a SELECT SPARQL query against the graph.
     * @param qs The SELECT SPARQL query.
     * @return A SPARQLXML result of solutions, in PipedInputStream object.
     */
    public PipedInputStream runSPARQL2XMLStream(String qs)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        TupleQuery query =
                this.con.prepareTupleQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);

        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        try {
            out.connect(in);
            SPARQLResultsXMLWriter w = new SPARQLResultsXMLWriter(out);
            query.evaluate(w);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return in;
    }

    /** 
     * Executes a SELECT SPARQL query against the graph.
     * @param qs The SELECT SPARQL query.
     * @return A SPARQLXML result of solutions, in String object.
     */
    public String runSPARQL2XMLString(String qs)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException {
        TupleQuery query =
                this.con.prepareTupleQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);

        String retVal = null;
        try {
            CharArrayWriter charArray = new CharArrayWriter();
            SPARQLResultsXMLWriter w = new SPARQLResultsXMLWriter(new info.aduna.xml.XMLWriter(charArray));
            query.evaluate(w);
            retVal = charArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * Clears a context with its data.
     * @param context The context of the data to be cleared.
     * @throws RepositoryException
     */
    public void clearContext(Resource context)
            throws RepositoryException {

        this.con.clear(context);
    }

    /**
     * Removes the statement(s) with the specified subject, predicate and object from the repository, optionally restricted to the specified contexts.
     * @param subject The subject of the triple.
     * @param predicate The predicate of the triple.
     * @param object The object of the triple.
     * @param context The context of the triple to be removed.
     * @throws RepositoryException
     */
    public void remove(Resource subject, URI predicate, Value object, Resource context)
            throws RepositoryException {

        this.con.remove(subject, predicate, object, context);
    }

    /**
     * Removes the supplied statement from the specified contexts in the repository.
     * @param st The statement of the triple to be removed.
     * @param context The context of the triple to be removed.
     * @throws RepositoryException
     */
    public void remove(Statement st, Resource context)
            throws RepositoryException {

        this.con.remove(st, context);
    }

    public void clearAllContexts()
            throws RepositoryException {
        RepositoryResult repres = this.con.getContextIDs();
        while (repres.hasNext()) {
            clearContext((Resource) repres.next());
        }
    }
}