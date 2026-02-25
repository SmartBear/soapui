PostUI is a tool for reusing HTTP calls and API testing. It is simpler than POSTman.  It is a fork of [SoapUI](https://www.soapui.org/).

## Table of content

* [Structure and submodules](#structure-and-submodules)
* [Build and run](#build-and-run)
  * [Prerequisites](#prerequisites)
  * [Get the source code for the first time](#get-the-source-code-for-the-first-time)
  * [Build](#build)
  * [Run](#run)
  * [Get the latest changes](#get-the-latest-changes)
* [Contribute](#contribute)
  * [IDE support](#ide-support)
    * [IntelliJ IDEA](#intellij-idea)
    * [Eclipse](#eclipse)
* [Documentation](#documentation)
* [Advanced Functionality](#advanced-functionality)
* [Additional resources](#additional-resources)

## Structure and submodules

* *[soapui](soapui)* - The core module that creates the soapui.jar file.
* *[soapui-system-test](soapui-system-test)* - Integration and system tests for PostUI.
* *[soapui-installer](soapui-installer)* - Creates PostUI distributions, such as installers and archives.

## Build and run

### Prerequisites

* [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/)
* [Maven (version 3.6.3 or later)](http://maven.apache.org/)
* [Git (version 1.8 or later)](http://git-scm.com)

### Build

* To build a PostUI project, run `mvn clean install` in the root folder.
* To build an individual submodule, run `mvn clean install` in the root folder of the submodule you want to build.

### Run

To run PostUI after it has been built, execute `mvn exec:java` in the root folder of the *soapui* submodule.

### IDE support

As PostUI is using a standard Maven 3 setup, building and running PostUI from an IDE is usually very straight forward, as long as the IDE has good Maven support.

#### [IntelliJ IDEA](https://www.jetbrains.com/idea/)

**Open project**

1. Clone the project.
2. In the main menu, select **File > Open**.
3. Enter the path to the root folder of the PostUI project.
4. Run `mvn compile` to generate necessary source files automatically.

**Run PostUI**

After you have created the IDEA project, navigate to the `com.eviware.soapui.SoapUI` class, right-click and select **Run**.

**Get the latest changes**

Right-click on the root folder in the **Project** panel and select **Git > Repository > Pull**.

**Tip**

To get the code style settings used by the PostUI team, select **File > Import Settings** and import the `intellij-codestyle.jar` file located in the root folder of the PostUI project.

#### [Eclipse](https://www.eclipse.org/ide/)

**Open project**

1. Clone the project
2. In the main menu, select **File > Import**.
3. In the **Import** dialog, select **Maven > Existing Maven projects** and click **Next**.
4. In the **Root directory**, enter the path to the root folder of the PostUI project and click **Finish**.

**Run PostUI**

After you have created the Eclipse project, navigate to the `com.eviware.soapui.SoapUI` class, right-click it and select **Run as > Java application**.

**Get the latest changes**

1. Right-click on the root folder in the **Package explorer** panel.
2. Select **Team > Pull**.

## Documentation
To generate documentation ([JavaDoc](http://www.oracle.com/technetwork/java/javase/documentation/index-jsp-135444.html)), run `mvn javadoc:javadoc` in the root folder of the submodule you want to create documentation for. The documentation will be located in the `target/site/javadoc` folder after the Maven command is executed successfully. 

## Additional resources

* [SoapUI community](https://community.smartbear.com/t5/SoapUI-Open-Source/bd-p/SoapUI_OS) - SmartBear's SoapUI Open Source community for discussing and participating in all things SoapUI.
* [SoapUI Groovy examples](https://github.com/SmartBear/soapui-groovy-examples) - This is a collection of SoapUI projects and Groovy scripts used to demonstrate the Groovy scripting capabilities in SoapUI.
* [Extending SoapUI](http://www.soapui.org/Developers-Corner/extending-soapui.html)
* [Custom factories](http://www.soapui.org/Developers-Corner/custom-factories.html)
* [Integrating with SoapUI](http://www.soapui.org/Developers-Corner/integrating-with-soapui.html)
* [Mastering SoapUI](https://www.packtpub.com/web-development/mastering-soapui) - Master the art of testing and automating your SOA using SoapUI
