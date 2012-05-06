package edu.unc.ils.mrc.hive.converter.embne;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;


public class MARCtoSKOSConverter {
	public static final boolean DEBUG = false;
	public static final String lang = "es";
	public static final String uri = "http://datos.bne.es/resource/";
	public  Record record = null;
    public  SKOSConcept skos = new SKOSConcept();
    public  Map<String, String> pref2lccn = new HashMap<String, String>();
    public  Map<String,Set<String>> broad2narrow = new HashMap<String, Set<String>>();
    public int noLCCNbroader = 0;
    public int noLCCNnarrower = 0;
    public int noLCCNrelated = 0;
    public PrintWriter nolccnout;
	
	public void readInputFile(String inputMARCfile, PrintWriter skosOutputStream, PrintWriter outputStream) {
		int count = 0;
		
		try { 
			nolccnout = new PrintWriter("C:\\hive\\HIVE-ES\\noLCCNs.txt");
		} catch (FileNotFoundException e) {
			System.out.println("Error opening file noLCCNs.txt");
			System.exit(0);
		}
		
		buildPref2lccn(inputMARCfile);
		
		try {
			InputStream in = new FileInputStream(inputMARCfile);
			MarcReader reader = new MarcStreamReader(in);
			while (reader.hasNext()) {
				count++;
				skos = new SKOSConcept();
				record = reader.next();
				List marcFields = record.getVariableFields();
				Set<String> tags = new TreeSet<String>();
				Iterator i = marcFields.iterator();
				while (i.hasNext()) {
			    	VariableField d = (VariableField)i.next();
				    String tag = d.getTag();
				    tags.add(tag);
				}
			    Iterator t = tags.iterator();
			    while (t.hasNext()) {
			    	String temp = (String)t.next();
			    	processTag(temp);
			    	//System.out.println(temp);
			    }
				writeSKOSConcept(skos, skosOutputStream);
				outputStream.print(record.toString());
			
			}
			if (DEBUG) System.out.println("Record count = " + count);
			in.close();
		} catch (Exception e) {
			System.out.println("Error reading input file");
		}
	}
	
