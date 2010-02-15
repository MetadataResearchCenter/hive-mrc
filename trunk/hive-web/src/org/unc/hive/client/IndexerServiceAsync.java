package org.unc.hive.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IndexerServiceAsync {
	
	public void getTags(String input, List<String> openedVocabularies,AsyncCallback<List<ConceptProxy>> callback);

}