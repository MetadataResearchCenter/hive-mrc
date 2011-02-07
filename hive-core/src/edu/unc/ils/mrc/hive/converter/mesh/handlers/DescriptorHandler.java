package edu.unc.ils.mrc.hive.converter.mesh.handlers;

import java.util.ArrayList;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Processes the DescriptorRecord element.
 */
public class DescriptorHandler extends MeshHandler 
{
	private static final Log logger = LogFactory.getLog(DescriptorHandler.class);
	
	Concept currentConcept = new Concept();
	String currentDescriptor = "";
	List<Concept> concepts = new ArrayList<Concept>();
	List<String> treeNumbers = new ArrayList<String>();
	List<String> relatedDescriptors = new ArrayList<String>();
	
	public DescriptorHandler(XMLReader parser, DefaultHandler parent) {
		super(parser, parent);
	}	
	
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	logger.trace("startElement: " + uri + "," + localName + "," + qName + "," + attributes);
    	
    	if (qName.equals("Concept")) {
    		currentConcept = new Concept();
        	String preferredConceptYN = attributes.getValue("PreferredConceptYN");
        	if (preferredConceptYN != null)
        		currentConcept.setPreferred(preferredConceptYN.equals("Y"));
    	}	
    	else if (qName.equals("ConceptUI")) {
    		currentValue = "";
    	}
    	else if (qName.equals("String")) {	
    		currentValue = "";
    	}      	
    	else if (qName.equals("ScopeNote")) {	
    		currentValue = "";
    	}    	
    	else if (qName.equals("ConceptRelationList")) {
    		ConceptRelationListHandler handler = new ConceptRelationListHandler(parser, this);
    		childHandler = handler;
    		parser.setContentHandler(handler);
    	}
    	else if (qName.equals("TermList")) {
    		TermListHandler handler = new TermListHandler(parser, this);
    		childHandler = handler;
    		parser.setContentHandler(handler);
    	}   
    	else if (qName.equals("SeeRelatedDescriptor")) {
    		RelatedDescriptorHandler handler = new RelatedDescriptorHandler(parser, this);
    		childHandler = handler;
    		parser.setContentHandler(handler);
    	}   
    	else if (qName.equals("TreeNumber")) {
    		currentValue = "";
    	}
    	else if (qName.equals("DescriptorUI")) {
    		currentValue = "";
    	}    	
    }

    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {	    	
    	logger.trace("endElement: " + uri + "," + localName + "," + qName);
    	
    	if (qName.equals("ConceptUI")) {
    		currentConcept.setConceptId(currentValue);
    		currentValue = "";
    	}
    	else if (qName.equals("String")) {	
    		currentConcept.setName(currentValue);
    		currentValue = "";
    	}    	    	
    	else if (qName.equals("ScopeNote")) {	
    		currentConcept.setScopeNote(currentValue);
    		currentValue = "";
    	}    	
    	else if (qName.equals("ConceptRelationList")) {
    		List<ConceptRelation> relations = ((ConceptRelationListHandler)childHandler).getRelations();
    		currentConcept.setRelations(relations);
    	}
    	else if (qName.equals("Concept")) {
    		List<Term> terms = ((TermListHandler)childHandler).getTerms();
    		currentConcept.setTerms(terms);
    		currentConcept.setDescriptorId(currentDescriptor);
    		concepts.add(currentConcept);
    	}
    	else if (qName.equals("ConceptList")) {
    		parser.setContentHandler(parent);
    	}    	
    	else if (qName.equals("TreeNumber")) {
    		if (childHandler != null) {
        		relatedDescriptors = ((RelatedDescriptorHandler)childHandler).getDescriptorIds();
    		}
    		treeNumbers.add(currentValue);
    		currentValue = "";
    	}
    	else if (qName.equals("DescriptorUI")) {
    		currentDescriptor = currentValue;
    		currentValue = "";
    	}    	
    }    
    
    
    /**
     * Returns a list of concepts associated with this descriptor
     * @return
     */
    public List<Concept> getConcepts() {
    	return concepts;
    }
    
    /**
     * Returns the list of tree numbers assigned to this descriptor
     * @return
     */
    public List<String> getTreeNumbers() {
    	return treeNumbers;
    }
    
    /**
     * Returns the list of related descriptors
     * @return
     */
    public List<String> getRelatedDescriptors() {
    	return relatedDescriptors;
    }
}
