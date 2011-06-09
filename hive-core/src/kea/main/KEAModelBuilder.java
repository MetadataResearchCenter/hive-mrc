package kea.main;


import java.io.BufferedOutputStream;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import kea.filters.KEAFilter;
import kea.stemmers.*;
import kea.stopwords.*;
import kea.vocab.Vocabulary;
import kea.vocab.VocabularyH2;

/**
 * Builds a keyphrase extraction model from the documents in a given
 * directory.  Assumes that the file names for the documents end with
 * ".txt".  Assumes that files containing corresponding
 * author-assigned keyphrases end with ".key". Optionally an encoding
 * for the documents/keyphrases can be defined (e.g. for Chinese
 * text).
 *
 * Valid options are:<p>
 *
 * -l "directory name"<br>
 * Specifies name of directory.<p>
 *
 * -m "model name"<br>
 * Specifies name of model.<p>
 *
 * -e "encoding"<br>
 * Specifies encoding.<p>
 * 
 * -v "vocabulary name" <br>
 * Specifies vocabulary name (e.g. agrovoc or none).<p>
 * 
 * -f "vocabulary format" <br>
 * Specifies vocabulary format (txt or skos).<p>
 *
 * -i "document language" <br>
 * Specifies document language (en, es, de, fr).<p>
 *
 * -d<br>
 * Turns debugging mode on.<p>
 *
 * -k<br>
 * Use keyphrase frequency statistic.<p>
 *
 * -r<br>
 * Use agrovoc relation as feature.<p>
 *
 * -p<br>
 * Disallow internal periods.<p>
 *
 * -x "length"<br>
 * Sets maximum phrase length (default: 3).<p>
 *
 * -y "length"<br>
 * Sets minimum phrase length (default: 1).<p>
 *
 * -o "number"<br>
 * The minimum number of times a phrase needs to occur (default: 2). <p>
 *
 * -s "name of class implementing list of stop words"<br>
 * Sets list of stop words to used (default: StopwordsEnglish).<p>
 *
 * -t "name of class implementing stemmer"<br>
 * Sets stemmer to use (default: IteratedLovinsStemmer). <p>
 *
 * -n<br>
 * Do not check for proper nouns. <p>
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version 1.0
 */
public class KEAModelBuilder implements OptionHandler {
	
    private static final Log logger = LogFactory.getLog(KEAModelBuilder.class);
	
	/** Stopwords path */
	String m_stopwordsPath;
	
	/** Name of directory */
	String m_dirName = null;
	
	/** Name of model */
	String m_modelName = null;
	
	/** Vocabulary name */
	String m_vocabulary = null;
	
	/** Format of the vocabulary */
	String m_vocabularyFormat = "skos";
	
	/** Document language */
	String m_documentLanguage = "en";
	
	/** Encoding */
	String m_encoding = "default";
	
	/** Debugging mode? */
	boolean m_debug = false;
	
	/** Use keyphrase frequency attribute? */
	boolean m_useKFrequency = false;
	
	/** Disallow internal periods? */
	boolean m_disallowIPeriods = false;
	
	/** The maximum length of phrases */
	private int m_MaxPhraseLength = 5;
	
	/** The minimum length of phrases */
	private int m_MinPhraseLength = 1;
	
	/** The minimum number of occurences of a phrase */
	private int m_MinNumOccur = 2;
	
	/** The KEA filter object */
	KEAFilter m_KEAFilter = null;
	
	/** The stemmer to be used */
	private Stemmer m_Stemmer = new SremovalStemmer();
	
	/** The list of stop words to be used */
	private Stopwords m_Stopwords;
	
	/** Determines whether check for proper nouns is performed */
	private boolean m_CheckForProperNouns = true;
	
	private Vocabulary vocabulary;
	
	public KEAModelBuilder(SKOSScheme scheme) {
		m_vocabularyFormat = "skos";
	}
	
	/**
	 * Get the M_CheckProperNouns value.
	 * @return the M_CheckProperNouns value.
	 */
	public boolean getCheckForProperNouns() {
		return m_CheckForProperNouns;
	}
	
	/**
	 * Set the M_CheckProperNouns value.
	 * @param newM_CheckProperNouns The new M_CheckProperNouns value.
	 */
	public void setCheckForProperNouns(boolean newM_CheckProperNouns) {
		this.m_CheckForProperNouns = newM_CheckProperNouns;
	}
	
