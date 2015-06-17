# Installing the HIVE Rest Service #

## Install Tomcat ##

See the Tomcat section of [InstallingHiveWebServices](InstallingHiveWebServices.md) for instructions installing Tomcat 6..

## Install HIVE-RS WAR ##

  * Download the [latest](http://hive-mrc.googlecode.com/files/hive-rs-1.0.war) HIVE REST archive
  * `cd /path/to/tomcat6/webapps`
  * `unzip /path/to/hive-rs-1.0.war`

### Install HIVE-web ###

Download and install the HIVE-web application:

  * `sudo wget http://hive-mrc.googlecode.com/files/hiveweb-1.0.war`
  * `cd /usr/share/tomcat6/webapps`
  * Remove the existing ROOT webapp, if present
  * `mkdir ROOT`
  * `cd ROOT`
  * `unzip path/to/hive-web-1.0.war`

## Install HIVE-web Data ##

Download the sample HIVE index data.
  * `sudo mkdir /usr/local/hive`
  * `cd /usr/local/`
  * `sudo wget http://hive-mrc.googlecode.com/files/hive-agrovoc-sample.zip`
  * `sudo unzip hive-agrovoc-sample.zip`

Data directory must be ownded by tomcat user:
  * `chown -R tomcat:tomcat /usr/local/hive`

## Configure HIVE ##

Modify the default HIVE configuration:
  * `cd /path/to/tomcat6/webapps/hive-rs/WEB-INF/conf`
  * `vi hive.properties`
    * Comment out all vocabularies except agrovoc}}}
    * Change "dummy" to "kea"
  * `vi agrovoc.properties`
    * Change paths to `/usr/local/hive/hive-data`

Start Tomcat and Test.