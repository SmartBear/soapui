@AutomatedWithFest @Acceptance
Feature: Managing auth profiles from the request/TestStep level

  Scenario: Add a new OAuth 2.0 profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    When the user creates an OAuth 2.0 profile with name New profile
    Then new profile selected with name New profile

  Scenario: Add a new basic authentication profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    When the user creates basic authentication profile for authentication type Basic
    Then new profile selected with name Basic

  Scenario: Delete an existing OAuth 2.0 profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    And the user creates an OAuth 2.0 profile with name New profile
    When the user selects Delete current in the authorization drop down
    And user confirms for deletion
    Then the profile with name New Profile is deleted
    And new profile selected with name No Authorization

  Scenario: Rename an existing OAuth 2.0 profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    And the user creates an OAuth 2.0 profile with name New profile
    When the user selects Rename current... in the authorization drop down
    And the changes the name to Newer profile
    Then new profile selected with name Newer profile


  Scenario: Available Add/Edit options for OAuth2.0 profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    When the user creates an OAuth 2.0 profile with name New Profile
    Then available options in authorization drop down are No Authorization,New Profile,Add New Authorization...,Rename current...,Delete current

  Scenario: Available Add/Edit options for basic authentication profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    When the user creates basic authentication profile for authentication type Basic
    Then available options in authorization drop down are No Authorization,Basic,Add New Authorization...,Delete current