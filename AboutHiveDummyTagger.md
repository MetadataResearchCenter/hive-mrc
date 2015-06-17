The `DummyTagger` implementation uses a [part-of-speech tagger](http://alias-i.com/lingpipe/demos/tutorial/posTags/read-me.html) and HIVE Lucene index. The `Postagger` class is apparently based on an example from the Ling-Pipe project, uses the lingpipe libraries, and the "medical" POS model (medtag).

### Building the LingPipe MedTag model ###


  * cd C:\mrc\nlp\lingpipe-4.0.1\demos\tutorial\posTags
  * ant -Ddata.pos.medpost=c:\mrc\nlp\medtag\medpost train-medpost
  * dir ..\..\models

```
01/03/2011  09:59 AM         4,974,338 pos-enbio-medpost.HiddenMarkovModel
```

  * The `TrainPostagger` class is taken directly from the lingpipe posTag tutorial.

```
java -cp build\classes;..\..\..\lingpipe-4.0.0.jar TrainMedPost c:\mrc\nlp\medtag\medpost myModel
```

### DummyTagger Algorithm ###

From `DummyTagger.extractKeyphrases`
  * Write text to in-memory Lucene index (IndexWriter)
  * Generate dictionary (POS/word) using `Postagger`
  * Read document using Lucene `IndexReader`
  * Get the term frequency vector
  * Get terms and frequencies
  * Calculate probability?
  * Add term to “Documento” and “Vocabulario”
  * Add Document to Colleccio
  * Calculate vocabulary probabilities
  * Calculate collection divergences?
  * For each “document” in the Coleccio
    * For each term in document
      * If TF > 0.1 and term “isAllowed” (valid part of speech) and term length > 1
        * Add term to ranking?
    * For each term in ranking
      * Add to keywords
    * Return keywords

### Postagger Performance ###

  * The Postagger loads the HMM for every instantiation.
  * Tokenization is slow
  * HMM.firstBest is very slow