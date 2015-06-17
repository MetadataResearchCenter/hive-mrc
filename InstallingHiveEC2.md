### Workshop EC2 Instance ###
[HIVE EC2 Instance](http://ec2-50-16-28-148.compute-1.amazonaws.com:8080/home.html)

### Install Tomcat 6 ###
Use `yum` to install the standard Tomcat distribution:

  * `sudo yum install tomcat6`
  * (Optional) `sudo yum install tomcat6-webapps`
  * (Optional) `sudo yum install tomcat6-admin-webapps`

Start Tomcat
  * `sudo service tomcat6 start`

Open port 8080:
  * Access your AWS account
    * Select Security Groups > quick-start-1
    * Add HTTP > TCP > 8080 > 8080 > 0.0.0.0/0

Confirm that the instance is running:
  * http://your-ec2-instance.amazonaws.com:8080

Stop Tomcat
  * `sudo service tomcat6 stop`

### Install HIVE-web ###

Download and install the HIVE-web application:

  * `sudo wget http://hive-mrc.googlecode.com/files/hiveweb-1.0.war`
  * `cd /usr/share/tomcat6/webapps`
  * Remove the existing ROOT webapp, if present
  * `mkdir ROOT`
  * `cd ROOT`
  * `unzip path/to/hive-web-1.0.war`

## Install HIVE-web Data ##

Download the sample HIVE index data:
  * `sudo mkdir /usr/local/hive`
  * `cd /usr/local/`
  * `sudo wget http://hive-mrc.googlecode.com/files/hive-agrovoc-sample.zip`
  * `sudo unzip hive-agrovoc-sample.zip`

Data directory must be ownded by tomcat user:
  * `chown -R tomcat:tomcat /usr/local/hive`

## Configure HIVE ##

Modify the default HIVE configuration:
  * `cd /usr/share/tomcat6/webapps/ROOT/WEB-INF/conf`
  * `vi hive.properties`
    * Comment out all vocabularies except agrovoc}}}
    * Change dummy to kea
  * `vi agrovoc.properties`
    * Change paths to `/usr/local/hive/hive-data`

## Start Tomcat ##
  * `sudo service tomcat6 start`
  * `tail -f catalina.out`