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
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.elmo.sesame.SesameManager;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.lucene.indexing.IndexAdministrator;

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
	
	/* Alphabetic index file name */
	private String alphaFilePath;
	
	/* Top concept index file name */
	private String topConceptIndexPath;

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
	
	/* Lingpipe model path */
	private String lingpipeModel;
	
	/* Sesame store manager */
	private SesameManager manager;

	/* Alphabetic index (serialized TreeMap) */
	private TreeMap<String, QName> alphaIndex;
	
	/* Top-concept index (serialized TreeMap) */
	private TreeMap<String, QName> topConceptIndex;
	
	private String date;
	private int numberOfConcepts;
	private int numberOfRelations;
	private int numberOfBroaders;
	private int numberOfNarrowers;
	private int numberOfRelated;	

	public SKOSSchemeImpl(String confPath, String vocabularyName,
			boolean firstTime) throws HiveException
	{		
		String propertiesFile = confPath + File.separator + vocabularyName + ".properties";		
		init(propertiesFile);

		if (!firstTime) {
			this.alphaIndex = IndexAdministrator
					.getAlphaIndex(this.alphaFilePath);
			this.topConceptIndex = IndexAdministrator
					.getTopConceptIndex(this.topConceptIndexPath);

			this.date = IndexAdministrator.getDate(this.indexDirectory);
			this.numberOfConcepts = IndexAdministrator
					.getNumconcepts(this.indexDirectory);
			this.numberOfRelations = IndexAdministrator
					.getNumRelationShips(this.indexDirectory);
			this.numberOfBroaders = IndexAdministrator
					.getNumBroader(this.indexDirectory);
			this.numberOfNarrowers = IndexAdministrator
					.getNumNarrower(this.indexDirectory);
			this.numberOfRelated = IndexAdministrator
					.getNumRelated(this.indexDirectory);
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
			
			// Alphabetic index file name
			this.alphaFilePath = properties.getProperty("alpha_file");
			if (alphaFilePath.isEmpty())
				logger.warn("alpha_file property is empty");
			
			// Top concept index file name
			this.topConceptIndexPath = properties.getProperty("top_concept_file");
			if (topConceptIndexPath.isEmpty())
				logger.warn("top_concept_file property is empty");

			// KEA+ model path
			this.KEAModelPath = properties.getProperty("kea_model");
			if (KEAModelPath.isEmpty())
				logger.warn("kea_model property is empty");

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
			
			fis.close();
			
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
	public TreeMap<String, QName> getAlphaIndex() {
		return this.alphaIndex;
	}

	@Override
	/**
	   Returns an index of all terms, sorted alphabetically.
	**/
	public TreeMap<String, QName> getSubAlphaIndex(String startLetter) {
		return IndexAdministrator
				.getSubAlphaIndex(startLetter, this.alphaIndex);
	}

	@Override
	/**
	   Returns the top level of the concept hierarchy.
	**/
	public TreeMap<String, QName> getTopConceptIndex() {
		return this.topConceptIndex;
	}

	@Override
	public TreeMap<String, QName> getSubTopConceptIndex(String startLetter) {
		return IndexAdministrator.getSubAlphaIndex(startLetter,
				this.topConceptIndex);
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
	public int getNumberOfConcepts() {
		return this.numberOfConcepts;
	}

	@Override
	public int getNumberOfBroader() {
		return this.numberOfBroaders;
	}

	@Override
	public int getNumberOfNarrower() {
		return this.numberOfNarrowers;
	}

	@Override
	public int getNumberOfRelated() {
		return this.numberOfRelated;
	}

	@Override
	public int getNumberOfRelations() {
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
		return this.manager;
	}

	@Override
	public void setManager(SesameManager manager) {
		this.manager = manager;
	}

	@Override
	public String getAlphaFilePath() {
		return alphaFilePath;
	}

	@Override
	public String getTopConceptIndexPath() {
		return topConceptIndexPath;
	}

	@Override
	public String getLingpipeModel() {
		return this.lingpipeModel;
	}
}