	/**
	 * Get the M_Stopwords value.
	 * @return the M_Stopwords value.
	 */
	public Stopwords getStopwords() {
		
		return m_Stopwords;
	}
	
	/**
	 * Set the M_Stopwords value.
	 * @param newM_Stopwords The new M_Stopwords value.
	 */
	public void setStopwords(Stopwords newM_Stopwords) {
		
		this.m_Stopwords = newM_Stopwords;
	}
	
	public void setStopwords(String stopwordsPath) {
		this.m_Stopwords = new StopwordsEnglish(stopwordsPath);
	}
	
	
	/**
	 * Get the Stemmer value.
	 * @return the Stemmer value.
	 */
	public Stemmer getStemmer() {
		
		return m_Stemmer;
	}
	
	/**
	 * Set the Stemmer value.
	 * @param newStemmer The new Stemmer value.
	 */
	public void setStemmer(Stemmer newStemmer) {
		
		this.m_Stemmer = newStemmer;
	}
	
	/**
	 * Get the value of MinNumOccur.
	 *
	 * @return Value of MinNumOccur.
	 */
	public int getMinNumOccur() {
		
		return m_MinNumOccur;
	}
	
	/**
	 * Set the value of MinNumOccur.
	 *
	 * @param newMinNumOccur Value to assign to MinNumOccur.
	 */
	public void setMinNumOccur(int newMinNumOccur) {
		
		m_MinNumOccur = newMinNumOccur;
	}
	
	/**
	 * Get the value of MaxPhraseLength.
	 *
	 * @return Value of MaxPhraseLength.
	 */
	public int getMaxPhraseLength() {
		
		return m_MaxPhraseLength;
	}
	
	/**
	 * Set the value of MaxPhraseLength.
	 *
	 * @param newMaxPhraseLength Value to assign to MaxPhraseLength.
	 */
	public void setMaxPhraseLength(int newMaxPhraseLength) {
		
		m_MaxPhraseLength = newMaxPhraseLength;
	}
	
	/**
	 * Get the value of MinPhraseLength.
	 *
	 * @return Value of MinPhraseLength.
	 */
	public int getMinPhraseLength() {
		
		return m_MinPhraseLength;
	}
	
	/**
	 * Set the value of MinPhraseLength.
	 *
	 * @param newMinPhraseLength Value to assign to MinPhraseLength.
	 */
	public void setMinPhraseLength(int newMinPhraseLength) {
		
		m_MinPhraseLength = newMinPhraseLength;
	}
	
	/**
	 * Get the value of disallowIPeriods.
	 *
	 * @return Value of disallowIPeriods.
	 */
	public boolean getDisallowIPeriods() {
		
		return m_disallowIPeriods;
	}
	
	/**
	 * Set the value of disallowIPeriods.
	 *
	 * @param newdisallowIPeriods Value to assign to disallowIPeriods.
	 */
	public void setDisallowIPeriods(boolean newdisallowIPeriods) {
		
		m_disallowIPeriods = newdisallowIPeriods;
	}
	
	/**
	 * Get the value of useKFrequency.
	 *
	 * @return Value of useKFrequency.
	 */
	public boolean getUseKFrequency() {
		
		return m_useKFrequency;
	}
	
	
	/**
	 * Set the value of useKFrequency.
	 *
	 * @param newuseKFrequency Value to assign to useKFrequency.
	 */
	public void setUseKFrequency(boolean newuseKFrequency) {
		
		m_useKFrequency = newuseKFrequency;
	}
	
	
	/**
	 * Get the value of debug.
	 *
	 * @return Value of debug.
	 */
	public boolean getDebug() {
		
		return m_debug;
	}
	
	/**
	 * Set the value of debug.
	 *
	 * @param newdebug Value to assign to debug.
	 */
	public void setDebug(boolean newdebug) {
		
		m_debug = newdebug;
	}
	
	/**
	 * Get the value of encoding.
	 *
	 * @return Value of encoding.
	 */
	public String getEncoding() {
		
		return m_encoding;
	}
	
	/**
	 * Set the value of encoding.
	 *
	 * @param newencoding Value to assign to encoding.
	 */
	public void setEncoding(String newencoding) {
		
		m_encoding = newencoding;
	}
	
