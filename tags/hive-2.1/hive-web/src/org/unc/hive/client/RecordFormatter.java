package org.unc.hive.client;

import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.unc.ils.mrc.hive.HiveException;

public class RecordFormatter {
	private static String skosRDFXML = "SKOS - RDF/XML";
	private static String skosNTriples = "SKOS - N Triples";
	private static String dublinCore = "Dublin Core";
	private static String modsXML = "MODS/XML";
	private static String marcXML = "MARC/XML";
	private static StringBuffer rec; 
	private static HashMap<String, HashMap<String,String>> props;
	
	private final ConceptBrowserServiceAsync conceptBrowserService = GWT.create(ConceptBrowserService.class);

    public void init() {
    	conceptBrowserService.getVocabularyProperties(
				new AsyncCallback<HashMap<String, HashMap<String,String>>>() {
				
				public void onFailure(Throwable caught) {
					Window.alert(caught.getMessage());
				}
				public void onSuccess(HashMap<String, HashMap<String,String>> result) {
				   props = result;
				}
				});	
    }
	
	public static String format(List<ConceptProxy> selectedConcepts, String recType) {
        rec = new StringBuffer();

		if (recType.equals(skosNTriples)) {
			return formatSKOSNTriples(selectedConcepts);
		} else if (recType.equals(dublinCore)) {
			return formatDublinCore(selectedConcepts);
		} else if (recType.equals(modsXML)) {
			return formatModsXML(selectedConcepts);
		} else if (recType.equals(marcXML)) {
			return formatMarcXML(selectedConcepts);
		} else
			return formatSKOSRDFXML(selectedConcepts);  //default

	}

	public static String formatSKOSRDFXML(List<ConceptProxy> selectedConcepts) {
		rec.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n");
		for (ConceptProxy cp : selectedConcepts) {
		    String skosRDFXML = cp.getSkosCode();
		    int startPos = skosRDFXML.indexOf("<rdf:Description");
		    int endPos = skosRDFXML.indexOf("</rdf:RDF>");
		    if (endPos > startPos) 
		    	skosRDFXML = "  " + skosRDFXML.substring(startPos,endPos);
			rec.append(skosRDFXML);
		}
		rec.append("</rdf:RDF>");
		return rec.toString();
	}

	public static String formatSKOSNTriples(List<ConceptProxy> selectedConcepts) {
		for (ConceptProxy cp : selectedConcepts) {
		    rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept>." + "\n");
		    if ((cp.getPreLabel() != null) && (cp.getPreLabel() != ""))
		        rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#prefLabel> " + "\"" + cp.getPreLabel() + "\"." + "\n");
		    
		    if ((cp.getAltLabel() != null) && (!cp.getAltLabel().isEmpty()))
		    	for (String label : cp.getAltLabel())  
		            rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#altLabel> " + "\"" + label + "\"." + "\n");
	       
		    if ((cp.getBroader() != null) && (!cp.getBroader().isEmpty())) {	
	        	for(String broaderConcept : cp.getBroader().keySet()){	
	        		String bc = cp.getBroader().get(broaderConcept);
	        		rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#broader> " + "<" + bc + ">." + "\n");
	        	}
	        }

	        if ((cp.getNarrower() != null) && (!cp.getNarrower().isEmpty())) {
	        	for(String narrowerConcept : cp.getNarrower().keySet()){	
	        		String nc = cp.getNarrower().get(narrowerConcept);
	        		rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#narrower> " + "<" + nc + ">." + "\n");
	        	}
	        }

	        if ((cp.getRelated() != null) && (!cp.getRelated().isEmpty())) {
	        	for(String relatedConcept : cp.getRelated().keySet()){	
	        		String rc = cp.getRelated().get(relatedConcept);
	        		rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#related> " + "<" + rc + ">." + "\n");
	        	}
	        } 

	        int pos = cp.getURI().indexOf("#");
	        String scheme = "";
	        if (pos >= 0)
	           scheme = cp.getURI().substring(0,pos+1);
	        if ((scheme != null) && (scheme != ""))
		        rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#inScheme> " + "<" + scheme + ">." + "\n");

	        if ((cp.getScopeNotes() != null) && (!cp.getScopeNotes().isEmpty()))    
	        	for (String note : cp.getScopeNotes())  
		            rec.append("<" + cp.getURI()  + ">" + " <http://www.w3.org/2004/02/skos/core#scopeNote> " + "\"" + note + "\"." + "\n");
		
		}
		return rec.toString();
	}

