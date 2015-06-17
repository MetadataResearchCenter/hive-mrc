

# Introduction #

The HIVE automatic metadata generation system is based on [KEA++](http://www.nzdl.org/Kea/description.html), an algorithm for controlled keyphrase indexing.

The KEA algorithm is based on a Naïve Bayes based classification system. Training data is used to build a statistical model which is used to recognize positive and negative examples. This statistical model is based on real world examples -- a corpus of documents with controlled terms assigned by hand.

The following instructions are based on http://www.nzdl.org/Kea/Download/Kea-5.0-Readme.txt


# Building a Keyphrase Extraction Model #

Before HIVE can extract controlled keyphrases for new documents, a keyphrase extraction model must be built from a set of pre-classified documents. For a given controlled vocabulary, training documents need to be copied to a central directory. For example, the following directory structure is currently used by HIVE.

```
   hive/
      conf/
         hive.properties
         vocabulary.properties

      vocabulary/
         vocabulary.rdf
         vocabularyKEA/
            train/
               training_file1.txt
               training_file1.key
      
```

  1. Download or convert the desired controlled vocabulary into SKOS RDF/XML format.
  1. Configure the vocabulary in HIVE.
  1. Identify a set of documents to train the keyphrase extractor. For examples, refer to the [AGROVOC sample](http://code.google.com/p/hive-mrc/downloads/detail?name=hive-agrovoc-sample.zip&can=2&q=).
  1. Create a directory that will contain the documents used to train the keyphrase extractor (e.g., "train").
  1. Documents must be in plain text format. For PDFs, see TrainingKEA#Converting\_PDFs\_to\_text below.
  1. Place author or indexer-assigned terms into a separate ".key" file. For example, if the document is called "doc1.txt", the file would be called "doc1.key". Each keyphrase must be on a separate line.
  1. Initialize the HIVE vocabularies with the "train" option.


To get good results, it is important that the input text for KEA is as
"clean" as possible. That means html tags etc. in the input documents
need to be deleted before the model is built and before keyphrases are
extracted from new documents. Also, make sure that you have enough documents in both training and extraction phase. For example, for training at least 20-30 manually indexed documents are required. It is important that manually assigned keyphrases in the files ".key" correspond to the entries in the controlled vocabulary that you use.

# Sample Files #

The following sample files are available in the [downloads](http://code.google.com/p/hive-mrc/downloads/) section. For a more complete example, download and extract [Agrovoc sample](http://hive-mrc.googlecode.com/files/hive-agrovoc-sample.zip) and review the files in the "agrovocKEA/training" directory.

  * [Sample training document](http://hive-mrc.googlecode.com/files/bostid_b02moe.txt)
  * [Sample key file](http://hive-mrc.googlecode.com/files/bostid_b02moe.key)

# Converting PDFs to Text #

HIVE uses the [Apache Tika](http://tika.apache.org/) toolkit for PDF conversion. To convert PDFs to text for use with HIVE, use the following:

```
java edu.unc.ils.mrc.hive.util.TextManager <path>
```