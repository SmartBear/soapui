@Automated @Acceptance
Feature: Basic OAuth 2 configuration

  Scenario: Request editor not supporting OAuth show not show it in the Auth dropdown
    Given SoapUI is started
    And a new SOAP project is created
    When the user opens the SOAP request editor
    And clicks on the Auth tab
    Then the OAuth 2 option is not visible in the Authentication Type dropdown
    And close SoapUI

  Scenario: You are able to fill in the basic OAuth 2 configuration GUI
    Given SoapUI is started
    And a new REST project is created
    When the user clicks on the Auth tab
    And clicks on the OAuth 2 Authorization Type
    And and fills out all fields
    And switches to another Authorization type and then back again
    Then the previously filled fields are still present
    And close SoapUI