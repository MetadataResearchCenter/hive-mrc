import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;
import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSScheme;

public class Hivey2 {
    public static void main(String[] args) {

	System.out.println("Hiya");
	
	SKOSServer skosServer = new SKOSServerImpl("/Users/ryan/lib/hive-mrc/trunk/hive-core/conf/vocabularies");
	SKOSSearcher searcher = skosServer.getSKOSSearcher();

	TreeMap<String, SKOSScheme> vocabularies = skosServer.getSKOSSchemas();
	Set<String> keys = vocabularies.keySet();
	Iterator<String> it = keys.iterator();
	while (it.hasNext()) {
	    SKOSScheme voc = vocabularies.get(it.next());
	    System.out.println("NAME: " + voc.getName());
	    System.out.println("\t LONG NAME: " + voc.getLongName());
	    System.out.println("\t NUMBER OF CONCEPTS: "
			       + voc.getNumberOfConcepts());
	    System.out.println("\t NUMBER OF RELATIONS: "
			       + voc.getNumberOfRelations());
	    System.out.println("\t DATE: " + voc.getLastDate());
	    System.out.println();
	    System.out.println("\t SIZE: "  + voc.getSubAlphaIndex("a").size());
	    System.out.println();
	    System.out.println("\t TOP CONCEPTS: "
			       + voc.getTopConceptIndex().size());


	/**
	 * Search by keyword test
	 */
	System.out.println("\t TEST CONCEPT \"activity\": "
			   + voc.getTopConceptIndex().size());
	List<SKOSConcept> ranking = searcher.searchConceptByKeyword("activity");
	System.out.println("Results in SKOSServer: " + ranking.size());
	String uri = "";
	String lp = "";
	for (SKOSConcept c : ranking) {
	    System.out.println("PrefLabel: " + c.getPrefLabel());
	    uri = c.getQName().getNamespaceURI();
	    lp = c.getQName().getLocalPart();
	    System.out.println("\t URI: " + uri + " Local part: " + lp);
	}
	System.out.println();
	}	

    }
}
