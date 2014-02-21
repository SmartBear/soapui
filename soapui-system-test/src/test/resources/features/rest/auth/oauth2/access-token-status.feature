@Manual @Acceptance
Feature: Access token status

  Scenario: Access token status is set to Entered manually the access token is entered manually
    Given the user has created a REST project with the Google Tasks API
    When the user enters a access token manually in the access token text box
    Then status label besides the the access token fields is set to Entered manually
    And there is a green tick mark besides the access token input field
    And the background color of the access token input field is set to green
    And there is a green tick mark in the Auth flap

  Scenario: Access token status is set to Pending when waiting for the Authorization URI
    Given the user has created a REST project with the Google Tasks API
    And the user has successfully configured its OAuth settings, but entered a slow responding Authorization URI
    When the user clicks on Get Access Token button in the Get Access Token dialog
    Then status label in the Get Access Token dialog is set to Pending
    And there is a green waiting symbol showing besides the Get Access Token dialog status

  Scenario: Access token status is set to Waiting for authorization when the consent screen is showing
    Given The user  has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When user clicks on Get Access Token button in the Get Access Token dialog
    And the user successfully fills outs its credentials and authenticate it self on the login screen
    And the user does not click OK on the concent screen
    Then status label in the Get Access Token dialog is set to Waiting for authorization
    And there is a green waiting symbol shown besides the Get Access Token button

  Scenario: Access token status is set to Recived authentication code when waiting for the Access token URI
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings, but entered a slow responding Access token URI
    When user clicks on Get access token button in the Get Access Token dialog
    And the user successfully authenticate on the login screen
    And clicks OK on the consent screen
    Then status label in the Get Access Token dialog is set to Waiting for authorization
    And there is a green waiting symbol shown besides the Get Access Token dialog status

  Scenario: Access token status is set to Failed to retrive
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When the user clicks on Get access token button in the Get Access Token dialog
    And the user closes the browser window
    Then status label in the Get Access Token dialog is set to Failed to retrive
    And there is a red exclamation showing besides the Get Access Token dialog status
    Then status label besides the the access token fields is set to Failed to retrive
    And there is a red exclamation mark besides the access token input field
    And the access token input field background color is set to red
    And there is a red exclamation mark in the Auth flap

  Scenario: Access token status is set to Failed to retrive when the browser window times out
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When the user clicks on Get access token button in the Get Access Token dialog
    And the user doesn't interact with the browser window in 1 minute
    Then the browser windows is closed automaticly
    And status label in the Get Access Token dialog is set to Failed to retrive
    And there is a red exclamation showing besides the Get Access Token dialog status
    And status label besides the the access token fields is set to Failed to retrive
    And there is a red exclamation mark besides the access token input field
    And the access token input field background color is set to red
    And there is a red exclamation mark in the Auth flap

  Scenario: Access token status is set to Recived from server
    Given the user has created a REST project with the Google Tasks API
    And has successfully configured its OAuth settings
    When user clicks on Get access token button in the Get Access Token dialog
    And the user successfully authenticate and approves the concent screen
    Then the browser windows and Get Access Token windows is closed
    And status label besides the the access token fields is set to Recived from server
    And the access token input field background color is set to green
    And there is a green tick mark besides the access token input field
    And there is a green tick mark in the Auth flap