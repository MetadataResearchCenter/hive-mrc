package org.unc.hive.client;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ConceptBrowser")
public interface ConceptBrowserService extends RemoteService {
//	List<String> getThesaurusTree(); /*Specify to get which Thesaurus?*/
//	List<String> searchConcept(String word);/*Specify which vocabulary to search?*/
//	List<String> getActiveVS(); /*Return current open vocabulary*/
//	List<String> getVStat(); /*Get the statistics of vocabularies information at HIVE*/
//    String openNewVocabulary(String name); /*Retrieve and open a new vocabulary from sever*/
//    String setup(String modelName);
	public List<ConceptProxy> getSubTopConcept(String vocabulary,String letter, boolean brief);
	public List<ConceptProxy> getChildConcept(String nameSpaceURI, String localPart);
	public Long getNumberOfConcept(String Vocabulary);
	public Long getNumberOfRelationships(String Vocabulary);
	public Date getLastUpdateDate(String vocabulary);
	public List<List<String>> getAllVocabularies();
	public ConceptProxy getConceptByURI(String namespaceURI, String localPart);
	public List<ConceptProxy> searchForConcept(String keywords, List<String> openedVocabularies);
	public List<String> getAllVocabulariesName();
	public ConceptProxy getFirstConcept(String vocabulary); 
	public HashMap<String,HashMap<String,String>> getVocabularyProperties(); 
	
}