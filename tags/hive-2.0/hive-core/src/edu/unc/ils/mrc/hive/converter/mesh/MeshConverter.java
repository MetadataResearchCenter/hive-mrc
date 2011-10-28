package edu.unc.ils.mrc.hive.converter.mesh;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.unc.ils.mrc.hive.converter.mesh.handlers.Concept;
import edu.unc.ils.mrc.hive.converter.mesh.handlers.ConceptRelation;
import edu.unc.ils.mrc.hive.converter.mesh.handlers.Descriptor;
import edu.unc.ils.mrc.hive.converter.mesh.handlers.DescriptorHandler;
import edu.unc.ils.mrc.hive.converter.mesh.handlers.Term;


/**
 * Converts MeSH vocabulary from MeSH-XML to SKOS RDF/XML using a SAX-based 
 * XML parser.
 * 
 * The mapping of MeSH to SKOS is not entirely straightforward. MeSH includes
 * a collection of Descriptors, each with one or more Concepts. Each Concept 
 * may be preferred/non-preferred. Each Concept contains Terms, which can also
 * be preferred or non-preferred. Concepts within a Descriptor are related, 
 * but descriptors are also related.
 * 
 * Descriptor D1
 *    Concept (Preferred) C1
 *       Term T1
 *       Term T2
 *    Concept (Narrower) C2
 *       Term T3
 *       Term T4 
 *    ConceptRelation (C1, C2, Narrower)
 *    Tree Number XX.XX.XX
 *    Tree Number XX.XX.YY
 *    RelatedDescriptor (D1, D2)
 * 
 * For example:
 * <code>
 * Cardiomegaly [Descriptor]                                    D006332
 *    Cardiomegaly                      [Concept, Preferred]  M0009952
 *         Cardiomegaly                    [Term, Preferred]   T019185
 *         Enlarged Heart                  [Term]              T366111
 *         Heart Enlargement               [Term]              T019186
 *    Cardiac Hypertrophy               [Concept, Narrower]   M0453089
 *         Cardiac Hypertrophy             [Term, Preferred]   T019187
 *         Heart Hypertrophy               [Term]              T019188
 *
 *    C14.280.195     [TreeNumber]
 *    C23.300.775.250 [TreeNumber]
 *    
 *    C14.280.195, M0009952
 *    C14.280.195, M0453089
 *    C23.300.775.250, M0009952
 *    C23.300.775.250, M0453089
 * </code>
 *    
 */
public class MeshConverter extends DefaultHandler 
{
	private static final Log logger = LogFactory.getLog(MeshConverter.class);
	

	XMLReader parser = null;

	// Output RDF/XML file
	String skosFile = null;
	
	// Map of MeSH concepts
	Map<String, Concept> meshConcepts = new TreeMap<String, Concept>();
	
	// Map of MeSH tree numbers to associated concept IDs
	Map<String, List<String>> meshTree = new TreeMap<String, List<String>>();
	
	// Map of parent MeSH tree numbers to associated sibling descriptor IDs.
	Map<String, List<String>> parentTree = new TreeMap<String, List<String>>();
	
	// List of MeSH descriptors
	List<Descriptor> meshDescriptors = new ArrayList<Descriptor>();
	
	// Current handler
	DefaultHandler currentHandler = null;

	
	/**
	 * Construct an instance of the MeshTreeParser on the specified XMLReader
	 * @param parser XMLReader
	 */
	public MeshConverter(XMLReader parser, String skosFile) {
		this.parser = parser;
		this.skosFile = skosFile;
	}
	
