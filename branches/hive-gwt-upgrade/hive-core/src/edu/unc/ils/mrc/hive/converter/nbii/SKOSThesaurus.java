package edu.unc.ils.mrc.hive.converter.nbii;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

public class SKOSThesaurus implements Thesaurus {

	private HashMap<String, Concept> thesaurus;

	public SKOSThesaurus() {
		this.thesaurus = new HashMap<String, Concept>();
	}

	public Iterator<Concept> getIterator() {
		return this.thesaurus.values().iterator();
	}

	public void addConcept(Concept concept) {
		this.thesaurus.put(concept.getUri(), concept);
	}

	public Concept getConcept(String prefLabel) {
		return this.thesaurus.get(prefLabel);
	}

	public int getSize() {
		return this.thesaurus.size();
	}

	public void printThesaurus(String fileName) {

		try {
			Iterator<String> it = this.thesaurus.keySet().iterator();
			File file = new File(fileName);
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream pr = new PrintStream(fos);
			pr.println("<?xml version='1.0' encoding='UTF-8'?>");
			pr
					.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' "
							+ "xmlns:skos=\'http://www.w3.org/2004/02/skos/core#\' >");
			while (it.hasNext()) {
				String s = it.next();
				Concept c = this.thesaurus.get(s);
				pr.println("<rdf:Description rdf:about='" + c.getUri() + "'>");
				pr.println("<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>");
				pr.println("\t<skos:prefLabel>" + c.getPrefLabel()
						+ "</skos:prefLabel>");
				for (String altLabel : c.getAltLabel()) {
					pr.println("\t<skos:altLabel>" + altLabel
							+ "</skos:altLabel>");
				}
				pr.println("\t<skos:scopeNote>" + c.getScopeNote()
						+ "</skos:scopeNote>");
				for (String hiddenLabel : c.getHiddenLabel()) {
					pr.println("\t<skos:hiddenLabel>" + hiddenLabel
							+ "</skos:hiddenLabel>");
				}
				for(String broaderURI : c.getBroaderURI()) {
					pr.println("\t<skos:broader rdf:resource='" + broaderURI + "'/>");
				}
				for(String narrowerURI : c.getNarrowerURI()) {
					pr.println("\t<skos:narrower rdf:resource='" + narrowerURI + "'/>");
				}
				for(String relatedURI : c.getRelatedURI()) {
					pr.println("\t<skos:related rdf:resource='" + relatedURI + "'/>");
				}
				pr.println("</rdf:Description>");
			}
			pr.println("</rdf:RDF>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
