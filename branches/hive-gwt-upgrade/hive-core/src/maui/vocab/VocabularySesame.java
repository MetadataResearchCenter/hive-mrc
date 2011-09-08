package maui.vocab;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;
import maui.vocab.store.VocabularyStore;

public class VocabularySesame implements Vocabulary, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Location of the rdf version of the controlled vocabulary it needs to be
	 * in the SKOS format!
	 */
	private static File SKOS;
	/**
	 * Location of the vocabulary's *.en file containing all terms of the
	 * vocabularies and their ids.
	 */
	private static File EN;
	/**
	 * Location of the vocabulary's *.use file containing ids of non-descriptor
	 * with the corresponding ids of descriptors.
	 */
	private static File USE;
	/**
	 * Location of the vocabulary's *.rel file containing semantically related
	 * terms for each descriptor in the vocabulary.
	 */
	private static File REL;

	private String vocabularyFormat;

	/** index : descriptor --> id */
	private HashMap<String, String> termIdIndex;

	/** reverse index : id --> descriptor */
	private HashMap<String, String> idTermIndex;

	/** normalized descriptor --> list of all possible meanings */
	private HashMap<String, Vector<String>> listsOfSenses;

	/** non-descriptor id --> descriptors id */
	private HashMap<String, String> nonDescriptorIndex = null;

	/** id --> list of related ids */
	private HashMap<String, Vector<String>> listsOfRelatedTerms = null;

	/** id-relatedId --> relation */
	private HashMap<String, String> relationIndex = null;

	/** Document language */
	private String language = "en";

	/** Document encoding */
	private String encoding = "UTF-8";

	/** Default stemmer to be used */
	private Stemmer stemmer;

	/** List of stopwords to be used */
	private Stopwords stopwords;

	/** Normalization to lower case - defaulte no */
	private boolean toLowerCase = true;

	/** Normalization via alphabetic reordering - default true */
	private boolean reorder = true;

	private boolean debugMode = false;

	public VocabularySesame(VocabularyStore store) throws Exception {

	}

	private void buildSKOS() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsNormalizedEntry(String phrase) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getID(String phrase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getRelated(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getRelated(String id, String relation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getSenses(String phrase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTerm(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAmbiguous(String phrase) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String normalizePhrase(String phrase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String pseudoPhrase(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDebug(boolean debugMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEncoding(String encoding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLanguage(String language) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLowerCase(boolean toLowerCase) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReorder(boolean reorder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStemmer(Stemmer stemmer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStopwords(Stopwords stopwords) {
		// TODO Auto-generated method stub

	}

}
