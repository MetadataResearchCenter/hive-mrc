By default, HIVE now uses KEA++ with an [H2](http://www.h2database.com/)-based vocabulary implementation, `kea.vocab.VocabularyH2`. Prior versions of HIVE used an in-memory implementation, `kea.vocab.VocabularySesame`. To improve indexing performance, you can switch to the in-memory implementation. Memory requirements are detailed below.

When using the KEA++ indexer with the in-memory vocabulary, depending on the size of the vocabularies used with HIVE, the service may require a significant amount of system memory.

To increase the maximum JVM heap size, use the -Xmx option. To limit the proportion of VM spent in garbage collection before an OutOfMemoryError is thrown, use -XX:UseGCOverheadLimit. In Tomcat, this can be set using the CATALINA\_OPTS variable, as in `catalina.sh`:

```
CATALINA_OPTS="-Xmx1512m -XX:-UseGCOverheadLimit"
```

Below are per-vocabulary memory requirements (this is in addition to basic Tomcat overhead). Memory requirements are slightly higher during the vocabulary import process.
|Agrovoc|115MB|
|:------|:----|
|LCSH   |800MB|
|MeSH   |100MB|
|NBII   |45MB |
|TGN    |1.5GB|

To operate HIVE with all vocabularies using the KEA++ indexer requires ~3.5GB of heap.