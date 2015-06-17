The HIVE REST service was developed by Duane Costa of the Long-Term Ecological Research Network (LTERNet). The HIVE REST interface provides JAX-RS based RESTful access to the HIVE Core API.

The HIVE REST service exposes the following interfaces:
  * Schemes Resource: Scheme information
  * Concepts Resources: Access to concepts within a scheme
  * Searcher Resources: Keyword searching

## About the RESTful Web Services ##

The RESTful approach to web services was developed in response to other web services paradigms, such as Simple Object Access Protocol (SOAP). REST relies on the HTTP protocol, universal resource indicators (URI), and standard data formats (e.g., XML) to implement interfaces to web services.

See Also:
  * [A RESTful Approach to Web Services](http://www.networkworld.com/ee/2003/eerest.html)
  * [RESTful Web Services](http://www.oracle.com/technetwork/articles/javase/index-137171.html) at Oracle
  * [Representational State Transfer](http://en.wikipedia.org/wiki/Representational_State_Transfer) at Wikipedia

## HIVE REST API ##
This section demonstrates the various methods exposed by the HIVE REST interface. For more information, see the [API documentation](http://hive-mrc.googlecode.com/svn/trunk/doc/hive-restful-web-services-api.txt).

### Schemes Resource ###

  * http://hive.nescent.org/hive-rs/schemes/schemeNames
  * http://hive.nescent.org/hive-rs/schemes/nbii/lastDate
  * http://hive.nescent.org/hive-rs/schemes/nbii/longName
  * http://hive.nescent.org/hive-rs/schemes/nbii/numberOfConcepts
  * http://hive.nescent.org/hive-rs/schemes/nbii/numberOfRelations
  * http://hive.nescent.org/hive-rs/schemes/nbii/schemaURI
  * http://hive.nescent.org/hive-rs/schemes/nbii/alphaIndex
  * http://hive.nescent.org/hive-rs/schemes/nbii/subAlphaIndex/a
  * http://hive.nescent.org/hive-rs/schemes/nbii/topConceptIndex
  * http://hive.nescent.org/hive-rs/schemes/nbii/subTopConceptIndex/a

### Concept Resources ###

  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/prefLabels
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/prefLabels/a
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/altLabels
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/broaders
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/narrowers
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/prefLabel
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/QName
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/relateds
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/SKOSFormat
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/Companies/children

### Searcher Resources: Sample URLs ###

  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/SKOSFormat?keyword=activity
  * http://hive.nescent.org/hive-rs/schemes/nbii/concepts/concept/SKOSFormat?prefLabel=Eruptions


## See Also ##

[Sample LTER hive-rs URLs](http://hive-mrc.googlecode.com/svn/trunk/doc/sample-lter-hive-urls.txt)

[Full hive-rs API documentation](http://hive-mrc.googlecode.com/svn/trunk/doc/hive-restful-web-services-api.txt)

[Sample Article Abstract](http://hive-mrc.googlecode.com/svn/trunk/doc/sampleAbstract.txt) for testing tagging service