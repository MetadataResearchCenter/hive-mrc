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

import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import kea.main.KEAModelBuilder;
import kea.stemmers.PorterStemmer;
import kea.stopwords.StopwordsEnglish;

public class KEAModelGenerator {

    private static final Log logger = LogFactory.getLog(KEAModelGenerator.class);
	
	private KEAModelBuilder km;
	private String trainDirName;
	private String modelName;
	private String vocabulary;
	private int maxPhraseLength;
	private int minPhraseLength;
	private int minNumOccur;
	private SKOSScheme scheme;

	public void setMaxPhraseLength(int maxPhraseLength) {
		this.maxPhraseLength = maxPhraseLength;
		this.km.setMaxPhraseLength(this.maxPhraseLength);
	}

	public void setMinPhraseLength(int minPhraseLength) {
		this.minPhraseLength = minPhraseLength;
		this.km.setMinPhraseLength(this.minPhraseLength);
	}

	public void setMinNumOccur(int minNumOccur) {
		this.minNumOccur = minNumOccur;
		km.setMinNumOccur(this.minNumOccur);
	}

	public KEAModelGenerator(SKOSScheme schema) {

		this.scheme = schema;
		
		String dirName = schema.getKEAtrainSetDir();
		String modelPath = schema.getKEAModelPath();
		String vocabularyPath = schema.getRdfPath();
		String stopwordsPath = schema.getStopwordsPath();
		
		this.km = new KEAModelBuilder(scheme);
		

		this.km.setStopwords(stopwordsPath);
		// A. required arguments (no defaults):

		// 1. Name of the directory -- give the path to your directory with
		// documents and keyphrases
		// documents should be in txt format with an extention "txt"
		// keyphrases with the same name as documents, but extension "key"
		// one keyphrase per line!
		this.km.setDirName(dirName);
		this.trainDirName = dirName;

		// 2. Name of the model -- give the path to where the model is to be
		// stored and its name
		this.km.setModelName(modelPath);
		this.modelName = modelPath;

		// 3. Name of the vocabulary -- name of the file (without extension)
		// that is stored in VOCABULARIES
		// or "none" if no Vocabulary is used (free keyphrase extraction).
		km.setVocabulary(vocabularyPath);
		this.vocabulary = vocabularyPath;

		// 4. Format of the vocabulary in 3. Leave empty if vocabulary = "none",
		// use "skos" or "txt" otherwise.
		km.setVocabularyFormat("skos");

		// B. optional arguments if you want to change the defaults
		// 5. Encoding of the document
		km.setEncoding("UTF-8");

		// 6. Language of the document -- use "es" for Spanish, "fr" for French
		// or other languages as specified in your "skos" vocabulary
		km.setDocumentLanguage("en"); // es for Spanish, fr for French

		// 7. Stemmer -- adjust if you use a different language than English or
		// if you want to alterate results
		// (We have obtained better results for Spanish and French with
		// NoStemmer)
		km.setStemmer(new PorterStemmer());

		// 8. Stopwords -- adjust if you use a different language than English!
		km.setStopwords(new StopwordsEnglish(stopwordsPath));

		// 9. Maximum length of a keyphrase
		km.setMaxPhraseLength(5);

		// 10. Minimum length of a keyphrase
		km.setMinPhraseLength(1);

		// 11. Minumum occurrence of a phrase in the document -- use 2 for long
		// documents!
		km.setMinNumOccur(2);

	}

	public String getVocabulary() {
		return vocabulary;
	}

	public String getTrainDirName() {
		return trainDirName;
	}

	public String getModelName() {
		return modelName;
	}

	public void createModel(String stopwordsPath) throws HiveException {
		
		try {
			StopWatch stopWatch = new Log4JStopWatch();
			logger.info("Create KEA model");
			km.buildModel(km.collectStems(),this.scheme,stopwordsPath,this.scheme.getManager());
			km.saveModel();
			logger.info("KEA model created");
			stopWatch.lap(vocabulary + " KEA model create");
		} catch (Exception e) {
			throw new HiveException ("Error creating KEA model", e);
		}
	}

}