	public void buildPref2lccn(String inputMARCfile) {
		try {
			InputStream in = new FileInputStream(inputMARCfile);
			MarcReader reader = new MarcStreamReader(in);
			skos = new SKOSConcept();  // reuse this instance for this processing
			while (reader.hasNext()) {
				record = reader.next();
				List marcFields = record.getVariableFields();
				Iterator i = marcFields.iterator();
			    while (i.hasNext()) {
			    	VariableField d = (VariableField)i.next();
				    String tag = d.getTag();
				    if (tag.equals("001") || tag.equals("150") || tag.equals("151"))
				       processTag(tag);
				}
				
				pref2lccn.put(skos.getPrefLabel(), skos.getDescriptorId());
			}
			in.close();
		} catch (Exception e) {
			System.out.println("Error reading input file");
		}
	}
	
	
	public void processTag(String tag) {
		String prefLabel = "";
		String altLabel = "";
		String relLabel = "";
		String term = "";
		String rbterm = "";
		DataField dfield;
		int tagno = Integer.parseInt(tag);
		boolean wfound = false;
		switch (tagno) {
		case 1:
        	ControlField cfield = (ControlField) record.getVariableField("001");
        	if (cfield != null) {
        		String lccn = cfield.getData();
        		lccn = lccn.replaceAll("[^A-Za-z0-9]", "");
        		skos.setDescriptorId(lccn);
        	}
            break;
        case 5:
            
            break;
        case 8:
            
            break;
        case 150:  // prefLabel, topical term
        	dfield = (DataField) record.getVariableField("150");
			if (dfield != null) {
				List subFields = dfield.getSubfields();
				Iterator i = subFields.iterator();
			    while (i.hasNext()) {
			    	Subfield s = (Subfield)i.next();
			    	if (prefLabel.equals(""))
			    		prefLabel = s.getData();
			    	else
				        prefLabel = prefLabel + "--" + s.getData();
				}
			    prefLabel = prefLabel.replaceAll("&","&amp;");
			    prefLabel = prefLabel.replaceAll("<duplicate_authority:U>", "");
			    String str = "$"; //"\u0024";
			    if (prefLabel.contains(str)) {
			        prefLabel = prefLabel.replace(str," "); //"\u0020");
			    }
			    skos.setPrefLabel(prefLabel.trim());
			    skos.setConceptScheme("topicalTerms");
			    if (DEBUG)  System.out.println(prefLabel);
			}
            break;
        case 151: //prefLabel, geographic term
        	dfield = (DataField) record.getVariableField("151");
			if (dfield != null) {
				List subFields = dfield.getSubfields();
				Iterator i = subFields.iterator();
			    while (i.hasNext()) {
			    	Subfield s = (Subfield)i.next();
			    	if (prefLabel.equals(""))
			    		prefLabel = s.getData();
			    	else
				        prefLabel = prefLabel + "--" + s.getData();
				}
			    prefLabel = prefLabel.replaceAll("&","&amp;");
			    prefLabel = prefLabel.replaceAll("<duplicate_authority:U>", "");
			    String str = "$"; //"\u0024";
			    if (prefLabel.contains(str)) {
			        prefLabel = prefLabel.replace(str," "); //"\u0020");
			    }
			    skos.setPrefLabel(prefLabel.trim());
			    skos.setConceptScheme("geographicNames");
			    if (DEBUG) System.out.println(prefLabel);
			}            
            break;
        case 450: //altLabel
        case 451:	
        	List varFields = record.getVariableFields(tag);
        	Iterator v = varFields.iterator();
        	while (v.hasNext()) {
        		altLabel = "";
        		dfield = (DataField) v.next();
        		List subFields = dfield.getSubfields();
				Iterator i = subFields.iterator();
			    while (i.hasNext()) {
			    	Subfield s = (Subfield)i.next();
			    	if (altLabel.equals(""))
			    		altLabel = s.getData();
			    	else
				        altLabel = altLabel + "--" + s.getData();
				}
			    altLabel = altLabel.replaceAll("&","&amp;");
			    skos.addAltLabel(altLabel);
        	}
            break;            
        case 550: // related and broader terms
        case 551:
        	List vFields = record.getVariableFields(tag);
        	Iterator vi = vFields.iterator();
        	while (vi.hasNext()) {
        		wfound = false;
        		rbterm = "";
        		dfield = (DataField) vi.next();
        		List subFields = dfield.getSubfields();
				Iterator i = subFields.iterator();
			    while (i.hasNext()) {
			    	Subfield s = (Subfield)i.next();
			    	if (s.getCode() == 'w') wfound = true;
			    	if (rbterm.equals(""))
			    		rbterm = s.getData();
			    	else
				        rbterm = rbterm + "--" + s.getData();
				}

		    	    String lccn = "";
		          	if (rbterm.length() > 0) {
		          		if (DEBUG) System.out.println(rbterm);
		       		    if ((!rbterm.substring(0,1).equals("g")) && (!rbterm.substring(0,1).equals("h"))) {
		       			   lccn = pref2lccn.get(rbterm);
		    	     	   if (lccn != null) 
		    		        	skos.addRelated(lccn);
		    	     	   else {
		    	     	    	nolccnout.println("No LCCN for related:  " + rbterm);
		    	     	    	noLCCNrelated++;
		    	     	    }
		       	        }
		       		    else if (wfound && rbterm.substring(0,1).equals("g")) {   //$wg broader
		       			    rbterm = rbterm.substring(3);
		       			    lccn = pref2lccn.get(rbterm);
		    	     	    if (lccn != null) {
		    		    	    skos.addBroader(lccn);
		    	     	    }
		    	     	    else {
		    	     	    	nolccnout.println("No LCCN for broader:  " + rbterm);
		    	     	    	noLCCNbroader++;
		    	     	    }
		       		    }
		       		 else if (wfound && rbterm.substring(0,1).equals("h")) {   //$wh narrower
		       			    rbterm = rbterm.substring(3);
		       			    lccn = pref2lccn.get(rbterm);
		    	     	    if (lccn != null) 
		    		    	    skos.addNarrower(lccn);
		    	     	    else {
		    	     	    	nolccnout.println("No LCCN for narrower: " + rbterm);
		    	     	        noLCCNnarrower++;
	    	     	        }
		       		    }
		       	    }
        	}  
            break;
        case 680:	 // scopeNote
        	String scopeNote = "";
        	List nields = record.getVariableFields(tag);
        	Iterator n = nields.iterator();
        	while (n.hasNext()) {
        		altLabel = "";
        		dfield = (DataField) n.next();
        		List subFields = dfield.getSubfields();
				Iterator i = subFields.iterator();
			    while (i.hasNext()) {
			    	Subfield s = (Subfield)i.next();
			    	if (scopeNote.equals(""))
			    		scopeNote = s.getData();
			    	else
				        scopeNote = scopeNote + " " + s.getData();
				}
			    scopeNote = scopeNote.replaceAll("&","&amp;");
			    skos.setScopeNote(scopeNote);
        	}
            break;                
        default: 
            
            break;
		}
	}
	