	/**
	 * Basic element handler for DescriptorRecords
	 */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	if (qName.equals("DescriptorRecord"))
    	{
    		// Only support topical descriptors (DescriptorClass = 1) or geographic (DescriptorClass = 4)
    		String descriptorClass = attributes.getValue("DescriptorClass");
    		if (descriptorClass.equals("1") || descriptorClass.equals("4")) 
    		{
	    		DescriptorHandler handler = new DescriptorHandler(parser, this);
	    		currentHandler = handler;
	    		parser.setContentHandler(handler);
    		}
    	}
    }
    
    /**
     * Basic element handler for DescriptorRecords 
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
    	if (qName.equals("DescriptorRecord"))
    	{	
    		Descriptor descriptor = new Descriptor();
    		
    		// Get the tree numbers associated with this Descriptor
    		List<String> treeNumbers = ((DescriptorHandler)currentHandler).getTreeNumbers();
    		
    		// Get the list of concepts for this Descriptor
    		List<Concept> concepts = ((DescriptorHandler)currentHandler).getConcepts();
    		
    		// Get the list of related descriptors for this Descriptor
    		List<String> relatedDescriptors = ((DescriptorHandler)currentHandler).getRelatedDescriptors();
    		
    		// Get the current descriptor Id
    		String descriptorId = ((DescriptorHandler)currentHandler).getDescriptorId();

    		descriptor.setDescriptorId(descriptorId);
    		
    		// Set the related descriptors
    		descriptor.setRelatedDescriptors(relatedDescriptors);
    		
    		// Set the concepts for this descriptor
    		descriptor.setConcepts(concepts);
    		
    		for (String treeNum : treeNumbers) 
    		{	
    			// If the tree number contains a period ("."), this is a child descriptor.
    			// Get the parent key number and descriptor
    			List<String> parentDesc = null;
    			String parentKey = null;
    			if (treeNum.contains(".")) {
        			parentKey = treeNum.substring(0, treeNum.lastIndexOf("."));
        			parentDesc = parentTree.get(parentKey);    				
    			} else {
    				parentKey = "TOP";
    			}

    			// Get the list of descriptors associated with this tree number
    			List<String> treeDesc = meshTree.get(treeNum);
    			
				// Create a new descriptor list, if none exists
				if (treeDesc == null) 
					treeDesc = new ArrayList<String>();        			
				
				// Add this concept to the list associated with the current tree number
				treeDesc.add(descriptorId);
				meshTree.put(treeNum, treeDesc);
				
				// Add this descriptor to the list of siblings
				if (parentDesc == null) 
					parentDesc = new ArrayList<String>();
				parentDesc.add(descriptorId);	
				parentTree.put(parentKey, parentDesc);
				
				descriptor.addTreeNumber(treeNum);
    		}    
        	meshDescriptors.add(descriptor);
    	}
    }
    
    /**
     * Process all of the concepts and generate the SKOS output
     */
    public void endDocument()
    {
    	// List of SKOS Concepts 
    	Map<String, MeshConcept> skosConcepts = new HashMap<String, MeshConcept>();
    	
    	for (Descriptor descriptor : meshDescriptors) 
    	{
    		MeshConcept meshConcept = new MeshConcept();
    		meshConcept.setDescriptorId(descriptor.getDescriptorId());
    		
    		for (Concept c : descriptor.getConcepts()) 
    		{

    			// Get terms for the current concept
    			List<Term> terms = c.getTerms();
    			for (Term term : terms) {
    				if (c.isPreferred() && term.isPreferred()) {
    					meshConcept.setPreferredTerm(term.getTermValue());
    					meshConcept.setScopeNote(c.getScopeNote());
    				}
    				else 
    					meshConcept.addAltTerm(term.getTermValue());
    			}
    			
    			List<String> treeNums = descriptor.getTreeNumbers();
    			for (String treeNum: treeNums) 
    			{
        			// Given the tree number, get the broader terms (parent nodes)
        			List<String> broaderDescriptors = getBroader(treeNum);
        			if (broaderDescriptors != null) {
    	    			for (String broaderDescriptor: broaderDescriptors) {
    	    				meshConcept.addBroader(broaderDescriptor);
    	    			}
        			}
        			
        			// Given the tree number, get the narrower terms (child nodes)
        			List<String> narrowerDescriptors = getNarrower(treeNum);
        			for (String narrowerDescriptor: narrowerDescriptors) {
        				meshConcept.addNarrower(narrowerDescriptor);
        			}

        		
        			// For each related descriptor,
        			List<String> relatedDescriptors = descriptor.getRelatedDescriptors();
        			for (String relatedDescriptor : relatedDescriptors) {
        				meshConcept.addRelated(relatedDescriptor);
        			}

        			// Get the relations between concepts in the current descriptor
        			//List<ConceptRelation> conceptRelations = c.getRelations();
        			//for (ConceptRelation conceptRelation: conceptRelations) {
        			//	if (conceptRelation.getRelation().equals("NRW"))
        			//		meshConcept.addNarrower(conceptRelation.getConcept2());
        			//	else if (conceptRelation.getRelation().equals("BRD"))
        			//		meshConcept.addBroader(conceptRelation.getConcept2());   
        			//	else if (conceptRelation.getRelation().equals("REL"))
        			//		meshConcept.addRelated(conceptRelation.getConcept2());
        			//}
        		}
    			skosConcepts.put(descriptor.getDescriptorId(), meshConcept);
    			
    		}
    	}
    	
    	
    	try {
			writeSKOS(skosFile, skosConcepts);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
    }

    public List<String> getBroader(String key) {
    	List<String> broaders = new ArrayList<String>();
    	if (key.contains(".")) {
    		String parentKey = key.substring(0, key.lastIndexOf("."));
    		broaders = meshTree.get(parentKey);
    	}
    	
    	return broaders;

    }
   
    /**
     * Given a tree number, return a list of child descriptor IDs
     * @param treeNum Tree Number
     * @return
     */
    private List<String> getNarrower(String treeNum) {
    	
    	// Get all descriptors for which this is the parent key
    	List<String> narrowers = new ArrayList<String>();
    	List<String> descriptors = parentTree.get(treeNum);
    	if (descriptors != null) {
	    	for (String descriptorId: descriptors) {
	    		narrowers.add(descriptorId);
	    	}
    	}
    	return narrowers;
    }
    
    /**
     * Writes the specified MeshConcepts to SKOS RDF/XML file.
     * @param path Path to the file
     * @param meshConcepts List of concepts
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
	public void writeSKOS(String path, Map<String, MeshConcept> meshConcepts) 
		throws FileNotFoundException, UnsupportedEncodingException 
	{
		File out = new File(path);
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(out), "utf-8");
		PrintWriter pr = new PrintWriter(osw);
		pr.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pr.println("<rdf:RDF");
		pr.println("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
		pr.println("xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" >");
		for (MeshConcept concept : meshConcepts.values()) {
			pr.println("<rdf:Description rdf:about=\"http://www.nlm.nih.gov/mesh/" + concept.getDescriptorId() + "#concept\">");
			pr.println("\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>");
			pr.println("\t<skos:inScheme rdf:resource=\"http://www.nlm.nih.gov/mesh#conceptScheme\"/>");
			pr.println("\t<skos:prefLabel>" + htmlEncode(concept.getPreferredTerm()) + "</skos:prefLabel>");
			List<String> altLabels = concept.getAltTerms();
			if(altLabels.size()>0) {
				for(String alt : altLabels)
				pr.println("\t<skos:altLabel>" + htmlEncode(alt) + "</skos:altLabel>");
			}

			Set<String> broaders = concept.getBroader();
			for(String bro : broaders)
				pr.println("\t<skos:broader rdf:resource=\"http://www.nlm.nih.gov/mesh/" + htmlEncode(bro) + "#concept\"/>");
			
			Set<String> narrowers = concept.getNarrower();
				for(String narrow : narrowers)
				pr.println("\t<skos:narrower rdf:resource=\"http://www.nlm.nih.gov/mesh/" + htmlEncode(narrow) + "#concept\"/>");

			Set<String> relateds = concept.getRelated();
				for(String rel : relateds)
				pr.println("\t<skos:related rdf:resource=\"http://www.nlm.nih.gov/mesh/" + htmlEncode(rel) + "#concept\"/>");
				
			String scopeNote = concept.getScopeNote();
			if(scopeNote != null)
				pr.println("\t<skos:scopeNote>" + htmlEncode(scopeNote) + "</skos:scopeNote>");
			pr.println("</rdf:Description>");
			
		}
		pr.println("</rdf:RDF>");
		pr.close();
		
		logger.info(meshConcepts.size() + " concepts printed");
	}    
	
	
	/**
	 * HTML encode common entities
	 * @param input Unencoded string
	 * @return Encoded string
	 */
	private static String htmlEncode(String input) {
		String encoded = input.replaceAll("&", "&amp;");
		encoded = encoded.replaceAll("<", "&lt;");
		encoded = encoded.replaceAll(">", "&gt;");
		encoded = encoded.replaceAll("\"", "&quot;");
	    encoded = encoded.replaceAll("\n", "");
		return encoded;
	}
	
	public static void main (String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		String xmlFile = "/Users/cwillis/dev/hive/sources/mesh/desc2011.xml"; //args[0];   // /usr/local/hive/sources/mesh/desc2011.xml
		String skosFile = "/Users/cwillis/dev/hive/sources/mesh/mesh_06272011.rdf"; //args[1];  // /usr/local/hive/hive-data/mesh/mesh.rdf
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser saxParser = spf.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    
	    MeshConverter parser = new MeshConverter(xmlReader, skosFile);
	    
	    xmlReader.setContentHandler(parser);
	    
	    xmlReader.parse(new InputSource(xmlFile));
	}
}
