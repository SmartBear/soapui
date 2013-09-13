# The SoapUI project

This is the main SoapUI project.

## Structure and submodules

* *[soapui](soapui)* - The core module.
* *[soapui-installer](soapui-installer)* - The installer module for creating packages.
* *[soapui-maven-plugin](soapui-maven-plugin)* – A Maven plugin used for running soapUI in a Continuous integration environment (such as [Jenkins](http://jenkins-ci.org)).
* *[soapui-maven-plugin-tester](soapui-maven-plugin-tester)* - A small test `pom.xml` used for testing the *soapui-maven-plugin*.

## Building and running

### Prerequisite

* [JDK (version 1.6 or higher)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven (version 2.0 or higher)](http://maven.apache.org/)
* [Git (version 1.8 or higher)](http://git-scm.com)

### Getting the source code for the first time

To get the source code run `git clone https://github.com/SmartBear/soapui.git` where you want to the root folder of the SoapUI project.

### Building

* To build the SoapUI project run `mvn clean install` in the root folder.
* To build a individual submodule run `mvn clean install` the root folder of the submodule you want to build.

### Running

To run SoapUI after it has been built, execute `mvn exec:java` in the root folder of the *soapui* submodule.

### Getting the latest changes

To get the latest source code changes clone the project by running `git pull` in the root directory of the SoapUI project or the root directory of the submodule you want to update.

## Contributing

When you have decided that you want to take a more active part in improving SoapUI then there are a few things you have to do before you can get started. Go to http://www.soapui.org/Developers-Corner/contribute-to-soapui.html for more information.

### IDE support

As SoapUI is using a standard Maven 3 setup, building and running SoapUI from an IDE is usually very straight forward, as long as the IDE has good Maven support.

**Intellij IDEA (version 11)**

* [Clone the project](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3#getting-the-source-code-for-the-first-time)
* In the main menu select *File* -> *New project* -> *Import project from external module* -> *Maven* -> At *Root directory* enter the path to the root folder of the SoapUI project and then finish the wizard.
* After you have created the IDEA project sucessfully, navigate to the main class `com.eviware.soapui.SoapUI` right click and select *Run*. 
* To get the latest changes right click on the root folder in the *Project* view and select *Git* -> *Repository* -> *Pull...*

**Eclipse (version 4 / Juno)**

* [Clone the project](https://github.com/SmartBear/soapui/tree/SOAPUI-3838-Convert-to-maven3#getting-the-source-code-for-the-first-time)
* In the main menu select *File* -> *Import* -> *Maven* -> *Existing Maven projects* and at *Root directory* enter the path to the root folder of the SoapUI project and then finish the wizard.
* After you have created the Eclipse project sucessfully, navigate to the main class `com.eviware.soapui.SoapUI` right click and select *Run as* -> *Java application*
* To get the latest changes right click on the root folder in the *Package explorer* view and select *Team* -> *Pull*

## Documentation
To generate documentation ([JavaDoc](http://www.oracle.com/technetwork/java/javase/documentation/index-jsp-135444.html)) run `mvn javadoc:javadoc` in the root folder of the submodule you want documentation for. The documentation will be located in the `target/site/javadoc` folder after the Maven command has terminated successfully. 

You can also get the latest JavaDoc for SoapUI [here](http://www.soapui.org/apidocs).


## Additional resources
* *[SoapUI Groovy examples](https://github.com/SmartBear/soapui-groovy-examples)* - This is a collection of SoapUI projects and Groovy Scripts used to demonstrate the Groovy scripting capabilities in SoapUI.
* [Extending SoapUI](http://www.soapui.org/Developers-Corner/extending-soapui.html)
* [Custom factories](http://www.soapui.org/Developers-Corner/custom-factories.html)
* [Integrating with SoapUI](http://www.soapui.org/Developers-Corner/integrating-with-soapui.html)
