package edu.unc.ils.mrc.hive.converter.itis.model;

import java.util.Collection;
import java.util.HashMap;

public class Taxonomy {
	
	private HashMap<String, Taxon> taxonomy;

	public Taxonomy() {
		this.taxonomy = new HashMap<String, Taxon>();
	}
	
	public void putTaxon(String tsn, Taxon taxon) {
		this.taxonomy.put(tsn, taxon);
	}
	
	public Collection<Taxon> getTaxons() {
		return taxonomy.values();
	}
	
	public String getHeader() {
		StringBuffer buffer = new StringBuffer();
		buffer
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF \n \t xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n \t xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" >");
		return buffer.toString();	
	}
	
	public String getFooter() {
		return "</rdf:RDF>";
	}
	
}
