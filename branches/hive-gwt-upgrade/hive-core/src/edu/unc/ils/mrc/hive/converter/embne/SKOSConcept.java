package edu.unc.ils.mrc.hive.converter.embne;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SKOSConcept {

	String descriptorId = "";
	Set<String> related = new HashSet<String>();
	Set<String> broader = new HashSet<String>();
	Set<String> narrower = new HashSet<String>();
	String conceptScheme = "";
	String prefLabel = "";
	Set<String> altLabels = new HashSet<String>();
	String scopeNote = "";
	
	public void addNarrower(String value) {
		narrower.add(value);
	}
	
	public void addBroader(String value) {
		broader.add(value);
	}
	
	public void addRelated(String value) {
		//related.put(descriptorId, "1");
		related.add(value);
	}
	
	public void addAltLabel(String term) {
		altLabels.add(term);
	}
	
	public String getDescriptorId() {
		return descriptorId;
	}
	public void setDescriptorId(String descriptorId) {
		this.descriptorId = descriptorId;
	}
	public Set<String> getNarrower() {
		return narrower;
	}

	public Set<String> getBroader() {
		return broader;
	}
	
	public Set<String> getRelated() {
		return related;
	}

	public String getPrefLabel() {
		return prefLabel;
	}
	
	public String getConceptScheme() {
		return conceptScheme;
	}
	
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}
	public void setConceptScheme(String scheme) {
		this.conceptScheme = scheme;
	}
	public Set<String> getAltLabels() {
		return altLabels;
	}

	public String getScopeNote() {
		return scopeNote;
	}
	public void setScopeNote(String scopeNote) {
		this.scopeNote = scopeNote;
	}
	
	
}
