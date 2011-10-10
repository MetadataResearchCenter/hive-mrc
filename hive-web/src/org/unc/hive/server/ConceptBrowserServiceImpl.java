package org.unc.hive.server;

import org.unc.hive.server.VocabularyService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.lang.Integer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.unc.hive.client.*;

import org.unc.hive.client.ConceptBrowserService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ConceptBrowserServiceImpl extends RemoteServiceServlet implements
		ConceptBrowserService {

	private VocabularyService service;

	public ConceptBrowserServiceImpl() {

	}
	
	// @Override
	public void init(ServletConfig config) {
		try {
			super.init(config);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		ServletContext context = this.getServletContext();
		String path = context.getRealPath("");
		this.service = VocabularyService.getInstance(path + "/WEB-INF/conf/hive.properties");
	}
	
	public void setVocabularyService(VocabularyService service) {
		this.service = service;
	}

	// @Override
	// public void init() {
	// InputStream is = this.getServletContext().getResourceAsStream(
	// "/WEB-INF/conf/hive.properties");
	// Properties properties = new Properties();
	//
	// try {
	// properties.load(is);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// String data = properties.getProperty("index");
	//
	// File dataDirectory = new File(data);
	// if (dataDirectory.isDirectory()) {
	// this.vocabularies = dataDirectory.list();
	// }
	//		
	// this.service = VocabularyService.getInstance("");
	//
	// }

	@Override
	public void destroy() {
		this.service.close();
	}


	@Override
	public Long getNumberOfConcept(String vocabulary) {
		return new Long(this.service.getNumberOfConcept(vocabulary));
	}

	@Override
	public Long getNumberOfRelationships(String vocabulary) {
		return new Long(this.service.getNumerOfRelations(vocabulary));
	}

	@Override
	public Date getLastUpdateDate(String vocabulary) {
		return this.service.getLastUpdateDate(vocabulary);
	}

	public List<List<String>> getAllVocabularies() {
		return this.service.getAllVocabularies();
	}

	public TreeMap<String, String> getChildConcepts(String father,
			String letter, String vocabulary) {
		return new TreeMap<String, String>();
	}

    /**
     *  @gwt.typeArgs <client.ConceptProxy>
     *  
     *   */
   
	
	public List<ConceptProxy> getChildConcept(String nameSpaceURI,
			String localPart) {
		return this.service.getChildConcept(nameSpaceURI, localPart);
	}

	public ConceptProxy getConceptByURI(String namespaceURI, String localPart) {
		return this.service.getConceptByURI(namespaceURI, localPart);
	}
	
    /**
     *  @gwt.typeArgs <client.ConceptProxy>
     *  
     *   */
   
	
	public List<ConceptProxy> searchForConcept(String keywords, List<String> openedVocabularies)
	{
		return this.service.searchConcept(keywords, openedVocabularies);
	}
	
	public List<String> getAllVocabulariesName()
	{
		return this.service.getAllVocabularyNames();
	}
	
	public List<ConceptProxy> getSubTopConcept(String vocabulary,String letter, boolean brief)
	{
		return this.service.getSubTopConcept(vocabulary, letter, brief);
	}
	
	public ConceptProxy getFirstConcept(String vocabulary)
	{
		return this.service.getFirstConcept(vocabulary);
	}

	/* Get the statistics of vocabularies information at HIVE */
	public String openNewVocabulary(String name) {
		return new String();
	}/* Retrieve and open a new vocabulary from sever */
	
	public HashMap<String, HashMap<String,String>> getVocabularyProperties() {
		return this.service.getVocabularyProperties();
	}

}
