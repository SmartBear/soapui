@Automated @Regression
Feature: Add SOAP mock service

  Scenario: Add mock service option available in soap operation context
    Given SoapUI is started
    And a new SOAP project is created
    When in soap operation context
    Then “Add to MockService” option is available
    And close SoapUI