	public void writeSKOSConcept(SKOSConcept skos, PrintWriter outputStream) {
		outputStream.println("    <rdf:Description rdf:about=\"" + uri + skos.getDescriptorId() + "#concept\">");
		outputStream.println("        <skos:prefLabel xml:lang=\"" + lang + "\">" + skos.getPrefLabel() + "</skos:prefLabel>" );
		if (!skos.getAltLabels().isEmpty()){
			Iterator a = skos.getAltLabels().iterator();
			while(a.hasNext()){
				outputStream.println("        <skos:altLabel xml:lang=\"" + lang + "\">" + a.next() + "</skos:altLabel>");
			}	
		}
		if (!skos.getBroader().isEmpty()){
			Iterator b = skos.getBroader().iterator();
			while(b.hasNext()){
				outputStream.println("        <skos:broader rdf:resource=\"" + uri +  b.next() + "#concept\"/>");
			}	
		}
		if (!skos.getNarrower().isEmpty()){
			Iterator b = skos.getNarrower().iterator();
			while(b.hasNext()){
				outputStream.println("        <skos:narrower rdf:resource=\"" + uri + b.next() + "#concept\"/>");
			}	
		}
		outputStream.println("        <skos:inScheme rdf:resource=\"" + uri + "#conceptScheme\"/>");
		outputStream.println("        <skos:inScheme rdf:resource=\"" + uri + "#" + skos.getConceptScheme() + "\"/>");
		outputStream.println("        <rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>");
		if (!skos.getRelated().isEmpty()){
			Iterator r = skos.getRelated().iterator();
			while(r.hasNext()){
				outputStream.println("        <skos:related rdf:resource=\"" + uri + r.next() + "#concept\"/>");
			}	
		}
		if (skos.getScopeNote() != "")
		    outputStream.println("        <skos:scopeNote xml:lang=\"" + lang + "\">" + skos.getScopeNote() + "</skos:scopeNote>" );
		outputStream.println("    </rdf:Description>");
	}
	
	public void writeSKOSheader(PrintWriter outputStream) {
		outputStream
		.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<rdf:RDF\n"
				+ "        xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "        xmlns:dcterms=\"http://purl.org/dc/terms/\"\n"
				+ "        xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "        xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" >");
	}	
	
	public void writeSKOSConceptSchemes(PrintWriter outputStream) {
		outputStream
		.println("    <rdf:Description rdf:about=\"" + uri + "#conceptScheme\">\n"
        + "        <rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#ConceptScheme\"/>\n"
        + "        <rdfs:label>Autoridades esquema de conceptos</rdfs:label>\n"
        + "        <rdfs:comment>Se trata de un esquema de conceptos generales para todas las autoridades.</rdfs:comment>\n"
        + "    </rdf:Description>");
		outputStream
		.println("    <rdf:Description rdf:about=\"" + uri + "#topicalTerms\">\n"
        + "        <rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#ConceptScheme\"/>\n"
        + "        <rdfs:label>Condiciones t�pico esquema de conceptos.</rdfs:label>\n"
        + "        <rdfs:comment>Se trata de un esquema de conceptos t�picos de las autoridades a largo plazo.</rdfs:comment>\n"
        + "    </rdf:Description>"); 
		outputStream
		.println("    <rdf:Description rdf:about=\"" + uri + "#geographicNames\">\n"
        + "        <rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#ConceptScheme\"/>\n"
        + "        <rdfs:label>Nombres Geogr�ficos de esquema de conceptos</rdfs:label>\n"
        + "        <rdfs:comment>Se trata de un esquema de conceptos para las autoridades de nombres geogr�ficos.</rdfs:comment>\n"
        + "    </rdf:Description>"); 		
		
	}
	public void writeSKOSfooter(PrintWriter outputStream) {
		outputStream.println("</rdf:RDF>\n");
	}
}
