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

import java.io.BufferedReader;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

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
    private static final Log logger = LogFactory.getLog(SKOSTaggerImpl.class);
	
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
				TaggerFactory.selectTagger(TaggerFactory.KEATAGGER);
				Tagger tagger = TaggerFactory.getTagger(schema
						.getKEAtestSetDir(), schema.getKEAModelPath(), schema
						.getStopwordsPath(), schema);
				this.taggers.put(vocName, tagger);
			}
		} else if (this.algorithm.equals("dummy")) {
			SKOSScheme schema = vocabularies.get(vocabularies.firstKey());
			TaggerFactory.selectTagger(TaggerFactory.DUMMYTAGGER);
			Tagger tagger = TaggerFactory.getTagger("", schema
					.getLingpipeModel(), "", null);
			this.taggers.put("Dummytagger", tagger);
		} else {
		    logger.fatal(this.algorithm + " algorithm is not suported");
		}
		logger.debug("NUMBER OF TAGGERS: " + this.taggers.size());
		for (Tagger tag : this.taggers.values()) {
		    logger.info("Tagger: " + tag.getVocabulary());
		}
	}

	public List<SKOSConcept> getTags(String inputFilePath, List<String> vocabulary,
			SKOSSearcher searcher) 
	{
		StopWatch stopwatch = new Log4JStopWatch();
		
		TextManager tm = new TextManager();
		String text = tm.getPlainText(inputFilePath);
		List<SKOSConcept> result = new ArrayList<SKOSConcept>();
		stopwatch.lap("GetPlainText");
		
		if (this.algorithm.equals("kea")) 
		{
			for (String voc : vocabulary) 
			{
				File testDir = new File(this.vocabularies.get(voc).getKEAtestSetDir());
				
				String tempFileName = UUID.randomUUID().toString();
				File keaInputFile =  new File(testDir + File.separator + tempFileName + ".txt");
				
				logger.debug("Creating " + keaInputFile.getAbsolutePath());
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(keaInputFile);
					PrintWriter pr = new PrintWriter(fos);
					pr.print(text);
					pr.close();
					fos.close();
				} catch (FileNotFoundException e) {
				    logger.error(e);
				} catch (IOException e) {
                    logger.error(e);
				}
				Tagger tagger = this.taggers.get(voc);
				String vocabularyName = tagger.getVocabulary();
				logger.info("Indexing with " + vocabularyName);
				try {
					tagger.extractKeyphrasesFromFile(tempFileName);
				} catch (RuntimeException e) {
					logger.error(e);
				}
				
				File keaOutputFile =  new File(testDir + File.separator + tempFileName + ".key");
				logger.debug("Reading key file " + keaOutputFile.getAbsolutePath());
				try {
					FileInputStream fis = new FileInputStream(keaOutputFile);
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
					logger.error("unable to find file", e);
				} catch (IOException e) {
				    logger.error("file processing problem", e);
				}
				
				// If we do not delete these files, they are re-read during subsequent
				// extractKeyphrases and cause performance degradation.
				logger.debug("Deleting "+ keaInputFile.getAbsolutePath());
				keaInputFile.delete();
				logger.debug("Deleting "+ keaOutputFile.getAbsolutePath());
				keaOutputFile.delete();
			}

		} else if (this.algorithm.equals("dummy")) {
			Tagger tagger = this.taggers.get("Dummytagger");
			logger.info("Dummy indexing with " + tagger.getVocabulary());
			logger.debug("extracting keyphrases");
			List<String> keywords = tagger.extractKeyphrases(text);
			logger.info("Number of keyphrases: " + keywords.size());
			int limit = LIMIT;
			if (limit > keywords.size()) {
				limit = keywords.size();
			}
			logger.debug("searching for keyphrases in index");
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
			logger.debug("tagging complete");
		}
		stopwatch.lap("GetTags");

		File inputFile = new File(inputFilePath);
		inputFile.delete();
		return result;
	}
}
