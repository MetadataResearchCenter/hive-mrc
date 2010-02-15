package edu.unc.ils.mrc.hive.api;

import java.util.List;

public interface SKOSTagger {
	
	public List<SKOSConcept> getTags(String text, List<String> vocabularie,SKOSSearcher searcher);

}
