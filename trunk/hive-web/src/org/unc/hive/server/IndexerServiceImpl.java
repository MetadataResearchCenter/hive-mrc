package org.unc.hive.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.unc.hive.client.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class IndexerServiceImpl extends RemoteServiceServlet implements
		IndexerService {

	private VocabularyService service;
	private String path;

	public IndexerServiceImpl() {
		
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
		this.path = context.getRealPath("");
		this.service = VocabularyService.getInstance(this.path + "/WEB-INF/conf/vocabularies");
	}

	@Override
	public void destroy() {
		this.service.close();
	}

   /**
     *  @gwt.typeArgs <client.ConceptProxy>
     *  
     *   */
   
	public List<ConceptProxy> getTags(String input, List<String> openedVocabularies) {
		if(input.contains("http://"))
			return this.service.getTags(input, openedVocabularies);
		else
			//return this.service.getTags("/home/hive/tomcat/webapps/ROOT/WEB-INF/tmp/" + input, openedVocabularies);
			return this.service.getTags(this.path + "/WEB-INF/tmp/" + input, openedVocabularies);
	}

}