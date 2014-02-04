@Manual @Acceptance
Feature: When we have a refresh token, and we know that the user's access token has expired (via information from the server), automatically refresh it when sending the request.

  Scenario: Access token is automatically refreshed.
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