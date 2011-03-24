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
 * Handler for the SeeRelatedDescriptor element
 */
public class RelatedDescriptorHandler extends MeshHandler 
{
	private static final Log logger = LogFactory.getLog(RelatedDescriptorHandler.class);
	
	List<String> descriptorIds = new ArrayList<String>();

	DefaultHandler childHandler = null;
	
	
	public RelatedDescriptorHandler(XMLReader parser, DefaultHandler parent) {
		super(parser, parent);
	}	
	
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	logger.trace("startElement: " + uri + "," + localName + "," + qName + "," + attributes);
    	
    	if (qName.equals("DescriptorUI")) {
    		currentValue = "";
    	}
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {	    	
    	logger.trace("endElement: " + uri + "," + localName + "," + qName);
    	
    	if (qName.equals("DescriptorUI")) {
    		descriptorIds.add(currentValue);
    		currentValue = "";
    	}
    	else if (qName.equals("SeeRelatedList")) {
    		parser.setContentHandler(parent);
    	}

    }    
    
    /**
     * Returns a list of related descriptor IDs
     * @return
     */
    public List<String> getDescriptorIds() {
    	return descriptorIds;
    }
    
}
