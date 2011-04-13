package edu.unc.ils.mrc.hive.converter.tgn;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TGNReader 
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		TGNThesaurus thesaurus = new TGNThesaurus();

		/*
		 * Pref Labels and Alternative Labels
		 */
		File file = new File("/home/hive/TGN/TERM.out");
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		while (line != null) {
			String[] elements = line.split("\t");
			String key = elements[10];
			if (elements[7].equals("P") && thesaurus.contains(key)) {
				TGNRecord record = thesaurus.getRecord(key);
				record.setPreferredTerm(elements[11]);
			} else if (elements[7].equals("V") && thesaurus.contains(key)) {
				TGNRecord record = thesaurus.getRecord(key);
				record.setAltTerm(elements[11]);
			} else if (elements[7].equals("P") && !thesaurus.contains(key)) {
				TGNRecord record = new TGNRecord(key);
				record.setPreferredTerm(elements[11]);
				thesaurus.setRecord(record);
			} else if (elements[7].equals("V") && !thesaurus.contains(key)) {
				TGNRecord record = new TGNRecord(key);
				record.setAltTerm(elements[11]);
				thesaurus.setRecord(record);
			}
			line = br.readLine();
		}
		
		br.close();
		isr.close();
		fis.close();

		/*
		 * Scope Notes
		 */
		 File fileS= new File("/home/hive/TGN/SUBJECT.out");
		 FileInputStream fisS = new FileInputStream(fileS);
		 InputStreamReader isrS = new InputStreamReader(fisS);
		 BufferedReader brS = new BufferedReader(isrS);
		 line = brS.readLine();
		 while (line != null) {
		 String[] elements = line.split("\t");
		 String key = elements[8];
		 TGNRecord r = thesaurus.getRecord(key);
		 r.setScopeNote(elements[5]);
		 line = brS.readLine();
		 }
		 
		 brS.close();
		 isrS.close();
		 fisS.close();

		/*
		 * Hierarchical Relationships
		 */
		 File fileH= new File("/home/hive/TGN/SUBJECT_RELS.out");
		 FileInputStream fisH = new FileInputStream(fileH);
		 InputStreamReader isrH = new InputStreamReader(fisH);
		 BufferedReader brH = new BufferedReader(isrH);
		 line = brH.readLine();
		 while (line != null) {
		 String[] elements = line.split("\t");
		 String parentKey = elements[6];
		 String childKey = elements[7];
		 TGNRecord parent = thesaurus.getRecord(parentKey);
		 TGNRecord child = thesaurus.getRecord(childKey);
		 parent.setNarrowerTerms(childKey);
		 child.setBroderTerms(parentKey);
		 line = brH.readLine();
		 }
		 
		brH.close();
		isrH.close();
		fisH.close();

		 /*
		  * Related Terms
		  */
		File fileR = new File("/home/hive/TGN/ASSOCIATIVE_RELS.out");
		FileInputStream fisR = new FileInputStream(fileR);
		InputStreamReader isrR = new InputStreamReader(fisR);
		BufferedReader brR = new BufferedReader(isrR);
		line = brR.readLine();
		while (line != null) {
			String[] elements = line.split("\t");
			String aKey = elements[5];
			String bKey = elements[6];
			TGNRecord record = thesaurus.getRecord(aKey);
			record.setRelatedTerms(bKey);
			line = brR.readLine();
		}
		
		brR.close();
		isrR.close();
		fisR.close();
		
		/*
		 * Print in standard output
		 */

//		for (String s : thesaurus.getKeySet()) {
//			TGNRecord re = thesaurus.getRecord(s);
//			if (re.getRelatedTerms().size() > 0) {
//				System.out.println("URI: " + re.getUri());
//				System.out.println("PREFERRED TERM: " + re.getPreferredTerm());
//				System.out.println("\t ALTERNATIVE TERM: "
//						+ re.getAltTerm().toString());
//				System.out.println("\t BROADER TERMS: "
//						+ re.getBroderTerms().toString());
//				System.out.println("\t NARROWER TERMS: "
//						+ re.getNarrowerTerms().toString());
//				System.out.println("\t RELATED TERMS: "
//						+ re.getRelatedTerms().toString());
//				System.out.println("\t SCOPE NOTE: " + re.getScopeNote());
//			}
//		}

		thesaurus.printSKOSThesaurus("/home/hive/tgn.rdf");

	}

}
