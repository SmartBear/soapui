@Automated @Regression
Feature: Add SOAP mock service

  Scenario: Add mock service option available in soap operation context
    Given SoapUI is started
    And a new SOAP project is created
    When in soap operation context
    And right clicking the current soap context
    Then “Add to MockService” soap option is available
    And close SoapUI

  Scenario: Add mock service option available in soap request context
    Given SoapUI is started
    And a new SOAP project is created
    When in soap request context
    And right clicking the current soap context
    Then “Add to MockService” soap option is available
    And close SoapUI