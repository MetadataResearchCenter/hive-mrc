

# Introduction #

This document describes the process used to convert a vocabulary to SKOS RDF/XML for import into HIVE.

## LCSH ##

SKOS versions of LCSH can be downloaded directly from the Library of Congress. No conversion is required.

  * http://id.loc.gov/download/
    * Select "LCSH RDF/XML"

## AGROVOC ##

SKOS versions of AGROVOC can be downloaded directly from the FAO:

  * http://aims.fao.org/website/Download/sub

## NBII ##

The NBII Biocomplexity Thesaurus is available as a SKOS web-service. The NBII data is also available for direct download in CSV and XML format. XML is currently used by HIVE for SKOS conversion.

  * http://my.nbii.gov
    * Directory > Browse directory
      * NBII Thesaurus
        * repgen.csv (3/6/2009)
        * repgen.xml (3/6/2009)
      * Latest update: 3/6/2009 as of 1/26/2011
  * Open SKOSCoverter project
    * Edit utils.myreaders.xml.NBIIXMLReader
      * In main(), specify path to repgen.xml and output directory for nbii.rdf
    * Run NBIIXMLReader
  * Note: NBII


## ITIS ##

Integrated Taxinomic Information System (ITIS) is available in a variety of formats. MySQL is currently used by HIVE for SKOS conversion.

  * Requires MySQL
  * Download ITIS (http://www.itis.gov/downloads/itisMySQL121610_v3.TAR.gz)
  * Extract archive
  * Follow instructions in README.txt to import ITIS data
  * Creates database ITIS
  * Open ITIS2SKOS project
  * Modify SKOSGenerator:
    * Specify itis.rdf output directory
    * Specify MySQL connection information
  * ` edu.unc.ils.mrc.hive.converter.itis.ITISConverter `

## TGN ##

TGN is available in relational and XML formats. Access to source TGN vocabularies requires licensing from the Getty. After you receive your TGN access information

  * Extract tgn\_rel\_10\_utf8\_zip
  * Open TGN2SKOS
  * Edit TGNReader, update path to source directory and output path
  * ` edu.unc.ils.mrc.hive.converter.tgn.TGNReader `

## LTER ##
SKOS versions of LTER can be downloaded directly subversion:

  * https://svn.lternet.edu/websvn/filedetails.php?repname=NIS&path=%2Fcontrib%2Flter-hive%2Fhive-data%2Flter%2Flter.rdf

## MeSH ##

Note: The SKOS version of MeSH available from http://thesauri.cs.vu.nl/eswc06 is not compatible with HIVE. A new converter has been written based on the MeSH XML source files.

MeSH is available for download in XML format. Access to MeSH vocabularies requires submitting an agreement form.

  * Download desc2011.xml from  http://www.nlm.nih.gov/mesh/filelist.html
  * ` java -Xmx1024m edu.unc.ils.mrc.hive.converter.mesh.MeshConverter <path_to_desc2001_xml> <path_to_output_RDF> `
  * Conversion requires ~1GB Ram. Specify java options -Xmx1024m