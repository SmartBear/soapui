# SoapUI Maven plugin tester

*This is a submodule of [SoapUI project](../)*

This module tests the `mock` and `test` goals of soapui-maven-plugin.

A mock service is started then the tests are run against the mock.

## Building and running

Execute `mvn clean verify` to run the tests.

The port of the mock service can be configured by adding the following parameter `-DmockServerPort=12345` (default value is `9090`)
