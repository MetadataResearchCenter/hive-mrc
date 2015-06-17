# Overview #

The HIVE 2.0 release introduces a simple Lucene-based autocomplete feature.

  * http://hive.nescent.org/autocomplete/sample.html


## HIVE Core ##
### vocabulary.properties ###

A new `autocomplete` property has been added to the `vocabulary.properties` file. This property specifies the path to the directory that will contain the autocomplete index.

### AdminVocabularies ###

A new flag `-x` has been added to the `AdminVocabularies` class to support initialization of the `autocomplete` index:

`java edu.unc.ils.mrc.hive.admin.AdminVocabularies -c <path-to-conf-dir> -v <vocabulary> -x `

Specifying this flag will create the autocomplete index.

### Autocomplete class ###

A new class `edu.unc.ils.mrc.hive.ir.lucene.search.Autocomplete` has been added to the `hive-core.jar`. This class handles the initialization of the autocomplete Lucene index and term suggestion (search).

## HIVE Web ##

### Autocomplete Servlet ###

A new servlet `org.unc.hive.servlet.AutocompleteServlet` has been added to the `hiveweb` web application. This servlet accepts two parameters `cv` and `term` and returns a JSON-based response with suggested terms.

### Autocomplete Widget ###

A simple JQuery-based autocomplete widget has been added to the project to demonstrate one possible integration approach.