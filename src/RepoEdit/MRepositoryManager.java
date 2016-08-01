package RepoEdit;

import java.lang.Object.*;

import org.openrdf.repository.Repository;

import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.RepositoryException;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import java.net.URL;


import org.openrdf.query.*;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.rdbms.RdbmsStore;

import org.openrdf.rio.*;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.model.*;

import COFORM.Tools.General;

import java.net.URLConnection;
import java.util.*;
import java.io.*;

/**
 * 
 * @author FORTH-ICS
 *
 */
public class MRepositoryManager {

    /**
     * 
     */
  
    private Repository myRepository;
    private static Sail mySail;//a Sail to the low-level store
    private static java.sql.Connection RDBMSStoreCon;
    private RepositoryConnection con;
    private String resourceBaseURI;

    /**
     * 
     * @param sesameServer1
     * @param repositoryID1
     * @throws RepositoryException
     */
    public MRepositoryManager(String sesameServer1, String repositoryID1) throws RepositoryException {
        if (myRepository == null) {
          
            myRepository = initHTTPRepository(sesameServer1, repositoryID1);
            this.con = this.myRepository.getConnection();
            myRepository.initialize();
          
           
        }
    }

    public MRepositoryManager(String[] repositoryArgs) throws RepositoryException {
        if (myRepository == null) {
            String storeType = repositoryArgs[0];
            if (storeType.equals("http")) {
                String sesameServer = repositoryArgs[1];
                String repositoryID = repositoryArgs[2];
                myRepository = initHTTPRepository(sesameServer, repositoryID);
                myRepository.initialize();
                //System.out.println("MRepositoryManager contextsindexsize>" + com.ontotext.trree.owlim_ext.Repository.. );

            } else if (mySail == null) {
                if (storeType.equals("native")) {
                    String repositoryPath = repositoryArgs[1];
                    String tripleIndexes = repositoryArgs[2];
                    mySail = initNativeStore(repositoryPath, tripleIndexes);
                    //org.openrdf.sesame.sailimpl.rdbms.CustomInferenceServices
                } else if (storeType.equals("rdbms")) {
                    String rdbmsRepositoryURL = repositoryArgs[1];
                    String rdbmsUser = repositoryArgs[2];
                    String rdbmsPwd = repositoryArgs[3];

                    mySail = initRDBMSStore(rdbmsRepositoryURL, rdbmsUser, rdbmsPwd);
                    setRDBMSStoreConnection(rdbmsRepositoryURL, rdbmsUser, rdbmsPwd);
                }


                try {
                    mySail.initialize();
                    myRepository = new SailRepository(mySail);
                } catch (SailException e) {
                    System.out.println("MRepositoryManager constructor error....");
                    e.printStackTrace();
                }
            } else {
                myRepository = new SailRepository(mySail);
            }

        }
    }

    /**
     * 
     * @param sesameServer
     * @param repositoryID
     * @throws RepositoryException
     */
    private HTTPRepository initHTTPRepository(String sesameServer, String repositoryID) throws RepositoryException {
        return new HTTPRepository(sesameServer, repositoryID);
    }

    /**
     * 
     * @param repositoryPath
     * @throws RepositoryException
     */
    private Sail initNativeStore(String repositoryPath, String tripleIndexes) throws RepositoryException {
        File f = new File(repositoryPath);
        ForwardChainingRDFSInferencer RDFSInferencer = new ForwardChainingRDFSInferencer(); //returns a sail object
        NativeStore nativeStore = new NativeStore();  //returns a sail
        nativeStore.setDataDir(f);
        nativeStore.setTripleIndexes(tripleIndexes);
        RDFSInferencer.setBaseSail(nativeStore);
        return RDFSInferencer; //returns a sail
        //rdbmsSail.setInferencerClass("org.openrdf.sesame.sailimpl.rdbms.CustomInferenceServices");rdbmsSail.setParameter("rule-file", "C:\\customRules.xml");		config.addSail(rdbmsSail);
    }

