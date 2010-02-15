package edu.unc.ils.mrc.hive.ir.tagging;

import java.util.List;

public interface Tagger {
	
	public void extractKeyphrases();
	public List<String> extractKeyphrases(String text);
	public String getVocabulary();

}
