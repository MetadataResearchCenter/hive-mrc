package edu.unc.ils.mrc.hive.converter.itis;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Hashtable;

public class ITISThesaurus 
{
	private Hashtable<String, ITISRecord> records;

	public ITISThesaurus() {
		this.records = new Hashtable<String, ITISRecord>();
	}

	public void setRecord(ITISRecord record) {
		this.records.put(record.getTermID(), record);
	}

	public ITISRecord getRecord(String key) {
		return this.records.get(key);
	}

	public boolean contains(String key) {
		if (this.records.get(key) != null)
			return true;
		else
			return false;
	}

	public int size() {
		return this.records.size();
	}

	public void printSKOS(String path) throws FileNotFoundException {
		File out = new File(path);
		PrintWriter pr = new PrintWriter(out);

		pr.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pr.println("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
		pr.println("xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" >");

		int n = 0;
		for (String s : this.records.keySet()) {
			ITISRecord re = this.records.get(s);
			n++;

			String termName = re.getTermName().replace(" ", "-");
			pr.println("<rdf:Description rdf:about=\"http://www.itis.gov/#"
					+ termName + "\">");
			pr
					.println("<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>");
			pr
					.println("<skos:inScheme rdf:resource=\"http://www.itis.gov/itis#conceptScheme\"/>");

			String broaderTerm = re.getBroaderTerm();
			if (broaderTerm.length() > 0) {
				pr.println("<skos:broader rdf:resource=\"http://www.itis.gov/#"
						+ broaderTerm + "\">");
			}

			String narrowerTerm = re.getNarrowerTerm();
			if (narrowerTerm.length() > 0) {
				pr
						.println("<skos:narrower rdf:resource=\"http://www.itis.gov/#"
								+ narrowerTerm + "\">");
			}
			pr.println("</rdf:Description>");
		}
		pr.println("</rdf:RDF>");
	}

}
