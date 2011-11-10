/**
 * Copyright (c) 2010, UNC-Chapel Hill and Nescent
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided 
that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and 
 * the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the UNC-Chapel Hill or Nescent nor the names of its contributors may be used to endorse or promote 
 * products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

@author Jose R. Perez-Aguera
 */

package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.io.File;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.elmo.sesame.SesameManager;

import java.text.SimpleDateFormat;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;
import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveVocabulary;
import edu.unc.ils.mrc.hive2.api.impl.HiveVocabularyImpl;

/**
 * This class represents a HIVE vocabulary and associated indexes as 
 * described in the HIVE vocabulary property file.
 * 
 * Each HIVE vocabulary consists of a Sesame store, Lucene index, and 
 * two serialized TreeMaps representing the alphabetic and top-concept
 * indexes.
 */
public class SKOSSchemeImpl implements SKOSScheme {

    private static final Log logger = LogFactory.getLog(SKOSSchemeImpl.class);
	
	/* Vocabulary/scheme name */
	private String schemeName;
	
	/* Vocabulary/scheme long name */	
	private String longName;

	/* Vocabulary/scheme URI*/
	private String schemaURI;

	/* Lucene index directory */
	private String indexDirectory;
	
	/* Sesame store directory */
	private String storeDirectory;
	
	/* H2 database directory */
	private String h2Directory;
	
	/* Alphabetic index file name */
	//private String alphaFilePath;
	
	/* Top concept index file name */
	//private String topConceptIndexPath;

	/* KEA+ stopwords file path */
	private String stopwordsPath;
	
	/* SKOS RDF/XML file path */
	private String rdfPath;
	
	/* KAE+ training set path */
	private String KEAtrainSetDir;
	
	/* KEA+ test set path */
	private String KEAtestSetDir;
	
	/* KEA+ model path */
	private String KEAModelPath;
	
	/* Maui model path */
	private String MauiModelPath;	
	
	/* Lingpipe model path */
	private String lingpipeModel;
	
	/* Vocabulary creation date */
	private Date creationDate;
	
	/* Atom feed URL */
	private String atomFeedURL;
	
	/* Autocomplete index path */
	private String autocompletePath;
	
	/* Stemmer class name */
	private String stemmerClass;
	
	private HiveVocabulary hiveVocab;
	
	private String date;
	private long numberOfConcepts;
	private long numberOfRelations;
	private long numberOfBroaders;
	private long numberOfNarrowers;
	private long numberOfRelated;	

	public SKOSSchemeImpl(String confPath, String vocabularyName,
			boolean firstTime) throws HiveException
	{		
		String propertiesFile = confPath + File.separator + vocabularyName + ".properties";		
		init(propertiesFile);

		if (!firstTime) {
		
			try {
				Map<String, Long> stats = hiveVocab.getStats();
				this.date = hiveVocab.getLastUpdateDate().toString();
				this.numberOfBroaders = stats.get("broader");
				this.numberOfConcepts = stats.get("concepts");
				this.numberOfNarrowers = stats.get("narrower");
				this.numberOfRelated = stats.get("related");
				this.numberOfRelations = numberOfBroaders + numberOfNarrowers + numberOfRelated;
			} catch (Exception e) {
				logger.error(e);
			}

		}
	}

