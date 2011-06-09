/**
 * Copyright (c) 2011, UNC-Chapel Hill and Nescent

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
 */

package edu.unc.ils.mrc.hive2.api.impl;

import java.io.File;


import java.io.IOException;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;

import edu.unc.ils.mrc.hive.ir.lucene.analysis.HIVEAnalyzer;
import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveIndex;

/**
 * Implements a full-text index of a Hive vocabulary using Lucene. The
 * Lucene index supplements the primary triple store to support full-text
 * searching.
 * 
 * @author craig.willis@unc.edu
 */
public class HiveLuceneIndexImpl implements HiveIndex
{
    private static final Log logger = LogFactory.getLog(HiveLuceneIndexImpl.class);
	
    /* Vocabulary name */
	String name;
	
	/* Base path for Lucene index */
	String lucenePath;
	
    /* Lucene index writer */
    IndexWriter writer;
   
    /* Fields */
    String ID_FIELD = "id";
    String URI_FIELD = "uri";
    String LOCAL_PART_FIELD = "localPart";
    String PREFLABEL_FIELD = "prefLabel";
    String ALTLABEL_FIELD = "altLabel";
   
    boolean inTransaction = false;
    
	
	/**
	 * Constructs a HiveLuceneIndexImpl for the specified vocabulary at the specified 
	 * path.
	 * 
	 * @param lucenePath	Base path for Lucene index for this vocabulary
	 * @param name		Vocabulary name
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public HiveLuceneIndexImpl(String lucenePath, String name) 
	{
		this.name = name;
		// Base path for Lucene index
		//this.lucenePath = basePath + File.separator + "lucene";
		this.lucenePath = lucenePath;
		
		init();
	}
	
	/**
	 * Initialize the Lucene index
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void init() 
	{
		logger.debug("init()");
		try 
		{
			boolean create = false;
			File indexDir = new File(lucenePath);
			
			if (!indexDir.exists())
				create = true;
			
			boolean retry = false;
			do 
			{
				try
				{
					this.writer = new IndexWriter(lucenePath,
						new HIVEAnalyzer(), create,
						IndexWriter.MaxFieldLength.UNLIMITED);
					retry = false;
				} catch (LockObtainFailedException e) {
					logger.warn(e);
					logger.warn("Attempting to delete write.lock and retry");
					File writeLock = new File(lucenePath + File.separator + "write.lock");
					if (writeLock.delete())
						retry = true;
					else
						logger.error("Unable to cleanup write.lock file");	
				}
			}
			while (retry);
			
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void startTransaction() {
		this.inTransaction = true;
	}
	
	public void commit() throws CorruptIndexException, IOException {
	
		writer.commit();
		this.inTransaction = false;
	}

	/**
	 * Adds the specified concept to the Lucene index
	 */
	public void addConcept(HiveConcept concept) 
	{
		logger.debug("addConcept(): " + concept.getQName());
		
		Document document = new Document();

		if (!inTransaction)
		{
			try {
				if (findDocument(concept.getQName()) != null)
					return;
	
			} catch (IOException e) {
				logger.error(e);
			} catch (ParseException e) {
				logger.error(e);
			}
		}
		
		String id = concept.getQName().getNamespaceURI() + concept.getQName().getLocalPart();
		Field idL = new Field(ID_FIELD, id , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		document.add(idL);
		
		String uri = concept.getQName().getNamespaceURI();
		Field uriL = new Field(URI_FIELD, uri , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		document.add(uriL);
		
		try
		{
			// Local Part
			String lp = concept.getQName().getLocalPart();
			Field lpL = new Field(LOCAL_PART_FIELD, lp , Field.Store.YES,
					Field.Index.NOT_ANALYZED);
			document.add(lpL);
		} catch (Exception e) {
			logger.warn("Concept " + concept.getQName() + " missing localPart. Skipping.");
		}
		
		try
		{
			// PrefLabel
			Field prefLabelL = new Field(PREFLABEL_FIELD,
				concept.getPrefLabel(), Field.Store.YES,
				Field.Index.ANALYZED);
			prefLabelL.setBoost(1.5f);
			document.add(prefLabelL);
		} catch (Exception e) {
			logger.warn("Concept " + concept.getQName() + " missing prefLabel. Skipping.");
		}

		try
		{
			// altLabels
			String a = "";
			for (String s : concept.getAltLabels()) {
				a = a + " " + s;
			}
			Field altLabelL = new Field(ALTLABEL_FIELD, a.trim(), Field.Store.YES,
					Field.Index.ANALYZED);
			document.add(altLabelL);
		} catch (Exception e) {
			logger.warn("Error retrieving altLabel, may not exist in store. Skipping.");
		}
		
		
		try {
			writer.addDocument(document);
			if (!inTransaction)
				writer.commit();
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Return the Lucene Document for the specified QName
	 * @param qname	QName of concept
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private Document findDocument(QName qname) throws IOException, ParseException 
	{
		logger.debug("findDocument(): " + qname);
		
		IndexSearcher searcher = new IndexSearcher(lucenePath);	
		QueryParser parser = new QueryParser(ID_FIELD, new StandardAnalyzer());
		Query query = parser.parse(qname.getNamespaceURI() +  qname.getLocalPart());
		TopDocs topDocs = searcher.search(query, 100);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		if (scoreDocs.length > 0) 
			return searcher.doc(scoreDocs[0].doc);
		else
			return null;
	}

	/**
	 * Close the index
	 */
	public void close() {
	    logger.trace("close()");
	    
		try {
			this.writer.close();
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void createIndex() throws Exception {
		// Not used
	}
	
	/**
	 * Updates a concept in the Lucene index
	 */
	@Override
	public void updateConcept(HiveConcept concept) throws Exception 
	{	
		logger.debug("updateConcept(): " + concept.getQName());
		
		Document document = new Document();
		String id = concept.getQName().getNamespaceURI() + concept.getQName().getLocalPart();
		Field idL = new Field(ID_FIELD, id , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		document.add(idL);
		
		String uri = concept.getQName().getNamespaceURI(); 
		Field uriL = new Field(URI_FIELD, uri , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		document.add(uriL);
		
		try
		{
			// Local Part
			String lp = concept.getQName().getLocalPart();
			Field lpL = new Field(LOCAL_PART_FIELD, lp , Field.Store.YES,
					Field.Index.NOT_ANALYZED);
			document.add(lpL);
		} catch (Exception e) {
			logger.warn("Concept " + concept.getQName() + " missing localPart. Skipping.");
		}
		
		try
		{
			// PrefLabel
			Field prefLabelL = new Field(PREFLABEL_FIELD,
				concept.getPrefLabel(), Field.Store.YES,
				Field.Index.ANALYZED);
			prefLabelL.setBoost(1.5f);
			document.add(prefLabelL);
		} catch (Exception e) {
			logger.warn("Concept " + concept.getQName() + " missing prefLabel. Skipping.");
		}
		
		try
		{
			// altLabels
			String a = "";
			for (String s : concept.getAltLabels()) {
				a = a + " " + s;
			}
			Field altLabelL = new Field(ALTLABEL_FIELD, a.trim(), Field.Store.YES,
					Field.Index.ANALYZED);
			document.add(altLabelL);
		} catch (Exception e) {
			logger.warn("Error retrieving altLabel, may not exist in store. Skipping.");
		}
		

		try {
			writer.updateDocument(new Term(ID_FIELD, id), document);
			writer.commit();
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void removeConcept(QName qname) throws Exception 
	{
		logger.debug("removeConcept(): " + qname);
		String id = qname.getNamespaceURI() + qname.getLocalPart();
		writer.deleteDocuments(new Term(ID_FIELD, id));
			
		writer.commit();
	}

	/**
	 * Returns the total number of concepts in the Lucene index.
	 */
	@Override
	public long getNumConcepts() throws Exception {
		return IndexReader.open(lucenePath).numDocs();
	}
	
	public String getPath() {
		return lucenePath;
	}
}