    /**
     * 
     * @param rdbmsRepositoryURL
     * @throws RepositoryException
     */
    private Sail initRDBMSStore(String rdbmsRepositoryURL, String user, String pwd) throws RepositoryException {
        RdbmsStore rdbmsStore = new RdbmsStore(rdbmsRepositoryURL, user, pwd);
        return rdbmsStore; //returns a sail
        //rdbmsSail.setInferencerClass("org.openrdf.sesame.sailimpl.rdbms.CustomInferenceServices");rdbmsSail.setParameter("rule-file", "C:\\customRules.xml");		config.addSail(rdbmsSail);
    }

    /**
     * 
     * @param baseURI
     * @throws RepositoryException
     */
    public void setResourceBaseURI(String baseURI)
            throws RepositoryException {
        this.resourceBaseURI = baseURI;
    }

    public void setNamespace(String prefix, String uri)
            throws RepositoryException {
        myRepository.getConnection().setNamespace(prefix, uri);
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
        myRepository.shutDown();
    }

    /**
     * Method is used to overcome a Sesame's bug for not closing connections on rdbms stores.
     * @throws RepositoryException
     */
    public static void closeRDBMSConnection()
            throws RepositoryException {
        String dropTable = "DROP TABLE locked";
        try {
            java.sql.Statement st = RDBMSStoreCon.createStatement();
            st.executeUpdate(dropTable);
        } catch (java.sql.SQLException e) {
            System.err.println("MRepositoryManager.closeRDBMSConnection error");
            e.printStackTrace();
        }
    }

    /** 
     * Inserts a triple into graph.
     * @param s The subject of the triple URIref.
     * @param p The predicate URIref.
     * @param o The value object (URIref or Literal).
     * @param context The context of the triple.
     */
    public void addTriple(Resource s, URI p, Value o, Resource... context)
            throws RepositoryException {
        /*
        ValueFactory myFactory = this.con.getValueFactory(); 
        Statement st = myFactory.createStatement((Resource)  
        s, p, (Value) o); 
        this.con.add(st, context);
         */

        if (context.length == 0) {
            this.con.add(s, p, o);
        }//just add to the default graph
        else {
            this.con.add(s, p, o, context);
        }
    }

    /** 
     * Inserts a triple/statement into graph.
     * @param st A triple statement.
     * @param context The context of the data.
     */
    public void addTripleStatement(Statement st, Resource... context)
            throws RepositoryException {
        if (context.length == 0) {
            this.con.add(st);
        }//just create the named graphs found in stream
        else {
            this.con.add(st, context);
        }
    }

    /** 
     * Imports RDF data from a string.
     * @param rdfstring The string with RDF data.
     * @param format The RDF format of the string (used to select parser).
     * @param context The context of the data.
     */
    public void addString(String rdfstring, String baseURI, RDFFormat format, Resource... context)
            throws RepositoryException, RDFParseException, IOException {
        StringReader sr = new StringReader(rdfstring);
        if (context.length == 0) {
            this.con.add(sr, baseURI, format);
        }//just create the named graphs found in stream
        else {
            this.con.add(sr, baseURI, format, context);
        }
    }

    /**
     * Imports RDF data from a stream.
     * @param instream The input stream of RDF data.
     * @param baseURI The base URI to resolve any relative URIs that are in the data against.
     * @param format The RDF format of the string (used to select parser).
     * @param context The context of the data.
     */
    public void addStream(InputStream instream, String baseURI, RDFFormat format, Resource... context)
            throws RepositoryException, RDFParseException, IOException {
        synchronized (instream) {

            if (baseURI == null) {
                baseURI = resourceBaseURI;
            }

            if (context.length == 0) {
                this.con.add(instream, baseURI, format);
            }//just create the named graphs found in stream
            else {
                this.con.add(instream, baseURI, format, context);
            }

            instream.close();
            instream = null;
        }//synchronized

    }

    /** 
     * Imports RDF data from a file.
     * @param filepath The (absolute) filepath of file (c:/path/file) with RDF data.
     * @param baseURI The base URI to resolve any relative URIs that are in the data against.
     * @param format The RDF format of the string (used to select parser).
     * @param context The context of the data.
     */
    public void addFile(String filepath, String baseURI, RDFFormat format, Resource... context)
            throws RepositoryException, RDFParseException, IOException {
        if (context.length == 0) {
            this.con.add(new File(filepath), baseURI, format);
        } else {
            this.con.add(new File(filepath), baseURI, format, context);
        }
    }

