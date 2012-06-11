package org.unc.hive.client;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("Indexer")
public interface IndexerService extends RemoteService {

	public List<ConceptProxy> getTags(String input, List<String> openedVocabularies, int maxHops, int numTerms,
			boolean diffOnly, int minOccur, String algorithm);
	
}