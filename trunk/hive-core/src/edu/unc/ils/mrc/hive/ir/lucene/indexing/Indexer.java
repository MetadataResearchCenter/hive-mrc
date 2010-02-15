package edu.unc.ils.mrc.hive.ir.lucene.indexing;

import org.openrdf.concepts.skos.core.Concept;

public interface Indexer {
	
	public void indexConcept(Concept concept);
	public void close();

}
