@Manual @Acceptance
Feature: As Mark, I can let SoapUI automatically retrieve a new access token for me when I send request, if I have added JavaScripts handling the OAuth2 browser interactions, and see information about the interactions in the SoapUI log

  Scenario: Access token is automatically retrieved by SoapUI.
    Given Mark has created a REST project with the Google Tasks API
    And has successfully configured his OAuth settings
    And has configured working JavaScripts for both the login screen and the consent screen
    And has sent a request to the API successfully
    And has saved the project file
    And has closed SoapUI
    And has edited the project file to change the value of "accessTokenExpirationTime" to 1
    When he opens the project in SoapUI
    And sends a new request to the Google Tasks API
    Then a new access token is retrieved automatically
    And the request can be sent successfully
    And messages are added to the SoapUI log before and after retrieving a new access token

  Scenario: Access token is automatically retrieved when a test is run from the command line.
    Given Mark has created a REST project with the Google Tasks API
    And has successfully configured his OAuth settings
    And has configured working JavaScripts for both the login screen and the consent screen
    And has created a test case
    And has sent a request to the API successfully by running the test case in SoapUI
    And has saved the project file
    And has closed SoapUI
    And has edited the project file to change the value of "accessTokenExpirationTime" to 1 in test step
    When he runs the test case from the command line using testrunner
    Then a new access token is retrieved automatically
    And the request can be sent successfully
    And messages are added to the SoapUI log before and after retrieving a new access token