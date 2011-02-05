package kea.vocab;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import kea.stemmers.Stemmer;
import kea.stopwords.Stopwords;

public abstract class Vocabulary implements Serializable 
{
	private static final long serialVersionUID = -7157202718619833534L;

	/** The document language */
	private String m_language;
	
	/** The default stemmer to be used */
	private Stemmer m_Stemmer;
	
	/** The list of stop words to be used */
	private Stopwords m_Stopwords;
	
	public Vocabulary(String documentLanguage) 
	{
		m_language = documentLanguage;
	}
	
	/**
	 * Initializes the vocabulary.
	 */
	public abstract void initialize();
	
	/**
	 * Builds the vocabulary index from SKOS RDF/XML files.
	 * @throws Exception
	 */
	public abstract void buildSKOS() throws Exception;

	/**
	 * Builds the vocabulary index from text files.
	 */
	public abstract void build() throws Exception;
	
	/**
	 * Builds the vocabulary index with descriptors/non-descriptors relations.
	 */
	public abstract void buildUSE() throws Exception;
	
	
	/**
	 * Builds the vocabulary index with semantically related terms.
	 */
	public abstract void buildREL() throws Exception;
	
	/**
	 * Builds the vocabulary index with semantically related terms.
	 * @throws Exception
	 */
	public abstract void buildRT() throws Exception;
	

	/**
	 * Checks whether a normalized version of a phrase (pseudo phrase)
	 * is a valid vocabulary term.
	 * 
	 * @param phrase
	 * @return true if phrase is in the vocabulary
	 */
	public abstract boolean containsEntry(String phrase);
	
	/**
	 * Given a phrase returns its id in the vocabulary.
	 * @param phrase
	 * @return id of the phrase in the vocabulary index
	 */
	public abstract String getID(String phrase);
	
	/**
	 * Given id, gets the original version of vocabulary term.
	 * @param id
	 * @return original version of the vocabulary term
	 */
	public abstract String getOrig(String id);
	
	/**
	 * Given id of the non-descriptor returs the id of the corresponding descriptor
	 * @param id of the non-descriptor
	 * @return id of the descriptor
	 */
	public abstract String getDescriptor(String id);
	
	/**
	 * Given id of a term returns the list with ids of terms related to this term.
	 * @param id
	 * @return a vector with ids related to the input id
	 */
	public abstract List<String> getRelated(String id);
	
	
	/**
	 * Given an ID of a term gets the list of all IDs of terms
	 * that are semantically related to the given term
	 * with a specific relation
	 * @param id, relation
	 * @return a vector with ids related to the input id by a specified relation
	 */
	public abstract List<String> getRelated (String id, String relation);
	
	public void setStemmer(Stemmer newStemmer) {	
		this.m_Stemmer = newStemmer;
	}
	
	public Stemmer getStemmer() {
		return m_Stemmer;
	}

	public void setStopwords(Stopwords newM_Stopwords) {	
		this.m_Stopwords = newM_Stopwords;
	}
	
	public String getLanguage() {
		return m_language;
	}
	
    public String remove (String[] words, int i) {

        String result = "";
        for (int j = 0; j < words.length; j++) {
            if ((j != i) && (!m_Stopwords.isStopword(words[j]))) {
               
                result = result + words[j];
               
                if ((j+1) != words.length) {
                    result = result + " ";
                }
            }
             
        }
        return result;
    }
	
	
	/** 
	 * Generates the preudo phrase from a string.
	 * A pseudo phrase is a version of a phrase
	 * that only contains non-stopwords,
	 * which are stemmed and sorted into alphabetical order. 
	 */
	public String pseudoPhrase(String str) {
		// System.err.print(str + "\t");
		String[] pseudophrase;
		String[] words;
		String str_nostop;
		String stemmed;
		
		
		str = str.toLowerCase();
		
		
		// This is often the case with Mesh Terms,
		// where a term is accompanied by another specifying term
		// e.g. Monocytes/*immunology/microbiology
		// we ignore everything after the "/" symbol.
		if (str.matches(".+?/.+?")) {
			String[] elements = str.split("/");		
			str = elements[0];
		}	
				
		// removes scop notes in brackets
		// should be replaced with a cleaner solution !!
		if (str.matches(".+?\\(.+?")) {
			String[] elements = str.split("\\(");		
			str = elements[0];			
		}	
	
		// Remove some non-alphanumeric characters
		
		// str = str.replace('/', ' ');
		str = str.replace('-', ' ');
		str = str.replace('&', ' ');
		

		str = str.replaceAll("\\*", "");
		str = str.replaceAll("\\, "," ");
		str = str.replaceAll("\\. "," ");
		str = str.replaceAll("\\:","");
	
		
		str = str.trim();
		
		// Stem string
		words = str.split(" ");
		str_nostop = "";
	
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (!m_Stopwords.isStopword(word)) {
				
				if (word.matches(".+?\\'.+?")) {
					String[] elements = word.split("\\'");		
					word = elements[1];			
				}	

				
				if (str_nostop.equals("")) {
					str_nostop = word;
				} else {
					str_nostop = str_nostop + " " + word;
				}
			}
		}

		stemmed = m_Stemmer.stemString(str_nostop);
		// System.err.println(stemmed + "\t" + str_nostop + "\t"+ str);
		pseudophrase = stemmed.split(" ");
		Arrays.sort(pseudophrase);
		//System.err.println(join(pseudophrase));
		return join(pseudophrase);
	}
	
	/** 
	 * Joins an array of strings to a single string.
	 */
	protected static String join(String[] str) {
		String result = "";
		for(int i = 0; i < str.length; i++) {
			if (result != "") {
				result = result + " " + str[i];
			} else {
				result = str[i];
			}
		}
		return result;
	}
	
	/**
	 * overloaded swap method: exchange 2 locations in an array of Strings.
	 */
	public static void swap(int loc1, int loc2, String[] a) {
		String temp = a[loc1];
		a[loc1] = a[loc2];
		a[loc2] = temp;
	} // end swap

	/**
	 * Sorts an array of Strings into alphabetic order
	 * 
	 */
	public static String[] sort(String[] a) {

		// rename firstAt to reflect new role in alphabetic sorting
		int i, j, firstAt;

		for (i = 0; i < a.length - 1; i++) {
			firstAt = i;
			for (j = i + 1; j < a.length; j++) {
				// modify to preserve ordering of a String that starts with
				// upper case preceding the otherwise identical String that
				// has only lower case letters
				if (a[j].toUpperCase().compareTo(a[firstAt].toUpperCase()) < 0) {
					// reset firstAt
					firstAt = j;
				}
				// if identical when converted to all same case
				if (a[j].toUpperCase().compareTo(a[firstAt].toUpperCase()) == 0) {
					// but a[j] precedes when not converted
					if (a[j].compareTo(a[firstAt]) < 0) {
						// reset firstAt
						firstAt = j;
					}
				}
			}
			if (firstAt != i) {
				swap(i, firstAt, a);
			}
		}
		return a;
	} // end method selectionSort
	
}


