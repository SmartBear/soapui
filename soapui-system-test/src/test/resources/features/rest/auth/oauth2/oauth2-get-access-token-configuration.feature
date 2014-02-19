@Automated @Acceptance
Feature: OAuth 2 Get Access Token configuration

  Scenario: Request editor not supporting OAuth show not show it in the Auth dropdown
    Given SoapUI is started
    And a new SOAP project is created
    When the user opens the SOAP request editor
    And clicks on the Auth tab
    Then the OAuth 2 option is not visible in the Authentication Type dropdown
    And SoapUI is closed

  Scenario: You are able to fill in Access Token field
    Given SoapUI is started
    And a new REST project is created
    When the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And enters the access token
    And switches to another Authorization type and then back again
    Then access token is present
    And SoapUI is closed

  Scenario: You are able to fill in the Get Access Token form
    Given SoapUI is started
    And a new REST project is created
    When the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And clicks on the disclosure button
    And and fills out all fields
    And switches to another Authorization type and then back again
    And clicks on the disclosure button
    Then the previously filled fields are still present
    And SoapUI is closed

  Scenario: Get Access Token form is closed on lost focus
    Given SoapUI is started
    And a new REST project is created
    When the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And clicks on the disclosure button
    And clicks outside of the Get Access token form
    Then the Get Access token form is closed
    And SoapUI is closed

  Scenario: Client id field is not visible when selecting the Implicit grant flow
    Given SoapUI is started
    And a new REST project is created
    When the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And clicks on the disclosure button
    And selects the OAuth 2 flow Implicit Grant
    Then clientId field is not visible
    And SoapUI is closed