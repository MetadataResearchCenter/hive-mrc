package edu.unc.ils.mrc.hive.converter.ncbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class NCBIConverter {

	public static File nodesdmp = new File("C:\\testNCBI\\nodes.dmp");
	public static File namesdmp = new File("C:\\testNCBI\\names.dmp");
	public static BufferedReader brInput = null;
	public static BufferedReader br2Input = null;
	public static Map<Integer, List<Integer>> nodes = new HashMap<Integer, List<Integer>>();
	public static Map<Integer, List<Integer>> narrower = new HashMap<Integer, List<Integer>>();
	public static HashSet<Integer> selectedNodes = new HashSet<Integer>();
	public static String rdfFileName = "C:\\testNCBI\\ncbiViruses5.rdf";
	public static PrintWriter outputStream = null;
	public static int selectedDivision = 9;

	public static void main(String[] args) {
        int count = 0;
		try {
			brInput = new BufferedReader(new FileReader(nodesdmp));
			String line = brInput.readLine();
			String[] nodeFields;
			count = 0;
			while (line != null) {
				nodeFields = line.split("\\|");
				if (nodeFields.length >= 5) {
					int division = Integer.parseInt(nodeFields[4].trim()); // division
					if (division == selectedDivision) {
						int taxid = new Integer(Integer.parseInt(nodeFields[0].trim())); 
						selectedNodes.add(taxid);
						count++;
					}
				}
				line = brInput.readLine();
			}
			brInput.close();
		} catch (Exception e) {
			System.out.println("BufferedReader error opening file "
					+ nodesdmp.getName());
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println(count + " selected nodes.");
		String line = "";
		String[] nodeFields;
		count = 0;
		try {
			br2Input = new BufferedReader(new FileReader(nodesdmp));
			line = br2Input.readLine();
			//System.out.println(line);
			while (line != null) {
				nodeFields = line.split("\\|");
				if (nodeFields.length >= 5) {
					int taxid = new Integer(Integer.parseInt(nodeFields[0].trim())); 
					int parentTaxId = Integer.parseInt(nodeFields[1].trim()); 
					if (selectedNodes.contains(taxid)) {
						//System.out.println("selectedNode = " + taxid);
						List<Integer> list = new ArrayList<Integer>();
					    if (selectedNodes.contains(parentTaxId)) {
					        list.add(parentTaxId);
					        if (narrower.get(parentTaxId) == null) {
								List<Integer> ln = new ArrayList<Integer>();
								ln.add(taxid);
								narrower.put(parentTaxId, ln);
							} else {
								narrower.get(parentTaxId).add(taxid);
							}
					    }
					    else 
					    	list.add(-1);   //parentTaxId is not a virus (division)
					    nodes.put(taxid, list); // tax_id
						count++;
					}
				}
				line = br2Input.readLine();
			}
			// printMap();
			br2Input.close();
			System.out.println(count + " nodes entries.");
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}

		try {
			outputStream = new PrintWriter(rdfFileName);

		} catch (FileNotFoundException e) {
			System.out.println("Error opening file " + rdfFileName);
			System.exit(0);
		}

		outputStream
				.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
						+ "<rdf:RDF\n"
						+ "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
						+ "  xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" >");
		outputStream
				.println("<skos:ConceptScheme rdf:about=\"http://www.ncbi.nlm.nih.gov/\"></skos:ConceptScheme>");

		readNamesDmp();
		outputStream.println("</rdf:RDF>");
		outputStream.close();
	}

	public static void printMap() {
		/*
		 * for (Integer key : nodes.keySet()) { List<Integer> l =
		 * nodes.get(key); System.out.print(key + ":"); System.out.println(l); }
		 */
		for (Integer key : narrower.keySet()) {
			List<Integer> l = narrower.get(key);
			System.out.print(key + ":");
			System.out.println(l);
		}
	}

	public static void readNamesDmp() {
		try {
			brInput = new BufferedReader(new FileReader(namesdmp));
		} catch (FileNotFoundException e) {
			System.out.println("BufferedReader error opening file "
					+ namesdmp.getName());
			e.printStackTrace();
			System.exit(0);
		}
		String line = "";
		String nameClass = "";
		String[] nameFields;
		int count = 0;
		try {
			line = brInput.readLine();
			Integer prevTaxid = new Integer(-1);
			String namesList = "";
			String prefLabel = "";
			while (line != null) {
				nameFields = line.split("\\|");
				if (nameFields.length == 4) {
					Integer taxid = new Integer(Integer.parseInt(nameFields[0]
							.trim()));
						if (!taxid.equals(prevTaxid)) {
							if (!prefLabel.equals("") && selectedNodes.contains(prevTaxid)) {
								formatSKOSoutput(prevTaxid, prefLabel,
										namesList);
							}
							namesList = "";
							prefLabel = "";
						}
						nameClass = nameFields[3].trim();
						if (nameClass.contains("scientific name")) {
							prefLabel = nameFields[1].trim();
							prefLabel = prefLabel.replaceAll("<", "(");
							prefLabel = prefLabel.replaceAll(">", ")");
							prefLabel = prefLabel.replaceAll("&", "and");
						} else {
							if ((!nameClass.contains("authority"))
									&& (!nameClass.contains("blast name"))) {
								String aname = nameFields[1].trim();
								aname = aname.replaceAll("<", "(");
								aname = aname.replaceAll(">", ")");
								aname = aname.replaceAll("&", "and");
								if (aname.length() > 0)
									namesList = namesList + aname + ",";
								aname = nameFields[2].trim();
								aname = aname.replaceAll("<", "(");
								aname = aname.replaceAll(">", ")");
								aname = aname.replaceAll("&", "and");
								if (aname.length() > 0)
									namesList = namesList + aname + ",";
							}
						}
				//	}
					prevTaxid = taxid;
				}
				count++;
				line = brInput.readLine();
			}

			System.out.println("count=" + count);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
	}

	public static void formatSKOSoutput(Integer taxid, String pref, String alts) {
		StringBuffer rec = new StringBuffer();
		String tax_id = taxid.toString();
		rec.append("  <skos:Concept rdf:about=\"http://www.ncbi.nlm.nih.gov/Concept/"
				+ tax_id + "\">\n");
		rec.append("    <skos:prefLabel xml:lang=\"en\">" + pref
				+ "</skos:prefLabel>" + "\n");

		String[] altLabels = alts.split(",");
		if (altLabels.length > 0) {
			for (int i = 0; i < altLabels.length; i++) {
				if (altLabels[i].length() > 0)
					rec.append("    <skos:altLabel xml:lang=\"en\">"
							+ altLabels[i] + "</skos:altLabel>" + "\n");
			}
		}
		List<Integer> ln = nodes.get(taxid);
		if (ln != null) {
			try {
				int broader = ln.get(0); // .toString();
				if (selectedNodes.contains(broader)) {
					if (ln.get(0) < 2)
						System.out.println(taxid + "  " + pref + " broader is "
								+ ln.get(0));
					if (broader > 0)
						rec.append("    <skos:broader rdf:resource=\"http://www.ncbi.nlm.nih.gov/Concept/"
								+ broader + "\"/>\n");
				} else
					System.out.println(broader
							+ " is not a broader virus concept for " + taxid);
			} catch (IndexOutOfBoundsException e) {
				/* do nothing, just skip the broader statement */
			}
			if ((ln = narrower.get(taxid)) != null) {
				Iterator<Integer> itr = ln.iterator();
				while (itr.hasNext()) {
					int narrow = itr.next();  //.toString();
					if (selectedNodes.contains(narrow)) {
						if (narrow > 0)
							rec.append("    <skos:narrower rdf:resource=\"http://www.ncbi.nlm.nih.gov/Concept/"
									+ narrow + "\"/>\n");
					} else
						System.out
								.println(narrow
										+ " is not a narrow virus concept for "
										+ taxid);
				}
			}
			rec.append("  </skos:Concept>");
			outputStream.println(rec);
		}
	}
}
