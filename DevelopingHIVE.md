

# Development Environment #


# Prerequisites #
  * Eclipse IDE for Java EE Developers
    * Subclipse (http://subclipse.tigris.org/update_1.6.x)
    * Google Plugin for Eclipse (http://code.google.com/eclipse/)
  * Apache Tomcat
  * Apache Ant
  * Subversion client
  * JDK 1.6

# Installation #

  * Download and install Tomcat
    * http://tomcat.apache.org/download-60.cgi
    * apache-tomcat-6.0.35 used for this documentation

  * Download and install Apache Ant (http://ant.apache.org/)
    * apache-ant-1.8.2 used for this documentation

  * Download and install GWT 2.4.0
    * http://code.google.com/webtoolkit/versions.html

  * Download and install Eclipse IDE for Java EE Developers
    * http://www.eclipse.org/downloads/
    * Eclipse Helios (3.7) used for this documentation

  * Install Subclipse Eclipse Plugin
    * Using the Eclipse Update Manager
    * Site: http://subclipse.tigris.org/update_1.6.x

  * Install Google Eclipse Plugin
    * Using the Eclipse Update Manager
    * Site: http://dl.google.com/eclipse/plugin/3.7
    * Select "Plugin" only

  * Configure Tomcat Server in Eclipse
    * Window > Preferences > Server > Runtime Environment (Note for Mac Users: Preferences are under Eclipse, not Window)
    * Add "Apache Tomcat v 6.0"

# Checkout the Projects #

  * Window > Preferences > General > Workspace
    * Text File Encoding: UTF-8
  * Window > Show View > Other > SVN Repositories
    * Add SVN Repository
      * Location: https://hive-mrc.googlecode.com/svn/trunk
  * Checkout hive-core
    * Basic Java project
  * Checkout hive-rs
    * Select "Checkout as a project configured using the New Project Wizard"
    * Select "Web > Dynamic Web Project"
    * Content directory: WebRoot
  * Checkout hive-web
    * Select "Checkout as a project configured using the New Project Wizard"
    * Select "Web > Dynamic Web Project"
    * Select "Target Runtime" (Tomcat 6 requires Dynamic Web module version 2.5)
    * Output path: war/WEB-INF/classes
    * Content directory: war
    * Project > Properties > Google > Web Toolkit
      * Check "Use Web Toolkit"
      * Select "Configure SDKs..."
      * Web Toolkit > Add > Path to gwt 2.4.0
    * Project > Properties > Java Build Path > Order and Export
      * Move GWT SDK to the top of the order

# Vocabulary Quick Start #

Before you can run the hive-web or hive-rs projects, you need to initialize the HIVE indexes with one or more vocabularies. For a quick start, download and configure the sample HIVE vocabulary:
  * Download and extract hive-agrovoc-sample.zip from http://code.google.com/p/hive-mrc/downloads/
  * In the hive-web project, edit WEB-INF/conf/hive.properties
    * Comment out all vocabularies except agrovoc
      * Select hive.tagger = kea
    * In the hive-web project, edit WEB-INF/conf/agrovoc.properties
      * Change the path to the location of your extracted agrovoc sample data

For more advanced users, review the ImportingVocabularies page.

# Running HIVE in Eclipse #

  * Window > Show View > Servers
  * New Server
    * Do not add projects here
    * Start/stop server to confirm it is working
  * Open Server
    * Select Modules tab
    * Add Web Module ...
    * Select module "hive-web"
    * Document base: hive-web
    * Path: / (Root)

# Working with GWT (hive-web only) #
Changes to GWT classes require recompile. GWT compile can be started manually or via the "gwtc" and "build" Ant steps:

To compile GWT manually:
  * Select the hive-web project
  * Select Google > GWT Compile
    * Entrypoint module: ConceptBrowser
  * This will generate the war/hivewebrowser directory

To compile GWT as part of the Ant build
  * ` ant gwtc `  or ` ant build `


If you see the following error, move the GWT SDK to the top of the build order. (Project > Properties > Java Build Path > Order and Export):

```
[ERROR] Unexpected
java.lang.NoSuchMethodError: org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding.closestReferenceMatch()Lorg/eclipse/jdt/internal/compiler/lookup/ReferenceBinding; 
```

# Building HIVE in Eclipse #

# Building HIVE with Ant #

# Debugging GWT #
  * Project > Debug As > Google Web Application
    * Select "home.html"
    * Launches GWT in Hosted Mode

  * Troubleshooting:
    * "You must use a 32-bit Java runtime to run GWT Hosted Mode."
      * http://code.google.com/p/google-web-toolkit/issues/detail?id=135
      * Install a 32-bit JRE
        * Download JRE http://www.oracle.com/technetwork/java/javase/downloads/index.html
        * In Eclipse
          * Window > Preferences > Java > Installed JREs
            * Add 32-bit JRE
          * Project > Properties
            * Run/Debug Settings > JRE > Alternate JRE
              * Select 32-bit JRE
      * "libstdc++.so.5: cannot open shared object file: No such file or directory"
        * yum install compat-libstdc++-33-3.2.3-61
      * Unknown argument: -codeServerPort
        * Debug Configuration > Arguments
          * Change "codeServerPort" to "portHosted"