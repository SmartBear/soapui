@Automated @Regression
Feature: Application

  Scenario: The main window is showing up without error when starting up SoapUI
    Given SoapUI is started
    Then ensure that the main window is showing up without error
    And SoapUI is closed