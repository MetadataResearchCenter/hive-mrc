The basic steps for deploying the HIVE web application are:
1. go into the hive-web directory
2. edit the build.properties file to have correct paths
3. run "ant deploy"
4. start (or restart) Tomcat
5. load vocabularies as described below

To use the HIVE core library in an application, compile the hive-core
directory (sorry, there isn't a buildfile there yet, but Eclipse or
another IDE should be able to do it for you).

NOTE: HIVE does not currently come with any vocabularies. You can
download the LCSH vocabulary from http://id.loc.gov, or find another
SKOS vocabulary to load.

------------------------------------------------------------------------
1- Importing Vocabularies

HIVE is capable to import any vocabulary from a RDF/SKOS file.

In order to import a new vocabulary you have to accomplish the following two steps:

	1- Create a configuration file where you include the paths to different files and indexes that will be generated in your serevr by HIVE import tools. 
		You can see how to create this configuration file bellow*
	2- Run edu.unc.ils.mrc.hive.admin.AdminVocabularies class on your console. The parameters that you have to include 
		are the following:
	
	1- Path to configuration directory
	2- Name of the vocabulary
	3- Activate training option for KEA algorithm (optional, If you don't train your system, you can not use automatic indexing classes)
	
	Ej (with train). java Adminvocabularies /home/hive/conf lcsh train
	
	Ej (without train). java Adminvocabularies /home/hive/conf lcsh
	
1.1 How to create your configuration file*

Each vocabulary have a different configuration file. This file have the following data:

#Vocabulary data
name = NBII
longName = CSA/NBII Biocomplexity Thesaurus
uri = http://thesaurus.nbii.gov

#Sesame Store
store = /home/hive/hive-data/nbii/nbiiStore

#Lucene Inverted Index
index = /home/hive/hive-data/nbii/nbiiIndex

#Alphabetical Index
alpha_file = /home/hive/hive-data/nbii/nbiiAlphaIndex

#Top Concept Index
top_concept_file = /home/hive/hive-data/nbii/nbiiTopConceptIndex

#KEA data files
stopwords = /home/hive/hive-data/nbii/nbiiKEA/data/stopwords/stopwords_en.txt
kea_training_set = /home/hive/hive-data/nbii/nbiiKEA/train
kea_test_set = /home/hive/hive-data/nbii/nbiiKEA/test
rdf_file = /home/hive/hive-data/nbii/nbii3.rdf
kea_model = /home/hive/hive-data/nbii/nbiiKEA/nbii

You must store this configuration file in the same directory that vocabularies file is stored. vocabularies file is the file that SKOSServer needs load vocabularies 
in the server in order to know what vocabularies will be opened when server starting and it is include in this distribution.

So, your HIVE configuration directory could looks like this:

conf/
	vocabularies
	nbii.properties
	lcsh.properties
	mesh.properties
	agrovoc.properties 
	...
	
1.2 How to train the automatic indexing algorithm

-----------------------------------------------------------------------

How is working KEA, the HIVE automatic metadata extraction algorithm?

The algorithm that we are using in HIVE is KEA++ http://www.nzdl.org/Kea/index.html, KEA++ has been
developed for Ian Witten's group from University of Waikakato. Last
version of KEA has been part of the Olena Medelyan phd. The other
interesting result of her thesis has been Maui, that is based on KEA http://code.google.com/p/maui-indexer/ 

The implementation that we are using in HIVE was very well explained
in this paper: http://www.cs.waikato.ac.nz/~olena/publications/efita2005_kea.pdf,
following I will try to explain how it works:

KEA use a Machine Learning approach for automatic keyphrase extraction:

Machine Learning approaches use to have two different phases:
- Training phase, where examples of solutions are used to explain to
the system how the problem can be resolved. In our case these
solutions are documents indexed by human indexers using controlled
vocabularies. From now this set of documents will be called training
set.
- Test phase, where the system try to solve new problems which are
similar to the solved problems used to train the system. In our case,
these problems to solve will be the documents that we want to index
using the vocabularies that we have in HIVE.

KEA use a Machine Learning scheme that can be divided in the following parts:
1 - Candidate identification using a set of features
    1.1 Candidate extraction
    1.2 Features identification
2 - Filtering
    2.1 Training the model using the training set
    2.2 Extracting keyphrases from new documents that are not present
in the training set
3- Future work for us

1- Candidate identification
Candidate identification is the process where the most representative
keyphrases are identified for each document in the training set.
1.1 Candidate extraction
Candidates are extracted from documents using some simple methods like
n-gram identification, stopwords filtering and stemming. KEA calls
this candidates pseudo-phrases. This pseudo phrases  are normalized
using the vocabularies. For example, phrases such as "algorithm
efficiency", "the algorithms's efficiency", "an efficient algorithm"
and even "these algorithms are very efficient" are matched to the same
pseudo phrase "algorithm effici", where "algorithm" and "effici" are
the stemmed versions for the corresponding full forms.

The result is a set of candidate index terms for a document, and their
occurrence counts. As an optional extension, the set is enriched with
all terms that are related to the candidate terms, even though they
may not correspond to pseudo-phrases that appear in the document. For
each candidate its one path related terms, i.e. its hierarchical
neighbors (BT and NT in Agrovoc), and associatively related terms
(RT), are included. If a term is related to an existing candidate, its
occurrence count is increased by that candidates count. For example,
suppose a term appears in the document 10 times and is one path
related to 6 terms that appear once each in the document and to 5 that
do not appear at all. Then its final frequency is 16, the frequency of
the other terms that occur is 11 (since the relations are
bidirectional), and the frequency of each non-occurring term is 10.
This technique helps to cover the entire semantic scope of the
document, and boosts the frequency of the original candidate phrases
based on their relations with other candidates.

In both cases with and without related terms the resulting candidate
descriptors are all grammatical terms that relate to the document's
content, and each has an occurrence count. The next step is to
identify a subset containing the most important of these candidates.

1.2 Features identification
The features which KEA use are the followings:
- TF*IDF
- First occurrence position of the keyphrase in the document
normalized by document length
- Length of the keyphrases (in words, which boots multiterms)
- Node degree reflects how richly the term is connected in the
thesaurus graph structure. The "degree" of a thesaurus term is the
number of semantic links that connect it to other terms for example, a
term with one broader term and four related terms has degree 5. KEA
considers three different variants:
    - the number of links that connect the term to other thesaurus terms
    - the number of links that connect the term to other candidate phrases
    - the ratio of the two.
- Appearance is a binary attribute that reflects whether the
pseudo-phrase corresponding to a term actually appears in the
document.

2 Filtering
2.1 Training the model using the training set
KEA use a classifier to create a model for good and bad keyphrases. In
Machine Learning clustering and classification are very common
techniques. In this case we have a classifier. Classifiers can be
understood using the same sense that we use when we classify books. In
these example we hay to classify keyphrases in only two classes: GOOD
Keyphrases (these keyphrases that were used by human indexers in our
training set) and BAD keyphrases (these keyphrases that, although
appears in the documents that we are using to train the system, has
not been used by our human indexer to index the documents). These kind
of binaries classifiers are very common in automatic classification
and there exits a lot of methods to classify elements. KEA use a
method named Naive Bayes, which is very simple and effective. When we
finish to classify the keyphrases that appears in our training set, we
have a model that we can use to estimate the probability that a
keyphrase have to be a good or a bad keyphrase for a new document,
based on the model that we are generated using our training set.

2.2 Extracting keyphrases from new documents that are not present in
the training set
Once the model has been generated, we can offer our system to the
users to index new documents. To index a new document, we only need to
know the probability that have the keyphrase that occur in this new
document to be a GOOD or a BAD keyphrase. The computation of this
probability is quite simple. We need to compute both probabilities
based on the information which we have in our model and in the new
document. So, suppose just the two features TF*IDF and position of
first occurrence are being used. When the Naive Bayes model is used on
a candidate pseudo phrase with feature values t and f respectively,
two quantities are computed:

P[ yes ] = (Y / Y + N) P_tfidf [ t | yes ]  P_distance[ f | yes ]

and

P[ no ] = (N / N + Y) P_tfidf [ t | no ]  P_distance[ f | no ]

where Y is the number of positive instances in the training files that
is, author identified keyphrases and N is the number of negative
instances that is, candidate phrases that are not keyphrases.

The overall probability that the candidate phrase is a keyphrase can
then be calculated:

P_k = P[ yes ] / (P[ yes ]+P[ no ])

Candidate phrases are ranked according to this value.
