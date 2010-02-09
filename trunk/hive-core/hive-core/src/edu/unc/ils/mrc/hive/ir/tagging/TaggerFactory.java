package edu.unc.ils.mrc.hive.ir.tagging;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.lucene.search.ConceptMultiSearcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.Searcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.SearcherFactory;

public class TaggerFactory {
	
	
	public static final int DUMMYTAGGER = 1;
	public static final int KEATAGGER = 2;
	
	private static int tagger = 1;

	public static void selectTagger(int tagger) {
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
