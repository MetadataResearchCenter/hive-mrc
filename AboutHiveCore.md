## What is HIVE Core? ##

HIVE Core is a SKOS-based library written in Java. HIVE solves problems traditionally associated with the use of controlled vocabularies in digital environments, including concept retrieval and automatic annotation.

The HIVE Core API supports the following:
  * Creation and administration of HIVE indexes
  * Keyword and SPARQL-based queries
  * Automatic concept tagging/keyphrase indexing
  * Vocabulary conversion

## HIVE Core Architecture ##

The HIVE Core API allows developers to access multiple SKOS-based vocabularies in Java, including natural language and SPARQL query interfaces. The natural language interface supports keyword queries to retrieve concepts using Lucene.

The HIVE Core API is used by both the HIVE [Web](AboutHiveWeb.md) and [REST](AboutHiveRestService.md) services.

<img src='http://hive-mrc.googlecode.com/svn/trunk/doc/HIVE-Documentation/img/hive-architecture.jpg' />


## Index Administration ##

The HIVE Core API supports the creation and administration of HIVE indexes used for searching and document tagging.
  * Lucene-based index for keyword searching
  * Sesame/OpenRDF-based index for SPARQL queries
  * In-memory or embedded H2-based index for automatic controlled keyphrase assignment (tagging)
  * Seralized maps containing the alphabetical and top-concept indexes

