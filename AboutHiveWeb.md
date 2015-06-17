

# HIVE Web interface #

The HIVE Web interface is a GWT-based application intended to demonstrate the capabilities of the HIVE system and core API.

## The HIVE Web UI ##

The HIVE Web user interface includes three main components:
  * Home Page
  * Concept Browser
  * Indexing

### Home ###
The home page for the HIVE Web service introduces the HIVE project and displays statistics about each of the supported vocabularies. It is intended to help the user understand what HIVE is and what it can help them to accomplish.

### Concept Browser ###
The Concept Browser, displayed in figure \ref{browser}, is a central component of the HIVE Web interface. The goal of the Concept Browser is to allow users to browse and search concepts from heterogeneous vocabularies. It also demonstrates how multiple vocabularies can be integrated using a common format and user interface.

The Concept Browser also supports the visualization of the context of a selected concept by showing the relations.

The Concept Browser serves the following functions:
  * Allow the user to navigate different vocabularies
  * Allow the user to open and close a vocabulary
  * Allow user to search concepts in multiple vocabularies, and filtering the concepts based on user selection.
  * Visualize the hierarchical structure of user selected vocabulary for browsing.
  * Visualize the context of concepts to help user better understand difficult concept.
  * Display the concept in SKOS metadata schema.

<img src='http://hive-mrc.googlecode.com/svn/trunk/doc/HIVE-Documentation/img/browser.jpg' width='700' height='600' />


### Indexing ###

The HIVE indexing interface, displayed in \ref{indexing}, demonstrates the application of automatic keyphrase indexing using heterogeneous vocabularies. The HIVE indexer automatically extracts concepts from an uploaded document based on the user's selected vocabulary. Extracted concepts are displayed in a term cloud, with color coding to indicate the source of the term and font save to indicate relevance.

<img src='http://hive-mrc.googlecode.com/svn/trunk/doc/HIVE-Documentation/img/indexing.jpg' width='700' height='600' />

#### Multi-hop Website Indexing ####

As of HIVE version 1.0, indexing a website only used content from the first page of the specified URL. As of HIVE version 1.1, support has been added for "multi-hop" indexing. Using this feature, a simple web crawler will download text for all pages within the selected number of "hops" from the initial URL.


### Linked data for concepts ###

HIVE is a multiple-vocabulary system that allows access to concepts and vocabularies using a simple URL. HIVE can support the storage of millions of concepts from different vocabularies and make them available on the Web through simple HTTP calls. Vocabularies can be imported into HIVE using the SKOS RDF/XML format.

## Rich Internet Application ##

The HIVE Web user interface is a rich internet application (RIA) based on the Google Web Toolkit (GWT).

In a traditional web application, the user interface is used to display information and send requests to the server. Data storage and application logic are maintained server-side. The traditional web application model has several drawbacks, including:
  * Each request to the server requires a full page load, reducing the responsiveness of the system.
  * The user interface is static, intended to present information, rather than interactive.
  * The computing power of the user's system is underutilized.

The RIA model distributes part of the processing to the client side, so that the user can perform more complex interactions with the system without requesting data from the server. If the user's interaction requires data from the server, the client can selectively retrieve only the information that has changed.

Technologies for implementing RIA have rapidly emerged during recent years. AJAX, Silverlight, and Flex are all popular technologies. RIA applications based on Flash require the user to install Adobe Flash. RIA applications based on AJAX (Asynchronous Javascript and XML) has two major drawbacks:
  * Programming large amounts of JavaScript is error-prone and difficult to maintain and reuse
  * JavaScript suffers from numerous browser incompatibilities.

Many AJAX frameworks have been developed to address these issues.

Future work on HIVE Web UI will focus on the usability testing to gain a constructive understanding of impact of RIA technologies on the usability of interactive web application.

## Google Web Toolkit ##

The HIVE project is based on the Google Web Toolkit (GWT). Flash was not selected, because HIVE should be accessible from a standard web browser without installing additional software. GWT was selected because it allows developers to build and maintain large, high-performance JavaScript interfaces in the Java programming language, while also addressing common browser incompatibilities.


## See Also ##
  * [Installing HIVE Web](InstallingHiveWeb.md)
  * [How to import vocabularies](ImportingVocabularies.md)
  * [Developing](DevelopingHIVE.md)