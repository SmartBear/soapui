Feature:Saves the access token to OAuth2 profile when valid OAuth 2.0 parameters (authorization URI, access token URI,
  redirectUri, scope, client id and client secret) are provided.

  Scenario Outline: Access token is saved for google tasks api
    Given Uwe Krull created a REST project with <RestURI>
    And an OAuth2 profile is created with <authURI>, <accessTokenURI>, <redirectUri>, <scope>, <clientID>, <clientSecret>
    And access token is not present in the OAuth profile
    When user clicks on get access token
    Then access token is fetched and saved to the profile.
    And the internal browser, opened for consent screen, is closed automatically.

  Examples:
  | RestURI                                             | authURI                                    | accessTokenURI                             | redirectUri                  | scope                                            | clientID                                   | clientSecret             |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | http://localhost:8080/       | https://www.googleapis.com/auth/tasks.readonly   | 669184148999.apps.googleusercontent.com    | vqWu7TpONtgABF2Ooay4fODG |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | urn:ietf:wg:oauth:2.0:oob    | https://www.googleapis.com/auth/tasks.readonly   | 669184148999.apps.googleusercontent.com    | vqWu7TpONtgABF2Ooay4fODG |


  Scenario Outline: Access token is overwritten if exists already
    Given Uwe Krull created a REST project with <RestURI>
    And an OAuth2 profile is created with <authURI>, <accessTokenURI>, <redirectUri>, <scope>, <clientID>, <clientSecret>
    And access token already exists
    When user clicks on get access token
    Then access token is fetched and the existing access token is overwritten with new one.
    And the internal browser, opened for consent screen, is closed automatically.

  Examples:
  | RestURI                                             | authURI                                    | accessTokenURI                             | redirectUri                  | scope                                            | clientID                                   | clientSecret             |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | http://localhost:8080/       | https://www.googleapis.com/auth/tasks.readonly   | 669184148999.apps.googleusercontent.com    | vqWu7TpONtgABF2Ooay4fODG |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | urn:ietf:wg:oauth:2.0:oob    | https://www.googleapis.com/auth/tasks.readonly   | 669184148999.apps.googleusercontent.com    | vqWu7TpONtgABF2Ooay4fODG |



  Scenario Outline: Error message is displayed if the OAuth parameters are not valid.
    Given Uwe Krull created a REST project with <RestURI>
    And an OAuth2 profile is created with <authURI>, <accessTokenURI>, <redirectUri>, <scope>, <clientID>, <clientSecret>
    When user clicks on get access token
    Then access token is not fetched and an <error message> is displayed on the GUI.
    And the authorization screen is not presented.


  Examples:
  | RestURI                                             | authURI                                    | accessTokenURI                             | redirectUri                  | scope      | clientID       | clientSecret             |  error message                 |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | htttps://accounts.google.com/o/oauth2/auth | https://accounts.google.com/o/oauth2/token | http://localhost:8080/       | readonly   | 669184148999   | vqWu7TpONtgABF2Ooay4fODG | Invalid authorization URI      |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | htttps://accounts.google.com/o/oauth2/token| urn:ietf:wg:oauth:2.0:oob    | readonly   | 669184148999   | vqWu7TpONtgABF2Ooay4fODG | Invalid access token URI       |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | htttp://localhost:8080/      | readonly   | 669184148999   | vqWu7TpONtgABF2Ooay4fODG | Invalid redirect URI           |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | http://localhost:8080/       |            | 669184148999   | vqWu7TpONtgABF2Ooay4fODG | Scope can't be empty.          |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | http://localhost:8080/       |  readonly  |                | vqWu7TpONtgABF2Ooay4fODG | Client ID can't be empty.      |
  | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth  | https://accounts.google.com/o/oauth2/token | http://localhost:8080/       |  readonly  | 669184148999   |                          | Client secret can't be empty.  |

  Scenario Outline: message is displayed when access token was not fetched due to some problems, even though the parameters are valid.
    Given Uwe Krull created a REST project with <RestURI>
    And a OAuth2 profile is created with <authURI>, <accessTokenURI>, <redirectUri>, <scope>, <clientID>, <clientSecret>
    When user clicks on get access token
    Then access token is not fetched and an <error message> is displayed in the internal browser.
    And browser is not closed automatically.

  Examples:
    | RestURI                                             | authURI                                    | accessTokenURI                             | redirectUri                  | scope      | clientID       | clientSecret             | error message                     |
    | https://www.googleapis.com/tasks/v1/users/@me/lists | https://accounts.google.com/o/oauth2/auth | https://accounts.google.com/o/oauth2/token  | http://localhost:8080/       | readonly   | 669184148999   | vqWu7TpONtgABF2Ooay4fODG | Client not registered             |