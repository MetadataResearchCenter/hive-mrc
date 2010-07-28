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

package edu.unc.ils.mrc.hive.ir.tagging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp.Dictionary;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp.Postagger;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking.Rankeable;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking.Ranking;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Coleccion;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Documento;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Termino;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Vocabulario;

public class DummyTagger implements Tagger {
	
	private String model;
	private Postagger postagger;

	
	public DummyTagger(String dirName, String modelName, String stopwordsPath, SKOSScheme schema) {
		this.model = modelName;
		try {
			postagger = new Postagger(this.model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<String> extractKeyphrases(String text) {
		List<String> keywords = new ArrayList<String>();
		// TODO Auto-generated method stub
		Analyzer analyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		IndexWriter w;
		try {
			w = new IndexWriter(index, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED);
			Document doc = new Document();
			doc.add(new Field("text", text, Field.Store.YES,
					Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
			w.addDocument(doc);
			text = text.trim();
			
			Dictionary dic = this.postagger.tagText(text);

			w.close();

			IndexSearcher searcher = new IndexSearcher(index);
			IndexReader reader = searcher.getIndexReader();
			//System.out.println("Number of Documents: " + reader.maxDoc());
			Coleccion collec = new Coleccion();
			Vocabulario vocabulario = new Vocabulario();
			for (int n = 0; n <= reader.maxDoc() - 1; n++) {
				Document Ldoc = reader.document(n);
				// System.out.println("----------NEW DOCUMENT----"+ n +
				// "-------------");
				TermFreqVector vector = reader.getTermFreqVector(n, "text");
				// System.out.println(vector);
				int[] freq = vector.getTermFrequencies();
				String[] terms = vector.getTerms();
				Documento documento = new Documento();
				documento.setName(Ldoc.get("title"));
				for (int i = 0; i < freq.length; i++) {
					double f = freq[i];
					double t = freq.length;
					double prob = f / t;
					documento.addTerm(terms[i], prob, freq[i]);
					vocabulario.addTerm(terms[i], freq[i]);
				}
				collec.addDocumento(documento);
			}
			vocabulario.calculaProbabilidades();
			collec.calculaDivergencias(vocabulario);
			Ranking ranking = new Ranking();
			for (Documento d : collec.getDocumentos()) {
				for (Termino t : d.getTerminos()) {
					if (t.getTf() > 0.1
							&& dic.isAllowed(t.getTermino())
							&& t.getTermino().length() > 1) {
						ranking.addValor(t);
					}
				}
				for(Rankeable term : ranking.getRanking()) {
					Termino te = (Termino) term;
					keywords.add(te.getTermino());
				}
				//ranking = new Ranking();
			}
			index.close();
			reader.close();
			searcher.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return keywords;
	}

	@Override
	public String getVocabulary() {
		return "Dummytagger";
	}

	@Override
	public void extractKeyphrases() {
		// TODO Auto-generated method stub
		
	}
	
}
