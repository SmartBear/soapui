@Manual @Acceptance
Feature: Access token status

  Background: When the user wants to retrieve an OAuth 2 access token, SoapUI will help the user with showing the status of
  the authorization process using the statuses in the following order:
  Pending -> Waiting for Authorization -> Received authorization code -> Retrieved from server
  Entered manually
  -> Failed to retrieve
  -> Expired

  Scenario: No status is shown when the Get Access Button hasn't been pressed
    Given the user has created a REST project with the Google Tasks API
    When the user has successfully configured its OAuth settings
    And not clicked the Get Access Token button
    Then there is no status icon in the Get Access Token dialog
    And there is no status label in the Get Access Token dialog
    And there is no status icon besides the access token field
    And there is no status label besides the access token field
    And the access token input field background color is set to white
    And there is a lock icon on the Auth tab

  Scenario: Access token status is set to Entered manually the access token is entered manually
    Given the user has created a REST project with the Google Tasks API
    When the user enters a access token manually in the access token text box
    Then there is no status icon in the Get Access Token dialog
    And there is no status label in the Get Access Token dialog
    And there is no status icon besides the access token field
    And there is no status label besides the access token field
    And the access token input field background color is set to white
    And there is a lock icon on the Auth tab

  Scenario: Access token status is set to Waiting for Authorization when the browser windows is showing, but the user has not logged in
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings
    When the user clicks on on the Get Access Token button in the Get Access Token dialog
    And not types or clicks in the browser window
    Then there is a waiting status icon in the Get Access Token dialog
    And the status label in the Get Access Token dialog is set to Waiting for Authorization
    And the OAuth 2 form has the same content as before clicking on the Get Token button

  Scenario: Access token status is set to Received authorization code when the the authorization code has been received
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its Authorization Code Grant OAuth settings, but entered the wrong Access token URI
    When user clicks on Get access token button in the Get Access Token dialog
    And the user successfully authenticate on the login screen
    And clicks OK on the consent screen
    Then there is a waiting status icon in the Get Access Token dialog
    And the status label in the Get Access Token dialog is set to Received authorization code
    And the OAuth 2 form has the same content as before clicking on the Get Token button

  Scenario: Access token status is set to Retrieved from server
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings
    When user clicks on Get access token button in the Get Access Token dialog
    And the user successfully authenticate on the login screen
    And clicks OK on the consent screen
    Then there is no status icon in the Get Access Token dialog
    And there is no status label in the Get Access Token dialog
    And there is a tick mark status icon besides the the access token field
    And the status label besides the the access token field is set to Retrieved from server
    And the access token input field background color is set to green
    And there is a lock icon on the Auth tab

  Scenario: Access token status is set to Expired when sending an expired token
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    And has a expired access token
    And has set the Refresh access token option to manual
    When the user sends the request
    Then status label besides the the access token field is set to Expired
    And there is no status icon in the Get Access Token dialog
    And there is no status label in the Get Access Token dialog
    And there is a red exclamation mark besides the access token input field
    And the access token input field background color is set to red
    And there is a red exclamation mark in the Auth tab

  Scenario: Access token status is set to canceled when closing the browser without getting an access token
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings
    When user clicks on Get access token button in the Get Access Token dialog
    And the user closes the browser window without entering anything in it
    Then there is a red exclamation mark icon in the Get Access Token dialog
    And the status label in the Get Access Token dialog is set to Retrieval canceled
    And the OAuth 2 form has the same content as before clicking on the Get Token button
    And the Get Access token button has the suffix (Resume)
    And there is a red exclamation mark in the Auth tab

  Scenario: The access token status is saved between sessions
    Given the user has created a REST project with the Google Tasks API
    And the user enters a access token manually in the access token text box
    And the status label besides the the access token field is empty
    When the user saves the project and restarts SoapUI
    And open the saved projects
    Then the status label besides the the access token field is still empty