	/**
	 * Get the value of vocabulary name.
	 *
	 * @return Value of vocabulary name.
	 */
	public String getVocabulary() {
		
		return m_vocabulary;
	}
	
	/**
	 * Set the value of vocabulary name.
	 *
	 * @param newvocabulary Value to assign to vocabulary name.
	 */
	public void setVocabulary(String newvocabulary) {
		
		m_vocabulary = newvocabulary;
	}
	
	/**
	 * Get the value of document language.
	 *
	 * @return Value of document language.
	 */
	public String getDocumentLanguage() {
		
		return m_documentLanguage;
	}
	
	/**
	 * Set the value of document language.
	 *
	 * @param newdocumentLanguage Value to assign to document language.
	 */
	public void setDocumentLanguage(String newdocumentLanguage) {
		
		m_documentLanguage = newdocumentLanguage;
	}
	
	/**
	 * Get the value of vocabulary format.
	 *
	 * @return Value of vocabulary format.
	 */
	public String getVocabularyFormat() {
		
		return m_vocabularyFormat;
	}
	
	/**
	 * Set the value of vocabulary format.
	 *
	 * @param newvocabularyFormat Value to assign to vocabulary format.
	 */
	public void setVocabularyFormat(String newvocabularyFormat) {
		
		m_vocabularyFormat = newvocabularyFormat;
	}
	
	
	/**
	 * Get the value of modelName.
	 *
	 * @return Value of modelName.
	 */
	public String getModelName() {
		
		return m_modelName;
	}
	
	/**
	 * Set the value of modelName.
	 *
	 * @param newmodelName Value to assign to modelName.
	 */
	public void setModelName(String newmodelName) {
		
		m_modelName = newmodelName;
	}
	
	/**
	 * Get the value of dirName.
	 *
	 * @return Value of dirName.
	 */
	public String getDirName() {
		
		return m_dirName;
	}
	
	/**
	 * Set the value of dirName.
	 *
	 * @param newdirName Value to assign to dirName.
	 */
	public void setDirName(String newdirName) {
		
		m_dirName = newdirName;
	}
	
