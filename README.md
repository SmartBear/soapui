# SoapUI

## Structure and submodules

* *[soapui](soapui)* - The core module that creates the SoapUI .jar file.
* *[soapui-system-test](soapui-system-test)* - Integration and system tests for SoapUI.
* *[soapui-installer](soapui-installer)* - Creates SoapUI distributions, such as installers and archives.
* *[soapui-maven-plugin](soapui-maven-plugin)* – A Maven plugin used to run SoapUI in a Continuous integration environment (such as [Jenkins](http://jenkins-ci.org)).
* *[soapui-maven-plugin-tester](soapui-maven-plugin-tester)* - A test pom.xml file used to test the *soapui-maven-plugin*.
 
## Build and run

### Prerequisite

* [JDK 8 (version 1.8 or later)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven (version 3.0.0 or later)](http://maven.apache.org/)
* [Git (version 1.8 or later)](http://git-scm.com)

### Get the source code for the first time

To get the source code, run `git clone https://github.com/SmartBear/soapui.git` in the folder where you want to clone the root folder of the SoapUI project.

### Build

* To build a SoapUI project, run `mvn clean install` in the root folder.
* To build an individual submodule, run `mvn clean install` in the root folder of the submodule you want to build.

### Run

To run SoapUI after it has been built, execute `mvn exec:java` in the root folder of the *soapui* submodule.

### Get the latest changes

To get the latest source code changes, clone the project by running `git pull` in the root directory of the SoapUI project or in the root directory of the submodule you want to update.

## Contribute

If you want to take a more active part in improving SoapUI, go to [http://www.soapui.org/Developers-Corner/contribute-to-soapui.html](SoapUI Developer's Corner) for more information.

### IDE support

As SoapUI is using a standard Maven 3 setup, building and running SoapUI from an IDE is usually very straight forward, as long as the IDE has good Maven support.

#### [IntelliJ IDEA](https://www.jetbrains.com/idea/)

**Open project**

1. Clone the project.
2. In the main menu, select **File > Open**.
3. Enter the path to the root folder of the SoapUI project.
4. Run `mvn compile` to generate necessary source files automatically.

**Run SoapUI**

After you have created the IDEA project, navigate to the `com.eviware.soapui.SoapUI` class, right-click and select **Run**.

**Get the latest changes**

Right-click on the root folder in the **Project** panel and select **Git > Repository > Pull**.

**Tip**

To get the code style settings used by the SoapUI team, select **File > Import Settings** and import the `intellij-codestyle.jar` file located in the root folder of the SoapUI project.

#### [Eclipse](https://www.eclipse.org/ide/)

**Open project**

1. Clone the project
2. In the main menu, select **File > Import**.
3. In the **Import** dialog, select **Maven > Existing Maven projects** and click **Next**.
4. In the **Root directory**, enter the path to the root folder of the SoapUI project and click **Finish**.

**Run SoapUI**

After you have created the Eclipse project, navigate to the `com.eviware.soapui.SoapUI` class, right-click it and select **Run as > Java application**.

**Get the latest changes**

1. Right-click on the root folder in the **Package explorer** panel.
2. Select **Team > Pull**.

## Documentation
To generate documentation ([JavaDoc](http://www.oracle.com/technetwork/java/javase/documentation/index-jsp-135444.html)), run `mvn javadoc:javadoc` in the root folder of the submodule you want to create documentation for. The documentation will be located in the `target/site/javadoc` folder after the Maven command is executed successfully. 

You can also get the latest information on SoapUI classes and methods [here](http://www.soapui.org/apidocs).


## Additional resources
* *[SoapUI Groovy examples](https://github.com/SmartBear/soapui-groovy-examples)* - This is a collection of SoapUI projects and Groovy scripts used to demonstrate the Groovy scripting capabilities in SoapUI.
* [Extending SoapUI](http://www.soapui.org/Developers-Corner/extending-soapui.html)
* [Custom factories](http://www.soapui.org/Developers-Corner/custom-factories.html)
* [Integrating with SoapUI](http://www.soapui.org/Developers-Corner/integrating-with-soapui.html)

test!
