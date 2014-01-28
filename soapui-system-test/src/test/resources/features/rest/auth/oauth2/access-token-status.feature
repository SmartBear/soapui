@Manual @Acceptance
Feature: Access token status is updated when 'Get access token' button is clicked with valid parameters.

  Scenario: Access token status is set to UPDATED_MANUALLY when access token is set/entered through the GUI
    Given A REST project is created
    And OAuth2 is selected as auth type in the Auth tab
    When user enters 'avy3423pbsdks2323kspdsp' in the access token text box
    Then access token status label is updated to text 'UPDATED_MANUALLY'.
    
    
  Scenario: Access token status is set to WAITING_FOR_AUTHORIZATION when consent screen is presented
    Given A REST project is created
    And OAuth2 is selected as auth type in the Auth tab
    And Cliend ID is set to <TEAMPASS: SoapUI Development Google account>
    And Client secret is set to <FROM TEAMPASS>
    And authURI is set to https://accounts.google.com/o/oauth2/auth
    And access token URI is set to https://accounts.google.com/o/oauth2/token
    And redirect URI is set to urn:ietf:wg:oauth:2.0:oob
    And scope is set to https://www.googleapis.com/auth/tasks.readonly
    When user clicks on 'Get access token' button
    Then access token status label is updated to text 'WAITING_FOR_AUTHORIZATION'.
    
  Scenario: Access token status is set to RECEIVED_AUTHORIZATION_CODE when resource owner authorizes the app
    Given A REST project is created
    And OAuth2 is selected as auth type in the Auth tab
    And Cliend ID is set to <TEAMPASS: SoapUI Development Google account>
    And Client secret is set to <FROM TEAMPASS>
    And authURI is set to https://accounts.google.com/o/oauth2/auth
    And access token URI is set to https://accounts.google.com/o/oauth2/token
    And redirect URI is set to urn:ietf:wg:oauth:2.0:oob
    And scope is set to https://www.googleapis.com/auth/tasks.readonly
    When user clicks on 'Get access token' button 
    And user authorizes the app by logging in
    Then access token status label is updated to text 'RECEIVED_AUTHORIZATION_CODE' just before setting the access token and closing the browser window.    
    
  Scenario: Access token status is set to RETRIEVED_FROM_SERVER when user authorizes the app and browser window is closed
    Given A REST project is created
    And OAuth2 is selected as auth type in the Auth tab
    And Cliend ID is set to <TEAMPASS: SoapUI Development Google account>
    And Client secret is set to <FROM TEAMPASS>
    And authURI is set to https://accounts.google.com/o/oauth2/auth
    And access token URI is set to https://accounts.google.com/o/oauth2/token
    And redirect URI is set to urn:ietf:wg:oauth:2.0:oob
    And scope is set to https://www.googleapis.com/auth/tasks.readonly
    When user clicks on 'Get access token' button 
    And user authorizes the app by logging in
    And browser window is closed
    Then access token status label is updated to text 'RETRIEVED_FROM_SERVER'       
