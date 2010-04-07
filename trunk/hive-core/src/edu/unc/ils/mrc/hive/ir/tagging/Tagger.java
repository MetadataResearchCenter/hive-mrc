package edu.unc.ils.mrc.hive.ir.tagging;

import java.util.List;

public interface Tagger {
	
	public void extractKeyphrases();
	
	public List<String> extractKeyphrases(String text);
	
	/**
	 * @return The vocabulary being used for this tagger.
	 */
	public String getVocabulary();

}
