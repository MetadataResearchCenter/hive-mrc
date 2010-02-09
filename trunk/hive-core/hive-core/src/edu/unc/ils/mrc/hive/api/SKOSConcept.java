package edu.unc.ils.mrc.hive.api;

import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

public interface SKOSConcept {
	
	public QName getQName();
	public String getPrefLabel();
	public List<String> getAltLabels();
	public TreeMap<String,QName> getBroaders();
	public TreeMap<String,QName> getRelated();
	public TreeMap<String,QName> getNarrowers();
	public List<String> getScopeNote();
	public List<String> getSchemes();
	public void setPrefLabel(String prefLabel);
	public void addBroader(String broader, QName uri);
	public void addRelated(String related, QName uri);
	public void addNarrower(String narrower, QName uri);
	public void addAltLabel(String altLabel);
	public void addScopeNote(String scopeNote);
	public void addScheme(String scheme);
	public String getSKOSFormat();
	public int getNumberOfChildren();
	public void setScore(double score);
	public double getScore();

}