    /** 
     * Imports data from URI source.
     * Request is made with proper HTTP ACCEPT header.
     * and will follow redirects for proper LOD source negotiation.
     * @param urlstring The (absolute) URI of the data source.
     * @param format The RDF format to request/parse from data source.
     * @param context The context of the data.
     */
    public void addURI(String urlstring, RDFFormat format, Resource... context)
            throws RepositoryException, RDFParseException, IOException {
        //	System.out.println("addURI: "+context.stringValue());
        URL url = new URL(urlstring);
        URLConnection uricon = (URLConnection) url.openConnection();
        uricon.addRequestProperty("accept", format.getDefaultMIMEType());
        InputStream instream = uricon.getInputStream();
        synchronized (instream) {
            if (context.length == 0) {
                this.con.add(instream, urlstring, format);
            } else {
                this.con.add(instream, urlstring, format, context);
            }
            instream.close();
            instream = null;
        }//synchronized
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
            throws RepositoryException, RDFParseException, MalformedQueryException, QueryEvaluationException, RDFHandlerException {// System.out.println("runSPARQL2GRAPHString>>>>>>>>>>>>>>>>>>>>>" + qs);

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
        GraphQuery query = this.con.prepareGraphQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
        GraphQueryResult qres = query.evaluate();
        ArrayList<ArrayList<Value>> reslist = new ArrayList<ArrayList<Value>>();


        // index indicates the size of the newly created list with the results.
        // It is used to run through the previously added elements-triples in the list
        // and check whether the current triple already exists in the list
        // If it exists already, the current triple will not be added in the list
        int index = 0;
        while (qres.hasNext()) {
            Statement st = qres.next();
            boolean addTriple = true;
            for (int i = 0; i < index; i++) {
                ArrayList<Value> tripleList2Check = reslist.get(i);
                if (((st.getSubject()).equals(tripleList2Check.get(0))
                        && (st.getPredicate()).equals(tripleList2Check.get(1))
                        && (st.getObject()).equals(tripleList2Check.get(2)))) {
                    addTriple = false;
                    break;
                }
            }

            if (addTriple == true) {
                ArrayList<Value> triple = new ArrayList<Value>(4);
                triple.add(st.getSubject());
                triple.add(st.getPredicate());
                triple.add(st.getObject());
                triple.add(st.getContext());
                System.out.println("" + triple);
                reslist.add(triple);
                index++;
            }
        }

        System.out.println("reslist " + reslist);
        return reslist;
    }

    /** 
     * Executes a ASK SPARQL query against the graph.
     * @param qs The ASK SPARQL query.
     * @return A boolean about existing solution.
     */
    public boolean runSPARQL2AskBoolean(String qs)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, Exception {
        BooleanQuery query = this.con.prepareBooleanQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);

        return query.evaluate();
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
        //System.out.println("2 runSPARQL2List OK " + query.toString());
        TupleQueryResult qres = query.evaluate();
        //System.out.println("3 runSPARQL2List OK");
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
        //System.out.println("2 runSPARQL2List OK \n" + reslist);
        return reslist;

    }

    /** 
     * Executes a SELECT SPARQL query against the graph.
     * @param qs The SELECT SPARQL query.
     * @return A SPARQLXML result of solutions, in PipedInputStream object.
     */
    public InputStream runSPARQL2XMLStream(String qs)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException, TupleQueryResultHandlerException {
        TupleQuery query =
                this.con.prepareTupleQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        info.aduna.xml.XMLWriter XMLWriter = new info.aduna.xml.XMLWriter(out, "UTF-8");//Creates a new XMLWriter that will write its data to the supplied OutputStream in the default UTF-8 character encoding.
        SPARQLResultsXMLWriter w = new SPARQLResultsXMLWriter(XMLWriter);

        query.evaluate(w);

        byte[] b = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(b);
        return in;
    }