	/**
	 * Parses a given list of options controlling the behaviour of this object.
	 * Valid options are:<p>
	 *
	 * -l "directory name" <br>
	 * Specifies name of directory.<p>
	 *
	 * -m "model name" <br>
	 * Specifies name of model.<p>
	 *
	 * -v "vocabulary name" <br>
	 * Specifies vocabulary name.<p>
	 * 
	 * -f "vocabulary format" <br>
	 * Specifies vocabulary format.<p>
	 *    
	 * -i "document language" <br>
	 * Specifies document language.<p>
	 * 
	 * -e "encoding" <br>
	 * Specifies encoding.<p>
	 * 
	 * -d<br>
	 * Turns debugging mode on.<p>
	 *
	 * -k<br>
	 * Use keyphrase frequency statistic.<p>
	 *
	 * -p<br>
	 * Disallow internal periods. <p>
	 *
	 * -x "length"<br>
	 * Sets maximum phrase length (default: 3).<p>
	 *
	 * -y "length"<br>
	 * Sets minimum phrase length (default: 3).<p>
	 *
	 * -o "number"<br>
	 * The minimum number of times a phrase needs to occur (default: 2). <p>
	 *
	 * -s "name of class implementing list of stop words"<br>
	 * Sets list of stop words to used (default: StopwordsEnglish).<p>
	 *
	 * -t "name of class implementing stemmer"<br>
	 * Sets stemmer to use (default: IteratedLovinsStemmer). <p>
	 *
	 * -n<br>
	 * Do not check for proper nouns. <p>
	 *
	 * @param options the list of options as an array of strings
	 * @exception Exception if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		
		String dirName = Utils.getOption('l', options);
		if (dirName.length() > 0) {
			setDirName(dirName);
		} else {
			setDirName(null);
			throw new Exception("Name of directory required argument.");
		}
		
		String modelName = Utils.getOption('m', options);
		if (modelName.length() > 0) {
			setModelName(modelName);
		} else {
			setModelName(null);
			throw new Exception("Name of model required argument.");
		}
		
		String vocabularyName = Utils.getOption('v', options);
		if (vocabularyName.length() > 0) {
			setVocabulary(vocabularyName);
		} else {
			setVocabulary(null);
			throw new Exception("Name of vocabulary required argument.");
		}
		
		String vocabularyFormat = Utils.getOption('f', options);
		
		if (!getVocabulary().equals("none")) {
			if (vocabularyFormat.length() > 0) {
				if (vocabularyFormat.equals("skos") || vocabularyFormat.equals("text")) {
					setVocabularyFormat(vocabularyFormat);
				} else {
					throw new Exception("Unsupported format of vocabulary. It should be either \"skos\" or \"text\".");
				}
			} else {
				setVocabularyFormat(null);
				throw new Exception("If a controlled vocabulary is used, format of vocabulary required argument (skos or text).");
			}
		} else {
			setVocabularyFormat(null);
		}
		
		String encoding = Utils.getOption('e', options);
		if (encoding.length() > 0) {
			setEncoding(encoding);
		} else {
			setEncoding("default");
		}
		
		String documentLanguage = Utils.getOption('i', options);
		if (documentLanguage.length() > 0) {
			setDocumentLanguage(documentLanguage);
		} else {
			setDocumentLanguage("en");
		}
		
		String maxPhraseLengthString = Utils.getOption('x', options);
		if (maxPhraseLengthString.length() > 0) {
			setMaxPhraseLength(Integer.parseInt(maxPhraseLengthString));
		} else {
			setMaxPhraseLength(5);
		}
		String minPhraseLengthString = Utils.getOption('y', options);
		if (minPhraseLengthString.length() > 0) {
			setMinPhraseLength(Integer.parseInt(minPhraseLengthString));
		} else {
			setMinPhraseLength(1);
		}
		String minNumOccurString = Utils.getOption('o', options);
		if (minNumOccurString.length() > 0) {
			setMinNumOccur(Integer.parseInt(minNumOccurString));
		} else {
			setMinNumOccur(2);
		}
		
		String stopwordsString = Utils.getOption('s', options);
		if (stopwordsString.length() > 0) {
			stopwordsString = "kea.stopwords.".concat(stopwordsString);
			setStopwords((Stopwords)Class.forName(stopwordsString).newInstance());
		}
		
		String stemmerString = Utils.getOption('t', options);
		if (stemmerString.length() > 0) {
			stemmerString = "kea.stemmers.".concat(stemmerString);
			setStemmer((Stemmer)Class.forName(stemmerString).newInstance());
		}
		setDebug(Utils.getFlag('d', options));
		setUseKFrequency(Utils.getFlag('k', options));
		setDisallowIPeriods(Utils.getFlag('p', options));
		setCheckForProperNouns(!Utils.getFlag('n', options));
		Utils.checkForRemainingOptions(options);
	}
	
	/**
	 * Gets the current option settings.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String [] getOptions() {
		
		String [] options = new String [26];
		int current = 0;
		
		options[current++] = "-l"; 
		options[current++] = "" + (getDirName());
		options[current++] = "-m"; 
		options[current++] = "" + (getModelName());
		options[current++] = "-v"; 
		options[current++] = "" + (getVocabulary());
		options[current++] = "-f"; 
		options[current++] = "" + (getVocabularyFormat());
		options[current++] = "-e"; 
		options[current++] = "" + (getEncoding());
		options[current++] = "-i"; 
		options[current++] = "" + (getDocumentLanguage());
		
		if (getUseKFrequency()) {
			options[current++] = "-k";
		}
		if (getDebug()) {
			options[current++] = "-d";
		}
		if (getDisallowIPeriods()) {
			options[current++] = "-p";
		}
		options[current++] = "-x"; 
		options[current++] = "" + (getMaxPhraseLength());
		options[current++] = "-y"; 
		options[current++] = "" + (getMinPhraseLength());
		options[current++] = "-o"; 
		options[current++] = "" + (getMinNumOccur());
		options[current++] = "-s"; 
		options[current++] = "" + (getStopwords().getClass().getName());
		options[current++] = "-t"; 
		options[current++] = "" + (getStemmer().getClass().getName());
		if (getCheckForProperNouns()) {
			options[current++] = "-n";
		}
		
		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	}
	
	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options
	 */
	public Enumeration listOptions() {
		
		Vector newVector = new Vector(14);
		
		newVector.addElement(new Option(
				"\tSpecifies name of directory.",
				"l", 1, "-l <directory name>"));
		newVector.addElement(new Option(
				"\tSpecifies name of model.",
				"m", 1, "-m <model name>"));
		newVector.addElement(new Option(
				"\tSpecifies vocabulary name.",
				"v", 1, "-v <vocabulary name>"));
		newVector.addElement(new Option(
				"\tSpecifies vocabulary format (text or skos or none).",
				"f", 1, "-f <vocabulary format>"));
		newVector.addElement(new Option(
				"\tSpecifies document language (en (default), es, de, fr).",
				"i", 1, "-i <document language>"));
		newVector.addElement(new Option(
				"\tSpecifies encoding.",
				"e", 1, "-e <encoding>"));
		newVector.addElement(new Option(
				"\tTurns debugging mode on.",
				"d", 0, "-d"));
		newVector.addElement(new Option(
				"\tUse keyphrase frequency statistic.",
				"k", 0, "-k"));
		newVector.addElement(new Option(
				"\tDisallow internal periods.",
				"p", 0, "-p"));
		newVector.addElement(new Option(
				"\tSets the maximum phrase length (default: 5).",
				"x", 1, "-x <length>"));
		newVector.addElement(new Option(
				"\tSets the minimum phrase length (default: 1).",
				"y", 1, "-y <length>"));
		newVector.addElement(new Option(
				"\tSet the minimum number of occurences (default: 2).",
				"o", 1, "-o"));
		newVector.addElement(new Option(
				"\tSets the list of stopwords to use (default: StopwordsEnglish).",
				"s", 1, "-s <name of stopwords class>"));
		newVector.addElement(new Option(
				"\tSet the stemmer to use (default: SremovalStemmer).",
				"t", 1, "-t <name of stemmer class>"));
		newVector.addElement(new Option(
				"\tDo not check for proper nouns.",
				"n", 0, "-n"));
		
		return newVector.elements();
	}
	
