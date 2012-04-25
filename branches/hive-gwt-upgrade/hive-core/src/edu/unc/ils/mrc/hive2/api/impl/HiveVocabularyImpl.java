/**
 * Copyright (c) 2011, UNC-Chapel Hill and Nescent

   All rights reserved.

   Redistribution and use in source and binary forms, with or without modification, are permitted provided 
   that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and 
    * the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
    * following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the UNC-Chapel Hill or Nescent nor the names of its contributors may be used to endorse or promote 
    * products derived from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
   STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.unc.ils.mrc.hive2.api.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.FSDirectory;
import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

import edu.unc.ils.mrc.hive.ir.lucene.search.Autocomplete;
import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;
import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveIndex;
import edu.unc.ils.mrc.hive2.api.HiveVocabulary;

/**
 * This class represents a single vocabulary in the Hive. Hive is 
 * primarily backed by a Sesame/OpenRDF native store with supplemental
 * relational (H2) and full-text (Lucene) stores.
 * 
 * The H2 database contains some administrative data as well as a 
 * table of brief concept records for fast lookup.
 * 
 * The Lucene index contains structured documents for natural language
 * queries over the vocabulary.
 *  
 * @author craig.willis@unc.edu
 *
 */
public class HiveVocabularyImpl implements HiveVocabulary
{
	private static final Log logger = LogFactory.getLog(HiveVocabularyImpl.class);
			
	private static Map<String, HiveVocabularyImpl> instances = new HashMap<String, HiveVocabularyImpl>();
	
	/* Vocabulary name */
	String name;
	
	/* Base path for Hive indexes for this vocabulary */
	String basePath;
	
	/* Base path for Sesame store for this vocabulary */
	String sesamePath;
	
	/* Sesame objects */
    NativeStore store;
    SesameManager manager;
    Repository repository;
    SesameManagerFactory factory;
    
    /* H2 index */
    HiveIndex h2Index;
    
    /* Lucene index */
    HiveLuceneIndexImpl luceneIndex;
    
    /* Autocomplete */
    Autocomplete autocomplete;
    
    /**
     * Constructs a HiveVocabulary instance for the specified vocabulary
     * at the specified path
     * @param basePath	Base path for vocabulary
     * @param name		Name of vocabulary
     */
    protected HiveVocabularyImpl(String basePath, String name) {
		this.name = name;
		this.sesamePath = basePath + File.separator + "sesame";
		String h2Path = basePath + File.separator + "h2";
		String lucenePath = basePath + File.separator + "lucene";
		String autocompletePath = basePath + File.separator + "autocomplete";
		init(h2Path, lucenePath, autocompletePath);
	}
	
    /**
     * Constructs a HiveVocabulary instance for the specified vocabulary
     * at the specified path
     * @param basePath	Base path for vocabulary
     * @param name		Name of vocabulary
     */
	protected HiveVocabularyImpl(String name, String lucenePath, String sesamePath, String h2Path, String autocompletePath) {
		this.name = name;;
		this.sesamePath = sesamePath;
		
		init(h2Path, lucenePath, autocompletePath);
	}
	
