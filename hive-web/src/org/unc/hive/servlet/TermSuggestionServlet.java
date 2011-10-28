package org.unc.hive.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;


import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.unc.hive.client.ConceptProxy;
import org.unc.hive.server.VocabularyService;

import edu.unc.ils.mrc.hive.api.ConceptNode;

/**
 * 
 */
public class TermSuggestionServlet extends HttpServlet 
{
	private static final long serialVersionUID = 2357815517668304804L;

	static final String PARAM_VOCAB = "cv";
	static final String PARAM_TEXT = "tx";
	static final String PARAM_MIN_PHRASE_OCCURRENCES = "mp";
	static final String PARAM_EXISTING_TERMS = "ex";
	static final String PARAM_FORMAT = "fmt";
	
	static final String FORMAT_TREE = "tree";
	static final String FORMAT_LIST = "list";
	
	public TermSuggestionServlet() {
		super();
	}
	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{

		ServletContext context = this.getServletContext();
		String path = context.getRealPath("");
		VocabularyService service = VocabularyService.getInstance(path + "/WEB-INF/conf/hive.properties");
		
		List<String> errors = new ArrayList<String>();
		
		// Get the selected vocabularies
		String vocab = request.getParameter(PARAM_VOCAB);
		List<String> vocabs = new ArrayList<String>();
		if (!StringUtils.isEmpty(vocab)) {
			vocabs = Arrays.asList(vocab.split("\\,"));
		} else {
			errors.add("No vocabulary specified");
		}
		
		// Read the input text
		String text = request.getParameter(PARAM_TEXT);
		if (StringUtils.isEmpty(text)) {
			errors.add("No input text specified");
		}
		
		// Get the minimum phrase occurrences, if specified. Otherwise default to 1.
		String mp = request.getParameter(PARAM_MIN_PHRASE_OCCURRENCES);
		int minPhraseOccur = 1;
		if (StringUtils.isEmpty(mp)) {
			try {
				minPhraseOccur = Integer.valueOf(mp);
			} catch (NumberFormatException e) {}
		}
		
		// Parse the list of existing terms, if any
		String existing = request.getParameter(PARAM_EXISTING_TERMS);
		List<String> existingTermsList = new ArrayList<String>();
		if (!StringUtils.isEmpty(existing))
			existingTermsList = Arrays.asList(existing.split("\\|"));
		
		// Get the requested format
		String format = request.getParameter(PARAM_FORMAT);
		if (StringUtils.isEmpty(format))
			format = FORMAT_TREE;
		
		String json = "";
		try
		{
			json = "[";
			
			if (format.equals(FORMAT_TREE))
			{
				List<ConceptNode> tree =null;
	
				tree = service.getTagsAsTree(text, vocabs, 15, minPhraseOccur);
				if (tree.size() > 0)
				{
					int i =0;
					for (ConceptNode node: tree)  {
						if (i > 0)
							json += ", ";
						json += getJsonTreeItem(node, existingTermsList);
						i++;
					}
				}
				else
				{
					json += "{" +
						"\"title\": \"No suggestions\"," +
						"\"hideCheckbox\": true" +
					"}";
		
				}
			}
			else
			{
				List<ConceptProxy> list =null;
				list = service.getTags(text, vocabs, 15, minPhraseOccur);
				if (list.size() > 0)
				{
					int i =0;
					for (ConceptProxy concept: list) {
						if (i > 0)
							json += ", ";
						json += getJsonListItem(concept, existingTermsList);
						i++;
					}
				}
				else
				{
					json += "{" +
								"\"title\": \"No suggestions\"," +
								"\"hideCheckbox\": true" +
							"}";
				}
			}
			json += "]";
		} catch (Exception e) {
			e.printStackTrace();
			errors.add("A server error has occurred.");
		}
			
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		if  (errors.size() > 0) {
			String errorMsg = "";
			for (String error: errors) 
				errorMsg += error;
			errorMsg = getJsonError(errorMsg);
			writer.write(errorMsg);
		} else {
			writer.write(json.toString());
		}
		
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected String getJsonError(String message) {
		return "[{" +
			"\"title\": \"" + message + "\"," +
			"\"hideCheckbox\": true" +
		"}]";
	}
	
	protected String getJsonTreeItem(ConceptNode node, List<String> ignore) {
		StringBuffer json = new StringBuffer("{");
		json.append("\"title\": \"");
		json.append(node.getLabel());
		json.append("\", \"key\": \"");
		json.append(node.getUri());
		json.append("\", \"url\": \"");
		json.append(URLEncoder.encode(node.getUri().replaceAll(" ", "")));
		json.append("\"");
		
		String tooltip = "";
		List<String> altLabels = node.getAltLabels();
		if (altLabels != null && altLabels.size() > 0)
		{
			tooltip = "Other terms: ";
			int i=0;
			for (String altLabel: altLabels) {
				tooltip += altLabel;
				if (i < altLabels.size()-1)
					tooltip += ", ";
				i++;
			}

		}
		json.append(", \"tooltip\": \"");
		json.append(tooltip);
		json.append("\"");
		if (ignore.contains(node.getLabel())) {
			json.append(",\"select\": true");
			json.append(",\"unselectable\": \"true\"");
		}
		if (node.getChildren().size() > 0) {
			json.append(", \"isFolder\": \"true\", \"expand\": \"true\", ");
			json.append("\"children\": [");
			int i = 1;
			for (ConceptNode child: node.getChildren()) {
				String j = getJsonTreeItem(child, ignore);
				json.append(j);
				if (i < node.getChildren().size()) {
					json.append(",");
				}
				i++;
			}
			json.append("]");
		}
		json.append("}");
		
		return json.toString();
	}
	
	protected String getJsonListItem(ConceptProxy node, List<String> ignore) {
		StringBuffer json = new StringBuffer("{");
		json.append("\"title\": \"");
		json.append(node.getPreLabel());
		json.append("\", \"key\": \"");
		json.append(node.getURI());
		json.append("\", \"url\": \"");
		json.append(URLEncoder.encode(node.getURI().replaceAll(" ", "")));
		json.append("\", \"origin\": \"");
		json.append(node.getOrigin());
		json.append("\", \"score\": \"");
		json.append(node.getScore());
		json.append("\"");
		if (ignore.contains(node.getPreLabel())) {
			json.append(",\"select\": true");
			json.append(",\"unselectable\": true");
		}
		String tooltip = "";
		List<String> altLabels = node.getAltLabel();
		if (altLabels != null && altLabels.size() > 0)
		{
			tooltip = "Other terms: ";
			int i=0;
			for (String altLabel: altLabels) {
				tooltip += altLabel;
				if (i < altLabels.size()-1)
					tooltip += ", ";
				i++;
			}

		}
		json.append(", \"tooltip\": \"");
		json.append(tooltip);
		json.append("\"");
		HashMap<String, String> broaders = node.getBroader();
		if (broaders != null && broaders.size() > 0)
		{
			json.append(",\"broaders\": [");
			int i = 1;
			for (String key: broaders.keySet()) {
				String uri = broaders.get(key);
				json.append("{\"title\": \"");
				json.append(key);
				json.append("\", \"url\": \"");
				json.append(uri);
				json.append("\"}");
				if (i < broaders.size()) {
					json.append(",");
				}
				i++;
			}
			json.append("]");
			String path = "";
			for (String broader : broaders.keySet())
				path += broader;
			
			json.append(", \"path\": \"");
			json.append(path);
			json.append("\"");
		}
		
		json.append("}");
		
		return json.toString();
	}

}
