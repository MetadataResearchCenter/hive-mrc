package org.unc.hive.client;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>ConceptBrowserService/code>.
 */
public interface ConceptBrowserServiceAsync {
	public void getSubTopConcept(String vocabulary, String letter, AsyncCallback<List<ConceptProxy>> callback);
	public void getNumberOfConcept(String Vocabulary,AsyncCallback<Integer> callback);
	public void getNumberOfRelationships(String Vocabulary,AsyncCallback<Integer> callback);
	public void getDate(String vocabulary,AsyncCallback<String> callback);
	public void getAllVocabularies(AsyncCallback<List<List<String>>> callback);
	public void getChildConcept(String nameSpaceURI, String localPart, AsyncCallback<List<ConceptProxy>> callback);
    public void getConceptByURI(String namespaceURI, String localPart, AsyncCallback<ConceptProxy> callback);
    public void searchForConcept(String keywords, List<String> openedVocabularies, AsyncCallback<List<ConceptProxy>> callback);
    public void getAllVocabulariesName(AsyncCallback<List<String>> callback);
    public void getFirstConcept(String vocabulary, AsyncCallback<ConceptProxy> callback);
}