package edu.unc.ils.mrc.hive.converter.nbii;

import java.io.FileNotFoundException;


import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/*
 * Problems to read repgen.xml. Not use this class for the moment!!!
 */
public class NBIIXMLReader extends DefaultHandler
{

	private XMLReader xr;
	private String currentElement;
	private Concept concept;
	private Thesaurus thesaurus;
	private boolean ok;

	public NBIIXMLReader() {
		try {
			this.xr = XMLReaderFactory.createXMLReader();
			this.xr.setContentHandler(this);
			this.xr.setErrorHandler(this);
		} catch (SAXException e) {
			System.err.println("Problem with XMLReader inicialization");
			e.printStackTrace();
		}

		this.thesaurus = new SKOSThesaurus();
		this.ok = false;
	}

	public Thesaurus readThesaurus(String file) {
		FileReader fr;
		try {
			fr = new FileReader(file);
			xr.parse(new InputSource(fr));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.thesaurus;
	}

	public Thesaurus readThesaurus(String[] file) {
		return null;
		// TODO
	}

	@Override
	public void startDocument() {
		System.out.println("Starting XML document");
	}

	@Override
	public void endDocument() {
		System.out.println("Finishing XML document");
	}

	@Override
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		this.currentElement = name;
		if (this.currentElement.equals("DESCRIPTOR")) {
			this.concept = new SKOSConcept("http://thesaurus.nbii.gov/");
			this.ok = true;
		}
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (this.currentElement.equals("UPD") && this.ok) {
			this.thesaurus.addConcept(this.concept);
			this.ok = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int end) throws SAXException {
		String s;
		s = new String(ch, start, end);
		s = s.trim();
		if (currentElement.equals("DESCRIPTOR") && !s.equals("")) {
			this.concept.setPrefLabel(s);
			this.concept.setUri(this.concept.getUri() + s);
			if (this.concept.getUri().contains(" ")) {
				this.concept.setUri(this.concept.getUri().replaceAll(" ", "-"));
			}
		}
		if (currentElement.equals("BT") && !s.equals("")) {
			this.concept.setBroader(s);
		}
		if (currentElement.equals("UF") && !s.equals("")) {
			this.concept.setAltLabel(s);
		}
		if (currentElement.equals("NT") && !s.equals("")) {
			this.concept.setNarrower(s);
		}
		if ((currentElement.equals("SN") || currentElement.equals("SC"))
				&& !s.equals("")) {
			this.concept.setScopeNote(s);
		}
		if (currentElement.equals("RT") && !s.equals("")) {
			this.concept.setRelated(s);
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException, SAXException {
		NBIIXMLReader lector = new NBIIXMLReader();
		// lector.leer("/home/jose/Desktop/qual2009.xml");
		lector.readThesaurus("/Users/cwillis/dev/hive/sources/nbii/repgen.xml");
		System.out.println("Thesaurus Size: " + lector.thesaurus.getSize());
		lector.thesaurus.printThesaurus("/tmp/nbii.rdf");

	}

}
