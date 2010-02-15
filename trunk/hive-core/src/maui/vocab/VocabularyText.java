package maui.vocab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;
import maui.vocab.store.VocabularyStore;

public class VocabularyText implements Vocabulary {

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

	/** Document language */
	private String language = "en";

	/** id --> list of related ids */
	private HashMap<String, Vector<String>> listsOfRelatedTerms = null;
	
	/** id-relatedId --> relation */
	private HashMap<String, String> relationIndex = null;

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

	/** non-descriptor id --> descriptors id */
	private HashMap<String, String> nonDescriptorIndex = null;

	/** index : descriptor --> id */
	private HashMap<String, String> termIdIndex;

	/** reverse index : id --> descriptor */
	private HashMap<String, String> idTermIndex;
	
	/** normalized descriptor --> list of all possible meanings */
	private HashMap<String, Vector<String>> listsOfSenses;
	

	public VocabularyText(VocabularyStore store) {
		EN = new File("data/vocabularies/" + store.getVocabularyName() + ".en");
		USE = new File("data/vocabularies/" + store.getVocabularyName()
				+ ".use");
		REL = new File("data/vocabularies/" + store.getVocabularyName()
				+ ".rel");

		if (!EN.exists())
			try {
				throw new Exception("File data/vocabularies/"
						+ store.getVocabularyName() + ".rdf does not exist.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (!USE.exists())
			try {
				throw new Exception("File data/vocabularies/"
						+ store.getVocabularyName() + ".rdf does not exist.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (!REL.exists())
			try {
				throw new Exception("File data/vocabularies/"
						+ store.getVocabularyName() + ".rdf does not exist.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	/**
	 * Starts initialization of the vocabulary.
	 * 
	 * @throws Exception
	 * 
	 */
	@Override
	public void initialize() throws Exception {
		buildTEXT();
		buildUSE();
		buildREL();
	}

	/**
	 * Builds the vocabulary index with semantically related terms.
	 */
	private void buildREL() throws Exception {
		System.err
				.println("-- Building the Vocabulary index with related pairs");

		listsOfRelatedTerms = new HashMap<String, Vector<String>>();

		String readline;
		String[] entry;

		InputStreamReader is = new InputStreamReader(new FileInputStream(REL));
		BufferedReader br = new BufferedReader(is);
		while ((readline = br.readLine()) != null) {
			entry = readline.split("\t");
			String[] temp = entry[1].split(" ");
			Vector<String> relatedTerms = new Vector<String>();
			for (int i = 0; i < temp.length; i++) {
				relatedTerms.add(temp[i]);
			}
			listsOfRelatedTerms.put(entry[0], relatedTerms);
		}
	}

	/**
	 * Builds the vocabulary index from the text files.
	 */
	private void buildTEXT() throws Exception {

		System.err.println("-- Building the Vocabulary index");

		termIdIndex = new HashMap<String, String>();
		idTermIndex = new HashMap<String, String>();

		String readline;
		String term;
		String avterm;
		String id;

		InputStreamReader is = new InputStreamReader(new FileInputStream(EN));
		BufferedReader br = new BufferedReader(is);
		while ((readline = br.readLine()) != null) {
			int i = readline.indexOf(' ');
			term = readline.substring(i + 1);

			avterm = normalizePhrase(term);

			if (avterm.length() >= 1) {
				id = readline.substring(0, i);
				termIdIndex.put(avterm, id);
				idTermIndex.put(id, term);
			}
		}

	}

	/**
	 * Builds the vocabulary index with descriptors/non-descriptors relations.
	 */
	private void buildUSE() throws Exception {

		nonDescriptorIndex = new HashMap<String, String>();
		String readline;
		String[] entry;

		InputStreamReader is = new InputStreamReader(new FileInputStream(USE));
		BufferedReader br = new BufferedReader(is);
		while ((readline = br.readLine()) != null) {
			entry = readline.split("\t");

			// if more than one descriptors for
			// one non-descriptors are used, ignore it!
			// probably just related terms (cf. latest edition of Agrovoc)

			if ((entry[1].indexOf(" ")) == -1) {
				nonDescriptorIndex.put(entry[0], entry[1]);
			}
		}
	}

	/**
	 * Checks whether a normalized phrase is a valid vocabulary term.
	 * 
	 * @param phrase
	 * @return true if phrase is in the vocabulary
	 */
	@Override
	public boolean containsNormalizedEntry(String phrase) {
		return listsOfSenses.containsKey(normalizePhrase(phrase));
	}

	/**
	 * Returns the id of the given term
	 * 
	 * @param phrase
	 * @return term id
	 */
	@Override
	public String getID(String phrase) {
		String id = termIdIndex.get(phrase.toLowerCase());
		if (id != null) {
			if (nonDescriptorIndex.containsKey(id))
				id = nonDescriptorIndex.get(id);
		}
		return id;
	}

	/**
	 * Given id of a term returns the list with ids of terms related to this
	 * term.
	 * 
	 * @param id
	 * @return a vector with ids related to the input id
	 */
	@Override
	public Vector<String> getRelated(String id) {
		return listsOfRelatedTerms.get(id);
	}

	/**
	 * Given an ID of a term gets the list of all IDs of terms that are
	 * semantically related to the given term with a specific relation
	 * 
	 * @param id
	 *            , relation
	 * @return a vector with ids related to the input id by a specified relation
	 */
	@Override
	public Vector<String> getRelated(String id, String relation) {
		Vector<String> related = new Vector<String>();
		Vector<String> all_related = listsOfRelatedTerms.get(id);
		if (all_related != null) {

			for (String rel_id : all_related) {
				String rel = relationIndex.get(id + "-" + rel_id);

				if (rel != null) {
					if (rel.equals(relation))
						related.add(rel_id);
				}
			}
		}
		return related;
	}

	/**
	 * Retrieves all possible descriptors for a given phrase
	 * 
	 * @param phrase
	 * @return
	 */
	@Override
	public Vector<String> getSenses(String phrase) {
		String normalized = normalizePhrase(phrase);

		Vector<String> senses = new Vector<String>();
		if (listsOfSenses.containsKey(normalized)) {
			for (String senseId : listsOfSenses.get(normalized)) {
				// 1. retrieve a descriptor if this sense is a non-descriptor
				if (nonDescriptorIndex.containsKey(senseId))
					senseId = nonDescriptorIndex.get(senseId);

				senses.add(senseId);
			}
		}
		return senses;

	}

	/**
	 * Returns the term for the given id
	 * 
	 * @param term
	 *            id
	 * @return phrase
	 */
	@Override
	public String getTerm(String id) {
		return idTermIndex.get(id);
	}

	/**
	 * Returns true if a phrase has more than one senses
	 * 
	 * @param phrase
	 * @return
	 */
	@Override
	public boolean isAmbiguous(String phrase) {
		Vector<String> meanings = listsOfSenses.get(normalizePhrase(phrase));
		if (meanings == null || meanings.size() == 1) {
			return false;
		}
		return true;
	}

	/**
	 * Generates the preudo phrase from a string. A pseudo phrase is a version
	 * of a phrase that only contains non-stopwords, which are stemmed and
	 * sorted into alphabetical order.
	 */
	@Override
	public String normalizePhrase(String phrase) {

		if (toLowerCase) {
			phrase = phrase.toLowerCase();
		}

		if (toLowerCase) {
			phrase = phrase.toLowerCase();
		}
		StringBuffer result = new StringBuffer();
		char prev = ' ';
		int i = 0;
		while (i < phrase.length()) {
			char c = phrase.charAt(i);

			// we ignore everything after the "/" symbol and everything in
			// brackets
			// e.g. Monocytes/*immunology/microbiology -> monocytes
			// e.g. Vanilla (Spice) -> vanilla
			if (c == '/' || c == '(')
				break;

			if (c == '-' || c == '&' || c == '.' || c == '.')
				c = ' ';

			if (c == '*' || c == ':') {
				prev = c;
				i++;
				continue;
			}

			if (c != ' ' || prev != ' ')
				result.append(c);

			prev = c;
			i++;
		}

		phrase = result.toString().trim();

		if (reorder || stopwords != null || stemmer != null) {
			phrase = pseudoPhrase(phrase);
		}
		if (phrase.equals("")) {
			// to prevent cases where the term is a stop word (e.g. Back).
			return result.toString();
		} else {
			return phrase;
		}
	}

	/**
	 * Generates the preudo phrase from a string. A pseudo phrase is a version
	 * of a phrase that only contains non-stopwords, which are stemmed and
	 * sorted into alphabetical order.
	 */
	@Override
	public String pseudoPhrase(String str) {
		String result = "";
		String[] words = str.split(" ");
		if (reorder) {
			Arrays.sort(words);
		}
		for (String word : words) {

			if (stopwords != null) {
				if (stopwords.isStopword(word)) {
					continue;
				}
			}

			int apostr = word.indexOf('\'');
			if (apostr != -1) {
				word = word.substring(0, apostr);
			}

			if (stemmer != null) {
				word = stemmer.stem(word);
			}
			result += word + " ";
		}
		return result.trim();
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public void setLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}

	@Override
	public void setReorder(boolean reorder) {
		this.reorder = reorder;
	}

	@Override
	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}

	@Override
	public void setDebug(boolean debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	public void setStopwords(Stopwords stopwords) {
		// TODO Auto-generated method stub

	}

}
