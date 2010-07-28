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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.Entity;


public class ConceptIndexer implements Indexer {

	private IndexWriter writer;

	public ConceptIndexer(String indexDir, boolean create) {
		try {
			this.writer = new IndexWriter(indexDir,
					new StandardAnalyzer(), create,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void indexConcept(Concept concept) {
		Document docLucene = new Document();

		// URI
		String uri = concept.getQName().getNamespaceURI();
		Field uriL = new Field("uri", uri , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		
		// Local Part
		String lp = concept.getQName().getLocalPart();
		Field lpL = new Field("localPart", lp , Field.Store.YES,
				Field.Index.NOT_ANALYZED);
		
		// PrefLabel
		Field prefLabelL = new Field("prefLabel",
				concept.getSkosPrefLabel(), Field.Store.YES,
				Field.Index.ANALYZED);

		// altLabels
		String a = "";
		for (String s : concept.getSkosAltLabels()) {
			a = a + " " + s;
		}
		Field altLabelL = new Field("altLabel", a.trim(), Field.Store.YES,
				Field.Index.ANALYZED);

		// Scope Notes
		String sc = "";
		for (Object s : concept.getSkosScopeNotes()) {
			a = a + " " + s;
		}
		Field scopeNoteL = new Field("scopeNote",
				sc, Field.Store.YES,
				Field.Index.ANALYZED);

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

		// Broader URIs
		String buri = "";
		for (Concept c : concept.getSkosBroaders()) {
			buri = buri + " " + c.getQName().getNamespaceURI() + c.getQName().getLocalPart();
		}
		Field broaderURIL = new Field("broaderURI", buri.trim(),
				Field.Store.YES, Field.Index.NOT_ANALYZED);

		// Narrower Terms
		String n = "";
		for (Concept c : concept.getSkosNarrowers()) {
			//System.out.println("CULO: " + c.getQName().getNamespaceURI() + " lp: " + c.getQName().getLocalPart());
			if (n.equals(""))
				n = c.getSkosPrefLabel();
			else
				n = n + "#" + c;
		}
		Field narrowerL = new Field("narrower", n.trim(), Field.Store.YES,
				Field.Index.ANALYZED);

		// Narrower URIs
		String nuri = "";
		for (Concept c : concept.getSkosNarrowers()) {
			nuri = nuri + " " + c.getQName().getNamespaceURI() + c.getQName().getLocalPart();
		}
		Field narrowerURIL = new Field("narrowerURI", nuri.trim(),
				Field.Store.YES, Field.Index.NOT_ANALYZED);

		// Related Terms
		String r = "";
		for (Concept c : concept.getSkosRelated()) {
			if (r.equals(""))
				r = c.getSkosPrefLabel();
			else
				r = r + "#" + c.getSkosRelated();
		}
		Field relatedL = new Field("related", r.trim(), Field.Store.YES,
				Field.Index.ANALYZED);

		// Related URIs
		String ruri = "";
		for (Concept c : concept.getSkosRelated()) {
			ruri = ruri + " " + c.getQName().getNamespaceURI() + c.getQName().getLocalPart();
		}
		Field relatedURIL = new Field("relatedURI", ruri.trim(),
				Field.Store.YES, Field.Index.NOT_ANALYZED);

		// titleField.setBoost(4);
		// categoryField.setBoost(2);
		docLucene.add(uriL);
		docLucene.add(lpL);
		docLucene.add(prefLabelL);
		docLucene.add(altLabelL);
		docLucene.add(scopeNoteL);
		docLucene.add(broaderL);
		docLucene.add(narrowerL);
		docLucene.add(relatedL);
		docLucene.add(broaderURIL);
		docLucene.add(narrowerURIL);
		docLucene.add(relatedURIL);
		try {
			writer.addDocument(docLucene);
			System.out.println(concept.getSkosPrefLabel() + " " + uri + " has been indexed");
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void close() {
		try {
			this.writer.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
