package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import edu.unc.ils.mrc.hive.api.SKOSConcept;

public class SKOSConceptImpl implements SKOSConcept {

	private QName qname;
	private String prefLabel;
	private TreeMap<String, QName> broaders;
	private TreeMap<String, QName> relateds;
	private TreeMap<String, QName> narrowers;
	private List<String> altLabels;
	private List<String> scopeNotes;
	private List<String> schemes;
	private double score;

	public SKOSConceptImpl(QName uri) {
		this.qname = uri;
		this.broaders = new TreeMap<String, QName>();
		this.narrowers = new TreeMap<String, QName>();
		this.relateds = new TreeMap<String, QName>();
		this.altLabels = new ArrayList<String>();
		this.schemes = new ArrayList<String>();
		this.scopeNotes = new ArrayList<String>();
	}
	
	public int getNumberOfChildren() {
		return this.narrowers.size();
	}

	@Override
	public List<String> getAltLabels() {
		return this.altLabels;
	}

	@Override
	public TreeMap<String, QName> getBroaders() {
		return this.broaders;
	}

	@Override
	public TreeMap<String, QName> getNarrowers() {
		return this.narrowers;
	}

	@Override
	public String getPrefLabel() {
		return this.prefLabel;
	}

	@Override
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	@Override
	public TreeMap<String, QName> getRelated() {
		return this.relateds;
	}

	@Override
	public List<String> getSchemes() {
		return this.schemes;
	}

	@Override
	public List<String> getScopeNote() {
		return this.scopeNotes;
	}

	@Override
	public QName getQName() {
		return this.qname;
	}

	@Override
	public void addAltLabel(String altLabel) {
		this.altLabels.add(altLabel);

	}

	@Override
	public void addBroader(String broader, QName uri) {
		this.broaders.put(broader, uri);
	}

	@Override
	public void addNarrower(String narrower, QName uri) {
		this.narrowers.put(narrower, uri);
	}

	@Override
	public void addRelated(String related, QName uri) {
		this.relateds.put(related, uri);
	}

	@Override
	public void addScheme(String scheme) {
		this.schemes.add(scheme);
	}

	@Override
	public void addScopeNote(String scopeNote) {
		this.scopeNotes.add(scopeNote);
	}

	public String getSKOSFormat(){
		
		String output = "<rdf:RDF>" + "\n";
		output = output + "\t<rdf:Description rdf:about=\"" + this.getQName().getNamespaceURI() + getQName().getLocalPart() + "\">\n";
		output = output + "\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>\n";
		output = output + "\t<skos:prefLabel>" + this.prefLabel + "</skos:prefLabel>" + "\n";
		for(String alt : this.altLabels) {
			output = output + "\t<skos:altLabel>" + alt + "</skos:altLabel>" + "\n";
		}
		for(String broader : this.broaders.keySet()){
			output = output + "\t<skos:broader rdf:resource=\"" + this.broaders.get(broader) + "/>" + "\n";
		}
		for(String narrower : this.narrowers.keySet()){
			output = output + "\t<skos:narrower rdf:resource=\"" + this.narrowers.get(narrower) + "/>" + "\n";
		}
		for(String related : this.relateds.keySet()){
			output = output + "\t<skos:related rdf:resource=\"" + this.relateds.get(related) + "/>" + "\n";
		}
		output = output + "\t<skos:inScheme rdf:resource=\"" + this.getQName().getNamespaceURI() +"\"/>\n";
		for(String scopeNote : this.scopeNotes){
			output = output + "\t<skos:scopeNote>" + scopeNote + "</skos:scopeNote>" + "\n";
		}
		output = output + "</rdf:RDF>" ;
		
		return output;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

}
