package edu.unc.ils.mrc.hive2.api;

import javax.xml.namespace.QName;

/**
 * Interface for Hive vocabulary indexes. Each Hive my be represented
 * by one or more underlying indexes (e.g., Lucene, Lucene-autocomplete, 
 * H2, KEA, etc).
 * 
 * @author craig.willis@unc.edu
 */
public interface HiveIndex 
{
	/**
	 * Create the index
	 * @throws Exception
	 */
	public void createIndex() throws Exception;
	
	/**
	 * Add a concept to the index
	 * @param concept
	 * @throws Exception
	 */
	public void addConcept(HiveConcept concept) throws Exception;
	
	/**
	 * Update an existing concept in the index
	 * @param concept
	 * @throws Exception
	 */
	public void updateConcept(HiveConcept concept) throws Exception;
	
	/**
	 * Remove a concept from the index
	 * @param qname
	 * @throws Exception
	 */
	public void removeConcept(QName qname) throws Exception;
	
	/**
	 * Returns the number of concepts in the index
	 * @return
	 * @throws Exception
	 */
	public long getNumConcepts() throws Exception;
	
	/** 
	 * Starts a transaction in the underlying index
	 */
	public void startTransaction();
	
	/**
	 * Commits changes to the underlying index
	 * @throws Exception
	 */
	public void commit() throws Exception;
}