    /** 
     * Executes a SELECT SPARQL query against the graph.
     * @param qs The SELECT SPARQL query.
     * @return A SPARQLXML result of solutions, in String object.
     */
    public String runSPARQL2XMLString(String qs)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, Exception {
        TupleQuery query =
                this.con.prepareTupleQuery(
                org.openrdf.query.QueryLanguage.SPARQL, qs);
        System.out.println("");
        String retVal = null;

        /*
         * This solution does not reserves the encoding of the data 
         * to be written to the output stream. 
         * 
        CharArrayWriter charArray = new CharArrayWriter();
        SPARQLResultsXMLWriter w = new SPARQLResultsXMLWriter(new info.aduna.xml.XMLWriter(charArray));
        query.evaluate(w);
        retVal = charArray.toString();
         *
         */

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        info.aduna.xml.XMLWriter XMLWriter = new info.aduna.xml.XMLWriter(out, "UTF-8");//Creates a new XMLWriter that will write its data to the supplied OutputStream in the default UTF-8 character encoding.
        SPARQLResultsXMLWriter w = new SPARQLResultsXMLWriter(XMLWriter);

        query.evaluate(w);

        retVal = out.toString("UTF-8");

        //System.out.println("String");
        return retVal;
    }

    /**
     * Clears a context with its data.
     * @param context The context of the data to be cleared.
     * @throws RepositoryException
     */
    public void clearContext(Resource... contexts)
            throws RepositoryException {
        for (Resource context : contexts) {
            synchronized (this.con) {
                RepositoryResult<Resource> repres = this.con.getContextIDs();
                while (repres.hasNext()) {
                    if (repres.next().stringValue().equals(context.stringValue())) {
                        //this.con.clear(context);///has bug
                        this.con.remove((Resource) null, (URI) null, (Value) null, context);
                        System.out.println("Context " + context.stringValue() + " is cleared.");
                    }
                }
                repres.close();
            }//synchronized


        }
        contexts = null;

    }

    public boolean contextExist(Resource context)
            throws RepositoryException {
        boolean contextExist = false;
        RepositoryResult<Resource> repres = this.con.getContextIDs();
        while (repres.hasNext()) {
            if (repres.next().stringValue().equals(context.stringValue())) {
                contextExist = true;
            }
        }
        return contextExist;
    }

    /**
     * Get all contexts.
     * @param context The context of the data to be cleared.
     * @throws RepositoryException
     */
    public List<Resource> getRepositoryContexts()
            throws RepositoryException {
        RepositoryResult<Resource> repres = this.con.getContextIDs();
        return repres.asList();
    }

    /**
     * Removes the statement(s) with the specified subject, predicate and object from the repository, optionally restricted to the specified contexts.
     * @param subject The subject of the triple.
     * @param predicate The predicate of the triple.
     * @param object The object of the triple.
     * @param context The context of the triple to be removed. 
     * Note that this parameter is a vararg and as such is optional. 
     * If no contexts are supplied the method operates on the contexts associated with the statement itself, 
     * and if no context is associated with the statement, on the entire repository.  
     * @throws RepositoryException
     */
    public void remove(Resource subject, URI predicate, Value object, Resource... context)
            throws RepositoryException {
        if (context.length == 0) {
            this.con.remove(subject, predicate, object);
        } else {
            this.con.remove(subject, predicate, object, context);
        }
    }

    /**
     * Removes the supplied statement from the specified contexts in the repository.
     * @param st The statement of the triple to be removed.
     * @param context The context of the triple to be removed.
     * Note that this parameter is a vararg and as such is optional. 
     * If no contexts are supplied the method operates on the contexts associated with the statement itself, 
     * and if no context is associated with the statement, on the entire repository.
     * @throws RepositoryException
     */
    public void remove(Statement st, Resource... context)
            throws RepositoryException {
        if (context.length == 0) {
            this.con.remove(st);
        } else {
            this.con.remove(st, context);
        }
    }

    public void clearAllContexts()
            throws RepositoryException {
        RepositoryResult repres = this.con.getContextIDs();
        while (repres.hasNext()) {
            clearContext((Resource) repres.next());
        }
    }

