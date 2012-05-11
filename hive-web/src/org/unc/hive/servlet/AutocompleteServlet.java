package org.unc.hive.servlet;

import java.io.IOException;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unc.hive.server.VocabularyService;

import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;

/**
 *  Autocomplete  servlet returns JSON/JSONP formatted data for use with JQuery
 * 
 *  Parameters: 
 *  	cv:       vocabulary name (required)
 *      term:     text (required)
 *      callback: callback function for JSONP support (optional)
 */
public class AutocompleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final String PARAM_CALLBACK = "callback";
       
	

	public AutocompleteServlet() {
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
		String term = request.getParameter("term");
		String callback = request.getParameter("callback");
		
		List<AutocompleteTerm> terms =null;
		try {
			terms = service.suggestTermsFor(vocab, term, 15);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuffer json = new StringBuffer("\n[");
		int i = 0;
		if (terms != null)
		{
			for (AutocompleteTerm t: terms) {
				if (i > 0)
					json.append(",\n");
				json.append("{");
				json.append("\"id\": \"");
				json.append(t.getId());
				json.append("\", \"label\": \"");
				json.append(t.getValue());
				json.append("\", \"value\": \"");
				json.append(t.getValue());
				json.append("\"}");
				i++;
			}
		}
		json.append("\n]");

		if (callback != null)
		{
			json.insert(0, callback + "(");
			json.append(")");
		}
		
		response.setContentType("text/json");
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(json.toString());
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
