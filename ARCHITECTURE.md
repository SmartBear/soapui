# SoapUI Architecture and Testing Procedures

This document provides an overview of the SoapUI project's architecture and testing procedures.

## Overall Architecture

SoapUI is a multi-module Java project built with Apache Maven. The project is structured as follows:

*   **`soapui`**: The core module of the application, containing the main application logic, UI, and model.
*   **`soapui-maven-plugin`**: A Maven plugin for running SoapUI tests from the command line.
*   **`soapui-installer`**: A module for creating the application installer.
*   **`soapui-system-test`**: A module containing system tests for the application.

The project uses Java 21 and a variety of open-source libraries, including:

*   **Swing**: For the graphical user interface.
*   **Jetty**: For embedded web server capabilities.
*   **Apache XMLBeans**: For XML data binding.
*   **Apache Log4j**: For logging.
*   **Groovy**: For scripting and dynamic behavior.
*   **Cucumber**: For Behavior-Driven Development (BDD) testing.

## Core Module (`soapui`)

The `soapui` module is the heart of the application. It contains the following key components:

*   **`com.eviware.soapui.SoapUI`**: The main entry point for the application. This class is responsible for initializing the application, building the UI, and managing the application's lifecycle.
*   **`com.eviware.soapui.SoapUICore`**: An interface that defines the core functionality of the application.
*   **`com.eviware.soapui.DefaultSoapUICore`**: The default implementation of the `SoapUICore` interface.
*   **`com.eviware.soapui.model`**: A package containing the data model for the application, including projects, test suites, test cases, and test steps.
*   **`com.eviware.soapui.ui`**: A package containing the UI components for the application, including the main window, navigator, and desktop panels.

## Testing Procedures

The project has a comprehensive testing strategy that includes unit tests, integration tests, and BDD tests.

### Unit Tests

Unit tests are located in the `soapui` module and are run with the `maven-surefire-plugin`. These tests are designed to test individual components of the application in isolation.

### Integration Tests

Integration tests are located in the `soapui-system-test` module and are run with the `maven-failsafe-plugin`. These tests are designed to test the application as a whole, including its interactions with external systems.

### BDD Tests

BDD tests are written in Gherkin syntax and are located in the `src/test/resources/features` directory of the `soapui-system-test` module. These tests are run with Cucumber and are designed to test the application's behavior from the user's perspective.

### Test Case Summary

The following table summarizes the test cases in the `soapui-system-test` module:

| Test Case | Purpose |
| --- | --- |
| `CucumberFestRunner.java` | Runs the Cucumber tests with the `@AutomatedWithFest` tag. |
| `CucumberJavaRunner.java` | Runs the Cucumber tests with the `@AutomatedWithJava` tag. |
| `EnabledWebViewBasedBrowserComponentTest.java` | Tests the embedded web browser component. |
| `AddParamActionTest.java` | Tests the "Add Parameter" action. |
| `DefaultEndpointStrategyConfigurationPaneTest.java` | Tests the default endpoint strategy configuration pane. |
| `SchemaUtilsTest.java` | Tests the schema utility methods. |
| `WsdlDefinitionExporterTestCaseTest.java` | Tests the WSDL definition exporter. |
| `WsdlImporterTestCaseTest.java` | Tests the WSDL importer. |
| `WsdlProjectTestCaseTest.java` | Tests the WSDL project. |
| `WsdlRequestTestCaseTest.java` | Tests the WSDL request. |
| `TestOnDemandCallerTest.java` | Tests the Test on Demand caller. |

To run the tests, you can use the following Maven command:

```bash
mvn clean install
```
