

# Installing HIVE Services #

This page describes how to install and initialize the HIVE services, including:

  * HIVE Web Service
  * Importing vocabularies into HIVE

## System Requirements ##

  * Java 1.6
  * Java servlet container/application server (Tomcat 6.x recommended)
  * See memory requirements below

## Memory Requirements ##

When using the KEA++ indexer with the H2-based vocabulary and all vocabularies loaded, HIVE requires ~512MB of memory.

To increase the maximum JVM heap size, use the -Xmx option. To limit the proportion of VM spent in garbage collection before an `OutOfMemoryError` is thrown, use `-XX:UseGCOverheadLimit`. In Tomcat, this can be set using the `CATALINA_OPTS` variable, as in `catalina.sh`:

```
CATALINA_OPTS="-Xmx512m -XX:-UseGCOverheadLimit"
```

For KEA++ memory requirements using the in-memory indexer, see the [HIVE memory usage](HIVEMemoryUsage.md) page.

## Installing Tomcat ##

The HIVE services can be installed using any Java servlet container or application server. For the purpose of this document, Tomcat 6.0 is assumed.

  * Download the latest [Tomcat 6.x release](http://tomcat.apache.org/download-60.cgi)
  * Follow the Tomcat [install instructions](http://tomcat.apache.org/tomcat-6.0-doc/setup.html)
  * Optional: Modify the `catalina.sh` to set `CATALINA_OPTS` for increased heap allocation (see memory requirements above).

Simple install steps:
  * Download Tomcat zip or .tar.gz file
  * Extract archive (`unzip *.zip` or `tar xfz *.tar.gz`)
  * `cd <tomcat_install>/bin`
  * `bin/startup.sh`
  * Goto http://localhost:8080/, confirm Tomcat startup page displays


## Installing the HIVE Web Service ##
  * [Download](http://code.google.com/p/hive-mrc/downloads/list) or build the `hiveweb.war`
  * Note: Due to a [limitation](http://claudiushauptmann.com/gwt-multipage/) in the version of GWT used by HIVE, the HIVE Web application must be installed as the ROOT webapp.
  * Extract the contents of the hiveweb.war into the ROOT webapp directory
```
  cd /path/to/tomcat6/webapps/ROOT
  unzip /path/to/hiveweb.war
```

  * Modify the `WEB-INF/conf/hive.properties` file. This file contains the list of vocabularies configured for this HIVE Web instance and the "tagger" used for automatic indexing.
```
  # Configured vocabularies
  hive.vocabulary = # vocabulary name, e.g., "lcsh"

  # Selected tagger
  hive.tagger = # "kea" or "dummy"
```

  * Create a `hive.vocabulary` row for each configured vocabulary.
  * Set the `hive.tagger` value to either "kea" for KEA++ indexing or "dummy" for basic indexer.
  * Create a file `<vocabulary>.properties` for each vocabulary listed in the `hive.properties` file.


## HIVE Sample Data ##

To get started quickly, you can also download the AGROVOC [sample data](http://code.google.com/p/hive-mrc/downloads/list). This sample data includes pre-initialized indexes and KEA++ model.

  1. Download and extract `hive-agrovoc-sample.zip` from the Downloads page
  1. Move the `hive-data` directory to the desired location (default is `/usr/local/hive/hive-data`)
  1. Copy `agrovoc.properties` to the `WEB-INF/conf` directory
  1. Edit `agrovoc.properties`, update paths to location of `hive-data` directory.


## Importing Vocabularies into HIVE ##

The HIVE services require the initialization of several indexes from one orm ore vocabularies in SKOS RDF/XML format. You will not be able to start the HIVE Web or Rest services until this initialization process is complete. This process creates a [Sesame](http://www.openrdf.org/) store, [Lucene](http://lucene.apache.org/) index, alphabetic and top-concept indexes from the source RDF. If [KEA++](http://www.nzdl.org/Kea/) indexing is enabled, the import process also creates and trains the KEA++ mode.

The import process is detailed in the ImportingVocabularies section. A brief summary of the requirements include:
  1. A vocabulary in SKOS RDF/XML format
  1. A HIVE vocabulary configuration file ((e.g., `<vocabulary>.properties`
  1. A hive.properties file
  1. If "kea" indexing is enabled, a set of training documents