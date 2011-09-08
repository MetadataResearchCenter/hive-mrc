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
 * Handler for the ConceptList element.
 */
public class ConceptListHandler extends MeshHandler 
{
	private static final Log logger = LogFactory.getLog(ConceptListHandler.class);
	
	List<Concept> concepts = new ArrayList<Concept>();
	
	public ConceptListHandler(XMLReader parser, DefaultHandler parent) {
		super(parser, parent);
	}	
	
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	logger.trace("startElement: " + uri + "," + localName + "," + qName + "," + attributes);
    	
    	if (qName.equals("Concept")) {
    		DescriptorHandler handler = new DescriptorHandler(parser, this);
    		childHandler = handler;
    		parser.setContentHandler(handler);
    	}
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
    	logger.trace("endElement: " + uri + "," + localName + "," + qName);
    	
    	if (qName.equals("Concept")) {
    		//Concept concept = ((ConceptHandler)childHandler).getConcept();
    		//concepts.add(concept);
    		//System.out.println("Concept name: " + concept.getName());
    	}
    	else if (qName.equals("ConceptList")) {
    		parser.setContentHandler(parent);
    	}   	
    }
    
    /**
     * Returns a list of Concepts contained within this ConceptList
     * @return
     */
    public List<Concept> getConcepts() {
    	return concepts;
    }
}
