package maui.vocab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;
import maui.vocab.store.VocabularyStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Builds an index with the content of the controlled vocabulary. Accepts
 * vocabularies as rdf files (SKOS format) and in plain text format:
 * vocabulary_name.en (with "ID TERM" per line) - descriptors & non-descriptors
 * vocabulary_name.use (with "ID_NON-DESCR \t ID_DESCRIPTOR" per line)
 * vocabulary_name.rel (with "ID \t RELATED_ID1 RELATED_ID2 ... " per line)
 * 
 * @author Olena Medelyan
 */

public class VocabularyJena implements Vocabulary, Serializable {

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

	/**
	 * Vocabulary constructor.
	 * 
	 * Given the name of the vocabulary and the format, it first checks whether
	 * the data/vocabularies directory contains the specified files:<br>
	 * - vocabularyName.rdf if skos format is selected<br>
	 * - or a set of 3 flat txt files starting with vocabularyName and with
	 * extensions<br>
	 * <li>.en (id term) <li>.use (non-descriptor \t descriptor) <li>.rel (id \t
	 * related_id1 related_id2 ...) If the required files exist, the vocabulary
	 * index is built.
	 * 
	 * @param vocabularyName
	 *            The name of the vocabulary file (before extension).
	 * @param vocabularyFormat
	 *            The format of the vocabulary (skos or text).
	 * @throws Exception
	 * */
	public VocabularyJena(VocabularyStore store) throws Exception {

		this.vocabularyFormat = store.getVocabularyFormat();

//		SKOS = new File("data/vocabularies/" + store.getVocabularyName()
//				+ ".rdf");
//		if (!SKOS.exists())
//			throw new Exception("File data/vocabularies/"
//					+ store.getVocabularyName() + ".rdf does not exist.");
		SKOS = new File(store.getRDFFile());
		if (!SKOS.exists())
			throw new Exception(store.getRDFFile() + " does not exist.");

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

	/**
	 * Starts initialization of the vocabulary.
	 * 
	 * @throws Exception
	 * 
	 */
	@Override
	public void initialize() throws Exception {
		buildSKOS();
	}

	/**
	 * Set the stopwords class.
	 * 
	 * @param stopwords
	 */
	@Override
	public void setStopwords(Stopwords stopwords) {
		this.stopwords = stopwords;
	}

	/**
	 * Builds the vocabulary indexes from SKOS file.
	 */
	private void buildSKOS() throws Exception {

		if (debugMode) {
			System.err
					.println("--- Building the Vocabulary index from the SKOS file...");
		}

		termIdIndex = new HashMap<String, String>();
		idTermIndex = new HashMap<String, String>();
		listsOfSenses = new HashMap<String, Vector<String>>();

		nonDescriptorIndex = new HashMap<String, String>();
		listsOfRelatedTerms = new HashMap<String, Vector<String>>();
		relationIndex = new HashMap<String, String>();

		Model model = ModelFactory.createDefaultModel();
		FileInputStream fileStream = new FileInputStream(SKOS);
		model.read(new InputStreamReader(fileStream, encoding), "");

		StmtIterator iter;
		Statement stmt;
		Resource concept;
		Property property;
		RDFNode value;

		// to create IDs for non-descriptors!
		int count = 0;
		// Iterating over all statements in the SKOS file
		iter = model.listStatements();

		while (iter.hasNext()) {
			stmt = iter.nextStatement();

			// id of the concept (Resource), e.g. "c_4828"
			concept = stmt.getSubject();
			String id = concept.getURI();

			// relation or Property of the concept, e.g. "narrower"
			property = stmt.getPredicate();
			String relation = property.getLocalName();

			// value of the property, e.g. c_4828 has narrower term "c_4829"
			value = stmt.getObject();
			String name = value.toString();

			if (relation.equals("prefLabel")) {

				String descriptor, language;
				int atPosition = name.indexOf('@');
				if (atPosition != -1) {
					language = name.substring(atPosition + 1);
					name = name.substring(0, atPosition);
					if (language.equals(this.language))
						descriptor = name;
					else
						continue;

				} else {
					descriptor = name;
				}

				String descriptorNormalized = normalizePhrase(descriptor);

				if (descriptorNormalized.length() >= 1) {
					Vector<String> ids = listsOfSenses
							.get(descriptorNormalized);
					if (ids == null)
						ids = new Vector<String>();
					ids.add(id);
					listsOfSenses.put(descriptorNormalized, ids);

					termIdIndex.put(descriptor.toLowerCase(), id);
					idTermIndex.put(id, descriptor);
				}

			} else if (relation.equals("altLabel")
					|| (relation.equals("hiddenLabel"))) {

				String non_descriptor, language;

				int atPosition = name.indexOf('@');
				if (atPosition != -1) {
					language = name.substring(atPosition + 1);
					name = name.substring(0, atPosition);
					if (language.equals(this.language))
						non_descriptor = name;
					else
						continue;

				} else {
					non_descriptor = name;
				}

				addNonDescriptor(count, id, non_descriptor);
				count++;

			} else if (relation.equals("broader")
					|| relation.equals("narrower")
					|| relation.equals("composite")
					|| relation.equals("compositeOf")
					|| relation.equals("hasTopConcept")
					|| relation.equals("related")) {

				String relatedId = name;

				Vector<String> relatedIds = listsOfRelatedTerms.get(id);
				if (relatedIds == null)
					relatedIds = new Vector<String>();

				relatedIds.add(relatedId);

				listsOfRelatedTerms.put(id, relatedIds);

				relationIndex.put(id + "-" + relatedId, relation);
				if (relation.equals("related")) {
					relationIndex.put(relatedId + "-" + id, relation);
				}
			}
		}

		if (debugMode) {
			System.err.println("--- Statistics about the vocabulary: ");
			System.err.println("\t" + termIdIndex.size() + " terms in total");
			System.err.println("\t" + nonDescriptorIndex.size()
					+ " non-descriptive terms");
			System.err.println("\t" + listsOfRelatedTerms.size()
					+ " terms have related terms");
		}
	}

	private void addNonDescriptor(int count, String idDescriptor,
			String nonDescriptor) {

		String idNonDescriptor = "d_" + count;
		count++;

		String normalizedNonDescriptor = normalizePhrase(nonDescriptor);
		if (normalizedNonDescriptor.length() >= 1) {
			Vector<String> ids = listsOfSenses.get(normalizedNonDescriptor);
			if (ids == null)
				ids = new Vector<String>();
			ids.add(idNonDescriptor);
			listsOfSenses.put(normalizedNonDescriptor, ids);
		}

		termIdIndex.put(nonDescriptor.toLowerCase(), idNonDescriptor);
		idTermIndex.put(idNonDescriptor, nonDescriptor);

		nonDescriptorIndex.put(idNonDescriptor, idDescriptor);
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

}
