package edu.unc.ils.mrc.hive.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;
import junit.framework.TestCase;

public class TaggerTest extends TestCase {

	public static void testKEAGetTagsForURI() throws MalformedURLException 
	{
		// Path to hive.properties
		String confPath = "/usr/local/hive/test/conf/hive.properties";

		// Vocabulary name
		String vocabulary = "agrovoc";
		String algorithm = "maui";

		List<String> vocabularies = new ArrayList<String>();
		vocabularies.add(vocabulary);
		
		SKOSServer server = new SKOSServerImpl(confPath);
		SKOSTagger tagger = server.getSKOSTagger(algorithm);
		SKOSSearcher searcher = server.getSKOSSearcher();
		String uri = "http://ils.unc.edu/mrc/wp-content/uploads/2010/12/greenberg_jlm_sci_introduction.pdf";
		tagger.getTags(new URL(uri), vocabularies, searcher, 2, 10, true, 2);
		//tagger.getTags(uri, vocabularies, searcher, 10);

	}
}
