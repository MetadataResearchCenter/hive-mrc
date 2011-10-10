package org.unc.hive.server;

import java.io.File;
import java.net.URL;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.unc.ils.mrc.hive.api.ConceptNode;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;
import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;
import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;

import javax.xml.namespace.QName;

import org.unc.hive.client.*;

//jpb
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class VocabularyService {
	private static final Log logger = LogFactory.getLog(VocabularyService.class); //jpb
	private static VocabularyService instance = null;
	private SKOSServer skosServer;
	
	protected VocabularyService(String configFile) {
		this.skosServer = new SKOSServerImpl(configFile);
	}

	public static VocabularyService getInstance(String configFile) {
        if (instance == null) {
			instance = new VocabularyService(configFile);
		}
		return instance;
	}

	public VocabularyService(SKOSServer skosServer) {
		this.skosServer = skosServer;
		// this.server = new SKOSServerImpl("war/WEB-INF/conf/vocabularies");
	}

	public SKOSSearcher getSKOSSearcher() {
		return this.skosServer.getSKOSSearcher();
	}

	public long getNumberOfConcept(String vocabularyName) {
		return this.skosServer.getSKOSSchemas().get(vocabularyName)
				.getNumberOfConcepts();
	}

	public long getNumerOfRelations(String vocabularyName) {
		return this.skosServer.getSKOSSchemas().get(vocabularyName)
				.getNumberOfRelations();
	}

	public Date getLastUpdateDate(String vocabularyName) {
		return this.skosServer.getSKOSSchemas().get(vocabularyName)
				.getLastUpdateDate();
	}

	public List<List<String>> getAllVocabularies() {
		TreeMap<String, SKOSScheme> vocabularyMap = this.skosServer
				.getSKOSSchemas();
		List<List<String>> vocabularyList = new ArrayList<List<String>>();
		Set<String> vnames = vocabularyMap.keySet();
		Iterator<String> it = vnames.iterator();
		while (it.hasNext()) {
			SKOSScheme vocabulary = vocabularyMap.get(it.next());
			List<String> vocabularyInfo = new ArrayList<String>();
			vocabularyInfo.add(vocabulary.getName());
			vocabularyInfo.add(Long.toString(vocabulary
					.getNumberOfConcepts()));
			vocabularyInfo.add(Long.toString(vocabulary
					.getNumberOfRelations()));
			Date lastUpdate = vocabulary.getLastUpdateDate();
			DateFormat df = new SimpleDateFormat("MMM d, yyyy");
			String date = df.format(lastUpdate);
			vocabularyInfo.add(date);
			vocabularyList.add(vocabularyInfo);
		}
		return vocabularyList;
	}

	public List<String> getAllVocabularyNames() {
		TreeMap<String, SKOSScheme> vocabularyMap = this.skosServer
				.getSKOSSchemas();
		Set<String> keys = vocabularyMap.keySet();
		List<String> names = new ArrayList<String>();
		for (String key : keys) {
			names.add(key.toUpperCase());
		}
		
		return names;

	}
	
	public HashMap<String, HashMap<String,String>> getVocabularyProperties() {
		HashMap<String,HashMap<String,String>> props = new HashMap<String,HashMap<String,String>>();
		TreeMap<String, SKOSScheme> vocabularyMap = this.skosServer
				.getSKOSSchemas();
		Set<String> keys = vocabularyMap.keySet();
		HashMap<String,String> propVals;
		for (String key : keys) {
			propVals = new HashMap<String,String>();
			SKOSScheme vocabulary = vocabularyMap.get(key);
			propVals.put("uri", vocabulary.getSchemaURI());
			props.put(key,propVals);
		}
		return props;
	}	

	// public ConceptProxy getRandomizedConcept()
	// {
	// String namespaceURI;
	// String localPart;
	// return this.getConceptByURI(namespaceURI, localPart);
	// }

	
	public List<ConceptProxy> getSubTopConcept(String vocabulary, String letter, boolean brief) {
		TreeMap<String, SKOSScheme> vocabularies = this.skosServer
				.getSKOSSchemas();
		SKOSScheme targetVoc = vocabularies.get(vocabulary);

		List<SKOSConcept> top = targetVoc.getSubTopConceptIndex(letter);
		List<ConceptProxy> fatherList = new ArrayList<ConceptProxy>();
		for(SKOSConcept sc: top) {

			QName q = sc.getQName();
			boolean isleaf = sc.isLeaf();
			
			if (!brief) 
			{
				SKOSConcept concept = this.skosServer.getSKOSSearcher()
					.searchConceptByURI(q.getNamespaceURI(), q.getLocalPart());
				int numberOfChildren = concept.getNumberOfChildren();
				
				if (numberOfChildren == 0)
					isleaf = true;
			}
			String uri = q.getNamespaceURI();
			String localPart = q.getLocalPart();
			String URI = uri + " " + localPart;
			String prefLabel = sc.getPrefLabel();
			ConceptProxy father = new ConceptProxy(vocabulary, prefLabel, URI,
					isleaf);
			fatherList.add(father);
		}
		return fatherList;
	}
	

	/**
	 * @gwt.typeArgs <client.ConceptProxy>
	 * 
	 * */

	public List<ConceptProxy> getChildConcept(String nameSpaceURI,
			String localPart) {
		SKOSSearcher searcher = this.skosServer.getSKOSSearcher();
		TreeMap<String, QName> children = searcher.searchChildrenByURI(
				nameSpaceURI, localPart);
		List<ConceptProxy> childrenList = null;
		if (children.size() != 0) {
			childrenList = new ArrayList<ConceptProxy>();
			for (String cl : children.keySet()) {
				String origin = this.skosServer.getOrigin(children.get(cl));
				String preLabel = cl;
				String namespace = children.get(cl).getNamespaceURI();
				String lp = children.get(cl).getLocalPart();
				SKOSConcept concept = this.skosServer.getSKOSSearcher()
						.searchConceptByURI(namespace, localPart);
				int numberOfChildren = concept.getNumberOfChildren();
				boolean isleaf = true;
				if (numberOfChildren != 0)
					isleaf = false;
				String URI = namespace + " " + lp;
				ConceptProxy cpr = new ConceptProxy(origin, preLabel, URI,
						isleaf);
				childrenList.add(cpr);
			}
		}
		return childrenList;
	}

	/**
	 * @gwt.typeArgs <client.ConceptProxy>
	 * 
	 * */

	public List<ConceptProxy> searchConcept(String keyword,
			List<String> openedVocabularies) {

		// maintain the rank list
		SKOSSearcher searcher = this.skosServer.getSKOSSearcher();
		List<SKOSConcept> result = searcher.searchConceptByKeyword(keyword, true);
		List<ConceptProxy> rankedlist = new ArrayList<ConceptProxy>();
		for(String s : openedVocabularies) {
			System.out.println(s);
		}
		if (result.size() != 0) {
			for (SKOSConcept c : result) {
				String origin = skosServer.getOrigin(c.getQName());
				//System.out.println("POR AQUI PASO");
				//System.out.println("ORIGEN: " + origin);
				//System.out.println("SIZE: " + openedVocabularies.size());
				if (openedVocabularies.contains(origin.toLowerCase())) {
					String preLabel = c.getPrefLabel();
					QName qname = c.getQName();
					String namespace = qname.getNamespaceURI();
					String localPart = qname.getLocalPart();
					String uri = namespace + " " + localPart;
					//System.out.println("ESTO ES MI DEBUGGING: " + uri);
					ConceptProxy cp = new ConceptProxy(origin, preLabel, uri);
					//System.out.println("CONCEPT PROXI: " + cp.getOrigin());
					rankedlist.add(cp);
					
				}
			}

		}
		return rankedlist;
	}

	public ConceptProxy getConceptByURI(String namespaceURI, String localPart) {
		SKOSSearcher searcher = this.skosServer.getSKOSSearcher();
		SKOSConcept concept = searcher.searchConceptByURI(namespaceURI,
				localPart);
		String preLabel = concept.getPrefLabel();
		QName q = concept.getQName();
		String origin = skosServer.getOrigin(q);
		String uri = q.getNamespaceURI() + q.getLocalPart();
		String skosCode = concept.getSKOSFormat();
		List<String> altLabel = concept.getAltLabels();
		List<String> scopeNotes = concept.getScopeNote();

		TreeMap<String, QName> broader = concept.getBroaders();
		Iterator<String> it = broader.keySet().iterator();
		HashMap<String, String> broaders = new HashMap<String, String>();
		while (it.hasNext()) {
			String key = it.next();
			QName qq = broader.get(key);
			String URI = qq.getNamespaceURI();
			String lp = qq.getLocalPart();
			String value = URI + " " + lp;
			broaders.put(key, value);
		}
		TreeMap<String, QName> narrower = concept.getNarrowers();
		Iterator<String> itn = narrower.keySet().iterator();
		HashMap<String, String> narrowers = new HashMap<String, String>();
		while (itn.hasNext()) {
			String key = itn.next();
			QName qq = narrower.get(key);
			String URI = qq.getNamespaceURI();
			String lp = qq.getLocalPart();
			String value = URI + " " + lp;
			narrowers.put(key, value);
		}

		TreeMap<String, QName> related = concept.getRelated();
		Iterator<String> itr = related.keySet().iterator();
		HashMap<String, String> relateds = new HashMap<String, String>();
		while (itr.hasNext()) {
			String key = itr.next();
			QName qq = related.get(key);
			String URI = qq.getNamespaceURI();
			String lp = qq.getLocalPart();
			String value = URI + " " + lp;
			relateds.put(key, value);
		}

		ConceptProxy cp = new ConceptProxy(origin, preLabel, uri);
		if (broaders.isEmpty())
			broaders = null;
		if (narrowers.isEmpty())
			narrowers = null;
		if (relateds.isEmpty())
			relateds = null;
		if (altLabel.isEmpty())
			altLabel = null;
		if (scopeNotes.isEmpty())
			scopeNotes = null;
		cp.put(altLabel, broaders, narrowers, relateds, scopeNotes, skosCode);
		return cp;
	}
	
	/**
	 * @gwt.typeArgs <client.ConceptProxy>
	 * 
	 * */
	public List<ConceptProxy> getTags(String input, List<String> openedVocabularies, int numTerms)
	{
		SKOSTagger tagger = this.skosServer.getSKOSTagger();
		List<SKOSConcept> candidates = tagger.getTags(input, openedVocabularies,
				this.getSKOSSearcher(), numTerms);
		List<ConceptProxy> result = new ArrayList<ConceptProxy>(); 
		for(SKOSConcept concept : candidates)
		{
		  String preLabel = concept.getPrefLabel();
		  QName qname = concept.getQName();
		  String namespace = qname.getNamespaceURI();
		  String lp = qname.getLocalPart();
		  String uri = namespace + " " + lp;
		  double score = concept.getScore();
		  String origin = skosServer.getOrigin(qname);
		  ConceptProxy cp = new ConceptProxy(origin, preLabel, uri, score);
		  result.add(cp);
		}
		return result;
	}
	
	public List<ConceptProxy> getTags(String input, List<String> openedVocabularies, 
			int numTerms, int minPhraseOccur)
	{
		SKOSTagger tagger = this.skosServer.getSKOSTagger();
		List<SKOSConcept> candidates = tagger.getTagsFromText(input, openedVocabularies,
				this.getSKOSSearcher(), numTerms, minPhraseOccur);
		List<ConceptProxy> result = new ArrayList<ConceptProxy>(); 
		for(SKOSConcept concept : candidates)
		{
		  String preLabel = concept.getPrefLabel();
		  QName qname = concept.getQName();
		  String namespace = qname.getNamespaceURI();
		  String lp = qname.getLocalPart();
		  String uri = namespace + " " + lp;
		  double score = concept.getScore();
		  String origin = skosServer.getOrigin(qname);
		  ConceptProxy cp = new ConceptProxy(origin, preLabel, uri, score);
		  
		  Map<String, String> broaderMap = getStringMap(concept.getBroaders());
		  cp.setBroader(broaderMap);
		  
		  Map<String, String> narrowerMap = getStringMap(concept.getNarrowers());
		  cp.setNarrower(narrowerMap);
		  cp.setAltLabel(concept.getAltLabels());
		  result.add(cp);
		}
		return result;
	}
	
	public Map<String, String> getStringMap(Map<String, QName> qnameMap)
	{
		Map<String, String> stringMap = new HashMap<String, String>();
		for (String key: qnameMap.keySet()) {
			QName value = qnameMap.get(key);
			stringMap.put(key, value.getNamespaceURI()+value.getLocalPart());
		}
		return stringMap;
	}
	
	public List<ConceptProxy> getTags(URL url, List<String> openedVocabularies, int maxHops, int numTerms)
	{
		SKOSTagger tagger = this.skosServer.getSKOSTagger();
		List<SKOSConcept> candidates = tagger.getTags(url, openedVocabularies,this.getSKOSSearcher(), maxHops, numTerms);
		List<ConceptProxy> result = new ArrayList<ConceptProxy>(); 
		for(SKOSConcept concept : candidates)
		{
		  String preLabel = concept.getPrefLabel();
		  QName qname = concept.getQName();
		  String namespace = qname.getNamespaceURI();
		  String lp = qname.getLocalPart();
		  String uri = namespace + " " + lp;
		  double score = concept.getScore();
		  String origin = skosServer.getOrigin(qname);
		  ConceptProxy cp = new ConceptProxy(origin, preLabel, uri, score);
		  result.add(cp);
		}
		return result;
	}
	
	public List<ConceptNode> getTagsAsTree(String text, List<String> openedVocabularies, int maxHops, int numTerms)
	{
		SKOSTagger tagger = this.skosServer.getSKOSTagger();
		List<ConceptNode> tree = tagger.getTagsAsTree(text, openedVocabularies,this.getSKOSSearcher(), maxHops, numTerms);
		return tree;
	}
	
	public ConceptProxy getFirstConcept(String vocabulary)
	{
		TreeMap<String, SKOSScheme> vocabularyMap = this.skosServer.getSKOSSchemas();
	    SKOSScheme voc = vocabularyMap.get(vocabulary.toLowerCase());
	    List<SKOSConcept> top = voc.getSubTopConceptIndex("a");
	    QName value = top.get(0).getQName();
	    ConceptProxy cp = this.getConceptByURI(value.getNamespaceURI(), value.getLocalPart());
	    return cp;  
	}

	public List<AutocompleteTerm> suggestTermsFor(String vocabulary, String str, int numTerms) throws Exception
	{
		TreeMap<String, SKOSScheme> vocabularyMap = this.skosServer.getSKOSSchemas();
	    SKOSScheme voc = vocabularyMap.get(vocabulary.toLowerCase());
	    return voc.suggestTermsFor(str, numTerms);
	}
	
	public void close() {
		this.skosServer.close();
	}

	public static void main(String[] args) {
		VocabularyService service = VocabularyService
				.getInstance("war/WEB-INF/conf/hive.properties");
		System.out.println("Number of Concepts: " + service.getNumberOfConcept("mesh"));
		TreeMap<String, SKOSScheme> voc = service.skosServer.getSKOSSchemas();
		Set<String> set = voc.keySet();
		for (String s : set) {
			System.out.println("KEY: " + s);
			SKOSScheme sc = voc.get(s);
			System.out.println("NAME: " + sc.getName());
			System.out.println("LONG NAME: " + sc.getLongName());
			System.out.println("bvDATE: " + sc.getLastDate());
		}

//		/**
//		 * Search by keyword test
//		 */
//		System.out.println("Search by keyword:");
//		List<SKOSConcept> ranking = service.getSKOSSearcher().searchConceptByKeyword("syndrome");
//		System.out.println("Results in SKOSServer: " + ranking.size());
//		String uri = "";
//		String lp = "";
//		for (SKOSConcept c : ranking) {
//			System.out.println("PrefLabel: " + c.getPrefLabel());
//			uri = c.getQName().getNamespaceURI();
//			lp = c.getQName().getLocalPart();
//			System.out.println("\t URI: " + uri + " Local part: " + lp);
//			//QName qname = new QName(uri, lp);
//			//System.out.println("\t Origin: " + service.server.getOrigin(qname));
//		}
//		System.out.println();
		
//		System.out.println(service.getTags("/home/hive/Desktop/ag086e00.pdf",
//				"agrovoc") + " terminos extraidos");

		/*
		 * Trying the tree
		 */
		
		ConceptProxy c = service.getFirstConcept("nbii");
		System.out.println(c.getPreLabel());
		
		service.close();
	}

}