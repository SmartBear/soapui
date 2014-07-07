@AutomatedWithFest @Acceptance
Feature: OAuth2 Advanced options

  Scenario Outline: Changes selected in OAuth2 advanced options dialog are set to profile
    Given a new REST project is created
    And the user clicks on the Auth tab
    And the user creates an OAuth 2.0 profile with name OAuth 2.0 - Profile
    And user clicks on Advanced options button
    When user selects access token position <accessTokenPosition>
    And selects refresh method <refreshMethod>
    And closes and reopens the advanced options dialog
    Then access token position is <accessTokenPosition>
    And refresh method is <refreshMethod>
    And closes the advanced options dialog

  Examples:
    | accessTokenPosition | refreshMethod |
    | Query               | Manual        |
    | Header              | Automatic     |

