@Manual @Acceptance
Feature: Apply OAuth Token

# TODO: with detailed examples since in this iteration we are supporting one work flow

  Scenario: Apply OAuth2 Token in rest request
    Given an existing rest project is open
    And there is an oauth2 profile with access token
    When a rest request is sent
    Then access token is applied to the request header

  Scenario: Apply OAuth2 Token in rest test step
    Given an existing rest project is open
    And there is an oauth2 profile with access token
    When a rest test step is sent
    Then access token is applied to the request header

  Scenario: Do not Apply OAuth2 Token in soap request
    Given an existing soap project is open
    And there is an oauth2 profile with access token
    When a soap request is sent
    Then no access token is applied to the request header


  Scenario: Do not apply OAuth2 Token
    Given an existing rest project is open
    And there is an oauth2 profile without access token
    When a rest request is sent
    Then no access token is applied to the request header
