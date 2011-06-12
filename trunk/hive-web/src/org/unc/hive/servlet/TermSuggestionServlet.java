package org.unc.hive.servlet;

import java.io.IOException;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unc.hive.client.ConceptProxy;
import org.unc.hive.server.VocabularyService;

import edu.unc.ils.mrc.hive.api.ConceptNode;
import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;

/**
 * 
 */
public class TermSuggestionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	

	public TermSuggestionServlet() {
		super();

	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ServletContext context = this.getServletContext();
		String path = context.getRealPath("");
		VocabularyService service = VocabularyService.getInstance(path + "/WEB-INF/conf/hive.properties");
		
		String vocab = request.getParameter("cv");
		String text = request.getParameter("text");
		String mp = request.getParameter("mp");
		
		int minPhraseOccur = 1;
		try {
			minPhraseOccur = Integer.valueOf(mp);
		} catch (NumberFormatException e) {
		}
		
		List<ConceptNode> tree =null;
		try {
			List<String> vocabs = new ArrayList<String>();
			vocabs.add(vocab);
			if (text == null || text.length() == 0)
				text = "the quick brown fox jumps over the lazy dog";
			tree = service.getTagsAsTree(text, vocabs, 15, minPhraseOccur);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String json = "[";
		for (ConceptNode node: tree) 
		{
			json += getJson(node) + ",";
		}
		json += "]";
		System.out.println(json);
		response.setContentType("text/json");
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(json.toString());
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	protected String getJson(ConceptNode node) {
		StringBuffer json = new StringBuffer("{");
		json.append("\"title\": \"");
		json.append(node.getLabel());
		json.append("\", \"key\": \"");
		json.append(node.getUri());
		json.append("\", \"url\": \"");
		json.append(node.getUri());
		json.append("\"");
		if (node.getChildren().size() > 0) {
			json.append(", \"isFolder\": \"true\", \"expand\": \"true\", ");
			json.append("\"children\": [");
			int i = 1;
			for (ConceptNode child: node.getChildren()) {
				String j = getJson(child);
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
		
		/*
		[
		    {title: "item1 with key and tooltip", tooltip: "Look, a tool tip!" },
		    {title: "item2: selected on init", select: true },
		    {title: "Folder", isFolder: true, key: "id3",
		      children: [
		        {title: "Sub-item 3.1",
		          children: [
		            {title: "Sub-item 3.1.1", key: "id3.1.1" },
		            {title: "Sub-item 3.1.2", key: "id3.1.2" }
		          ]
		        },
		        {title: "Sub-item 3.2",
		          children: [
		            {title: "Sub-item 3.2.1", key: "id3.2.1" },
		            {title: "Sub-item 3.2.2", key: "id3.2.2" }
		          ]
		        }
		      ]
		    }
		  ];

				 */

	}

}
