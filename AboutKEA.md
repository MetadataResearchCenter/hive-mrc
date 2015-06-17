

# Introduction to KEA #

Problem: _How can we automatically identify the main topics of documents_?

The HIVE indexing service uses the [KEA++](http://www.nzdl.org/Kea/description.html) algorithm and open-source library for extracting keyphrases from documents using SKOS vocabularies.

KEA++ was developed by [Alyona Medelyan](http://www.medelyan.com/), based on earlier work by Ian Whitten (KEA) at the Digital Libraries and Machine Learning Lab at the University of Waikoto, New Zealand.

For more information, see:
  * Medelyan, O. and Whitten, I.H. (2008). Domain-independent automatic keyphrase indexing with small training sets. _Journal of the American Society for Information Science and Technology_ 59(7). DOI: 10.1002/asi.v59:7

# How KEA++ works #

The KEA algorithm is based on a Naïve Bayes classification system. Training data is used to build a statistical model which is used to recognize positive and negative examples. This statistical model is based on real world examples -- a corpus of documents with controlled terms assigned by hand, generally professional indexers.

Machine learning has two separate phases:
  * Training phase: Examples of solutions are used to explain to the system how the problem can be resolved. In general, these are documents indexed by human indexers using controlled vocabularies. From now this set of documents will be called trainingset. KEA is trained based on features of related phrases in the thesaurus.
  * Test phase: The system tries to solve new problems which are similar to the solved problems used to train the system. In HIVE, the new problems are documents that we want to index with vocabularies in HIVE.


# KEA++ Process #

After training, keyphrase extraction in KEA++ has two separate phases:
  * Candidate identification: Identify all of the possibly keyphrases in the document.
  * Keyphrase selection: Identify the most significant terms



KEA use a Machine Learning scheme that can be divided in the following parts:
  * Candidate identification using a set of features
    * Candidate extraction
    * Features identification
  * Filtering
    * Training the model using the training set
    * Extracting keyphrases from new documents that are not present in the training set

## Candidate identification ##

Candidate identification is the process of extracting the most representative keyphrases identified for each document.

Basic steps:
  * Parse the text into tokens based on whitespace and punctuation
  * Create word n-grams based on the longest term in the CV
  * Count the number of occurrences
  * Remove all stopwords from the n-gram
  * Stem to grammatical root (Porter) (aka "pseudophrase")
  * Stem vocabulary terms to root (Porter)
  * Replace non-descriptors with descriptors using synonym links
  * Match stemmed n-grams to stemmed vocabulary terms.

### Candidate extraction ###

Candidates are extracted from documents using some simple methods like
n-gram identification, stopwords filtering and stemming. KEA calls
this candidates pseudo-phrases. This pseudo phrases  are normalized
using the vocabularies. For example, phrases such as “algorithm
efficiency”, “the algorithms’ efficiency”, “an efficient algorithm”
and even “these algorithms are very efficient” are matched to the same
pseudo phrase “algorithm effici”, where “algorithm” and “effici” are
the stemmed versions for the corresponding full forms.

The result is a set of candidate index terms for a document, and their
occurrence counts. As an optional extension, the set is enriched with
all terms that are related to the candidate terms, even though they
may not correspond to pseudo-phrases that appear in the document. For
each candidate its one-path related terms, i.e. its hierarchical
neighbors (BT and NT in Agrovoc), and associatively related terms
(RT), are included. If a term is related to an existing candidate, its
occurrence count is increased by that candidate’s count. For example,
suppose a term appears in the document 10 times and is one-path
related to 6 terms that appear once each in the document and to 5 that
do not appear at all. Then its final frequency is 16, the frequency of
the other terms that occur is 11 (since the relations are
bidirectional), and the frequency of each non-occurring term is 10.
This technique helps to cover the entire semantic scope of the
document, and boosts the frequency of the original candidate phrases
based on their relations with other candidates.

In both cases—with and without related terms—the resulting candidate
descriptors are all grammatical terms that relate to the document’s
content, and each has an occurrence count. The next step is to
identify a subset containing the most important of these candidates.

## Features identification ##
The features which KEA use are the followings:

  * TF\*IDF: Number of documents containing the phrase in the global corpus (training set only)
  * Position of first occurence: Distance from the beginning of the document. Candidates with higher/lower values have higher relevance.
  * Length of the keyphrases (in words)
  * Node degree: Reflects how richly the term is connected in the
thesaurus graph structure. The “degree” of a thesaurus term is the
number of semantic links that connect it to other terms—for example, a
term with one broader term and four related terms has degree 5. KEA
considers three different variants:
  * the number of links that connect the term to other thesaurus terms
  * the number of links that connect the term to other candidate phrases
  * the ratio of the two.
**Appearance is a binary attribute that reflects whether the
pseudo-phrase corresponding to a term actually appears in the
document.**

## Filtering ##
### Training the model using the training set ###
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

### Extracting keyphrases from new documents that are not present in the training set ###
Once the model has been generated, we can offer our system to the
users to index new documents. To index a new document, we only need to
know the probability that have the keyphrase that occur in this new
document to be a GOOD or a BAD keyphrase. The computation of this
probability is quite simple. We need to compute both probabilities
based on the information which we have in our model and in the new
document. So, suppose just the two features $TF×IDF$ and position of
first occurrence are being used. When the Naïve Bayes model is used on
a candidate pseudo phrase with feature values t and f respectively,
two quantities are computed:

$P[yes ](.md) = (Y / Y + N) P\_tfidf [t | yes ](.md)  P\_distance[f | yes ](.md)$

and

$P[no ](.md) = (N / N + Y) P\_tfidf [t | no ](.md)  P\_distance[f | no ](.md)$

where Y is the number of positive instances in the training files—that
is, author-identified keyphrases—and N is the number of negative
instances—that is, candidate phrases that are not keyphrases.

The overall probability that the candidate phrase is a keyphrase can
then be calculated:

$P\_k = P[yes ](.md) / (P[yes ](.md)+P[no ](.md))$

Candidate phrases are ranked according to this value.