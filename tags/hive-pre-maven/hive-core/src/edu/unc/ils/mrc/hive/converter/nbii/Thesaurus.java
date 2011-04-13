package edu.unc.ils.mrc.hive.converter.nbii;

import java.util.Iterator;

public interface Thesaurus {
	
	public void addConcept(Concept concept);
	public Concept getConcept(String prefLabel);
	public int getSize();
	public Iterator<Concept> getIterator();
	public void printThesaurus(String file);//use only for debugging

}
