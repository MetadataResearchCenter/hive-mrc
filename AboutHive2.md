# New Features #

This release includes a major restructuring of the HIVE vocabulary storage model and upgrade of the Sesame store that will require re-initialization of each vocabulary.

  * LCSH [Synchronization](VocabularySynchronization.md)
  * Sample [autocomplete](Autocomplete.md) implementation
  * Upgrade to Sesame 2.4 and Tika 0.7
  * Finalized ITIS converter
  * Moved alpha and top-concept indexes to the H2 database


## LCSH Synchronization ##

See the [synchronization](VocabularySynchronization.md) page.

## Autocomplete widget ##

The HIVE 2.0 release introduces a new Lucene-based autocomplete index and a simple widget to demonstrate integration of HIVE with external services.

## Term suggestion widget ##

The term suggestion widget is a simple example of how HIVE can be integrated into external services.

## Sesame upgrade ##

To support the HIVE 2.0 API features (update/remove), the Sesame triple store was upgraded. Earlier versions of the Sesame store had notable performance problems when removing items.

## ITIS converter ##

The preliminary version of the ITIS-to-SKOS converter did not include support for common names as alternate labels.

## Alpha and top-concept indexes ##

In earlier versions of HIVE, the alpha and top-concept indexes were stored as serialized maps loaded during service initialization. For larger vocabularies, these maps required significant memory. By moving these indexes into the existing H2 database, overall memory requirements have been reduced.