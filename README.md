# The soapUI project

This is the main soapUI project

## Structure

* *soapui* - The core module
* *maven-plugin-tester* – [The Maven plugin](http://www.soapui.org/Test-Automation/maven-2x.html) used for running soapUI in a CI environment
* *soapui-maven-plugin-tester* - A small test `pom.xml` used for testing the *soapui-maven-plugin*

## Building and running

### Prerequisite

* [JDK (version 1.6 or higher)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven (version 2.0 or higher)](http://maven.apache.org/)
* [Git (version 1.8 or higher)](http://git-scm.com)

`mvn clean install`