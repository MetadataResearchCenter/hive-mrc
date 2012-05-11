package edu.unc.ils.mrc.hive.converter.mesh.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Handler for ConceptRelation element
 */
public class ConceptRelationHandler extends MeshHandler 
{
	private static final Log logger = LogFactory.getLog(ConceptRelationHandler.class);
	
	ConceptRelation conceptRelation = new ConceptRelation();
	
	String relation = "";
	
	public ConceptRelationHandler(XMLReader parser, DefaultHandler parent, String relation) {
		super(parser, parent);
		conceptRelation.setRelation(relation);
	}
	
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	logger.trace("startElement: " + uri + "," + localName + "," + qName + "," + attributes);
    	
    	if (qName.equals("Concept1UI")) {
    		currentValue = "";
    	}
    	else if (qName.equals("Concept2UI")) {
    		currentValue = "";  		
    	}
    }
    
    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
    	logger.trace("endElement: " + uri + "," + localName + "," + qName);
    	
    	if (qName.equals("Concept1UI")) {
    		conceptRelation.setConcept1(currentValue); 
    		currentValue = "";
    	}
    	else if (qName.equals("Concept2UI")) {	
    		conceptRelation.setConcept2(currentValue); 
    		currentValue = "";
    		parser.setContentHandler(parent);
    	}    	
    }    
    
    /**
     * Returns the parsed concept relation
     * @return
     */
    public ConceptRelation getRelation() {
    	return conceptRelation;
    }
}
