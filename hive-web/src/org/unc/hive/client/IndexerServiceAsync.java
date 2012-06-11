package org.unc.hive.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IndexerServiceAsync {
	
	public void getTags(String input, List<String> openedVocabularies, int maxHops, 
			int numTerms, boolean diffOnly, int minOccur, String algorithm, 
			AsyncCallback<List<ConceptProxy>> callback);

}