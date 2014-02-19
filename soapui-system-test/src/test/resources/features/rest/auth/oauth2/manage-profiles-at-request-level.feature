@Manual @Acceptance

Feature: Managing OAuth 2 profile from the request/TestStep level

  Scenario: Add a new profile
    Given SoapUI is started
    And a new REST project is created
    And the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And the user creates a profile with the name New profile
    Then a new OAuth 2 profile is created with the name New profile
    And SoapUI is closed

  Scenario: Edit an existing profile
    Given SoapUI is started
    And a new REST project is created
    And the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And the user creates a profile with the name New profile
    When the user selects the profile New profile
    And sets the value of the clientId field to clientIdValue
    Then the clientId value is updated in the OAuth 2 profile
    And SoapUI is closed

  Scenario: Delete an existing profile
    Given SoapUI is started
    And a new REST project is created
    And the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And the user creates a profile with the name New profile
    When the user selects the profile New profile
    And the user selects Delete profile in the profile drop down
    Then the New profile is deleted
    And SoapUI is closed

  Scenario: Rename an existing profile
    Given SoapUI is started
    And a new REST project is created
    And the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And the user creates a profile with the name New profile
    When the user selects the profile New profile
    And the user selects Rename profile in the profile drop down
    And the changes the name to Newer profile
    Then the name of the OAuth 2 profile is Newer profile
    And SoapUI is closed

  Scenario: Clone an existing profile
    Given SoapUI is started
    And a new REST project is created
    And the user clicks on the Auth tab
    And selects the OAuth 2 Authorization Type
    And the user creates a profile with the name New profile
    When the user selects the profile New profile
    And the user selects Clone profile in the profile drop down
    And gives the new profile the name Cloned profile
    Then a new OAuth 2 profile is created with the name Cloned profile
    And SoapUI is closed
