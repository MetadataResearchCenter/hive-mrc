package edu.unc.ils.mrc.hive.converter.itis.model;

import java.util.ArrayList;
import java.util.List;

public class Taxon {

	private String tsn;
	private String name;
	private String longName;
	private List<String> children;
	private String parent;
	private List<String> synonyms;
	private String taxonomic_rank;

	public Taxon(String tsn) {
		this.tsn = tsn;
		this.children = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public List<String> getChilds() {
		return children;
	}

	public void setChilds(List<String> children) {
		this.children = children;
	}
	
	public void addchild(String child) {
		this.children.add(child);
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}

	public String getTaxonomic_rank() {
		return taxonomic_rank;
	}

	public void setTaxonomic_rank(String taxonomicRank) {
		taxonomic_rank = taxonomicRank;
	}

	public String getTsn() {
		return tsn;
	}

	public String toSKOS() {
		StringBuffer buffer = new StringBuffer();
		buffer
				.append("<rdf:Description rdf:about=\"http://www.itis.gov/itis/");
		buffer.append(tsn);
		buffer.append("#concept\">\n");
		buffer
				.append("\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>\n");
		buffer
				.append("\t<skos:inScheme rdf:resource=\"http://www.itis.gov/itis#conceptScheme\"/>\n");

		if (this.children != null) {
			for (String c : this.children) {
				buffer
						.append("\t<skos:narrower rdf:resource=\"http://www.itis.gov/itis/");
				buffer.append(c);
				buffer.append("#concept\"/>\n");
			}
		}
		if (this.parent != null) {
			buffer
					.append("\t<skos:broader rdf:resource=\"http://www.itis.gov/itis/");
			buffer.append(this.parent);
			buffer.append("#concept\"/>\n");
		}
		buffer.append("\t<skos:prefLabel>");
		buffer.append(this.longName);
		buffer.append("</skos:prefLabel>\n");
		
		if (this.synonyms != null) {
			for (String alt : synonyms){
				buffer.append("\t<skos:altLabel>");
				buffer.append(alt);
				buffer.append("</skos:altLabel>\n");
			}
		}
		buffer.append("</rdf:Description>");

		return buffer.toString();
	}
}
