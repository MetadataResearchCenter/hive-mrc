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
 */

package edu.unc.ils.mrc.hive.ir.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.TopDocCollector;
import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.sesame.SesameManager;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSConceptImpl;

/*
 * This class load Lucene indexes and create a Multisearcher with them
 */

public class ConceptMultiSearcher implements Searcher {

	private static int NUMBER_RESULTS = 1500;

	private Searchable[] searchers;
	private MultiSearcher searcher;

	public ConceptMultiSearcher(String[] indexes) {
		this.searchers = new IndexSearcher[indexes.length];
		this.initIndex(indexes);
	}

	@Override
	public List<SKOSConcept> search(String word, SesameManager[] managers) {
		String[] fields = { "prefLabel", "altLabel" };
		MultiFieldQueryParser parser = new MultiFieldQueryParser(fields,
				new StandardAnalyzer());

		List<Concept> ranking = new ArrayList<Concept>();

		try {
			Query query = parser.parse(word);
			TopDocCollector collector = new TopDocCollector(NUMBER_RESULTS);
			this.searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Numero total de resultados: "
					+ collector.getTotalHits());
			for (int i = 0; i < hits.length; i++) {
				Concept concept;
				int docId = hits[i].doc;
				Document doc = searcher.doc(docId);
				String uri = doc.get("uri");
				String lp = doc.get("localPart");
				for (int n = 0; n < managers.length; n++) {
					concept = managers[n].find(Concept.class,
							new QName(uri, lp));
					if (concept != null)
						ranking.add(concept);
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<SKOSConcept> skosConceptList = new ArrayList<SKOSConcept>();

		for (Concept elmoConcept : ranking) {
			SKOSConcept sconcept = new SKOSConceptImpl(elmoConcept.getQName());
			sconcept.setPrefLabel(elmoConcept.getSkosPrefLabel());
			Set<String> altSet = elmoConcept.getSkosAltLabels();
			for (String alt : altSet) {
				sconcept.addAltLabel(alt);
			}
			Set<Concept> broaderSet = elmoConcept.getSkosBroaders();
			for (Concept broader : broaderSet) {
				sconcept.addBroader(broader.getSkosPrefLabel(), broader
						.getQName());
			}
			Set<Concept> narrowerSet = elmoConcept.getSkosNarrowers();
			for (Concept narrower : narrowerSet) {
				sconcept.addNarrower(narrower.getSkosPrefLabel(), narrower
						.getQName());
			}
			Set<Concept> relatedSet = elmoConcept.getSkosRelated();
			for (Concept related : relatedSet) {
				sconcept.addRelated(related.getSkosPrefLabel(), related
						.getQName());
			}

			Set<Object> scopeNotes = elmoConcept.getSkosScopeNotes();
			for (Object scopeNote : scopeNotes) {
				sconcept.addScopeNote((String)scopeNote);
			}

			skosConceptList.add(sconcept);
		}

		return skosConceptList;
	}

	private void initIndex(String[] indexList) {
		try {
			for (int i = 0; i < indexList.length; i++)
				this.searchers[i] = new IndexSearcher(indexList[i]);
			this.searcher = new MultiSearcher(this.searchers);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void close() {
		try {
			this.searcher.close();
			for (Searchable s : this.searchers)
				s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
