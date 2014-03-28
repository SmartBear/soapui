@Manual @Acceptance
Feature: As Mark I can dispatch the mock responses under the same resource path, the dispatch method could be SEQUENCE or SCRIPT

Scenario: In MockAction Editor user can dispatch one mock response by SEQUENCE and get the correct response in test request or test step

Scenario: In MockAction Editor user can dispatch multiple mock responses by SEQUENCE and get the correct response in test request or test step

Scenario: In MockAction Editor user can dispatch one mock response by SCRIPT and get the correct response in test request or test step and the default response works

Scenario: In MockAction Editor user can dispatch multiple mock responses by SCRIPT(match by Path) and get the correct response in test request or test step and the default response works( not set, or select one)

Scenario: In MockAction Editor user can dispatch multiple mock responses by SCRIPT(match by query parameter) and get the correct response in test request or test step and the default response works( not set, or select one)

Scenario: In MockAction Editor user can dispatch multiple mock responses by SCRIPT(match by header) and get the correct response in test request or test step and the default response works( not set, or select one)

Scenario: User can have two test steps using different mock resources which have different mock dispatch methods, when submitting the test steps user get the correct response

Scenario: User can have two test steps using different mock resources but all using SEQUENCE dispatch methods, when submitting the test steps user get the correct response

Scenario: User can have two test steps using different mock resources but all using SCRIPT dispatch methods, when submitting the test steps user get the correct response

Scenario: User can have two test steps using different mock services and have different mock dispatch methods, when submitting the test steps get the correct response
