package edu.unc.ils.mrc.hive.api;

import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.openrdf.elmo.sesame.SesameManager;

public interface SKOSScheme {
	
	public String getName();
	public String getLastDate();
	public int getNumberOfConcepts();
	public int getNumberOfBroader();
	public int getNumberOfNarrower();
	public int getNumberOfRelated();
	public int getNumberOfRelations();
	public String getStoreDirectory();
	public SesameManager getManager();
	public void setManager(SesameManager manager);
	public String getIndexDirectory();
	public String getSchemaURI();
	
	public String getLongName();
	
	public String getStopwordsPath();
	public String getKEAtrainSetDir();
	public String getKEAtestSetDir();
	public String getKEAModelPath();
	public String getRdfPath();
	public String getTopConceptIndexPath();
	public String getAlphaFilePath();

	public String getLingpipeModel();
	
	public TreeMap<String,QName> getAlphaIndex();
	public TreeMap<String,QName> getSubAlphaIndex(String startLetter);
	public TreeMap<String, QName> getTopConceptIndex();
	public TreeMap<String, QName> getSubTopConceptIndex(String startLetter);
}
