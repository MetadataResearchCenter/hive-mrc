package org.unc.hive.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.unc.hive.server.VocabularyService;

import com.noelios.restlet.ext.servlet.ServletContextAdapter;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;

public class ConceptListResource extends Resource {

	private List<HashMap> conceptList;

	public ConceptListResource(Context context, Request request,
			Response response) {

		super(context, request, response);
		
		conceptList = new ArrayList<HashMap>();

		// Declare the kind of representations supported by this resource.
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));

		//ServletContextAdapter adapter = (ServletContextAdapter) getContext();
		//ServletContext servletContext = adapter.getServletContext();
		//String path = servletContext.getRealPath("");
		//System.out.println(servletContext.getRealPath(""));
		VocabularyService service = VocabularyService.getInstance("/home/hive/tomcat/webapps/ROOT/WEB-INF/conf/vocabularies");

		// Get the "itemName" attribute value taken from the URI template
		// /{vocabularyName}/{SPARQLquery}.
		System.out.println("Size matters: " + getRequest().getAttributes().size());
		for(String s : getRequest().getAttributes().keySet()) {
			System.out.println("Las claves son: " + s);
		}
		String SPARQLquery = (String) getRequest().getAttributes().get(
				"SPARQLquery");
		String vocabularyName = (String) getRequest().getAttributes().get(
				"vocabularyName");

		System.out.println("Esta es la queryyyyy!!!" + SPARQLquery);
		
		SKOSSearcher searcher = service.getSKOSSearcher();

		//SPARQLquery = "SELECT ?s ?p ?p WHERE {?s ?p ?o} LIMIT 10";
		SPARQLquery = SPARQLquery.replace("query=", "");
		SPARQLquery = SPARQLquery.replace("+", " ");
		SPARQLquery = SPARQLquery.replace("%3F", "?");
		SPARQLquery = SPARQLquery.replace("%7B", "{");
		SPARQLquery = SPARQLquery.replace("%7D", "}");
		System.out.println(SPARQLquery);
		this.conceptList = searcher.SPARQLSelect(SPARQLquery, vocabularyName);

	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		Representation representation = new StringRepresentation(
				this.conceptList.toString(), MediaType.TEXT_PLAIN);
		return representation;
	}
}
