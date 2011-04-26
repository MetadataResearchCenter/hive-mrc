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

package edu.unc.ils.mrc.hive.ir.lucene.indexing;

import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

import org.openrdf.concepts.skos.core.Concept;

import edu.unc.ils.mrc.hive.ir.lucene.analysis.HIVEAnalyzer;

/**
 * Create or update a Lucene index of SKOS concepts. This class uses a document-oriented
 * approach to represent SKOS concepts in the inverted index, where each concept is 
 * represented as a document with multiple fields. Each field is an element in the SKOS
 * vocabulary (preferred term, broader term, scope notes, etc).
 */
public class ConceptIndexer implements Indexer 
{
    private static final Log logger = LogFactory.getLog(ConceptIndexer.class);
	
	private IndexWriter writer;

	/**
	 * Construct a ConceptIndexer in the specified directory.
	 * @param indexDir
	 * @param create
	 */
	public ConceptIndexer(String indexDir, boolean create) {
		try {
			
			this.writer = new IndexWriter(indexDir,
					new HIVEAnalyzer(), create,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (LockObtainFailedException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/**
	 * Index the specified Concept
	 */
	public void indexConcept(Concept concept) 
	{
		logger.trace("indexConcept " + concept.getQName());
		
		Document docLucene = new Document();

		String uri = concept.getQName().getNamespaceURI();
		Field uriL = new Field("uri", uri , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		docLucene.add(uriL);
		
		try
		{
			// Local Part
			String lp = concept.getQName().getLocalPart();
			Field lpL = new Field("localPart", lp , Field.Store.YES,
					Field.Index.NOT_ANALYZED);
			docLucene.add(lpL);
		} catch (Exception e) {
			logger.warn("Concept " + concept.getQName() + " missing localPart. Skipping.");
		}
		
		try
		{
			// PrefLabel
			Field prefLabelL = new Field("prefLabel",
				concept.getSkosPrefLabel(), Field.Store.YES,
				Field.Index.ANALYZED);
			prefLabelL.setBoost(1.5f);
			docLucene.add(prefLabelL);
		} catch (Exception e) {
			logger.warn("Concept " + concept.getQName() + " missing prefLabel. Skipping.");
		}

		try
		{
			// altLabels
			String a = "";
			for (String s : concept.getSkosAltLabels()) {
				a = a + " " + s;
			}
			Field altLabelL = new Field("altLabel", a.trim(), Field.Store.YES,
					Field.Index.ANALYZED);
			docLucene.add(altLabelL);
		} catch (Exception e) {
			logger.warn("Error retrieving altLabel, may not exist in store. Skipping.");
		}
		
		try
		{
			// Scope Notes
			String a = "";
			String sc = "";
			for (Object s : concept.getSkosScopeNotes()) {
				a = a + " " + s;
			}
			Field scopeNoteL = new Field("scopeNote",
					sc, Field.Store.YES,
					Field.Index.ANALYZED);
			docLucene.add(scopeNoteL);
		} catch (Exception e) {
			logger.warn("Error retrieving scope note, may not exist in store. Skipping.");
		}
		
		try
		{
			// Broader Terms
			String b = "";
			for (Concept c : concept.getSkosBroaders()) {
				if (b.equals(""))
					b = c.getSkosPrefLabel();
				else
					b = b + "#" + c.getSkosPrefLabel();
			}
			Field broaderL = new Field("broader", b.trim(), Field.Store.YES,
					Field.Index.ANALYZED);
			docLucene.add(broaderL);
	
			// Broader URIs
			String buri = "";
			for (Concept c : concept.getSkosBroaders()) {
				buri = buri + " " + c.getQName().getNamespaceURI() + c.getQName().getLocalPart();
			}
			Field broaderURIL = new Field("broaderURI", buri.trim(),
					Field.Store.YES, Field.Index.NOT_ANALYZED);
			docLucene.add(broaderURIL);
			
		} catch (ClassCastException e) {
			// If the broader concept does not exist, a ClassCastException is thrown. 
			// This can prevent the file from being indexed. Warn and continue.			
			logger.warn("Error retrieving broader concept, may not exist in store. Skipping.");
		}			

		try
		{
			// Narrower Terms
			String n = "";
			for (Concept c : concept.getSkosNarrowers()) {
				//logger.debug("Narrower: " + c.getQName().getNamespaceURI() + " lp: " + c.getQName().getLocalPart());
				if (n.equals(""))
					n = c.getSkosPrefLabel();
				else
					n = n + "#" + c;
			}
			Field narrowerL = new Field("narrower", n.trim(), Field.Store.YES,
					Field.Index.ANALYZED);
			docLucene.add(narrowerL);
	
			// Narrower URIs
			String nuri = "";
			for (Concept c : concept.getSkosNarrowers()) {
				nuri = nuri + " " + c.getQName().getNamespaceURI() + c.getQName().getLocalPart();
			}
			Field narrowerURIL = new Field("narrowerURI", nuri.trim(),
					Field.Store.YES, Field.Index.NOT_ANALYZED);			
			docLucene.add(narrowerURIL);			
		} catch (ClassCastException e) {
			// If the narrower concept does not exist, a ClassCastException is thrown. 
			// This can prevent the file from being indexed. Warn and continue.			
			logger.warn("Error retrieving narrower concept, may not exist in store. Skipping.");
		}

		// Related Terms
		String r = "";
		try
		{
			for (Concept c : concept.getSkosRelated()) {
				if (r.equals(""))
					r = c.getSkosPrefLabel();
				else
					r = r + "#" + c.getSkosRelated();
			}
			Field relatedL = new Field("related", r.trim(), Field.Store.YES,
					Field.Index.ANALYZED);			
			docLucene.add(relatedL);
			
			// Related URIs
			String ruri = "";
			for (Concept c : concept.getSkosRelated()) {
				ruri = ruri + " " + c.getQName().getNamespaceURI() + c.getQName().getLocalPart();
			}
			Field relatedURIL = new Field("relatedURI", ruri.trim(),
					Field.Store.YES, Field.Index.NOT_ANALYZED);
			docLucene.add(relatedURIL);

		} catch (ClassCastException e) {
			// If the related concept does not exist, a ClassCastException is thrown. 
			// This can prevent the file from being indexed. Warn and continue.			
			logger.warn("Error retrieving related concept, may not exist in store. Skipping.");			
		}
		
		// titleField.setBoost(4);
		// categoryField.setBoost(2);

		try {
			writer.addDocument(docLucene);
			logger.debug("Indexed: " + concept.getSkosPrefLabel() + " " + uri);
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/**
	 * Close the index
	 */
	public void close() {
	    logger.trace("close");
	    
		try {
			this.writer.close();
		} catch (CorruptIndexException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
