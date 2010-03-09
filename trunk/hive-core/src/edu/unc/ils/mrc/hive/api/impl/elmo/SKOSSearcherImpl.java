package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;

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
				System.out.println(this.files[i].getAbsolutePath());
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
		} catch (RepositoryException r) {
			r.printStackTrace();
		}

		SearcherFactory
				.selectSearcher(SearcherFactory.BASICLUCENECONCEPTSEARCHER);
		this.searcher = SearcherFactory.getSearcher(this.indexes);
	}

	@Override
	public List<SKOSConcept> searchConceptByKeyword(String keyword) {
		// Retrieve a concept from lucene indexes
		List<SKOSConcept> ranking = searcher.search(keyword, this.managers);
		return ranking;
	}

	@Override
	public SKOSConcept searchConceptByURI(String uri, String lp) {
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
		for (int i = 0; i < this.managers.length; i++) {
			this.managers[i].close();
			System.out.println("Manager " + i + " closed OK");
			this.factories[i].close();
			System.out.println("Factory " + i + " closed OK");
			try {
				this.repositories[i].shutDown();
				System.out.println("Repository " + i + " closed OK");
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.searcher.close();
		System.out.println("Indexes closed OK");
	}

}
