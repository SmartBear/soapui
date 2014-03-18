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
    And there is a tick mark status icon besides the the access token field
    And the status label besides the the access token field is set to Entered Manually
    And the access token input field background color is set to green
    And there is a tick mark icon on the Auth tab

  Scenario: Access token status is set to Waiting for Authorization when the browser windows is showing, but the user has not logged in
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings
    When the user clicks on on the Get Access Token button in the Get Access Token dialog
    And not types or clicks in the browser window
    Then there is a waiting status icon in the Get Access Token dialog
    And the status label in the Get Access Token dialog is set to Waiting for Authorization
    And there is a waiting status icon besides the access token field
    And the status label besides the the access token field is set to Waiting for Authorization
    And the access token input field background color is set to white
    And there is a waiting status icon on the Auth tab

  Scenario: Access token status is set to Received authorization code when the the authorization code has been received
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings, but entered the wrong Access token URI
    When user clicks on Get access token button in the Get Access Token dialog
    And the user successfully authenticate on the login screen
    And clicks OK on the consent screen
    Then there is a waiting status icon in the Get Access Token dialog
    And the status label in the Get Access Token dialog is set to Received authorization code
    And there is a waiting status icon besides the access token field
    And the status label besides the the access token field is set to Received authorization code
    And the access token input field background color is set to white
    And there is a waiting status icon on the Auth tab

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
    And there is a tick mark icon on the Auth tab

  Scenario: The access token status is saved between sessions
    Given the user has created a REST project with the Google Tasks API
    And the user enters a access token manually in the access token text box
    And the status is set to Entered manually
    When the user saves the project and restarts SoapUI
    And open the saved projects
    Then the status is still Entered manually