	/**
	 * Initialize the Hive indexes
	 */
	private void init(String h2Path, String lucenePath, String autocompletePath)
	{
		/* Initialize Sesame store */
		//String indexes = "spoc,posc,ospc";
		String indexes = "spoc,ospc";
        this.store = new NativeStore(new File(sesamePath), indexes);
        this.repository = new SailRepository(store);
        
        try
        {
            repository.initialize();            
            ElmoModule module = new ElmoModule();           
            this.factory = new SesameManagerFactory(module, repository);         
            this.manager = factory.createElmoManager();      
            
            // Initialized the H2 and Lucene indexes
            this.h2Index = new HiveH2IndexImpl(h2Path, name);
            this.luceneIndex = new HiveLuceneIndexImpl(lucenePath, name);
            this.autocomplete = new Autocomplete(autocompletePath);
            
        } catch (RepositoryException e) {
        	logger.error(e);
        } catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public static HiveVocabularyImpl getInstance(String basePath, String name) {
		HiveVocabularyImpl instance = instances.get(name);
		if (instance == null) {
			instance = new HiveVocabularyImpl(basePath, name);
			instances.put(name, instance);
		}
		return instance;
	}
	
	public static HiveVocabularyImpl getInstance(String name, String lucenePath, String sesamePath, 
			String h2Path, String autocompletePath) {
		HiveVocabularyImpl instance = instances.get(name);
		if (instance == null) {
			instance = new HiveVocabularyImpl(name, lucenePath, sesamePath, h2Path, autocompletePath);
			instances.put(name, instance);
		}
		return instance;
	}

	/**
	 * Add a SKOS Concept to the Hive indexes
	 * @param concept	Hive SKOS Concept
	 */
	@Override
	public void addConcept(HiveConcept concept) throws Exception 
	{
		logger.debug("addConcept: " + concept.getQName());
		
		HiveConcept existingConcept = findConcept(concept.getQName());
		if (existingConcept != null) 
		{
			// If a concept exists for this URI, update instead.
			updateConcept(concept);
		}
		else 
		{
			// Add concept to Sesame store
			addConceptInternal(concept);
			
			// Add concept to H2 index
			h2Index.addConcept(concept);
			
			// Add concept to Lucene index
			luceneIndex.addConcept(concept);
		}
	}
	
	/**
	 * Adds the concept to the underlying Sesame store
	 * @param concept
	 * @throws Exception
	 */
	protected void addConceptInternal(HiveConcept concept) throws Exception
	{
		logger.debug("addConceptInternal: " + concept.getQName());
		
		ContextAwareConnection con = manager.getConnection();
		
		ValueFactory vf = repository.getValueFactory();
		String uri = concept.getQName().getNamespaceURI() + concept.getQName().getLocalPart();
		
		URI subject = vf.createURI(uri);
		
		URI skosConcept = vf.createURI(HiveConcept.CONCEPT);
		URI skosPrefLabel = vf.createURI(HiveConcept.PREFLABEL);
		URI skosAltLabel = vf.createURI(HiveConcept.ALTLABEL);
		URI skosBroader = vf.createURI(HiveConcept.BROADER);
		URI skosNarrower = vf.createURI(HiveConcept.NARROWER);
		URI skosRelated = vf.createURI(HiveConcept.RELATED);
		URI skosScopeNote = vf.createURI(HiveConcept.SCOPENOTE);

		con.add(vf.createStatement(subject, RDF.TYPE, skosConcept));
		
		con.add(vf.createStatement(subject, skosPrefLabel, vf.createLiteral(concept.getPrefLabel())));

		for (String altLabel: concept.getAltLabels())
			con.add(vf.createStatement(subject, skosAltLabel, vf.createLiteral(altLabel)));

		for (String scopeNote: concept.getScopeNotes())
			con.add(vf.createStatement(subject, skosScopeNote, vf.createLiteral(scopeNote)));
		
		for (String broader: concept.getBroaderConcepts()) {
			con.add(vf.createStatement(vf.createURI(broader), RDF.TYPE, skosConcept));
			con.add(vf.createStatement(subject, skosBroader, vf.createURI(broader)));
		} 

		for (String narrower: concept.getNarrowerConcepts())  {
			con.add(vf.createStatement(vf.createURI(narrower), RDF.TYPE, skosConcept));
			con.add(vf.createStatement(subject, skosNarrower, vf.createURI(narrower)));
		}

		for (String related: concept.getRelatedConcepts()) {
			con.add(vf.createStatement(vf.createURI(related), RDF.TYPE, skosConcept));
			con.add(vf.createStatement(subject, skosRelated, vf.createURI(related)));
		}
		

	
		
		manager.flush();
	}

	/**
	 * Updates the specified concept in the Hive vocabulary.
	 */
	@Override
	public void updateConcept(HiveConcept hc) throws Exception 
	{
		logger.debug("updateConcept: " + hc.getQName());
		
		ContextAwareConnection con = manager.getConnection();
		
		ValueFactory vf = repository.getValueFactory();
		String uri = hc.getQName().getNamespaceURI() + hc.getQName().getLocalPart();
		
		URI subject = vf.createURI(uri);
		
		Concept conceptOld = manager.find(Concept.class, hc.getQName());
		
		URI skosBroader = vf.createURI(HiveConcept.BROADER);
		URI skosNarrower = vf.createURI(HiveConcept.NARROWER);
		URI skosRelated = vf.createURI(HiveConcept.RELATED);
		URI skosAltLabel = vf.createURI(HiveConcept.ALTLABEL);
		URI skosScopeNote = vf.createURI(HiveConcept.SCOPENOTE);
		
		// If narrowers exist in old concept, but not in new, remove statements from narrower
		Set<Concept> narrowersOld = conceptOld.getSkosNarrowers();
		List<String> narrowersNew = hc.getNarrowerConcepts();
		for (Concept n: narrowersOld) {
			String narrowerUri = n.getQName().getNamespaceURI() + n.getQName().getLocalPart();
			if (!narrowersNew.contains(narrowerUri)) {
				// Narrower relation has been removed. Delete broader statement, if present
				con.remove(vf.createStatement(vf.createURI(narrowerUri), skosBroader, subject));
				con.remove(vf.createStatement(subject, skosNarrower, vf.createURI(narrowerUri)));
			}
		}		

		// If broaders exist in old concept, but not in new, remove statements from broader
		Set<Concept> broadersOld = conceptOld.getSkosBroaders();
		List<String> broadersNew = hc.getBroaderConcepts();
		for (Concept b: broadersOld) {
			String broaderUri = b.getQName().getNamespaceURI() + b.getQName().getLocalPart();
			if (!broadersNew.contains(broaderUri)) {
				// Broader relation has been removed. Delete narrower statement, if present
				con.remove(vf.createStatement(vf.createURI(broaderUri), skosNarrower, subject));
				con.remove(vf.createStatement(subject, skosBroader, vf.createURI(broaderUri)));
			}
		}
		
		// If relateds exist in old concept, but not in new, remove statements from related
		Set<Concept> relatedOld = conceptOld.getSkosRelated();
		List<String> relatedNew = hc.getRelatedConcepts();
		for (Concept r: relatedOld) {
			String relatedUri = r.getQName().getNamespaceURI() + r.getQName().getLocalPart();
			if (!relatedNew.contains(relatedUri)) {
				// Related relation has been removed. Delete related statement, if present
				con.remove(vf.createStatement(vf.createURI(relatedUri), skosRelated, subject));
				con.remove(vf.createStatement(subject, skosRelated, vf.createURI(relatedUri)));
			}
		}
		
		// Remove old alt labels and scope notes
		con.remove(subject, skosAltLabel, null);
		con.remove(subject, skosScopeNote, null);
		
		h2Index.updateConcept(hc);
		
		addConceptInternal(hc);
	}

	/**
	 * Removes the specified concept from the Hive vocabulary. Calling 
	 * this method will remove the concept from Sesame, Lucene and H2.
	 * @param qname	Qname of concept to be removed
	 */
	@Override
	public void removeConcept(QName qname) throws Exception
	{
		logger.debug("removeConcept: " + qname);
		
		ContextAwareConnection con = manager.getConnection();
		
		ValueFactory vf = repository.getValueFactory();
		String uri = qname.getNamespaceURI() + qname.getLocalPart();
		
		URI subject = vf.createURI(uri);

		//con.remove((Resource)null, (URI)null, subject);
		con.remove(vf.createStatement(subject, null, null));
		con.remove(vf.createStatement(null, null, subject));
		
		// Remove concept from H2
		h2Index.removeConcept(qname);
		
		// Remove concept from Lucene
		luceneIndex.removeConcept(qname);
	}
	
	/**
	 * Returns the total number of concepts in this vocabulary.
	 */
	@Override
	public long getNumConcepts() throws Exception {
		// Sesame doesn't support a simple count(*), so use H2 for this total
		return h2Index.getNumConcepts();
	}
	

	/**
	 * Returns a HiveConcept representing the SKOS concept 
	 * for the specified QName.
	 * 
	 * @param 	qname	QName of concept
	 */
	@Override
	public HiveConcept findConcept(QName qname) throws Exception {
	    HiveConcept hc = null;
		// Concepts are always retrieved from Sesame store
		Concept c = manager.find(Concept.class, qname);
		
		if (c != null)
			hc = new HiveConcept(c);
		
		return hc;
	}

	@Override
	public void importConcepts(String path, String format) throws Exception 
	{
		importConcepts(path, true, true, true, true, true, format);
	}
	/**
	 * Imports all concepts from the specified RDF/XML formatted file. 
	 * 
	 * @param	path	Path to RDF/XML file.
	 */
	@Override
	public void importConcepts(String path, boolean doSesame, boolean doLucene, 
			boolean doH2, boolean doKEAH2, boolean doAutocomplete, String format) throws Exception 
	{
		logger.info("importConcepts " + path);
		
		RDFFormat rdfformat = RDFFormat.RDFXML;
		if (format.equals("rdfxml")) {
			rdfformat = RDFFormat.RDFXML;
		} else if (format.equals("n3")) {
			rdfformat = RDFFormat.N3;
		} else if (format.equals("ntriples")) {
			rdfformat = RDFFormat.NTRIPLES;
		} else if (format.equals("turtle")) {
			rdfformat = RDFFormat.TURTLE;
		}		
		
		// Import RDF/XML directly to Sesame
		if (doSesame)
		{
			logger.info("Importing " + path + " to Sesame store");
			manager.getConnection().add(new InputStreamReader(new FileInputStream(path), "UTF-8"), "", rdfformat);

			manager.flush();
			logger.info("Import to Sesame store complete");
		}
		else
			logger.info("Skipping Sesame import");
		
		// Import concepts from Sesame into the Lucene and H2 indexes
		if (doLucene)
			luceneIndex.startTransaction();
		if (doH2)
			h2Index.startTransaction();
		
		// For each Concept in Sesame, add to H2 and Lucene indexes
		if (doLucene)
			logger.info("Initializing Lucene index");
		else
			logger.info("Skipping Lucene initialization");
		
		if (doH2)
			logger.info("Initializing H2 index");
		else
			logger.info("Skipping H2 initialization");
		
		if (doLucene || doH2)
		{
	        for (Concept concept : this.manager.findAll(Concept.class)) {
	            
	        	if (concept.getSkosPrefLabel() != null)
	        	{
	        		try
	        		{
	        			HiveConcept hc = new HiveConcept(concept);
	        			
	            		// Add concept to Lucene
	        			if (doLucene)
	        				luceneIndex.addConcept(hc);
	            		
	            		// Add concept to H2 index
	        			if (doH2)
	        				h2Index.addConcept(hc);
	            		
	        		} catch (Exception e) {
	        			logger.error("Error adding " + concept.getQName());
	        			logger.error(e);
	        		}
	        		
	        	} else {
	        		logger.warn("Concept " + concept.getQName() + " missing prefLabel. Skipping.");
	        	}
	        }
		}
        
        if (doLucene)
        	luceneIndex.commit();
        if (doH2)
        	h2Index.commit();
        
        luceneIndex.close();
        
        if (doAutocomplete) 
        {
        	logger.info("Initializing autocomplete index");
      
        	autocomplete.reIndex(FSDirectory.getDirectory(luceneIndex.getPath(), null), "prefLabel");
        	
        	logger.info("Autocomplete index initialization complete");
        }
        else
        	logger.info("Skipping autocomplete initialization");
        
	}
	
	/**
	 * Imports a single SKOS concept from an RDF/XML formatted file.
	 * 
	 * @param qname	QName of concept to be imported
	 * @param path	Path to RDF/XML file
	 */
	@Override
	public void importConcept(QName qname, String path) throws Exception 
	{
		logger.debug("importConcept " + qname + ", " + path);
		

		// Read RDF/XML file into in-memory Sesame Store
		logger.debug("Reading concept from file " + path);
		MemoryStore tempStore = new MemoryStore();
		SailRepository tempRepository = new SailRepository(tempStore);
		tempRepository.initialize();            
        ElmoModule tempElmo = new ElmoModule();           
        SesameManagerFactory tempFactory = new SesameManagerFactory(tempElmo, tempRepository);         
        SesameManager tempManager = tempFactory.createElmoManager();
        if (path.startsWith("http"))
        	tempManager.getConnection().add(new InputStreamReader(new URL(path).openStream(), "UTF-8"), "", RDFFormat.RDFXML);
        else
        	tempManager.getConnection().add(new InputStreamReader(new FileInputStream(path), "UTF-8"), "", RDFFormat.RDFXML);
        tempManager.flush();
        
        // Get concept from temp store
        Concept c = tempManager.find(Concept.class, qname);
        if (c != null) 
        {
        	logger.debug("Adding concept to HIVE");
        	HiveConcept hc = new HiveConcept(c);
        	addConcept(hc);
        }
        else
        	logger.error("The specified concept was not found");
        
        tempManager.close();
	}
	
	/**
	 * Returns the total number of statements in the tripe store.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public long getNumStatements() {
		long count = 0;
		try 
		{
			String qs = "SELECT ?s ?p ?o  WHERE {?s ?p ?o .}";
			List<HashMap<String, Value>> rs = SPARQLSelect(qs);
			for (HashMap<String, Value> hm: rs)
				count ++;
		} catch (RepositoryException e) {
			logger.error(e);
		}
		return count;
	}
	
	
	/**
	 * Returns the total number of concepts via SPARQL
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public long getNumConceptsSPARQL() {
		long count = 0;
		try 
		{
			String qs = "SELECT ?s ?p ?o WHERE { ?s a <http://www.w3.org/2004/02/skos/core#Concept> }";
			List<HashMap<String, Value>> rs = SPARQLSelect(qs);
			for (HashMap<String, Value> hm: rs)
				count ++;
		} catch (RepositoryException e) {
			logger.error(e);
		}
		return count;
	}
	
	/**
	 * Returns the total number of SKOS broader statements in this vocabulary.
	 * @return
	 */
	@SuppressWarnings("unused")
	public long getNumBroader() {
		long count = 0;
		try {
			String qs = "SELECT ?s ?p ?o WHERE { ?s <http://www.w3.org/2004/02/skos/core#broader> ?o }";
			List<HashMap<String, Value>> rs = SPARQLSelect(qs);
			for ( HashMap<String, Value> hm: rs)
				count ++;
		} catch (RepositoryException e) {
			logger.error(e);
		}
		return count;
	}
	
	/**
	 * Returns the total number of SKOS narrower statements in this vocabulary.
	 * @return
	 */
	@SuppressWarnings("unused")
	public long getNumNarrower() {
		long count = 0;
		try {
			String qs = "SELECT ?s ?p ?o WHERE { ?s <http://www.w3.org/2004/02/skos/core#narrower> ?o }";
			List<HashMap<String, Value>> rs = SPARQLSelect(qs);
			for (HashMap<String, Value> hm: rs)
				count ++;
		} catch (RepositoryException e) {
			logger.error(e);
		}
		return count;
	}
	
	/**
	 * Returns the total number of SKOS related statements in this vocabulary.
	 * @return
	 */
	@SuppressWarnings("unused")
	public long getNumRelated() {
		long count = 0;
		try 
		{
			String qs = "SELECT ?s ?p ?o WHERE { ?s <http://www.w3.org/2004/02/skos/core#related> ?o }";
			List<HashMap<String, Value>> rs = SPARQLSelect(qs);
			for (HashMap<String, Value> hm: rs)
				count ++;
		} catch (RepositoryException e) {
			logger.error(e);
		}
		return count;
	}
	
	/**
	 * Returns the total number of top concepts in this vocabulary.
	 * @return
	 */
	public long getNumTopConcepts()
	{
		long topConcepts = 0;
		try {
			topConcepts = ((HiveH2IndexImpl)h2Index).getNumTopConcepts();
		} catch (Exception e) {
			logger.error(e);
		}
		return topConcepts;
	}
	
	/**
	 * Executes a SPARQL query and returns a list of maps of names and values.
	 * 
	 * @param qs	SPARQL query string
	 * @return
	 * @throws RepositoryException
	 */
	protected List<HashMap<String, Value>> SPARQLSelect(String qs) throws RepositoryException 
	{
			RepositoryConnection con = null;
			
			try 
			{
				con = repository.getConnection();
				TupleQuery query = con.prepareTupleQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
				TupleQueryResult rs = query.evaluate();
				List<HashMap<String, Value>> reslist = new ArrayList<HashMap<String, Value>>();
				while(rs.hasNext()) 
				{
					BindingSet b = rs.next();
					Set<String> names = b.getBindingNames();
					HashMap<String, Value> hm = new HashMap<String, Value>();
					for(String n : names) {
						hm.put(n, b.getValue(n));
					}
					reslist.add(hm);
				}
				return reslist;
			} catch (MalformedQueryException e) {
				logger.error(e);
			} catch (QueryEvaluationException e) {
				logger.error(e);
			}
			finally {
				con.close();
			}
			return null;
	}
	
	
	/**
	 * Returns a reference to the underlying H2 store
	 * @return
	 */
	public HiveIndex getH2Index() {
		return this.h2Index;
	}
	
	/**
	 * Returns a reference to the underlying Lucene Index
	 * @return
	 */
	public HiveIndex getLuceneIndex() {
		return this.luceneIndex;
	}
	
	/**
	 * 
	 */
    public void close() {
        try {
            this.manager.close();
            this.factory.close();
            this.repository.shutDown();
        } catch (RepositoryException e) {
            logger.error(e);
        }
    }
    
	/**
	 * Writes all of the statements in the triple store to stdout
	 */
	protected void dumpStatements() 
	{
		try 
		{
			String qs = "SELECT ?s ?p ?o  WHERE {?s ?p ?o .}";
			List<HashMap<String, Value>> rs = SPARQLSelect(qs);
			for (HashMap<String, Value> hm: rs)
			{
				Value s = hm.get("s");
				Value p = hm.get("p");
				Value o = hm.get("o");
				System.out.println(s.stringValue() + ", " + p.stringValue() + ", " + o.stringValue());
			}
		} catch (RepositoryException e) {
			logger.error(e);;
		}
	}

	@Override
	public List<HiveConcept> findConcepts(String pattern, boolean topOnly)
			throws Exception {
		List<HiveConcept> hcs = new ArrayList<HiveConcept>();
		try {
			hcs = ((HiveH2IndexImpl)h2Index).findConceptsByName(pattern, topOnly);
		} catch (Exception e) {
			logger.error(e);
		}
		return hcs;
	}

	@Override
	public Date getLastUpdateDate() throws Exception {
		return ((HiveH2IndexImpl)h2Index).getLastUpdate();
	}

	@Override
	public SesameManager getManager() throws Exception {
		return this.manager;
	}
	
	@Override
	public List<AutocompleteTerm> suggestTermsFor(String str, int numTerms) throws Exception {
		return autocomplete.suggestTermsFor(str, numTerms);
	}
	
	@Override
	public Map<String, Long> getStats() throws Exception {
		return((HiveH2IndexImpl)h2Index).getStats();
	}
	
	public HiveConcept findConceptByName(String name) throws Exception 
	{
		return((HiveH2IndexImpl)h2Index).findConceptByName(name);
	}
	
	public Map<String, QName> findAllConcepts(boolean topOnly) {
	
		Map<String, QName> concepts = new TreeMap<String, QName>();
		try
		{
			concepts = ((HiveH2IndexImpl)h2Index).findAllConcepts(topOnly); 
		} catch (SQLException e) {
			logger.error(e);
		}
		return concepts;
	}
}
