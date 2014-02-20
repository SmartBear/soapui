@Automated @Acceptance
Feature: Manual access token refresh

  Scenario: Refresh button is not showing when the refresh token is not present
    Given a new REST project is created
    And the user clicks on the Auth tab
    And clicks on the OAuth 2 Authorization Type
    Then refresh button is not visible

  Scenario: Refresh button is showing when the refresh token is present and refresh method is manual
    Given a new REST project is created
    And there is a refresh token in the OAuth 2 profile
    And the user clicks on the Auth tab
    And clicks on the OAuth 2 Authorization Type
    And sets refresh method to MANUAL
    Then refresh button is visible

  Scenario: Refresh button is not showing when the refresh token is present and refresh method is automatic
    Given a new REST project is created
    And there is a refresh token in the OAuth 2 profile
    And the user clicks on the Auth tab
    And clicks on the OAuth 2 Authorization Type
    And sets refresh method to AUTOMATIC
    Then refresh button is not visible