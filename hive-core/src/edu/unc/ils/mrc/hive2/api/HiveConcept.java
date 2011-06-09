package edu.unc.ils.mrc.hive2.api;

import java.util.ArrayList;

import java.util.List;
import java.util.Set;


import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.elmo.Entity;
import org.openrdf.concepts.skos.core.Concept;

/**
 * This class represents a HIVE Concept. HIVE concepts are simplified versions of SKOS concepts. 
 * 
 * @author cwillis
 *
 */
public class HiveConcept
{
	private static final Log logger = LogFactory.getLog(HiveConcept.class);
			 
	/* SKOS predicates */
	public static final String CONCEPT 	 = "http://www.w3.org/2004/02/skos/core#Concept";
	public static final String PREFLABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
	public static final String ALTLABEL  = "http://www.w3.org/2004/02/skos/core#altLabel";
	public static final String BROADER   = "http://www.w3.org/2004/02/skos/core#broader";
	public static final String NARROWER  = "http://www.w3.org/2004/02/skos/core#narrower";
	public static final String RELATED 	 = "http://www.w3.org/2004/02/skos/core#related";
	public static final String SCOPENOTE = "http://www.w3.org/2004/02/skos/core#scopeNote";

	/* Qname of concept */
	QName qname = null;
	
	/* Preferred label of concept */
	String prefLabel = null;
	
	/* Alternate labels for concept */
	List<String> altLabels = new ArrayList<String>();
	
	/* Broader concepts (represented by URIs) */
	List<String> broaderConcepts  = new ArrayList<String>();
	
	/* Narrower concepts (represented by URIs) */
	List<String> narrowerConcepts = new ArrayList<String>();
	
	/* Related concepts (represented by URIs) */
	List<String> relatedConcepts  = new ArrayList<String>();
	
	/* Scope notes */
	List<String> scopeNotes = new ArrayList<String>();
	
	/* Is this concept a top concept? */
	boolean isTopConcept = false;
	
	/* Is this concept a leaf node? */
	boolean isLeaf = false;

	// Default constructor
	public HiveConcept() 
	{		
	}
	
	/**
	 * Constructs a HiveConcept based on an Elmo concept.
	 * @param c
	 */
	public HiveConcept(Concept c) 
	{
		this.qname = c.getQName();
		this.prefLabel = c.getSkosPrefLabel();
		
		Set<Concept> broaders = c.getSkosBroaders();
		try
		{
			for (Entity b: broaders) {
				QName qname = b.getQName();
				String uri = qname.getNamespaceURI() + qname.getLocalPart();
				broaderConcepts.add(uri);
			}
		} catch (Exception e) {
			logger.warn(e);
		}
		
		try
		{
			Set<Concept> narrowers = c.getSkosNarrowers();
			for (Entity n: narrowers) {
				QName qname = n.getQName();
				String uri = qname.getNamespaceURI() + qname.getLocalPart();
				narrowerConcepts.add(uri);
			}
		} catch (Exception e) {
			logger.warn(e);
		}
		
		try
		{
			Set<Concept> relateds = c.getSkosRelated();
			for (Entity r: relateds) {
				QName qname = r.getQName();
				String uri = qname.getNamespaceURI() + qname.getLocalPart();
				relatedConcepts.add(uri);
			}
		} catch (Exception e) {
			logger.warn(e);
		}
		
		Set<String> alts = c.getSkosAltLabels();
		for (String a : alts) {
			altLabels.add(a);
		}
		
		Set<Object> notes = c.getSkosScopeNotes();
		for (Object n : notes) {
			scopeNotes.add((String)n);
		}
		
		// Other values available from the Elmo Concept implementation:
		/*
			c.getRdfsComment();
			c.getRdfsIsDefinedBy();
			c.getRdfsLabel();
			c.getRdfsMembers();
			c.getRdfsSeeAlso();
			c.getRdfTypes();
			c.getRdfValues();
			c.getSkosAltSymbols();
			c.getSkosChangeNotes();
			c.getSkosDefinitions();
			c.getSkosEditorialNotes();
			c.getSkosExamples();
			c.getSkosHiddenLabels();
			c.getSkosHistoryNotes();
			c.getSkosInSchemes();
			c.getSkosIsPrimarySubjectOfs();
			c.getSkosIsSubjectOfs();
			c.getSkosNotes();
			c.getSkosPrefSymbols();
			c.getSkosPrimarySubjects();
			c.getSkosScopeNotes();
			c.getSkosSemanticRelations();
			c.getSkosSubjectIndicators();
			c.getSkosSubjects();
			c.getSkosSymbols();
		*/
	}
	
