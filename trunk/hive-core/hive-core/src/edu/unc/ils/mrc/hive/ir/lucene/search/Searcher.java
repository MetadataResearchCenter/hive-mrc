package edu.unc.ils.mrc.hive.ir.lucene.search;

import java.util.List;

import org.openrdf.elmo.sesame.SesameManager;

import edu.unc.ils.mrc.hive.api.SKOSConcept;

public interface Searcher {
	
	public List<SKOSConcept> search(String word,SesameManager[] manager);
	public void close();

}
