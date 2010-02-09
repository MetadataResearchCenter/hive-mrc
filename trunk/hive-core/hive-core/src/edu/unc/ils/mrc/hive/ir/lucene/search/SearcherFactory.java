package edu.unc.ils.mrc.hive.ir.lucene.search;


public class SearcherFactory {

	public static final int BASICLUCENECONCEPTSEARCHER = 1;

	private static int searcher = 1;

	public static void selectSearcher(int searcher) {
		SearcherFactory.searcher = searcher;
	}

	public static Searcher getSearcher(String[] indexList) {
		if (searcher == BASICLUCENECONCEPTSEARCHER)
			return new ConceptMultiSearcher(indexList);
		else
			return null;

	}

}
