package edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp;

import java.util.ArrayList;

public class Dictionary {
	
	private String[] words;
	private String[] tags;
	
	public Dictionary() {
		
	}

	public String[] getWords() {
		return words;
	}

	public void setWords(String[] words) {
		this.words = words;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
	public boolean isAllowed(String n) {
		boolean nn = false;
		for(int i = 0 ;i<this.words.length;i++) {
			if(this.words[i].toLowerCase().equals(n.toLowerCase())){
				String tag = this.tags[i];
				if(tag.equals("NN") || tag.equals("JJ") || tag.equals("NNS") || tag.equals("NNP"))
					return true;
			}
		}
		return nn;
	}

}