    static void setRDBMSStoreConnection(String url, String dbUsername, String dbPassword) {
        try {
            RDBMSStoreCon = java.sql.DriverManager.getConnection(url, dbUsername, dbPassword);
        } catch (Exception e) {
            System.err.println("setRDBMSStoreConnection connection error...");
            e.printStackTrace();
        }
    }

    /**
     * Exports all triple-store into trig file.
     */
    void exportTrig()
            throws RepositoryException, RDFHandlerException, FileNotFoundException, IOException {
        java.sql.Timestamp t = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
        String fs = General.FILE_SEPARATOR;
        FileOutputStream out;
        String pathname = "C:" + fs + "tmp" + fs + "export_" + t.toString().replaceAll(":", ",") + ".trig";
        File f = new File(pathname);
        out = new FileOutputStream(f);
        TriGWriter writer = new TriGWriter(out);
        this.con.export(writer);
    }

    /**
     * Exports all statements with a specific subject, predicate and/or object from the repository, optionally from the specified contexts. 
     * @param subj The subject, or null if the subject doesn't matter.
     * @param pred The predicate, or null if the predicate doesn't matter.
     * @param The object, or null if the object doesn't matter.
     * @param includeInferred if false, no inferred statements are returned; if true, inferred statements are returned if available 
     * @param handler The handler that will handle the RDF data. Valid input is: "RDFXMLWriter", "TriGWriter", "TriXWriter", "TurtleWriter",  "N3Writer", "NTriplesWriter".
     * @param contexts The context(s) to get the data from. Note that this parameter is a vararg and as such is optional. If no contexts are supplied the method operates on the entire repository.
     */
    void exportStatements(Resource subj,
            URI pred,
            Value obj,
            boolean includeInferred,
            String handler,
            Resource... contexts)
            throws RepositoryException, RDFHandlerException, FileNotFoundException, IOException {

        java.sql.Timestamp t = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
        String fs = General.FILE_SEPARATOR;
        FileOutputStream out;

        String pathname = "C:" + fs + "tmp" + fs + "export_" + t.toString().replaceAll(":", ",") + ".txt";
        File f = new File(pathname);
        f.setWritable(true);
        out = new FileOutputStream(f);
        RDFHandler rdfHandler = null;
        if (handler.equals("RDFXMLWriter")) {
            rdfHandler = new RDFXMLWriter(out);
        } else if (handler.equals("TriGWriter")) {
            rdfHandler = new TriGWriter(out);
        } else if (handler.equals("TriXWriter")) {
            rdfHandler = new TriXWriter(out);
        } else if (handler.equals("TurtleWriter")) {
            rdfHandler = new TurtleWriter(out);
        } else if (handler.equals("N3Writer")) {
            rdfHandler = new N3Writer(out);
        } else if (handler.equals("NTriplesWriter")) {
            rdfHandler = new NTriplesWriter(out);
        }

        this.con.exportStatements(subj, pred, obj, includeInferred, rdfHandler, contexts);

    }

    void controlledExportStatements(FileOutputStream out, Resource subj,
            URI pred,
            Value obj,
            boolean includeInferred,
            String handler,
            Resource... contexts)
            throws RepositoryException, RDFHandlerException, FileNotFoundException, IOException {

        RDFHandler rdfHandler = null;
        if (handler.equals("RDFXMLWriter")) {
            rdfHandler = new RDFXMLWriter(out);
        } else if (handler.equals("TriGWriter")) {
            rdfHandler = new TriGWriter(out);
        } else if (handler.equals("TriXWriter")) {
            rdfHandler = new TriXWriter(out);
        } else if (handler.equals("TurtleWriter")) {
            rdfHandler = new TurtleWriter(out);
        } else if (handler.equals("N3Writer")) {
            rdfHandler = new N3Writer(out);
        } else if (handler.equals("NTriplesWriter")) {
            rdfHandler = new NTriplesWriter(out);
        }

        this.con.exportStatements(subj, pred, obj, includeInferred, rdfHandler, contexts);

    }
}
/*
Important: 

getStatements(null, null, null, true);
is not the same as: 

getStatements(null, null, null, true, (Resource)null);
The former (without any context id parameter) retrieves all statements in the repository, ignoring any context information. The latter, however, only retrieves statements that explicitly do not have any associated context.
 */