	public static String formatDublinCore(List<ConceptProxy> selectedConcepts) {
		rec.append("<?xml version=\"1.0\"?>\n\n" + "<metadata\n"
				+ "  xmlns=\"http://hive.nescent.org\"" + "\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ "\n" + "  xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
				+ "\n" + "  xmlns:dcterms=\"http://purl.org/dc/terms/\">"
				+ "\n\n");
		for (ConceptProxy cp : selectedConcepts) {
			String dc =  "  <dc:subject xsi:type=\"dcterms:URI\">" +  cp.getURI() + "</dc:subject>"  + "\n";
			rec.append(dc);
		}
		rec.append("\n</metadata>");
		return rec.toString();
	}

	public static String formatModsXML(List<ConceptProxy> selectedConcepts) {
		rec.append("<modsCollection xmlns:xlink=\"http://www.w3.org/1999/xlink\""
				+ "\n"
				+ "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ "\n"
				+ "   xmlns=\"http://www.loc.gov/mods/v3\""
				+ "\n"
				+ "   xsi:schemaLocation=\"http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd\">"
				+ "\n");
		rec.append("   <mods version=\"3.3\">" + "\n");
		for (ConceptProxy cp : selectedConcepts) {
			String authority = getAuthorityForVocabulary(cp.getOrigin().toLowerCase());
			rec.append("      <subject " + authority + ">" + "\n");
	        rec.append("         <topic" + " valueURI=\"" + cp.getURI() + "\">" + cp.getPreLabel()  + "</topic>" + "\n");
	        rec.append("      </subject>" + "\n");
		}
		rec.append("   </mods>" + "\n" + "</modsCollection>");
		return rec.toString();
	}
	
	public static String getAuthorityForVocabulary(String vocName) {
		String authority = "";
		HashMap<String,String> vals;
		vals = props.get(vocName);
		if (vals != null) {
		    authority = vals.get("uri");
		}
		if ((authority.equals("")) || (authority == null))
		   authority = "authority=\"" + vocName + "\"";
		else
		   authority = "authorityURI=\"" + authority + "\"";

		return authority;
	}

	public static String formatMarcXML(List<ConceptProxy> selectedConcepts) {
		String origin = "";
		String tag = "650";
		String ind2 = "";
		rec.append("<collection xmlns:xlink=\"http://www.w3.org/1999/xlink\""
				+ "\n"
				+ "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ "\n"
				+ "   xsi:schemaLocation=\"http://www.loc.gov/standards/marcxml/schema/xml.xsd\">"
				+ "\n");
		rec.append("   <record>" + "\n");
		for (ConceptProxy cp : selectedConcepts) {
			origin = cp.getOrigin().toLowerCase();
			tag = "";
			if (origin.equalsIgnoreCase("tgn"))
				tag = "651";
			else
				tag = "650";
			if (origin.equalsIgnoreCase("lcsh"))
				ind2 = "0";
			else if (origin.equalsIgnoreCase("lcshac"))
				ind2 = "1";
			else if (origin.equalsIgnoreCase("mesh"))
				ind2 = "2";
			else if (origin.equalsIgnoreCase("nal"))
				ind2 = "3";
			else if (origin.equalsIgnoreCase("cash"))
				ind2 = "5";
			else if (origin.equalsIgnoreCase("rvm"))
				ind2 = "6";
			else
				ind2 = "7";
			rec.append("      <datafield tag=\"" + tag + "\" ind1=\" \" ind2=\"" + ind2 + "\">" + "\n");
	        rec.append("         <subfield code=\"a\">" + cp.getPreLabel() + ".</subfield>" + "\n");
	        if (ind2.equals("7"))
	            rec.append("         <subfield code=\"2\">" + origin + "</subfield>" + "\n");
	        rec.append("      </datafield>" + "\n");
		}
		rec.append("   </record>" + "\n" + "</collection>");
		return rec.toString();
	}
}
