package org.unc.hive.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IndexerServiceAsync {
	
	public void getTags(String input, List<String> openedVocabularies, int maxHops, AsyncCallback<List<ConceptProxy>> callback);

}