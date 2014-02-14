@Manual @Acceptance
Feature: As Mark I can get access token automatically refreshed when I send request if I have a refresh token and access token expiration time, and see the refresh information in SoapUI log

  Scenario: Access token is automatically refreshed when run from SoapUI.
    Given Mark has created a REST project with the Google Tasks API
      And has successfully configured his OAuth settings
      And has received a refresh token
      And has sent a request to the API successfully
      And has saved the project file
      And has closed SoapUI
      And has edited the project file to change the value of "accessTokenExpirationTime" to 1
    When he opens the project in SoapUI
      And sends a new request to the Google Tasks API
    Then the request has been given a new access token
      And logs the messages in SoapUI log before and after refreshing the access token
      
  Scenario: Access token is automatically refreshed when run from command line.
    Given Mark has created a REST project with the Google Tasks API
      And has successfully configured his OAuth settings
      And has received a refresh token
      And has created a test case
      And has sent a request to the API successfully by running the test case in SoapUI
      And has saved the project file
      And has closed SoapUI
      And has edited the project file to change the value of "accessTokenExpirationTime" to 1 in test step
    When he opens the project in SoapUI
      And runs the test case from command line using testrunner
    Then the request has been given a new access token
      And logs the messages in SoapUI log before and after refreshing the access token
    
    Scenario: Shows error message in SoapUI log and test case log when it fails to refresh access token automatically
    Given Mark has created a REST project with the Google Tasks API
      And has successfully configured his OAuth settings
      And has received a refresh token
      And has created a test case for the request
      And has sent a request to the API successfully by running the test case
      And has saved the project file
      And has closed SoapUI
      And has edited the project file to change the value of "accessTokenExpirationTime" to 1
    When he opens the project in SoapUI
      And changes the access token URI to something non-existing (e.g. appends 'a' in the end) in test step
      And runs the test case
    Then the test case fails as it fails to refresh the access token
      And logs the failure message in the SoapUI log
      And shows the error message on the test case GUI
