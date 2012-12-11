![The soapUI logo](http://www.soapui.org/images/stories/homepage/soapUI_header_logo.png)
# The soapUI project

This is the main soapUI project.

## Structure and submodules

* *[soapui](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3/soapui)* - The core module.
* *[soapui-maven-plugin](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3/soapui-maven-plugin)* – A Maven plugin used for running soapUI in a Continuous integration environment (such as [Jenkins](http://jenkins-ci.org)).
* *[soapui-maven-plugin-tester](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3/soapui-maven-plugin-tester)* - A small test `pom.xml` used for testing the *soapui-maven-plugin*.

## Building and running

### Prerequisite

* [JDK (version 1.6 or higher)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven (version 2.0 or higher)](http://maven.apache.org/)
* [Git (version 1.8 or higher)](http://git-scm.com)

### Building

* To build the whole soapUI project including all submodules, run `mvn clean install` in the root folder.
* To build a individual submodule run `mvn clean install` the root folder of the submodule you want to build.

### Running

To run soapUI after it has been built, execute `mvn exec:java` in the root folder of the *soapui* submodule.

### IDE support

As soapUI is using a standard Maven 3 setup, building and running soapUI from an IDE is usually very straight forward, as long as the IDE has good Maven support.

**Intellij IDEA (version 11)**

* Go to *File* -> *New project* -> *Import project from external module* -> *Maven* -> At *Root directory* enter the path to the root folder of the soapUI project and then finish the wizard.
* After you have created the IDEA project sucessfully, navigate to the main class `com.eviware.soapui.SoapUI` right click and select *Run*. 

**Eclipse (version 4 / Juno)**
TODO!

## Documentation
To generate documentation ([JavaDoc](http://www.oracle.com/technetwork/java/javase/documentation/index-jsp-135444.html)) run `mvn javadoc:javadoc` in the root folder of the submodule you want documentation for. The documentation will be located in the `target/site/javadoc` folder after the Maven command has terminated successfully. 

You can also get the latest JavaDoc for soapUI [here](http://www.soapui.org/apidocs).


## Additional resources
* *[soapUI Groovy examples](https://github.com/SmartBear/soapui-groovy-examples)* - This is a collection of soapUI projects and Groovy Scripts used to demonstrate the Groovy scripting capabilities in soapUI.
* [Extending soapUI](http://www.soapui.org/Developers-Corner/extending-soapui.html)
* [Custom factories](http://www.soapui.org/Developers-Corner/custom-factories.html)
* [Integrating with soapUI](http://www.soapui.org/Developers-Corner/integrating-with-soapui.html)
