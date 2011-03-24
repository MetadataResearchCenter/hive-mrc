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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.Searcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.SearcherFactory;

/*
 * This class load Sesame Repositories and use a Searcher to retrieve from Lucene indexes
 */

public class SKOSSearcherImpl implements SKOSSearcher {

    private static final Log logger = LogFactory.getLog(SKOSSearcherImpl.class);
    
	private Map<String, SKOSScheme> vocabularies;
	private String[] indexes;
	private Repository[] repositories;
	private SesameManagerFactory[] factories;
	private SesameManager[] managers;
	private NativeStore stores[];
	private File files[];
	private Searcher searcher;

	public SKOSSearcherImpl(Map<String, SKOSScheme> vocabularies) {
		this.vocabularies = vocabularies;
		this.repositories = new Repository[this.vocabularies.size()];
		this.factories = new SesameManagerFactory[this.vocabularies.size()];
		this.managers = new SesameManager[this.vocabularies.size()];
		this.stores = new NativeStore[this.vocabularies.size()];
		this.files = new File[this.vocabularies.size()];
		this.indexes = new String[this.vocabularies.size()];
		Set<String> keys = this.vocabularies.keySet();
		int i = 0;
		try {
			for (String schemeName : keys) {
				this.files[i] = new File(this.vocabularies.get(schemeName)
						.getStoreDirectory());
				logger.debug(this.files[i].getAbsolutePath());
				this.stores[i] = new NativeStore(this.files[i]);
				// create repository
				repositories[i] = new SailRepository(stores[i]);
				repositories[i].initialize();
				ElmoModule module = new ElmoModule();
				factories[i] = new SesameManagerFactory(module, repositories[i]);
				this.managers[i] = factories[i].createElmoManager();
				// this.managers[i].setLocale(Locale.ENGLISH);
				this.indexes[i] = this.vocabularies.get(schemeName)
						.getIndexDirectory();
				this.vocabularies.get(schemeName).setManager(this.managers[i]);
				i++;
			}
		} catch (RepositoryException e) {
			logger.error(e);
		}

		SearcherFactory
				.selectSearcher(SearcherFactory.BASICLUCENECONCEPTSEARCHER);
		this.searcher = SearcherFactory.getSearcher(this.indexes);
	}

	@Override
	public List<SKOSConcept> searchConceptByKeyword(String keyword) {
	    logger.trace("searchConceptByKeyword " + keyword);
	    
		// Retrieve a concept from lucene indexes
		List<SKOSConcept> ranking = searcher.search(keyword, this.managers);
		return ranking;
	}

	@Override
	public SKOSConcept searchConceptByURI(String uri, String lp) {
	    logger.trace("searchConceptByURI " + uri + "," + lp);
	    
		Concept elmoConcept = null;
		QName qName = new QName(uri, lp);
		for (int n = 0; n < managers.length; n++) {
			elmoConcept = managers[n].find(Concept.class, qName);
			if (elmoConcept != null) {
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

				return sconcept;
			}
		}
		return null;
	}

	public TreeMap<String,QName> searchChildrenByURI(String uri, String lp) {
	    logger.trace("searchChildrenByURI " + uri + "," + lp);
	    
		Concept elmoConcept = null;
		QName qName = new QName(uri, lp);
		
		for (int n = 0; n < managers.length; n++) {
			elmoConcept = managers[n].find(Concept.class, qName);
			if (elmoConcept != null) {
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

				return sconcept.getNarrowers();
			}
		}
		return null;
	}
	
	public List<HashMap> SPARQLSelect(String qs, String vocabulary) {
	    logger.trace("SPARQLSelect " + qs + "," + vocabulary);
	    
		try {
			List<String> voc = new ArrayList<String>();
			voc.addAll(this.vocabularies.keySet());
			int i = voc.indexOf(vocabulary.toLowerCase());
			RepositoryConnection con = this.repositories[i].getConnection();
			try {
				TupleQuery query = con.prepareTupleQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
				TupleQueryResult qres = query.evaluate();
				List<HashMap> reslist = new ArrayList<HashMap>();
				while(qres.hasNext()) {
					BindingSet b = qres.next();
					Set<String> names = b.getBindingNames();
					HashMap hm = new HashMap<String, Value>();
					for(String n : names) {
						hm.put(n, b.getValue(n));
					}
					reslist.add(hm);
				}
				return reslist;
			} catch (MalformedQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				con.close();
			}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException a) {
			System.err.println("The name of the vocabulary has not been recognized");
		}
		
		return null;
	}

	public void close() {
	    logger.trace("close");
	    
		for (int i = 0; i < this.managers.length; i++) {
			this.managers[i].close();
			logger.debug("Manager " + i + " closed OK");
			this.factories[i].close();
			logger.debug("Factory " + i + " closed OK");
			try {
				this.repositories[i].shutDown();
				logger.debug("Repository " + i + " closed OK");
			} catch (RepositoryException e) {
				logger.error(e);
			}
		}
		this.searcher.close();
		logger.debug("Indexes closed OK");
	}

}