	/**
	 * Returns the Qname for this concept
	 * @return
	 */
	public QName getQName() {
		return qname;
	}
	
	/**
	 * Sets the Qname for this concept
	 * @param qname
	 */
	public void setQName(QName qname) {
		this.qname = qname;
	}
	
	/**
	 * Returns the preferred label for this concept.
	 * @return
	 */
	public String getPrefLabel() {
		return prefLabel;
	}
	
	/**
	 * Sets the preferred label for this concept
	 * @param prefLabel
	 */
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}
	
	/**
	 * Returns the list of alternate labels for this concept
	 * @return
	 */
	public List<String> getAltLabels() {
		return altLabels;
	}
	
	/**
	 * Sets the alternate labels for this concept
	 * @param altLabels
	 */
	public void setAltLabels(List<String> altLabels) {
		this.altLabels = altLabels;
	}
	
	/**
	 * Returns the list of broader concept URIs
	 * @return
	 */
	public List<String> getBroaderConcepts() {
		return broaderConcepts;
	}
	
	/**
	 * Sets the broader concept URIs for this concept
	 * @param broaderConcepts
	 */
	public void setBroaderConcepts(List<String> broaderConcepts) {
		this.broaderConcepts = broaderConcepts;
	}
	
	/**
	 * Returns narrower concept URIs for this concept
	 * @return
	 */
	public List<String> getNarrowerConcepts() {
		return narrowerConcepts;
	}
	
	/**
	 * Returns the scope notes for this concept
	 * @return
	 */
	public List<String> getScopeNotes() {
		return scopeNotes;
	}
	
	/**
	 * Sets the narrower concept URIs for this concept
	 * @param narrowerConcepts
	 */
	public void setNarrowerConcepts(List<String> narrowerConcepts) {
		this.narrowerConcepts = narrowerConcepts;
	}
	
	/**
	 * Returns the related concept URIs for this concept
	 * @return
	 */
	public List<String> getRelatedConcepts() {
		return relatedConcepts;
	}
	
	/**
	 * Sets the related concept URIs for this concept
	 * @param relatedConcepts
	 */
	public void setRelatedConcepts(List<String> relatedConcepts) {
		this.relatedConcepts = relatedConcepts;
	}
	
	/**
	 * Sets the scope notes for this concept
	 * @param scopeNotes
	 */
	public void setScopeNotes(List<String> scopeNotes) {
		this.scopeNotes = scopeNotes;
	}
	
	/**
	 * Adds a broader concept URI
	 * @param uri
	 */
	public void addBroaderConcept(String uri) {
		this.broaderConcepts.add(uri);
	}
	
	/**
	 * Adds a narrower concept URI
	 * @param uri
	 */
	public void addNarrowerConcept(String uri) {
		this.narrowerConcepts.add(uri);
	}
	
	/**
	 * Adds a related concept URI
	 * @param uri
	 */
	public void addRelatedConcept(String uri) {
		this.relatedConcepts.add(uri);
	}
	
	/**
	 * Adds a scope note
	 * @param note
	 */
	public void addScopeNote(String note) {
		this.scopeNotes.add(note);
	}
	
	/**
	 * Returns true if this is a top concept
	 * @return
	 */
	public boolean isTopConcept() {
		return isTopConcept;
	}

	/**
	 * Sets whether this is a top concept;
	 * @param isTopConcept
	 */
	public void setTopConcept(boolean isTopConcept) {
		this.isTopConcept = isTopConcept;
	}

	/**
	 * Returns true if this is a leaf node.
	 * @return
	 */
	public boolean isLeaf() {
		return isLeaf;
	}

	/**
	 * Sets whether this is a leaf node.
	 * @param isLeaf
	 */
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
}
