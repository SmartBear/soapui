# The soapUI project

This is the main soapUI project

## Structure and submodules

* *[soapui](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3/soapui)* - The core module
* *[soapui-maven-plugin](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3/soapui-maven-plugin)* – [The Maven plugin](http://www.soapui.org/Test-Automation/maven-2x.html) used for running soapUI in a CI environment
* *[soapui-maven-plugin-tester](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3/soapui-maven-plugin-tester)* - A small test `pom.xml` used for testing the *soapui-maven-plugin*

## Building and running

### Prerequisite

* [JDK (version 1.6 or higher)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven (version 2.0 or higher)](http://maven.apache.org/)
* [Git (version 1.8 or higher)](http://git-scm.com)

### Building

* To build the whole soapUI project including all submodules, just run `mvn clean install` in the root folder.
* To build the individual submodules run `mvn clean install` the root folder of the submodule you want to build.

### Running

The easiest way to run soapui after it has been build is by executing `mvn exec:java -Dexec.mainClass="com.eviware.soapui.SoapUI"` in the root folder of the *soapui* submodule.

### IDE support

As soapUI is using a standard Maven 3 setup, building and running soapUI from an IDE is usually very straight forward as long as IDE has good Maven support.

**Intellij IDEA (version 11)**

* Go to *File* -> *New project* -> *Import project from external module* -> *Maven* -> At *Root directory* enter the path to the root folder of the soapUI project and then finish the wizard.
* After you have created the IDEA project sucessfully, just navigate to the main class `com.eviware.soapui.Soapui` right click and select *Run*. 

**Eclipse (version 4 / Juno)**
TODO!
