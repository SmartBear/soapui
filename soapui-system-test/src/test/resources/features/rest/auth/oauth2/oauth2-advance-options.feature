@Automated @Acceptance
Feature: OAuth2 Advanced options

  Scenario Outline: Changes selected in OAuth2 advance options dialog are set to profile
    Given SoapUI is started
    And a new REST project is created
    And the user clicks on the Auth tab
    And clicks on the OAuth 2 Authorization Type
    And user clicks on Advance options button
    When user selects access token position <accessTokenPosition>
    And closes and reopens the advance options dialog
    Then access token position is <accessTokenPosition>
    And closes the advance options dialog
    And close SoapUI

  Examples:
  |accessTokenPosition  |
  | QUERY               |

