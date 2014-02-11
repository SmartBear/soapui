@Automated @Regression
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

  Scenario: Option is available in rest request context
    Given SoapUI is started
    And a new REST project is created
    When in rest request context
    And right clicking the current rest context
    Then “Add to REST mock service” rest option is available
    And close SoapUI

  Scenario: Mock service tree node structure is created
    Given SoapUI is started
    And a new REST project is created
    When in rest request context
    And right clicking the current rest context
    And selecting "Add to REST mock service" from rest context menu
    And click "OK" in "Add to REST mock service" dialog
    Then there is a "MockService 1" rest tree node
    And there is a "MockService 1/Sub-resource Request 1" rest tree node
    And there is a "MockService 1/Sub-resource Request 1/Response 1" rest tree node

  Scenario: View mock response editor
    Given SoapUI is started
    And a rest-project-with-mock-service.xml is imported
    When in rest project tree "Projects/REST Project 1 Regression/MockService 1/Xml Request 1/Response 1"
    And double clicking the current rest context
    Then a rest mock response editor should be shown

