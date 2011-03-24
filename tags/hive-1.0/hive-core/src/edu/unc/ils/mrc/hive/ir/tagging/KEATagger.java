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

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import kea.main.KEAKeyphraseExtractor;
import kea.stemmers.PorterStemmer;
import kea.stopwords.StopwordsEnglish;

public class KEATagger implements Tagger{

	private static Logger log = Logger.getLogger(KEATagger.class);
	private KEAKeyphraseExtractor ke;
	private String vocabulary;

	public KEATagger(String dirName, String modelName, String stopwordsPath,
			SKOSScheme schema) {
		
		this.vocabulary = schema.getLongName();
		this.ke = new KEAKeyphraseExtractor(schema);

		// A. required arguments (no defaults):

		// 1. Name of the directory -- give the path to your directory with
		// documents
		// documents should be in txt format with an extention "txt".
		// Note: keyphrases with the same name as documents, but extension "key"
		// one keyphrase per line!

		this.ke.setDirName(dirName);

		// 2. Name of the model -- give the path to the model
		this.ke.setModelName(modelName);

		// 3. Name of the vocabulary -- name of the file (without extension)
		// that is stored in VOCABULARIES
		// or "none" if no Vocabulary is used (free keyphrase extraction).
		this.ke.setVocabulary(schema.getName().toLowerCase());

		// 4. Format of the vocabulary in 3. Leave empty if vocabulary = "none",
		// use "skos" or "txt" otherwise.
		this.ke.setVocabularyFormat("skos");

		// B. optional arguments if you want to change the defaults
		// 5. Encoding of the document
		this.ke.setEncoding("UTF-8");

		// 6. Language of the document -- use "es" for Spanish, "fr" for French
		// or other languages as specified in your "skos" vocabulary
		this.ke.setDocumentLanguage("en"); // es for Spanish, fr for French

		// 7. Stemmer -- adjust if you use a different language than English or
		// want to alterate results
		// (We have obtained better results for Spanish and French with
		// NoStemmer)
		this.ke.setStemmer(new PorterStemmer());

		// 8. Stopwords
		this.ke.setStopwords(new StopwordsEnglish(stopwordsPath));
		
		// 9. Number of Keyphrases to extract
		this.ke.setNumPhrases(10);

		// 10. Set to true, if you want to compute global dictionaries from the
		// test collection
		this.ke.setBuildGlobal(false);
		this.ke.setAdditionalInfo(true);

		try {
			this.ke.loadModel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.fatal("unable to load KEA", e);
		}
		
		this.ke.loadThesaurus();
	}

	/**
	 * Extracts keyphrases from .txt files in the directory specified
	 * in the constructor. Keyphrases are written to .key files which can be
	 * read by the calling application.
	 */
	@Override
	public void extractKeyphrases() {
		StopWatch stopwatch = new Log4JStopWatch();
		try {
			this.ke.extractKeyphrases(ke.collectStems());
		} catch (Exception e) {
			e.printStackTrace();
		}
		stopwatch.lap("ExtractKeyPhrases - " + vocabulary);
	}
	

	/**
	 * Extracts keyphrases from the specified file baseName. The 
	 * underlying class will append a ".txt" suffix to read the file
	 * and a ".key" suffix to the generated keyphrase file name. 
	 * The calling application can read the generated keyphrases from
	 * the ".key" file.
	 */
	@Override
	public void extractKeyphrasesFromFile(String baseName) {
		StopWatch stopwatch = new Log4JStopWatch();
		try {
			Hashtable<String, Double> stems = new Hashtable<String, Double>();
			stems.put(baseName, new Double(0));		
			this.ke.extractKeyphrases(stems);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stopwatch.lap("ExtractKeyPhrasesFromFile - " + vocabulary);
	}
	
	// Not implemented
	@Override
	public List<String> extractKeyphrases(String text) {
		return null;
	}
	
	public String getVocabulary() {
		return this.vocabulary;
	}

}
