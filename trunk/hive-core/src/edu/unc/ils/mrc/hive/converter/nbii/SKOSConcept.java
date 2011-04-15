package edu.unc.ils.mrc.hive.converter.nbii;

import java.util.ArrayList;

import java.util.List;

public class SKOSConcept implements Concept{
	
	private String uri;
	private String prefLabel;
	private String scopeNote;
	private List<String> altLabel;
	private List<String> hiddenLabel;
	private List<String> narrower;
	private List<String> narrowerURI;
	private List<String> broader;
	private List<String> broaderURI;
	private List<String> related;
	private List<String> realtedURI;
	
	public SKOSConcept(String uri) {
		this.uri = uri;
		this.narrower = new ArrayList<String>();
		this.narrowerURI = new ArrayList<String>();
		this.broader = new ArrayList<String>();
		this.broaderURI = new ArrayList<String>();
		this.altLabel = new ArrayList<String>();
		this.hiddenLabel = new ArrayList<String>();
		this.related = new ArrayList<String>();
		this.realtedURI = new ArrayList<String>();
		this.scopeNote  = "";
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return this.uri;
	}

	public String getPrefLabel() {
		return this.prefLabel;
	}

	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	public String getScopeNote() {
		return this.scopeNote;
	}

	public void setScopeNote(String scopeNote) {
		this.scopeNote = this.scopeNote.concat(" " + scopeNote);
	}

	public List<String> getAltLabel() {
		return this.altLabel;
	}

	public void setAltLabel(String altLabel) {
		this.altLabel.add(altLabel);
	}
	
	public List<String> getHiddenLabel() {
		return this.hiddenLabel;
	}

	public void setHiddenLabel(String hiddenLabel) {
		this.hiddenLabel.add(hiddenLabel);
	}

	public List<String> getNarrower() {
		return this.narrower;
	}
	
	public List<String> getNarrowerURI() {
		return this.narrowerURI;
	}

	public void setNarrower(String narrower) {
		this.narrower.add(narrower);
	}
	
	public void setNarrowerURI(String narrowerURI) {
		this.narrowerURI.add(narrowerURI);
	}

	public List<String> getBroader() {
		return this.broader;
	}
	
	public List<String> getBroaderURI() {
		return this.broaderURI;
	}

	public void setBroader(String broader) {
		this.broader.add(broader);
	}
	
	public void setBroaderURI(String broaderURI) {
		this.broaderURI.add(broaderURI);
	}
	
	public List<String> getRelated() {
		return this.related;
	}
	
	public List<String> getRelatedURI() {
		return this.realtedURI;
	}

	public void setRelated(String related) {
		this.related.add(related);
	}
	
	public void setRelatedURI(String relatedURI) {
		this.realtedURI.add(relatedURI);
	}

}
