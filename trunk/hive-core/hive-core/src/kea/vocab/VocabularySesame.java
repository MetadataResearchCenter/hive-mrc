package kea.vocab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.sesame.SesameManager;

import kea.stemmers.SpanishStemmerSB;
import kea.stemmers.Stemmer;
import kea.stopwords.Stopwords;

/**
 * Builds an index with the content of the controlled vocabulary. Accepts
 * vocabularies as rdf files (SKOS format) and in plain text format:
 * vocabulary_name.en (with "ID TERM" per line) - descriptors & non-descriptors
 * vocabulary_name.use (with "ID_NON-DESCR \t ID_DESCRIPTOR" per line)
 * vocabulary_name.rel (with "ID \t RELATED_ID1 RELATED_ID2 ... " per line) See
 * KEA's homepage for more details.
 * 
 * @author Olena Medelyan
 * Modifications by Jose R. Perez-Aguera
 */

public class VocabularySesame implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Location of the rdf version of the controlled vocabulary it needs to be
	 * in the SKOS format!
	 */
	public static File SKOS;
	/**
	 * Location of the vocabulary's *.en file containing all terms of the
	 * vocabularies and their ids.
	 */
	public static File EN;
	/**
	 * Location of the vocabulary's *.use file containing ids of non-descriptor
	 * with the corresponding ids of descriptors.
	 */
	public static File USE;
	/**
	 * Location of the vocabulary's *.rel file containing semantically related
	 * terms for each descriptor in the vocabulary.
	 */
	public static File REL;

	// if the type of the semantic relation will be required later
	// this could be a file containing
	// this information
	// public static File RT;

	/**
	 * Boolean describing which vocabulary format has been chosen: true if SKOS,
	 * false if text.
	 */
	private boolean useSkos;

	/** <i>Vocabulary</i> index */
	private HashMap VocabularyEN = null;
	/** <i>Vocabulary</i> reverse index */
	private HashMap VocabularyENrev = null;
	/** <i>Vocabulary</i> non-descriptors - descriptors list */
	private HashMap VocabularyUSE = null;
	/** <i>Vocabulary</i> related terms */
	private HashMap VocabularyREL = null;
	private HashMap VocabularyRT = null;

	private SesameManager manager;

	/** The document language */
	private String m_language;

	/** The default stemmer to be used */
	private Stemmer m_Stemmer;

	/** The list of stop words to be used */
	private Stopwords m_Stopwords;

	/**
	 * Vocabulary constructor.
	 * 
	 * Given the name of the vocabulary and the format it first checks whether
	 * the VOCABULARIES directory contains the specified files: -
	 * vocabularyName.rdf if skos format is selected - or a set of 3 flat files
	 * starting with vocabularyName and with extensions .en (id term) .use
	 * (non-descriptor \t descriptor) .rel (id \t related_id1 related_id2 ...)
	 * If the required files exist, the vocabulary index is built.
	 * 
	 * @param vocabularyName
	 *            The name of the vocabulary file (before extension).
	 * @param vocabularyFormat
	 *            The format of the vocabulary (skos or text).
	 * */

	public VocabularySesame(String vocabularyName, String vocabularyFormat,
			String documentLanguage, SesameManager manager) {
		this.manager = manager;
		m_language = documentLanguage;
		if (vocabularyFormat.equals("skos")) {
			// SKOS = new File(vocabularyDir + "/" + vocabularyName + ".rdf");
			//SKOS = new File(vocabularyName);
//			if (!SKOS.exists()) {
//				System.err.println("File " + vocabularyPath + "/ "
//						+ vocabularyName + ".rdf does not exist.");
//				System.exit(1);
//			}
			useSkos = true;

		}
	}

	/**
	 * Starts initialization of the vocabulary.
	 * 
	 */
	public void initialize() {

		System.out.println("-- Loading the Index...");
		if (useSkos) {
			try {
				buildSKOS();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			try {
				build();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Set the Stemmer value.
	 * 
	 * @param newStemmer
	 *            The new Stemmer value.
	 */
	public void setStemmer(Stemmer newStemmer) {

		this.m_Stemmer = newStemmer;

	}

	/**
	 * Set the M_Stopwords value.
	 * 
	 * @param newM_Stopwords
	 *            The new M_Stopwords value.
	 */
	public void setStopwords(Stopwords newM_Stopwords) {
		this.m_Stopwords = newM_Stopwords;
	}

	/**
	 * Builds the vocabulary indexes from SKOS file.
	 */
	public void buildSKOS() throws Exception {

		System.out.println("-- Building the Vocabulary index from SKOS Sesame Store");

		VocabularyEN = new HashMap();
		VocabularyENrev = new HashMap();
		VocabularyUSE = new HashMap();
		VocabularyREL = new HashMap();
		VocabularyRT = new HashMap();

		try {

			int count = 1;

			for (Concept concept : manager.findAll(Concept.class)) {

				// id of the concept (Resource), e.g. "c_4828"
				String id = concept.getQName().getNamespaceURI()
						+ concept.getQName().getLocalPart();

				// value of the property, e.g. c_4828 has narrower term "c_4829"
				//String val = concept.getSkosPrefLabel();

				/*
				 * For prefLabels
				 */
				String descriptor = concept.getSkosPrefLabel();

//				if (val.contains("@")) {
//					String[] val_components = val.split("@");
//					// System.err.println(val_components[1] + " " +
//					// m_language);
//					if (val_components[1].equals(m_language)) {
//						// System.err.println("Yes");
//						descriptor = val_components[0];
//					} else {
//						continue;
//					}
//				} else {
//					descriptor = val;
//				}

				String avterm = pseudoPhrase(descriptor);
				if (avterm == null) {
					avterm = descriptor;
				}

				if (avterm.length() > 1) {
					VocabularyEN.put(avterm, id);
					VocabularyENrev.put(id, descriptor);
				}

				/*
				 * For altLabels
				 */

				String non_descriptor;
				Set<String> altLabels = concept.getSkosAltLabels();
				for (String a : altLabels) {
					non_descriptor = a;
//					val = a;
//					if (val.contains("@")) {
//						String[] val_components = val.split("@");
//						// System.err.println(val_components[1] + " " +
//						// m_language);
//						if (val_components[1].equals(m_language)) {
//							// System.err.println("Yes");
//							non_descriptor = val_components[0];
//						} else {
//							continue;
//						}
//					} else {
//						non_descriptor = val;
//					}

					// first add the non_descriptor to the index hash
					// then fill here non-descriptor hash

					// id => id_non_descriptor
					addNonDescriptor(count, id, non_descriptor);
					count++;

				}

				/*
				 * For broader terms
				 */

				String id_broader;
				Set<Concept> broaders = concept.getSkosBroaders();
				for (Concept b : broaders) {
					id_broader = b.getQName().getNamespaceURI()
							+ b.getQName().getLocalPart();

					if (VocabularyREL.get(id) == null) {
						Vector rt = new Vector();
						rt.add(id_broader);
						VocabularyREL.put(id, rt);
					} else {
						Vector rt = (Vector) VocabularyREL.get(id);
						rt.add(id_broader);
						VocabularyREL.put(id, rt);
					}

					VocabularyRT.put(id + "-" + id_broader, "broader");
				}

				/*
				 * For narrower terms
				 */

				String id_narrower;
				Set<Concept> narrowers = concept.getSkosNarrowers();
				for (Concept n : narrowers) {
					id_narrower = n.getQName().getNamespaceURI()
							+ n.getQName().getLocalPart();

					if (VocabularyREL.get(id) == null) {
						Vector rt = new Vector();
						rt.add(id_narrower);
						VocabularyREL.put(id, rt);
					} else {
						Vector rt = (Vector) VocabularyREL.get(id);
						rt.add(id_narrower);
						VocabularyREL.put(id, rt);
					}

					VocabularyRT.put(id + "-" + id_narrower, "narrower");
				}

				/*
				 * For related terms
				 */

				String id_related;
				Set<Concept> related = concept.getSkosRelated();
				for (Concept r : related) {
					id_related = r.getQName().getNamespaceURI()
							+ r.getQName().getLocalPart();
					VocabularyRT.put(id + "-" + id_related, "related");
					VocabularyRT.put(id_related + "-" + id, "related");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addNonDescriptor(int count, String id_descriptor,
			String non_descriptor) {
		// id => id_non_descriptor
		String id_non_descriptor = "d_" + count;
		count++;

		String avterm = pseudoPhrase(non_descriptor);
		if (avterm.length() > 2) {
			VocabularyEN.put(avterm, id_non_descriptor);
			VocabularyENrev.put(id_non_descriptor, non_descriptor);
		}
		VocabularyUSE.put(id_non_descriptor, id_descriptor);
	}

	public String remove(String[] words, int i) {

		String result = "";
		for (int j = 0; j < words.length; j++) {
			if ((j != i) && (!m_Stopwords.isStopword(words[j]))) {

				result = result + words[j];

				if ((j + 1) != words.length) {
					result = result + " ";
				}
			}

		}
		return result;
	}

	/**
	 * Builds the vocabulary index from the text files.
	 */
	public void build() throws Exception {

		System.out.println("-- Building the Vocabulary index");

		VocabularyEN = new HashMap();
		VocabularyENrev = new HashMap();

		String readline;
		String term;
		String avterm;
		String id;

		try {
			InputStreamReader is = new InputStreamReader(
					new FileInputStream(EN));
			BufferedReader br = new BufferedReader(is);
			while ((readline = br.readLine()) != null) {
				int i = readline.indexOf(' ');
				term = readline.substring(i + 1);

				avterm = pseudoPhrase(term);

				if (avterm.length() > 2) {
					id = readline.substring(0, i);
					VocabularyEN.put(avterm, id);
					VocabularyENrev.put(id, term);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Builds the vocabulary index with descriptors/non-descriptors relations.
	 */
	public void buildUSE() throws Exception {
		if (!useSkos) {
			VocabularyUSE = new HashMap();
			String readline;
			String[] entry;

			try {

				InputStreamReader is = new InputStreamReader(
						new FileInputStream(USE));
				BufferedReader br = new BufferedReader(is);
				while ((readline = br.readLine()) != null) {
					entry = split(readline, "\t");

					// if more than one descriptors for
					// one non-descriptors are used, ignore it!
					// probably just related terms (cf. latest edition of
					// Agrovoc)

					if ((entry[1].indexOf(" ")) == -1) {
						VocabularyUSE.put(entry[0], entry[1]);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Builds the vocabulary index with semantically related terms.
	 */
	public void buildREL() throws Exception {
		if (!useSkos) {

			System.err
					.println("-- Building the Vocabulary index with related pairs");

			VocabularyREL = new HashMap();

			String readline;
			String[] entry;

			try {

				InputStreamReader is = new InputStreamReader(
						new FileInputStream(REL));
				BufferedReader br = new BufferedReader(is);
				while ((readline = br.readLine()) != null) {
					entry = split(readline, "\t");
					String[] temp = split(entry[1], " ");
					Vector rt = new Vector();
					for (int i = 0; i < temp.length; i++) {
						rt.add(temp[i]);
					}
					VocabularyREL.put(entry[0], rt);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// Might be useful later, when the kind of relation is important
	// or wether two terms are related or not
	// public void buildRT() throws Exception {
	//		
	// VocabularyRT = new HashMap();
	//		
	// String[] entry;
	// String readline;
	// try {
	// InputStreamReader is2 = new InputStreamReader(new FileInputStream(RT));
	// BufferedReader br2 = new BufferedReader(is2);
	// while((readline=br2.readLine()) != null) {
	// entry = split(readline,"\t");
	// String pair = entry[0] + "-" + entry[1];
	// VocabularyRT.put(pair,"1");
	//				
	// }
	// } catch (Exception e) {
	// System.err.println("You need to put the .pairs file into KEA directory");
	// }
	//		
	// }
	//	

	/**
	 * Checks whether a normalized version of a phrase (pseudo phrase) is a
	 * valid vocabulary term.
	 * 
	 * @param phrase
	 * @return true if phrase is in the vocabulary
	 */
	public boolean containsEntry(String phrase) {
		return VocabularyEN.containsKey(phrase);
	}

	/**
	 * Given a phrase returns its id in the vocabulary.
	 * 
	 * @param phrase
	 * @return id of the phrase in the vocabulary index
	 */
	public String getID(String phrase) {
		String pseudo = pseudoPhrase(phrase);
		String id = null;
		if (pseudo != null) {
			id = (String) VocabularyEN.get(pseudo);
			if (VocabularyUSE.containsKey(id)) {
				id = (String) VocabularyUSE.get(id);
			}
		}
		return id;
	}

	/**
	 * Given id, gets the original version of vocabulary term.
	 * 
	 * @param id
	 * @return original version of the vocabulary term
	 */
	public String getOrig(String id) {
		return (String) VocabularyENrev.get(id);
	}

	/**
	 * Given id of the non-descriptor returs the id of the corresponding
	 * descriptor
	 * 
	 * @param id
	 *            of the non-descriptor
	 * @return id of the descriptor
	 */
	public String getDescriptor(String id) {
		return (String) VocabularyUSE.get(id);
	}

	/**
	 * Given id of a term returns the list with ids of terms related to this
	 * term.
	 * 
	 * @param id
	 * @return a vector with ids related to the input id
	 */
	public Vector getRelated(String id) {
		return (Vector) VocabularyREL.get(id);
	}

	/**
	 * Given an ID of a term gets the list of all IDs of terms that are
	 * semantically related to the given term with a specific relation
	 * 
	 * @param id
	 *            , relation
	 * @return a vector with ids related to the input id by a specified relation
	 */
	public Vector getRelated(String id, String relation) {
		Vector related = new Vector();
		Vector all_related = (Vector) VocabularyREL.get(id);
		if (all_related != null) {

			for (int d = 0; d < all_related.size(); d++) {
				String rel_id = (String) all_related.elementAt(d);

				String rel = (String) VocabularyRT.get(id + "-" + rel_id);

				if (rel != null) {
					if (rel.equals(relation)) {
						related.add(rel_id);
					}
				} else {
					System.err.println("Problem with " + getOrig(id) + " and "
							+ getOrig(rel_id));
				}
			}
		}
		return related;
	}

	/**
	 * Splits a string str at given character sequence (separator) into an
	 * array.
	 * 
	 * @param str
	 *            , separator
	 * @return String array with string parts separated by the separator string
	 */
	public String[] split(String str, String separator) {

		ArrayList lst = new ArrayList();
		String word = "";

		for (int i = 0; i < str.length(); i++) {
			int j = i + 1;
			String letter = str.substring(i, j);
			if (!letter.equalsIgnoreCase(separator)) {
				word = word + str.charAt(i);
			} else {
				lst.add(word);
				word = "";
			}
		}
		if (word != "") {
			lst.add(word);
		}
		String[] result = (String[]) lst.toArray(new String[lst.size()]);
		return result;
	}

	/**
	 * Generates the preudo phrase from a string. A pseudo phrase is a version
	 * of a phrase that only contains non-stopwords, which are stemmed and
	 * sorted into alphabetical order.
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
		str = str.replaceAll("\\, ", " ");
		str = str.replaceAll("\\. ", " ");
		str = str.replaceAll("\\:", "");

		str = str.trim();

		// Stem string
		words = str.split(" ");
		str_nostop = "";

		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (!m_Stopwords.isStopword(word)) {

				if (word.matches(".+?\\'.+?")) {
					String[] elements = word.split("\\'");
					if (elements.length < 1)
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
		pseudophrase = sort(stemmed.split(" "));
		// System.err.println(join(pseudophrase));
		return join(pseudophrase);
	}

	/**
	 * Joins an array of strings to a single string.
	 */
	private static String join(String[] str) {
		String result = "";
		for (int i = 0; i < str.length; i++) {
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
