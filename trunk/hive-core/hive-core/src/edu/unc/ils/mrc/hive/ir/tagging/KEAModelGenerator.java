package edu.unc.ils.mrc.hive.ir.tagging;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import kea.main.KEAModelBuilder;
import kea.stemmers.PorterStemmer;
import kea.stopwords.StopwordsEnglish;

public class KEAModelGenerator {

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

	public void createModel(String stopwordsPath) {
		try {
			km.buildModel(km.collectStems(),this.vocabulary,stopwordsPath,this.scheme.getManager());
			km.saveModel();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
