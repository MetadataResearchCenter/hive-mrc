package maui.vocab;

import java.util.Vector;

import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;

public interface Vocabulary {

    public abstract void setLanguage(String language);

    public abstract void setEncoding(String encoding);

    public abstract void setLowerCase(boolean toLowerCase);

    public abstract void setReorder(boolean reorder);

    public abstract void setStemmer(Stemmer stemmer);

    public abstract void setDebug(boolean debugMode);

    /**
     * Starts initialization of the vocabulary.
     * @throws Exception 
     *
     */
    public abstract void initialize() throws Exception;

    /**
     * Set the stopwords class.
     * @param stopwords 
     */
    public abstract void setStopwords(Stopwords stopwords);

    /**
     * Returns the id of the given term
     * @param phrase
     * @return term id
     */
    public abstract String getID(String phrase);

    /**
     * Returns the term for the given id
     * @param id - id of some phrase in the vocabulary
     * @return phrase, i.e. the full form listed in the vocabulary
     */
    public abstract String getTerm(String id);

    /**
     * Retrieves all possible descriptors for a given phrase
     * @param phrase
     * @return a vector list of all senses of a given term
     */
    public abstract Vector<String> getSenses(String phrase);

    /**
     * Given id of a term returns the list with ids of terms related to this term.
     * @param id
     * @return a vector with ids related to the input id
     */
    public abstract Vector<String> getRelated(String id);

    /**
     * Given an ID of a term gets the list of all IDs of terms
     * that are semantically related to the given term
     * with a specific relation
     * @param id - id of some term in the vocabulary
     * @param relation - a given semantic relation
     * @return a vector with ids related to the input id by a specified relation
     */
    public abstract Vector<String> getRelated(String id, String relation);

}