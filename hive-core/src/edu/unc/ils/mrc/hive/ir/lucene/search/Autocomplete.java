package edu.unc.ils.mrc.hive.ir.lucene.search;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unc.ils.mrc.hive.ir.lucene.analysis.AutocompleteAnalyzer;


/**
 * Preliminary autocompete implementation based on
 * http://stackoverflow.com/questions/120180/how-to-do-query-auto-completion-suggestions-in-lucene
 * 
 * @author craig.willis@unc.edu
 */
public final class Autocomplete 
{

	/* Lucene index field name to store the grammed words */
    private static final String GRAMMED_WORDS_FIELD = "words";

    /* Lucene index field to store the full source word */
    private static final String SOURCE_WORD_FIELD = "sourceWord";

    /* Lucene index field to store the word count */
    private static final String COUNT_FIELD = "count";
    
    /* Lucene index field to store the sort order */
    private static final String SORT_FIELD = "sort";

    /* Lucene directory for autocomplete index */
    private final Directory autoCompleteDirectory;

    private IndexReader autoCompleteReader;

    private IndexSearcher autoCompleteSearcher;

    public Autocomplete(String autoCompleteDir) throws IOException {
    	this.autoCompleteDirectory = FSDirectory.getDirectory(autoCompleteDir,
    			null);

    	reOpenReader();
    }

    /**
     * Returns a list of suggested terms for the specified string
     * @param str	String to suggest terms for
     * @param numTerms	Number of terms to return
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public List<String> suggestTermsFor(String str, int numTerms) throws IOException, ParseException {
 
    	str = str.replaceAll(" ", "");
    	str = str.toLowerCase();

    	Query query = new TermQuery(new Term(GRAMMED_WORDS_FIELD, str));
    	Sort sort = new Sort(SORT_FIELD, false);
    	
    	TopDocs docs = autoCompleteSearcher.search(query, null, numTerms, sort);
    	List<String> suggestions = new ArrayList<String>();
    	for (ScoreDoc doc : docs.scoreDocs) {
    		suggestions.add(autoCompleteReader.document(doc.doc).get(
    				SOURCE_WORD_FIELD));
    	}
    	
    	return suggestions;
    }


    /**
     * Creates the autocomplete index from a source Lucene index.
     * @param sourceDirectory
     * @param fieldToAutocomplete
     * @throws CorruptIndexException
     * @throws IOException
     */
    public void reIndex(Directory sourceDirectory, String fieldToAutocomplete)
    		throws CorruptIndexException, IOException {

    	IndexReader sourceReader = IndexReader.open(sourceDirectory);

    	// use a custom analyzer so we can do EdgeNGramFiltering
    	IndexWriter writer = new IndexWriter(autoCompleteDirectory,
    			new AutocompleteAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);

    	writer.setMergeFactor(300);
    	writer.setMaxBufferedDocs(150);

    	Map<String, Integer> wordsMap = new TreeMap<String, Integer>();

    	for (int i = 0; i<sourceReader.numDocs(); i++)
    	{
    		Document d = sourceReader.document(i);
    		String[] values = d.getValues("prefLabel");
    		for (String value: values) 
    		{
    			if (!wordsMap.containsKey(value)) {
	    			// use the number of documents this word appears in
    				System.out.println("Adding: " + value);
	    			wordsMap.put(value, sourceReader.docFreq(new Term(
	    					fieldToAutocomplete, value)));
	    		}
    		}

    	} 
    	
    	int i = 0;
    	for (String word : wordsMap.keySet()) 
    	{
    		System.out.println("Indexing: " + word);
    		i++;
    		
    		// TODO: Need better way strip characters from terms.
    		String nospaces = word.replaceAll(" ", "");
    		nospaces = nospaces.replaceAll("\\(", "");
    		nospaces = nospaces.replaceAll("\\)", "");
    		nospaces = nospaces.replaceAll("\\.", "");
    		
    		// ok index the word
    		Document doc = new Document();
    		doc.add(new Field(SOURCE_WORD_FIELD, word, Field.Store.YES,
    				Field.Index.NOT_ANALYZED)); // orig term
    		doc.add(new Field(GRAMMED_WORDS_FIELD, nospaces, Field.Store.YES,
    				Field.Index.ANALYZED)); // grammed
    		doc.add(new Field(COUNT_FIELD,
    				Integer.toString(wordsMap.get(word)), Field.Store.NO,
    				Field.Index.NOT_ANALYZED)); // count
    		doc.add(new Field(SORT_FIELD,
    				Integer.toString(i), Field.Store.NO,
    				Field.Index.NOT_ANALYZED)); // count

    		writer.addDocument(doc);
    		
    	}
    	writer.commit();

    	sourceReader.close();

    	// close writer
    	writer.optimize();
    	writer.close();

    	// re-open our reader
    	reOpenReader();
    }

    private void reOpenReader() throws CorruptIndexException, IOException {
    	if (autoCompleteReader == null) {
    		autoCompleteReader = IndexReader.open(autoCompleteDirectory);
    	} else {
    		autoCompleteReader.reopen();
    	}

    	autoCompleteSearcher = new IndexSearcher(autoCompleteReader);
    }
}
