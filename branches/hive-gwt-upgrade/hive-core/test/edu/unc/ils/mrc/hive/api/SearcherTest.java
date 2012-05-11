package edu.unc.ils.mrc.hive.api;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Value;

import com.aliasi.io.FileExtensionFilter;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;

/**
 * Simple command line tagger. Given a directory of PDF files, generates text files
 * with keyphrases from the specified vocabulary.
 */
public class SearcherTest 
{
	public static void main(String[] args) 
	{
		// Path to hive.properties
		String confPath = args[0];

		// Vocabulary name
		String vocabulary = args[2];

		List<String> vocabularies = new ArrayList<String>();
		vocabularies.add(vocabulary);
		
		SKOSServer server = new SKOSServerImpl(confPath);
		SKOSSearcher searcher = server.getSKOSSearcher();
		
		
	    //searcher.searchAlpha("a");
	    //searcher.searchAlpha("b");
	    //searcher.searchAlpha("c");
		
		
		//List<SKOSConcept> concepts = searcher.searchConceptByKeyword("mammals");
		//for(SKOSConcept concept: concepts) {
		//	System.out.println(concept.getPrefLabel());
		//}
		
		// 	PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?uri ?label
		//  WHERE { <http://thesaurus.nbii.gov/nbii#Mud> skos:broader ?uri . ?uri skos:prefLabel ?label}"
		List<HashMap>results = searcher.SPARQLSelect(
				  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " + 
				  "SELECT ?uri ?label WHERE { <http://id.loc.gov/authorities/sh85116169#concept> " + 
				  "skos:broader ?uri . ?uri skos:prefLabel ?label}",
				  "lcsh");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		
		
		results = searcher.SPARQLSelect(
				  "SELECT ?s ?p ?o WHERE {<http://id.loc.gov/authorities/sh85116169#concept> ?p ?o} LIMIT 10", 
				  "lcsh");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		
		results = searcher.SPARQLSelect(
				  //"SELECT ?s ?p ?o WHERE {?s ?p <http://id.loc.gov/authorities/sh85116169#concept>} LIMIT 10", 
				  //"lcsh");
						  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " + 
						  "DELETE { <http://id.loc.gov/authorities/sh85116169#concept> ?p ?o } ",
						  "lcsh");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		
		results = searcher.SPARQLSelect(
				  "SELECT ?s ?p ?o WHERE {<http://id.loc.gov/authorities/sh85116169#concept> ?p ?o} LIMIT 10", 
				  "lcsh");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		
		/*
		results = searcher.SPARQLSelect(
				  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " + 
				  "SELECT ?s ?p ?o WHERE { " + 
				  	"{?s skos:narrower  <http://id.loc.gov/authorities/sh85116169#concept>} " +
				  	" UNION {?s skos:broader <http://id.loc.gov/authorities/sh85116169#concept>} " +
				  	" UNION {?s skos:related  <http://id.loc.gov/authorities/sh85116169#concept>} " +
				  "}",
				  "lcsh");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		*/
		
		/*
		List<HashMap> results = searcher.SPARQLSelect(
				  "SELECT ?s ?p ?p WHERE {?s ?p ?o} LIMIT 10", 
				  "lcsh");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}

		// PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?s ?p ?o WHERE {  ?s ?p ?o . ?s skos:prefLabel "Damage" .}
	    results = searcher.SPARQLSelect(
				  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
				  "SELECT ?s ?p ?o WHERE {  ?s ?p ?o . ?s skos:prefLabel \"Damage\" .}",
				  "nbii");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		
		// 	PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?uri ?label WHERE { <http://thesaurus.nbii.gov/nbii#Mud> skos:broader ?uri . ?uri skos:prefLabel ?label}",
		results = searcher.SPARQLSelect(
				  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " + 
				  "SELECT ?uri ?label WHERE { <http://thesaurus.nbii.gov/nbii#Mud> " + 
				  "skos:broader ?uri . ?uri skos:prefLabel ?label}",
				  "nbii");
		for (HashMap map: results) {
			Set<String> keys = map.keySet();
			for(String key: keys) {
				Value val = (Value)map.get(key);
				System.out.println(key + ":" + val);
			}
		}
		*/
	}
}
