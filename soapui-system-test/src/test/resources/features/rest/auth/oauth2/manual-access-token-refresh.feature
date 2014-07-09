@AutomatedWithFest @Acceptance
Feature: Manual access token refresh

  Scenario: Refresh button is not showing when the refresh token is not present
    Given a new REST project is created
    And the user clicks on the Auth tab
    And the user creates an OAuth 2.0 profile with name OAuth 2.0 - Profile
    Then refresh button is not visible

  Scenario: Refresh button is showing when the refresh token is present and refresh method is manual
    Given a new REST project is created
    And the user clicks on the Auth tab
    And the user creates an OAuth 2.0 profile with name OAuth 2.0 - Profile
    And there is a refresh token in the profile with name OAuth 2.0 - Profile
    And sets refresh method to Manual
    Then refresh button is visible

  Scenario: Refresh button is not showing when the refresh token is present and refresh method is automatic
    Given a new REST project is created
    And the user clicks on the Auth tab
    And the user creates an OAuth 2.0 profile with name OAuth 2.0 - Profile
    And there is a refresh token in the profile with name OAuth 2.0 - Profile
    And sets refresh method to Automatic
    Then refresh button is not visible