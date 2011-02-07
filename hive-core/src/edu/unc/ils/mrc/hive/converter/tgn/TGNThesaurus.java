package edu.unc.ils.mrc.hive.converter.tgn;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class TGNThesaurus
{
	
	private Hashtable<String, TGNRecord> records;
	
	public TGNThesaurus() {
		this.records = new Hashtable<String, TGNRecord>();
	}
	
	public boolean contains(String key) {
		if(this.records.get(key) != null)
			return true;
		else
			return false;
	}
	
	public TGNRecord getRecord(String key) {
		return this.records.get(key);
	}
	
	public void setRecord(TGNRecord record) {
		this.records.put(record.getSubjectID(), record);
	}
	
	public Set<String> getKeySet() {
		return this.records.keySet();
	}
	
	public void printSKOSThesaurus(String path) throws FileNotFoundException {
		File out = new File(path);
		PrintWriter pr = new PrintWriter(out);
		pr.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pr.println("<rdf:RDF");
		pr.println("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
		pr.println("xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" >");
		int n = 0;
		for (String s : this.records.keySet()) {
			TGNRecord re = this.records.get(s);
			n++;
			String uri = re.getUri();
			pr.println("<rdf:Description rdf:about=\"" + uri + "\">");
			pr.println("\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>");
			pr.println("\t<skos:inScheme rdf:resource=\"" + re.getURIBASE() + "\"/>");
			pr.println("\t<skos:prefLabel>" + re.getPreferredTerm() + "</skos:prefLabel>");
			List<String> altLabels = re.getAltTerm();
			if(altLabels.size()>0) {
				for(String alt : altLabels)
				pr.println("\t<skos:altLabel>" + alt + "</skos:altLabel>");
			}
			List<String> broaders = re.getBroderTerms();
			if(broaders.size()>0) {
				for(String bro : broaders)
				pr.println("\t<skos:broader rdf:resource=\"" + bro + "\"/>");
			}
			List<String> narrowers = re.getNarrowerTerms();
			if(narrowers.size()>0) {
				for(String narrow : narrowers)
				pr.println("\t<skos:narrower rdf:resource=\"" + narrow + "\"/>");
			}
			List<String> relateds = re.getRelatedTerms();
			if(relateds.size()>0) {
				for(String rel : relateds)
				pr.println("\t<skos:related rdf:resource=\"" + rel + "\"/>");
			}
			String scopeNote = re.getScopeNote();
			if(scopeNote.length()>0)
				pr.println("\t<skos:scopeNote>" + scopeNote + "</skos:scopeNote>");
			pr.println("</rdf:Description>");
			
		}
		pr.println("</rdf:RDF>");
		pr.close();
		System.out.println(n + " concepts printed");
	}

}
