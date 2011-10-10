package org.unc.hive.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>ConceptBrowserService/code>.
 */
public interface ConceptBrowserServiceAsync { 
	public void getSubTopConcept(String vocabulary, String letter, boolean brief, 
			AsyncCallback<List<ConceptProxy>> callback);

	public void getNumberOfConcept(String Vocabulary,AsyncCallback<Long> callback);
	public void getNumberOfRelationships(String Vocabulary,AsyncCallback<Long> callback);
	public void getLastUpdateDate(String vocabulary,AsyncCallback<Date> callback);
	public void getAllVocabularies(AsyncCallback<List<List<String>>> callback);
	public void getChildConcept(String nameSpaceURI, String localPart, AsyncCallback<List<ConceptProxy>> callback);
    public void getConceptByURI(String namespaceURI, String localPart, AsyncCallback<ConceptProxy> callback);
    public void searchForConcept(String keywords, List<String> openedVocabularies, AsyncCallback<List<ConceptProxy>> callback);
    public void getAllVocabulariesName(AsyncCallback<List<String>> callback);
    public void getFirstConcept(String vocabulary, AsyncCallback<ConceptProxy> callback);
    public void getVocabularyProperties(AsyncCallback<HashMap<String, HashMap<String,String>>> callback);
}