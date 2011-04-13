package edu.unc.ils.mrc.hive.api;

import java.util.ArrayList;
import java.util.List;

import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;
import junit.framework.TestCase;

public class TaggerTest extends TestCase {

	public static void testKEAGetTagsForURI() 
	{
		// Path to hive.properties
		String confPath = "/Users/cwillis/dev/hive/conf/hive.properties";

		// Vocabulary name
		String vocabulary = "nbii";

		List<String> vocabularies = new ArrayList<String>();
		vocabularies.add(vocabulary);
		
		SKOSServer server = new SKOSServerImpl(confPath);
		SKOSTagger tagger = server.getSKOSTagger();
		SKOSSearcher searcher = server.getSKOSSearcher();
		String uri = "http://www.datadryad.org";
		tagger.getTags(uri, vocabularies, searcher, 10);

	}
}
