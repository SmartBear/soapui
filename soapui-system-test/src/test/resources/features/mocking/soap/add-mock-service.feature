@Automated @Regression
Feature: Add SOAP mock service

  Scenario: Add mock service option available in soap operation context
    Given SoapUI Project exists
    When in soap operation context
    Then “Add to MockService” option is available
    And close SoapUI
