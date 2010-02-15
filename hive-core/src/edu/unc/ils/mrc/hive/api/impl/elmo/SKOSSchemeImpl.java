package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.openrdf.elmo.sesame.SesameManager;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.lucene.indexing.IndexAdministrator;

public class SKOSSchemeImpl implements SKOSScheme {

	private String schemeName;
	private String longName;
	private String indexDirectory;
	private String storeDirectory;
	private String alphaFilePath;
	private String topConceptIndexPath;

	private String schemaURI;

	private SesameManager manager;

	private String date;
	private int numberOfConcepts;
	private int numberOfRelations;
	private int numberOfBroaders;
	private int numberOfNarrowers;
	private int numberOfRelated;

	private String stopwordsPath;
	private String rdfPath;
	private String KEAtrainSetDir;
	private String KEAtestSetDir;
	private String KEAModelPath;
	
	private String lingpipeModel;

	private TreeMap<String, QName> alphaIndex;
	private TreeMap<String, QName> topConceptIndex;

	public SKOSSchemeImpl(String confPath, String vocabularyName,
			boolean firstTime) {
		String propertiesFile = confPath + vocabularyName + ".properties";
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(propertiesFile);
			properties.load(fis);
			this.schemeName = properties.getProperty("name");
			this.indexDirectory = properties.getProperty("index");
			this.storeDirectory = properties.getProperty("store");
			this.longName = properties.getProperty("longName");
			this.schemaURI = properties.getProperty("uri");

			this.alphaFilePath = properties.getProperty("alpha_file");
			this.topConceptIndexPath = properties
					.getProperty("top_concept_file");
			this.KEAModelPath = properties.getProperty("kea_model");
			this.KEAtestSetDir = properties.getProperty("kea_test_set");
			this.KEAtrainSetDir = properties.getProperty("kea_training_set");
			this.stopwordsPath = properties.getProperty("stopwords");
			this.rdfPath = properties.getProperty("rdf_file");

			this.lingpipeModel = properties.getProperty("lingpipe_model");
			
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	public TreeMap<String, QName> getSubAlphaIndex(String startLetter) {
		return IndexAdministrator
				.getSubAlphaIndex(startLetter, this.alphaIndex);
	}

	@Override
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