## Creating Training Data for Automatic Metadata Generation ##
The HIVE automatic keyphrase indexing is based on the [KEA++](http://www.nzdl.org/Kea) algorithm and library.

When a vocabulary is imported into HIVE, a language model is built for keyphrase extraction. For more information, see the section [training KEA](http://code.google.com/p/hive-mrc/wiki/TrainingKEA).


## HIVE Core API ##
The HIVE Core API includes the following top-level interfaces:
  * `SKOSServer`: Provides access to one or more vocabularies
  * `SKOSScheme`: Represents an individual vocabulary
  * `SKOSSearcher`: Supports searching across multiple vocabularies
  * `SKOSTagger`: Supports tagging/keyphrase extraction across multiple vocabularies

### SKOS Server ###

`SKOSServer` is the top-level class used to initialize the vocabulary server. This class reads the `hive.properties` file and initializes the `SKOSScheme` (vocabulary management), `SKOSSearcher` (concept searching),  `SKOSTagger` (indexing) instances based on the vocabulary configurations.

```
 SKOSServer server = new SKOSServerImpl("/home/hive/workspace/hive-core/conf/hive.properties");
```

The "hive.properties" file contains a list of vocabularies to be initialized. Each vocabulary has a separate properties file containing paths to the various data model elements.

<img src='http://hive-mrc.googlecode.com/svn/trunk/doc/HIVE-Documentation/img/skosserver.jpg' />


### SKOSScheme ###

Each HIVE vocabulary is modeled using the `SKOSScheme` class. This class contains information about the vocabularies and methods to manage them, including statistics about each vocabulary (number of terms, and relations, updates, etc.).

The following example demonstrates how to retrieve the statistics for each configured vocabulary/scheme:
```
TreeMap<String, SKOSScheme> vocabularies = server.getSKOSSchemas();
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
		  System.out.println("\t SIZE: " + voc.getSubAlphaIndex("a").size());
		  System.out.println();
		  System.out.println("\t TOP CONCEPTS: "
		      + voc.getTopConceptIndex().size());
	  }
```

## Searching HIVE ##

The `SKOSSearcher` interface supports searching across one or more configured vocabularies. HIVE supports two types of searching:

  * Formal Search (SPARQL)
  * Keyword based search (natural language)

### Formal Search based on SPARQL ###

Formal search is based on SPARQL, a formal language to get information from RDF databases. SPARQL is the standard query language for
Semantic Web applications and it is useful to implement Web Services based on SPARQL endpoints, and so allowing third part applications to retrieve information from a RDF database.

```
 searcher.SPARQLSelect(
  "SELECT ?s ?p ?p WHERE {?s ?p ?o} LIMIT 10", 
  "nbii");
```

```
 searcher.SPARQLSelect(
  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> 
  SELECT ?s ?p ?o WHERE {  ?s ?p ?o . ?s skos:prefLabel \"Damage\" .}",
  "nbii");
```

```
 searcher.SPARQLSelect(
  "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> 
  SELECT ?uri ?label WHERE { <http://thesaurus.nbii.gov/nbii#Mud> 
  skos:broader ?uri . ?uri skos:prefLabel ?label}",
  "nbii");
```

### Keyword based search ###

Keyword based search is based on natural language search, so the user of the system can write queries without constraints. HIVE keyword search is described in detail in José R. Pérez-Agüera, Javier Arroyo, Jane Greenberg, Joaquin Perez-Iglesias and Victor Fresno. Using BM25F for Semantic Search. Semantic Search Workshop at the 19th Int. World Wide Web Conference WWW2010 April 26, 2010 (Workshop Day), Raleigh, NC, USA

```
System.out.println("Search by keyword:");
List<SKOSConcept> ranking = searcher.searchConceptByKeyword("accidents");
System.out.println("Results in SKOSServer: " + ranking.size());
String uri = "";
String lp = "";
  for (SKOSConcept c : ranking) {
    uri = c.getQName().getNamespaceURI();
    lp = c.getQName().getLocalPart();
    QName qname = new QName(uri, lp);
    String origin = server.getOrigin(qname);
    if (origin.toLowerCase().equals("nbii")) {
      System.out.println("PrefLabel: " + c.getPrefLabel());
      System.out.println("\t URI: " + uri + " Local part: " + lp);
      System.out.println("\t Origin: " + server.getOrigin(qname));
    }
  }
```

### URI based search ###

Concepts can be retrieved using their URI:

```
System.out.println("Search by URI:");
SKOSConcept c2 = searcher.searchConceptByURI(
  "http://thesaurus.nbii.gov/nbii#", "Enzymatic-activity");
Concept c2 = searcher.searchConceptByURI(uri, lp);
List<String> alt = c2.getAltLabels();
System.out.println("PrefLabel: " + c2.getPrefLabel());
for (String a : alt) {
  System.out.println("\t altLabel: " + a);
}
System.out.println("\t Origin: " + server.getOrigin(c2));
System.out.println("\t SKOS Format: \n" + c2.getSKOSFormat());
\end{verbatim}

```

If you need get the children of a term given a URI you can use the following example.

```
SKOSConcept con = searcher.searchConceptByURI(
    "http://id.loc.gov/authorities/sh2001009743#", "concept");
TreeMap<String,QName> children = searcher.searchChildrenByURI(
  "http://id.loc.gov/authorities/sh2001009743#", "concept");
for (String c : children.keySet()) {
  System.out.println("prefLabel: " + c);
}
```


## Tagging documents with SKOS Tagger ##

One of the most important features of HIVE is the module for automatic metadata extraction. This is performed using the SKOSTagger class. This class is accesed via the SKOSServer method getSKOSTagger().

Once we have an SKOSTagger we can use the method getTags() to get the keywords for a given document. The arguments required for this method are the source of the text, usually a document, the list of vocabularies to normalize the keywords and a SKOSSearcher object. An example:

```
SKOSTagger tagger = server.getSKOSTagger();

String source = "/home/hive/Desktop/ag086e00.pdf";
source = "http://en.wikipedia.org/wiki/Biology";

List<String> vocabs = new ArrayList<String>();
vocabs.add("nbii");
vocabs.add("lcsh");
vocabs.add("agrovoc");

List<SKOSConcept> l = tagger.getTags(source, vocabs,
  server.getSKOSSearcher());
System.out.println();
System.out.println("Tagging Results for ALL");
for (SKOSConcept s : l) {
  System.out.println(s.getPrefLabel());
  System.out.println(s.getQName().getNamespaceURI());
}
```