package edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTest;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.FSDirectory;

import edu.unc.ils.mrc.hive.ir.lucene.search.Autocomplete;
import junit.framework.TestCase;

public class AutocompleteTest extends TestCase {

	public void testAutocomplete() throws IOException, ParseException {
    	String vocab = "agrovoc";
    	Autocomplete autocomplete = new Autocomplete("/usr/local/hive/hive-data/" + vocab + "/" + vocab + "Autocomplete");

    	// run this to re-index from the current index, shouldn't need to do
    	// this very often
    	autocomplete.reIndex(FSDirectory.getDirectory("/usr/local/hive/hive-data/" + vocab + "/" + vocab + "Index", null),
    	"prefLabel");


    	System.out.println(autocomplete.suggestTermsFor("sal", 10));
    	System.out.println(autocomplete.suggestTermsFor("salix", 10));
    	System.out.println(autocomplete.suggestTermsFor("salix ex", 10));
    	// prints [steve, steven, stevens, stevenson, stevenage]
	}
}