	/**
	 * Collects the stems of the file names.
	 */
	public Hashtable collectStems() throws Exception {
		
		Hashtable stems = new Hashtable();
		
		try {
			File dir = new File(m_dirName);
			String[] files = dir.list();
			for (int i = 0; i < files.length; i++) {
				if (files[i].endsWith(".key") ||
						files[i].endsWith(".txt")) {
					String stem = files[i].substring(0, files[i].length() - 4);
					if (!stems.containsKey(stem)) {
						stems.put(stem, new Double(0));
					}
				}
			}
		} catch (Exception e) {
			throw new Exception("Problem opening directory " + m_dirName);
		}
		return stems;
	}
	
	/**
	 * Builds the model from the files
	 */
	public void buildModel(Hashtable stems, SKOSScheme schema, String stopwordsPath, SesameManager manager) throws Exception {
		
		String h2path = new File(schema.getRdfPath()).getParentFile().getAbsolutePath();
		//h2path += File.separator + schema.getName().toLowerCase() + "H2" + File.separator + schema.getName().toLowerCase();
		this.vocabulary = new VocabularyH2(schema.getName(), h2path, m_documentLanguage, schema.getManager());
		
		// Check whether there is actually any data
		if (stems.size() == 0) {
			throw new Exception("Couldn't find any data!");
		}
		
		FastVector atts = new FastVector(2);
		atts.addElement(new Attribute("doc", (FastVector) null));
		atts.addElement(new Attribute("keyphrases", (FastVector) null));
		Instances data = new Instances("keyphrase_training_data", atts, 0);
		
		// Build model
		m_KEAFilter = new KEAFilter();
		m_stopwordsPath = stopwordsPath;
		m_KEAFilter.setStopwords(m_stopwordsPath);
		
		m_KEAFilter.setDebug(m_debug);
		m_KEAFilter.setDisallowInternalPeriods(getDisallowIPeriods());
		m_KEAFilter.setKFused(getUseKFrequency());
		
		m_KEAFilter.setMaxPhraseLength(getMaxPhraseLength());
		m_KEAFilter.setMinPhraseLength(getMinPhraseLength());
		m_KEAFilter.setMinNumOccur(getMinNumOccur());
		m_KEAFilter.setStemmer(getStemmer());
		m_KEAFilter.setDocumentLanguage(getDocumentLanguage());
		m_KEAFilter.setVocabulary(getVocabulary());
		m_KEAFilter.setVocabularyFormat(getVocabularyFormat());
		m_KEAFilter.setStopwords(getStopwords());
		m_KEAFilter.setCheckForProperNouns(getCheckForProperNouns());
		m_KEAFilter.setInputFormat(data);
		
//		if (getVocabulary().equals("none")) {
//			m_KEAFilter.m_NODEfeature = false;
//		} else {
//			m_KEAFilter.loadThesaurus(getStemmer(),getStopwords(),this.vocabulary);
//		}
		m_KEAFilter.loadThesaurus(getStemmer(),getStopwords(),this.vocabulary);
		m_KEAFilter.setNumFeature();
		
		logger.info("-- Reading the documents");
		
		Enumeration elem = stems.keys();
		while (elem.hasMoreElements()) {
			String str = (String)elem.nextElement();
			
			double[] newInst = new double[2];
			try {
				File txt = new File(m_dirName + "/" + str + ".txt");
				InputStreamReader is;
				if (!m_encoding.equals("default")) {
					is = new InputStreamReader(new FileInputStream(txt), m_encoding);
				} else {
					is = new InputStreamReader(new FileInputStream(txt));
				}
				StringBuffer txtStr = new StringBuffer();
				int c;
				while ((c = is.read()) != -1) {
					txtStr.append((char)c);
				}
				is.close();
				newInst[0] = (double)data.attribute(0).addStringValue(txtStr.toString());
			} catch (Exception e) {
				if (m_debug) {
					System.err.println("Can't find document for stem " + str + ".");
				}
				newInst[0] = Instance.missingValue();
			}
			try {
				File key = new File(m_dirName + "/" + str + ".key");
				InputStreamReader is; 
				if (!m_encoding.equals("default")) {
					is = new InputStreamReader(new FileInputStream(key), m_encoding);
				} else {
					is = new InputStreamReader(new FileInputStream(key));
				}
				StringBuffer keyStr = new StringBuffer();
				int c;
				while ((c = is.read()) != -1) {
					keyStr.append((char)c);
				}      
				newInst[1] = (double)data.attribute(1).addStringValue(keyStr.toString());
			} catch (Exception e) {
				if (m_debug) {
					System.err.println("Can't find keyphrases for stem " + str + ".");
				}
				newInst[1] = Instance.missingValue();
			}
			data.add(new Instance(1.0, newInst));
			m_KEAFilter.input(data.instance(0));
			data = data.stringFreeStructure();
		}
		m_KEAFilter.batchFinished(this.vocabulary);
		
		while ((m_KEAFilter.output()) != null) {};
	}
	
