package edu.unc.ils.mrc.hive.converter.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MeshConcept {

	String conceptId = "";
	List<String> narrower = new ArrayList<String>();
	List<String> broader = new ArrayList<String>();
	Map<String, String> related = new HashMap<String, String>();
	String preferrerTerm = "";
	List<String> altTerms = new ArrayList<String>();
	String scopeNote = "";
	
	public void addNarrower(String conceptId) {
		narrower.add(conceptId);
	}
	
	public void addBroader(String conceptId) {
		broader.add(conceptId);
	}
	
	public void addRelated(String conceptId) {
		related.put(conceptId, "1");
	}
	
	public void addAltTerm(String term) {
		altTerms.add(term);
	}
	
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	public List<String> getNarrower() {
		Collections.sort(narrower);
		return narrower;
	}
	public void setNarrower(List<String> narrower) {
		this.narrower = narrower;
	}
	public List<String> getBroader() {
		return broader;
	}
	public void setBroader(List<String> broader) {
		Collections.sort(broader);
		this.broader = broader;
	}
	public Set<String> getRelated() {
		return related.keySet();
	}

	public String getPreferrerTerm() {
		return preferrerTerm;
	}
	public void setPreferrerTerm(String preferrerTerm) {
		this.preferrerTerm = preferrerTerm;
	}
	public List<String> getAltTerms() {
		return altTerms;
	}
	public void setAltTerms(List<String> altTerms) {
		this.altTerms = altTerms;
	}
	public String getScopeNote() {
		return scopeNote;
	}
	public void setScopeNote(String scopeNote) {
		this.scopeNote = scopeNote;
	}
	
	
}
