@Manual @Acceptance
Feature: Access token status

  Background: When the user wants to retrive an OAuth 2 access token, SoapUI will help the user with showing the status of
  the authorization process using the statuses in the following order:
  Pending -> Waiting for Authorization -> Recived authorization code -> Retrived from server
  Entered manually
  -> Failed to retrive
  -> Expired

  Scenario: No status is shown when the Get Access Button hasn't been pressed
    Given the user has created a REST project with the Google Tasks API
    When the user has successfully configured its OAuth settings
    And not clicked the Get Access Token button
    Then there is no status label in the Get Access Token dialog
    And there is no status label besides the the access token fields
    And the access token input field background color is white

  Scenario: Access token status is set to Entered manually the access token is entered manually
    Given the user has created a REST project with the Google Tasks API
    When the user enters a access token manually in the access token text box
    Then the status label besides the the access token fields is set to Entered manually
    And there is a green tick mark besides the access token input field
    And the background color of the access token input field is set to green
    And there is a green tick mark in the Auth tab

  # This might be removed
  Scenario: Access token status is set to Pending when waiting for the Authorization URI
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings, but entered a slow responding Authorization URI
    When the user clicks on the Get Access Token button in the Get Access Token dialog
    Then status label in the Get Access Token dialog is set to Pending
    And there is a green waiting symbol showing besides the Get Access Token dialog status

  Scenario: Access token status is set to Waiting for authorization when the browser windows is showing, but the user has not logged in
    Given The user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When user clicks on the Get Access Token button in the Get Access Token dialog
    Then status label in the Get Access Token dialog is set to Waiting for authorization
    And there is a green waiting symbol shown besides the Get Access Token button

  Scenario: Access token status is set to Recived authorization code when the the authorization code has been recived
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings, but entered a slow responding Access token URI
    When user clicks on Get access token button in the Get Access Token dialog
    And the user successfully authenticate on the login screen
    And clicks OK on the consent screen
    Then status label in the Get Access Token dialog is set to Recived authorization code
    And there is a green waiting symbol shown besides the Get Access Token dialog status

  Scenario: Access token status is set to Failed to retrive when the user have the wrong credentials and closes the window
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When the user clicks on the Get access token button in the Get Access Token dialog
    And tried to authenticate with the wrong credentials
    And finally gives up and closes the browser window
    Then status label in the Get Access Token dialog is set to Failed to retrive
    And there is a red exclamation showing besides the Get Access Token dialog status
    And status label besides the the access token fields is set to Failed to retrive
    And there is a red exclamation mark besides the access token input field
    And the access token input field background color is set to red
    And there is a red exclamation mark in the Auth tab

  Scenario: Access token status is set to Failed to retrive when the browser window times out
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When the user clicks on Get access token button in the Get Access Token dialog
    And the user doesn't interact with the browser window in X seconds
    Then the browser windows is closed automaticly
    And status label in the Get Access Token dialog is set to Failed to retrive
    And there is a red exclamation showing besides the Get Access Token dialog status
    And status label besides the the access token fields is set to Failed to retrive
    And there is a red exclamation mark besides the access token input field
    And the access token input field background color is set to red
    And there is a red exclamation mark in the Auth tab

  Scenario: Access token status is set to Retrieved from server
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When user clicks on the Get access token button in the Get Access Token dialog
    And the user successfully authenticate and approves the concent screen
    Then the browser window and the Get Access Token window is closed
    And status label besides the the access token fields is set to Retrieved from server
    And the access token input field background color is set to green
    And there is a green tick mark besides the access token input field
    And there is a green tick mark in the Auth tab