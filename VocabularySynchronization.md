# Vocabulary Synchronization #

One of the challenges of a centralized vocabulary service is keeping the vocabularies up-to-date with the external sources. For example, the Library of Congress publishes vocabulary changes as [Atom](http://en.wikipedia.org/wiki/Atom_%28standard%29) feed via http://id.loc.gov/authorities/feed/. Changes are loaded every 3-4 weeks.

With the 2.0 release, the HIVE service includes support for synchronization of the LCSH vocabulary with the LC Atom feed. This page describes the implementation.

## lcsh.properties ##

Two new optional properties have been added to the `lcsh.properties` file:
  * `creationDate`:  Creation date of the SKOS/RDF file. This is used to determine the last update date during the initial synchronization.
  * `atomFeedURL`: URL of the atom feed used for synchronization.

## Database Schema Changes ##

The HIVE 2.0 release introduces a new SETTINGS table in the H2 database for each vocabulary. The SETTINGS table includes a new column LAST\_UPDATE that is updated during each import or add/update/remove of a concept. The last update date is used during sychronization.


## HIVE Core ##

A new class `edu.unc.ils.mrc.hive.sync.lcsh.AtomSynchronizer` has been added to the `hive-core.jar`. This class implements the actual synchronization process -- reading updated concepts from the atom feed and acting on the HIVE data store. Adds, updates, and removes are applied across each of the HIVE indexes (Sesame, Lucene, H2).


## HIVE Web ##

Automatic synchronization has been implemented using the [Quartz](http://www.quartz-scheduler.org/) scheduler in the `hiveweb` web application.

Configuration:
  * `quartz-jobs.xml`: Specifies the synchronization job class and the cron-based trigger configuration (how frequently the updates run)
  * `quartz.properties`: General Quartz configuration

Synchronization job:
  * `edu.unc.hive.sync.SyncJob` implements the Quarts `Job` interface. Currently this job only updates the LCSH vocabulary using the above `AtomSynchronizer` class.