	/** 
	 * Saves the extraction model to the file.
	 */
	public void saveModel() throws Exception {
		
		BufferedOutputStream bufferedOut = 
			new BufferedOutputStream(new FileOutputStream(m_modelName));
		ObjectOutputStream out = new ObjectOutputStream(bufferedOut);
		out.writeObject(m_KEAFilter);
		out.flush();
		out.close();
	}
	
	/**
	 * The main method.  
	 * @throws RepositoryException 
	 */
	public static void main(String[] ops) throws RepositoryException {
		
		String trainDir = "/home/hive/hive-data/nbii/nbiiKEA/train";
		String stopwordsPath = "/home/hive/hive-data/nbii/nbiiKEA/data/stopwords/stopwords_en.txt";
		
		String confPath = "/home/hive/workspace/hive-core/conf/";
		String vocabularyName = "nbii";
		
		try
		{
			SKOSScheme schema = new SKOSSchemeImpl(confPath, vocabularyName, false);

			KEAModelBuilder kmb = new KEAModelBuilder(schema);
			try {
				kmb.setOptions(ops);
				kmb.setDirName(trainDir);
				System.err.print("Building model with options: ");
				String[] optionSettings = kmb.getOptions();
				for (int i = 0; i < optionSettings.length; i++) {
					System.err.print(optionSettings[i] + " ");
				}
				System.err.println();
				kmb.buildModel(kmb.collectStems(),schema,stopwordsPath,null);
				kmb.saveModel();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				System.err.println("\nOptions:\n");
				Enumeration en = kmb.listOptions();
				while (en.hasMoreElements()) {
					Option option = (Option) en.nextElement();
					System.err.println(option.synopsis());
					System.err.println(option.description());
				}
			}
		} catch (HiveException e) {
			e.printStackTrace();
		}
	}
}

