package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSTagger;
import edu.unc.ils.mrc.hive.ir.tagging.Tagger;
import edu.unc.ils.mrc.hive.ir.tagging.TaggerFactory;
import edu.unc.ils.mrc.hive.util.TextManager;

/*
 * This class just normalize terms using the thesaurus
 */

public class SKOSTaggerImpl implements SKOSTagger {

	private static final int LIMIT = 10;

	private TreeMap<String, Tagger> taggers;
	private TreeMap<String, SKOSScheme> vocabularies;
	public String algorithm;

	public SKOSTaggerImpl(TreeMap<String, SKOSScheme> vocabularies,
			String algorithm) {
		this.algorithm = algorithm;
		this.vocabularies = vocabularies;
		this.taggers = new TreeMap<String, Tagger>();
		Set<String> set = vocabularies.keySet();
		Iterator<String> it = set.iterator();
		if (this.algorithm.equals("kea")) {
			while (it.hasNext()) {
				String vocName = it.next();
				SKOSScheme schema = vocabularies.get(vocName);
				TaggerFactory.selectTagger(2);
				Tagger tagger = TaggerFactory.getTagger(schema
						.getKEAtestSetDir(), schema.getKEAModelPath(), schema
						.getStopwordsPath(), schema);
				this.taggers.put(vocName, tagger);
			}
		} else if (this.algorithm.equals("dummy")) {
			SKOSScheme schema = vocabularies.get(vocabularies.firstKey());
			TaggerFactory.selectTagger(1);
			Tagger tagger = TaggerFactory.getTagger("", schema
					.getLingpipeModel(), "", null);
			this.taggers.put("Dummytagger", tagger);
		} else {
			System.out.println(this.algorithm + " algorithm is not suported");
		}
		System.out.println("NUMBER OF TAGGERS: " + this.taggers.size());
		for (Tagger tag : this.taggers.values()) {
			System.out.println("Tagger: " + tag.getVocabulary());
		}
	}

	public List<SKOSConcept> getTags(String source, List<String> vocabulary,
			SKOSSearcher searcher) {
		TextManager tm = new TextManager();
		String text = tm.getPlainText(source);
		List<SKOSConcept> result = new ArrayList<SKOSConcept>();
		if (this.algorithm.equals("kea")) {
			Date date = new Date();
			for (String voc : vocabulary) {
				String path = this.vocabularies.get(voc).getKEAtestSetDir();
				String fileName = path + "/" + date.getTime();
				File input = new File(fileName + voc + ".txt");
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(input);
					PrintWriter pr = new PrintWriter(fos);
					pr.print(text);
					pr.close();
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Tagger tagger = this.taggers.get(voc);
				System.out.println("Indexing with " + tagger.getVocabulary());
				tagger.extractKeyphrases();
				File output = new File(fileName + voc + ".key");
				try {
					FileInputStream fis = new FileInputStream(output);
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(isr);
					String line = br.readLine();
					while (line != null) {
						String[] elements = line.split("\t");
						String uri = elements[1];
						String[] uri_elements = uri.split("#");
						SKOSConcept concept = searcher.searchConceptByURI(
								uri_elements[0] + "#", uri_elements[1]);
						concept.setScore(new Double(elements[2]));
						result.add(concept);
						line = br.readLine();
					}
					br.close();
					isr.close();
					fis.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (this.algorithm.equals("dummy")) {
			Tagger tagger = this.taggers.get("Dummytagger");
			System.out.println("Indexing with " + tagger.getVocabulary());
			List<String> keywords = tagger.extractKeyphrases(text);
			System.out.println("Number of keywords: " + keywords.size());
			int limit = LIMIT;
			if (limit > keywords.size())
				limit = keywords.size();
			for (int i = 0; i < limit; i++) {
				List<SKOSConcept> concepts = searcher
						.searchConceptByKeyword(keywords.get(i));
				if (concepts.size() > 0)
					result.add(concepts.get(0));
				if (concepts.size() > 1)
					result.add(concepts.get(1));
				if (concepts.size() > 2)
					result.add(concepts.get(2));
			}
		}
		return result;
	}

}
