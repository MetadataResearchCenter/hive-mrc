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

package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;

public class SKOSServerImpl implements SKOSServer {
	private static Logger log = Logger.getLogger(SKOSServerImpl.class);

	private SKOSSearcher searcher;
	private SKOSTagger tagger;
	private TreeMap<String, SKOSScheme> schemes;

	public SKOSServerImpl(String configFile) {
		List<String> vocabularies = new ArrayList<String>();

		File file = new File(configFile);
		String path = file.getPath().replaceAll("vocabularies", "");

		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			while (line != null) {
				vocabularies.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.schemes = new TreeMap<String, SKOSScheme>();

		for (String voc : vocabularies) {
			SKOSScheme schema = new SKOSSchemeImpl(path, voc, false);
			this.schemes.put(voc, schema);
		}

		this.searcher = new SKOSSearcherImpl(this.schemes);
		this.tagger = new SKOSTaggerImpl(this.schemes, "kea");//kea or dummy

	}

	public SKOSTagger getSKOSTagger() {
		return this.tagger;
	}

	@Override
	public TreeMap<String, SKOSScheme> getSKOSSchemas() {
		return this.schemes;
	}

	@Override
	public SKOSSearcher getSKOSSearcher() {
		return this.searcher;
	}

	@Override
	public String getOrigin(QName uri) {
		Collection<SKOSScheme> values = this.schemes.values();
		try {
			URI myuri = new URI(uri.getNamespaceURI());
			for (SKOSScheme s : values) {
				if (s.getSchemaURI().contains(myuri.getHost())) {
					return s.getName();
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public void close() {
		this.searcher.close();
	}

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();
		
		log.debug("starting SKOSServerImpl");
		// Levanto el servidor de vocabularios
		SKOSServer server = new SKOSServerImpl(args[0]);
		// Le pido un Searcher
		SKOSSearcher searcher = server.getSKOSSearcher();

		/**
		 * Statistics test
		 */

		TreeMap<String, SKOSScheme> vocabularies = server.getSKOSSchemas();
		Set<String> keys = vocabularies.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			SKOSScheme voc = vocabularies.get(it.next());
			System.out.println("NAME: " + voc.getName());
			System.out.println("\t LONG NAME: " + voc.getLongName());
			System.out.println("\t NUMBER OF CONCEPTS: "
					+ voc.getNumberOfConcepts());
			System.out.println("\t NUMBER OF RELATIONS: "
					+ voc.getNumberOfRelations());
			System.out.println("\t DATE: " + voc.getLastDate());
			System.out.println();
			System.out.println("\t SIZE: " + voc.getSubAlphaIndex("a").size());
			System.out.println();
			System.out.println("\t TOP CONCEPTS: "
					+ voc.getTopConceptIndex().size());
		}

		// /**
		// * Search by keyword test
		// */
		// System.out.println("Search by keyword:");
		// List<SKOSConcept> ranking = searcher
		// .searchConceptByKeyword("accidents");
		// System.out.println("Results in SKOSServer: " + ranking.size());
		// String uri = "";
		// String lp = "";
		// for (SKOSConcept c : ranking) {
		// uri = c.getQName().getNamespaceURI();
		// lp = c.getQName().getLocalPart();
		// QName qname = new QName(uri, lp);
		// String origin = server.getOrigin(qname);
		// if (origin.toLowerCase().equals("nbii")) {
		// System.out.println("PrefLabel: " + c.getPrefLabel());
		// System.out.println("\t URI: " + uri + " Local part: " + lp);
		// System.out.println("\t Origin: " + server.getOrigin(qname));
		// }
		// }
		// System.out.println();

		// /**
		// * Search by URI test
		// */

		// System.out.println("Search by URI:");
		// SKOSConcept c2 = searcher.searchConceptByURI(
		// "http://thesaurus.nbii.gov/nbii#", "Enzymatic-activity");
		// // Concept c2 = searcher.searchConceptByURI(uri, lp);//TODO Cuando no
		// // hay resultados esto explota, controlar excepcion
		// List<String> alt = c2.getAltLabels();
		// System.out.println("PrefLabel: " + c2.getPrefLabel());
		// for (String a : alt) {
		// System.out.println("\t altLabel: " + a);
		// }
		// //System.out.println("\t Origin: " + server.getOrigin(c2));
		// System.out.println("\t SKOS Format: \n" + c2.getSKOSFormat());

		// /**
		// * SKOS tagger test
		// */
		//
		// SKOSTagger tagger = server.getSKOSTagger();
		//
		// String source = "/home/hive/Desktop/ag086e00.pdf";
		// source = "http://en.wikipedia.org/wiki/Biology";
		//		
		// List<String> vocabs = new ArrayList<String>();
		// vocabs.add("nbii");
		// vocabs.add("lcsh");
		// vocabs.add("agrovoc");
		//
		// List<SKOSConcept> l = tagger.getTags(source, vocabs,
		// server.getSKOSSearcher());
		// System.out.println();
		// System.out.println("Tagging Results for ALL");
		// for (SKOSConcept s : l) {
		// System.out.println(s.getPrefLabel());
		// //System.out.println(s.getQName().getNamespaceURI());
		// }
		//
		// System.out.println();
		// System.out
		// .println("-----------------------------------------------------------------");
		//
		// /**
		// * Get Children by URI test
		// */
		//
		// vocabularies = server.getSKOSSchemas();
		// keys = vocabularies.keySet();
		// it = keys.iterator();
		// while (it.hasNext()) {
		// SKOSScheme voc = vocabularies.get(it.next());
		// int n = voc.getSubTopConceptIndex("x").size();
		// int a = voc.getSubAlphaIndex("x").size();
		// System.out.println("Vocabulary: " + voc.getLongName());
		// System.out.println("\t Size for X in Top Concept Index: " + n);
		// System.out.println("\t Size for X in Alpha Index: " + a);
		// for (String g : voc.getSubTopConceptIndex("x").keySet()) {
		// System.out.println("lina go home: " + g);
		// }
		// File outputTAX = new File("/home/hive/taxonomySESAME"
		// + voc.getName());
		// FileOutputStream fos = new FileOutputStream(outputTAX);
		// PrintWriter pr = new PrintWriter(fos);
		// TreeMap<String, QName> top = voc.getTopConceptIndex();
		// Set<String> topConcepts = top.keySet();
		// Iterator<String> it2 = topConcepts.iterator();
		// while (it2.hasNext()) {
		// String term = it2.next();
		// TreeMap<String, QName> children = searcher.searchChildrenByURI(
		// top.get(term).getNamespaceURI(), top.get(term)
		// .getLocalPart());
		// pr.println(term);
		// for (String c : children.keySet()) {
		// String term2 = c;
		// pr.println("\t" + term2);
		// TreeMap<String, QName> ch = searcher.searchChildrenByURI(
		// children.get(term2).getNamespaceURI(),
		// children.get(term2).getLocalPart());
		// for (String c3 : ch.keySet()) {
		// pr.println("\t \t" + c3);
		// }
		// }
		// }
		// pr.close();
		// fos.close();
		// }
		//
		// System.out.println();
		// System.out
		// .println("Children for http://id.loc.gov/authorities/sh2001009743#concept");
		// SKOSConcept con = searcher.searchConceptByURI(
		// "http://id.loc.gov/authorities/sh2001009743#", "concept");
		// System.out.println(con.getPrefLabel());
		// TreeMap<String,QName> children = searcher.searchChildrenByURI(
		// "http://id.loc.gov/authorities/sh2001009743#", "concept");
		// for (String c : children.keySet()) {
		// System.out.println("prefLabel: " + c);
		// }
		// System.out.println();

		/*
		 * SPARQL test
		 */

		List solutions1 = searcher.SPARQLSelect(
				"SELECT ?s ?p ?p WHERE {?s ?p ?o} LIMIT 10", "nbii");
		List solutions2 = searcher
				.SPARQLSelect(
						"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?s ?p ?o WHERE {  ?s ?p ?o . ?s skos:prefLabel \"Damage\" .}",
						"nbii");
		List solutions3 = searcher
				.SPARQLSelect(
						"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?uri ?label WHERE { <http://thesaurus.nbii.gov/nbii#Mud> skos:broader ?uri . ?uri skos:prefLabel ?label}",
						"nbii");
		if (solutions1 != null)
			System.out.println("SOLUTIONS 1: " + solutions1.toString());
		if (solutions2 != null)
			System.out.println("SOLUTIONS 2: " + solutions2.toString());
		if (solutions3 != null)
			System.out.println("SOLUTIONS 3: " + solutions3.toString());

		// Closing the server
		server.close();
	}
}