	/**
	 * Initialize the scheme based on the specified properties file
	 * @param propertiesFile
	 */
	private void init(String propertiesFile) throws HiveException
	{
	    logger.trace("init " + propertiesFile);
	    
		logger.info("Loading vocabulary configuration from " + propertiesFile);
			
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(propertiesFile);
			properties.load(fis);
			
			// Scheme name
			this.schemeName = properties.getProperty("name");
			if (schemeName.isEmpty())
				logger.warn("name property is empty");

			// Scheme long name
			this.longName = properties.getProperty("longName");
			if (longName.isEmpty())
				logger.warn("longName property is empty");

			// Scheme URI
			this.schemaURI = properties.getProperty("uri");
			if (schemaURI.isEmpty())
				logger.warn("uri property is empty");
			
			// Lucene index path
			this.indexDirectory = properties.getProperty("index");
			if (indexDirectory.isEmpty())
				logger.warn("index property is empty");			
				
			// Sesame store path
			this.storeDirectory = properties.getProperty("store");
			if (storeDirectory.isEmpty())
				logger.warn("store property is empty");
			
			// H2 store path
			this.h2Directory = properties.getProperty("h2");
			if (h2Directory.isEmpty())
				logger.warn("h2 property is empty");

			// KEA+ model path
			this.KEAModelPath = properties.getProperty("kea_model");
			if (KEAModelPath.isEmpty())
				logger.warn("kea_model property is empty");
			
			// Maui model path
			this.MauiModelPath = properties.getProperty("maui_model");
			if (MauiModelPath.isEmpty())
				logger.warn("maui_model property is empty");			

			// KEA+ test set path
			this.KEAtestSetDir = properties.getProperty("kea_test_set");
			if (KEAtestSetDir.isEmpty())
				logger.warn("kea_test_set property is empty");

			// KEA+ training set path
			this.KEAtrainSetDir = properties.getProperty("kea_training_set");
			if (KEAtrainSetDir.isEmpty())
				logger.warn("kea_training_set property is empty");

			// KEA+ stopwords path
			this.stopwordsPath = properties.getProperty("stopwords");
			if (stopwordsPath.isEmpty())
				logger.warn("stopwords property is empty");
			
			// Path to SKOS/RDF file
			this.rdfPath = properties.getProperty("rdf_file");
			if (rdfPath.isEmpty())
				logger.warn("rdf_file property is empty");			

			// Lingpipe model path
			this.lingpipeModel = properties.getProperty("lingpipe_model");
			if (lingpipeModel == null || lingpipeModel.isEmpty())
				logger.warn("lingpipe_model property is empty");		
			
			String dateStr = properties.getProperty("creationDate");
			SimpleDateFormat df = new SimpleDateFormat("MM-DD-yyyy");
			try {
				this.creationDate = df.parse(dateStr);
			} catch (Exception e) {
				logger.warn("Missing or invalid creationDate");
			}
			
			// Atom feed URL for synchronization
			this.atomFeedURL = properties.getProperty("atomFeedURL");
			if (atomFeedURL == null || atomFeedURL.isEmpty())
				logger.warn("atomFeedURL property is empty");	
			
			// Autocomplete index path
			this.autocompletePath = properties.getProperty("autocomplete");
			if (autocompletePath == null || autocompletePath.isEmpty())
				logger.warn("autocomplete property is empty");
			
			// Stemmer class
			this.stemmerClass = properties.getProperty("stemmerClass", "kea.stemmer.PorterStemmer");
			System.out.println("Using stemmer " + stemmerClass);
			if (stemmerClass == null || stemmerClass.isEmpty())
				logger.warn("stemmerClass property is empty, defaulting to kea.stemer.PorterStemmer");
			
			fis.close();
			
			this.hiveVocab = HiveVocabularyImpl.getInstance(schemeName, indexDirectory, storeDirectory,
					h2Directory, autocompletePath);
			
			
		} catch (FileNotFoundException e) {
			throw new HiveException("Property file not found", e);
		} catch (IOException e) {
			throw new HiveException ("Error occurred during scheme initialization", e);
		}
	}
	
	
	@Override
	public String getStopwordsPath() {
		return stopwordsPath;
	}

	@Override
	public String getRdfPath() {
		return rdfPath;
	}

	@Override
	public String getKEAtrainSetDir() {
		return KEAtrainSetDir;
	}

	@Override
	public String getKEAtestSetDir() {
		return KEAtestSetDir;
	}

	@Override
	public String getKEAModelPath() {
		return KEAModelPath;
	}
	
	@Override
	public String getMauiModelPath() {
		return MauiModelPath;
	}
	
	public String getAtomFeedURL() {
		return this.atomFeedURL;
	}
	
	@Override
	public String getAutoCompletePath() {
		return this.autocompletePath;
	}

	@Override
	/**
	   Returns an index of all terms, sorted alphabetically.
	**/
	public TreeMap<String, QName> getSubAlphaIndex(String startLetter) {
		TreeMap<String, QName> terms = new TreeMap<String, QName>();
		
		try
		{
			List<HiveConcept> hcs = hiveVocab.findConcepts(startLetter + "%", false);
			for (HiveConcept hc: hcs) {
				terms.put(hc.getPrefLabel(), hc.getQName());
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return terms;
	}

	@Override
	public List<SKOSConcept> getSubTopConceptIndex(String startLetter) {
		List<SKOSConcept> terms = new ArrayList<SKOSConcept>();
		try
		{
			List<HiveConcept> hcs = hiveVocab.findConcepts(startLetter + "%", true);
			for (HiveConcept hc: hcs) {
				SKOSConceptImpl sc = new SKOSConceptImpl(hc.getQName());
				sc.setPrefLabel(hc.getPrefLabel());
				sc.setIsLeaf(hc.isLeaf());
				terms.add(sc);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return terms;
	}

	@Override
	public String getLastDate() {
		return this.date;
	}


	@Override
	public String getName() {
		return this.schemeName;
	}

	@Override
	public long getNumberOfConcepts() {
		return this.numberOfConcepts;
	}

	@Override
	public long getNumberOfBroader() {
		return this.numberOfBroaders;
	}

	@Override
	public long getNumberOfNarrower() {
		return this.numberOfNarrowers;
	}

	@Override
	public long getNumberOfRelated() {
		return this.numberOfRelated;
	}

	@Override
	public long getNumberOfRelations() {
		return this.numberOfRelations;
	}

	@Override
	public String getLongName() {
		return longName;
	}

	@Override
	public String getStoreDirectory() {
		return storeDirectory;
	}

	@Override
	public String getIndexDirectory() {
		return this.indexDirectory;
	}

	@Override
	public String getSchemaURI() {
		return this.schemaURI;
	}

	@Override
	public SesameManager getManager() {
		try {
			return hiveVocab.getManager();
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	@Override
	public String getLingpipeModel() {
		return this.lingpipeModel;
	}
	
	@Override
	public Date getCreationDate() {
		return this.creationDate;
	}

	@Override
	public void importConcept(String uri) throws Exception {
		hiveVocab.importConcept(QName.valueOf(uri), uri);
	}

	@Override
	public void deleteConcept(String uri) throws Exception {
		deleteConcept(QName.valueOf(uri));
	}
	
	@Override
	public void deleteConcept(QName qname) throws Exception {
		hiveVocab.removeConcept(qname);
	}
		
	@Override
	public long getNumberOfTopConcepts() throws Exception {
		return hiveVocab.getNumTopConcepts();
	}

	@Override
	public void importConcepts(String path) throws Exception {
		hiveVocab.importConcepts(path);
	}
	
	@Override
	public void importConcepts(String path, boolean doSesame, boolean doLucene,
			boolean doH2, boolean doH2KEA, boolean doAutocomplete) throws Exception {
		hiveVocab.importConcepts(path, doSesame, doLucene, doH2, doH2KEA, doAutocomplete);
	}

	@Override
	public void importConcept(QName qname, String path) throws Exception {
		hiveVocab.importConcept(qname, path);
	}

	@Override
	public Date getLastUpdateDate() {
		Date lastUpdate = null;
		try
		{
			lastUpdate = hiveVocab.getLastUpdateDate();
		} catch (Exception e) {
			logger.error(e);
		}
		return lastUpdate;
	}
	
	@Override
	public void close() throws Exception {
		hiveVocab.close();
	}
	
	@Override
	public List<AutocompleteTerm> suggestTermsFor(String str, int numTerms) throws Exception
	{
		return hiveVocab.suggestTermsFor(str, numTerms);
	}
	@Override
	public HiveVocabulary getHiveVocabulary() {
		return hiveVocab;
	}
	
	@Override
	public Map<String, QName> getAlphaIndex() {
		return hiveVocab.findAllConcepts(false);
	}
	
	@Override
	public Map<String, QName> getTopConceptIndex() {
		return hiveVocab.findAllConcepts(true);
	}
	
	@Override
	public String getStemmerClass()
	{
		return stemmerClass;
	}
}
