@Manual @Acceptance
Feature: Auth status

  Scenario: Lock symbols are shown when an authorization is added to a request
    Given you have open a request editor containing the Auth tab
    When you have added an Authorization to the request
    Then a lock symbol is shown on the Auth tab
    And a lock symbol is shown besides the endpoint input field