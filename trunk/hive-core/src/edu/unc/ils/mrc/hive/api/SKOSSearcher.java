package edu.unc.ils.mrc.hive.api;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

public interface SKOSSearcher {
	
	public List<SKOSConcept> searchConceptByKeyword(String keyword);
	public SKOSConcept searchConceptByURI(String uri, String lp);
	public TreeMap<String,QName> searchChildrenByURI(String uri, String lp);
	public List<HashMap> SPARQLSelect(String query, String vocabulary);
	public void close();

}
