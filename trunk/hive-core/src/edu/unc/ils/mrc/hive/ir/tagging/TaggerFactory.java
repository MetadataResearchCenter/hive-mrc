package edu.unc.ils.mrc.hive.ir.tagging;

import org.apache.log4j.Logger;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSTaggerImpl;
import edu.unc.ils.mrc.hive.ir.lucene.search.ConceptMultiSearcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.Searcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.SearcherFactory;

public class TaggerFactory {
	
	private static Logger log = Logger.getLogger(TaggerFactory.class);
	public static final String DUMMYTAGGER = "dummy";
	public static final String KEATAGGER = "KEA";
	
	private static String tagger = "dummy";

	public static void selectTagger(String tagger) {
		TaggerFactory.tagger = tagger;
	}

	public static Tagger getTagger(String dirName, String modelName, String stopwordsPath,
			SKOSScheme schema) {
		if (tagger == DUMMYTAGGER)
			return new DummyTagger(dirName, modelName, stopwordsPath, schema);
		else if(tagger == KEATAGGER) {
			return new KEATagger(dirName, modelName, stopwordsPath, schema);
		}
		else
			return null;

	}

}
