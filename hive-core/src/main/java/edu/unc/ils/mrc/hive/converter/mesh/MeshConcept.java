package edu.unc.ils.mrc.hive.converter.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MeshConcept {

	String descriptorId = "";
	Map<String, String> narrower = new TreeMap<String, String>();
	Map<String, String> broader = new TreeMap<String, String>();
	Map<String, String> related = new HashMap<String, String>();
	String preferredTerm = "";
	List<String> altTerms = new ArrayList<String>();
	String scopeNote = "";
	
	public void addNarrower(String descriptorId) {
		narrower.put(descriptorId, descriptorId);
	}
	
	public void addBroader(String descriptorId) {
		broader.put(descriptorId, descriptorId);
	}
	
	public void addRelated(String descriptorId) {
		related.put(descriptorId, "1");
	}
	
	public void addAltTerm(String term) {
		altTerms.add(term);
	}
	
	public String getDescriptorId() {
		return descriptorId;
	}
	public void setDescriptorId(String descriptorId) {
		this.descriptorId = descriptorId;
	}
	public Set<String> getNarrower() {
		//Collections.sort(narrower);
		return narrower.keySet();
	}

	public Set<String> getBroader() {
		return broader.keySet();
	}
	
	public Set<String> getRelated() {
		return related.keySet();
	}

	public String getPreferredTerm() {
		return preferredTerm;
	}
	public void setPreferredTerm(String preferredTerm) {
		this.preferredTerm = preferredTerm;
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
