package maui.vocab;

import java.util.Vector;

import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;

public interface Vocabulary {
	
	public void setLanguage(String language);

	public void setEncoding(String encoding);

	public void setLowerCase(boolean toLowerCase);

	public void setReorder(boolean reorder);

	public void setStemmer(Stemmer stemmer);
	
	public void setDebug(boolean debugMode);

	/**
	 * Starts initialization of the vocabulary.
	 * @throws Exception 
	 *
	 */
	public void initialize() throws Exception;

	/**
	 * Set the stopwords class.
	 * @param stopwords 
	 */
	public void setStopwords(Stopwords stopwords);

	/**
	 * Returns the id of the given term
	 * @param phrase
	 * @return term id
	 */
	public String getID(String phrase);

	/**
	 * Returns the term for the given id
	 * @param term id
	 * @return phrase
	 */
	public String getTerm(String id);

	/**
	 * Checks whether a normalized phrase 
	 * is a valid vocabulary term.
	 * @param phrase
	 * @return true if phrase is in the vocabulary
	 */
	public boolean containsNormalizedEntry(String phrase);

	/**
	 * Returns true if a phrase has more than one senses
	 * @param phrase
	 * @return
	 */
	public boolean isAmbiguous(String phrase);

	/**
	 * Retrieves all possible descriptors for a given phrase
	 * @param phrase
	 * @return
	 */
	public Vector<String> getSenses(String phrase);

	/**
	 * Given id of a term returns the list with ids of terms related to this term.
	 * @param id
	 * @return a vector with ids related to the input id
	 */
	public Vector<String> getRelated(String id);

	/**
	 * Given an ID of a term gets the list of all IDs of terms
	 * that are semantically related to the given term
	 * with a specific relation
	 * @param id, relation
	 * @return a vector with ids related to the input id by a specified relation
	 */
	public Vector<String> getRelated(String id, String relation);

	/** 
	 * Generates the preudo phrase from a string.
	 * A pseudo phrase is a version of a phrase
	 * that only contains non-stopwords,
	 * which are stemmed and sorted into alphabetical order. 
	 */
	public String normalizePhrase(String phrase);

	/** 
	 * Generates the preudo phrase from a string.
	 * A pseudo phrase is a version of a phrase
	 * that only contains non-stopwords,
	 * which are stemmed and sorted into alphabetical order. 
	 */
	public String pseudoPhrase(String str);


}
