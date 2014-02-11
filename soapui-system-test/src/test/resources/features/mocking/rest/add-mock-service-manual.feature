@Regression
Feature: Add rest mock service
  As Mark I would like to Mock away third party API calls
  and therefore, I would like to create REST mock services easily


  Scenario: Option is available from rest operation context
    Given SoapUI is started
    And a new REST project is created
    When in rest resource context
    And right clicking the current rest context
    Then “Add to REST mock service” rest option is available
    And close SoapUI

