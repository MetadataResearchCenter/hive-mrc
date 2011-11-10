package org.unc.hive.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unc.hive.client.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class IndexerServiceImpl extends RemoteServiceServlet implements
		IndexerService {

	private VocabularyService service;
	private String path;

	private static final Log logger = LogFactory.getLog(IndexerServiceImpl.class);
	
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
		this.service = VocabularyService.getInstance(this.path + "/WEB-INF/conf/hive.properties");
	}

	@Override
	public void destroy() {
		this.service.close();
	}

   /**
     *  @gwt.typeArgs <client.ConceptProxy>
     *  
     *   
     */
	public List<ConceptProxy> getTags(String input, List<String> openedVocabularies, int maxHops, 
			int numTerms, boolean diff, String algorithm) 
	{
		logger.debug("getTags for " + input);
		if(input.startsWith("http://") || input.startsWith("https://")) {
			try
			{
				URL url = new URL (input);
				return this.service.getTags(url, openedVocabularies, maxHops, numTerms, diff, algorithm);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		else 
		{
			String filePath = this.path + "/WEB-INF/tmp/" + input;
			
			List<ConceptProxy> concepts = new ArrayList<ConceptProxy>();
			concepts = this.service.getTags(filePath, openedVocabularies, numTerms, algorithm);
			
			// Delete the temporary file
			File file = new File(filePath);
			file.delete();
			
			return concepts;
		}
	}

}