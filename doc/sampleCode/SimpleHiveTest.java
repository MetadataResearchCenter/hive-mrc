import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;
import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSScheme;

/**
   A simple class to demonstrate basic HIVE method calls.
**/


public class SimpleHiveTest {
    public static final String TEST_TERM="activity";

    public static void main(String[] args) {

	// set up the basic HIVE objects
	SKOSServer skosServer = new SKOSServerImpl("/Users/ryan/lib/hive-mrc/trunk/hive-core/conf/vocabularies");
	SKOSSearcher searcher = skosServer.getSKOSSearcher();

	
	// for each available vocabulary...
	TreeMap<String, SKOSScheme> vocabularies = skosServer.getSKOSSchemas();
	Set<String> keys = vocabularies.keySet();
	Iterator<String> it = keys.iterator();
	while (it.hasNext()) {
	    SKOSScheme voc = vocabularies.get(it.next());

	    // list some basic properties
	    System.out.println("NAME: " + voc.getName());
	    System.out.println("\t LONG NAME: " + voc.getLongName());
	    System.out.println("\t NUMBER OF CONCEPTS: "
			       + voc.getNumberOfConcepts());

	    // find a test term and list its properties
	    List<SKOSConcept> ranking = searcher.searchConceptByKeyword(TEST_TERM);
	    System.out.println("Results in SKOSServer: " + ranking.size());
	    for (SKOSConcept c : ranking) {
		System.out.println("PrefLabel: " + c.getPrefLabel());
		String uri = c.getQName().getNamespaceURI();
		String lp = c.getQName().getLocalPart();
		System.out.println("\t URI: " + uri + " Local part: " + lp);
	    }
	    System.out.println();
	}	
	
    }
}
