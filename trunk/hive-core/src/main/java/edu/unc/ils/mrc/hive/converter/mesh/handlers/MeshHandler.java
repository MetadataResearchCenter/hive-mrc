package edu.unc.ils.mrc.hive.converter.mesh.handlers;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Superclass for all MeSH element handlers
 */
public class MeshHandler extends DefaultHandler 
{
	XMLReader parser = null;
	DefaultHandler parent = null;
	DefaultHandler childHandler = null;
	
	String currentValue = "";
	
	public MeshHandler(XMLReader parser, DefaultHandler parent) {
		this.parser = parser;
		this.parent = parent;
	}
	
    public void characters(char[] ch, int start, int length) throws SAXException  {
    	currentValue += new String(ch, start, length);
    }
}
