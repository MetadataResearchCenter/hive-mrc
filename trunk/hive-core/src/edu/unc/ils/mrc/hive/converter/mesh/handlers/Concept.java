package edu.unc.ils.mrc.hive.converter.mesh.handlers;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a concept as parsed from the MeSH Concept element
 */
public class Concept {

	String conceptId = null;
	String conceptName = null;
	String descriptorId = null;
	
	boolean isPreferred = false;
	String scopeNote = null;
	List<Term> terms = new ArrayList<Term>();
	List<ConceptRelation> relations = new ArrayList<ConceptRelation>();
	List<String> relatedDescriptors = new ArrayList<String>();
	
	public Concept() 
	{
	}
	
	public Concept(String conceptId, String conceptName, boolean isPreferred)
	{
		this.conceptId = conceptId;
		this.conceptName = conceptName;
		this.isPreferred = isPreferred;
	}
	
	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public String getName() {
		return conceptName;
	}

	public void setName(String conceptName) {
		this.conceptName = conceptName;
	}

	public boolean isPreferred() {
		return isPreferred;
	}

	public void setPreferred(boolean isPreferred) {
		this.isPreferred = isPreferred;
	}

	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}
	
	public List<Term> getTerms() {
		return terms;
	}

	public String getScopeNote() {
		return scopeNote;
	}

	public void setScopeNote(String scopeNote) {
		this.scopeNote = scopeNote;
	}

	public List<ConceptRelation> getRelations() {
		return relations;
	}

	public void setRelations(List<ConceptRelation> relations) {
		this.relations = relations;
	}

	public List<String> getRelatedDescriptors() {
		return relatedDescriptors;
	}

	public void setRelatedDescriptors(List<String> relatedDescriptors) {
		this.relatedDescriptors = relatedDescriptors;
	}

	public String getDescriptorId() {
		return descriptorId;
	}

	public void setDescriptorId(String descriptorId) {
		this.descriptorId = descriptorId;
	}
}
