@Automated @Regression
Feature: Add rest mock service

  Scenario: Add mock service option available in rest operation context
    Given SoapUI is started
    And a new REST project is created
    When in rest resource context
    And right clicking the current rest context
    Then “Add to REST mock service” rest option is available
    And close SoapUI

  Scenario: Add mock service option available in rest request context
    Given SoapUI is started
    And a new REST project is created
    When in rest request context
    And right clicking the current rest context
    Then “Add to REST mock service” rest option is available
    And close SoapUI
