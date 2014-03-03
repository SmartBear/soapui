@Manual @Regression
Feature: User can add, delete, extract and revert default values for query parameters in top URI bar Parameter field

  Scenario:User can add parameters with values in top URI bar Parameter field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    When user clicks in the Parameters field of top URI bar
    And clicks the add parameter button to add a new parameter named abc with value 02 in the popup window
    And close the popup window
    Then the parameter field will change to ?test=01&abc=02
    And a query parameter named abc with value 02 is added in the request parameter list

  Scenario: User can set parameter values as property expansion in top URI bar Parameter field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    And user creates a project custom property named value with value xyz
    When user clicks in the Parameters field of top URI bar
    And clicks the add parameter button to add a new parameter named abc with value ${#Project#value} in the popup window
    And closes the popup window
    And submits the request
    Then the parameter field will change to ?test=01&abc=${#Project#value}
    And a query parameter named abc with value ${#Project#value} is added in the request parameter list
    And the URI in raw view changed to www.example.com/resource/path?test=01&abc=xyz

  Scenario:User can delete parameters in top URI bar Parameter field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    When user clicks in the Parameters field of top URI bar
    And clicks the delete parameter button to delete the parameter named test with value 01 in the popup window
    And close the popup window
    Then the parameter field will change to empty
    And it is empty in the request parameter list

  Scenario: User can extract matrix parameters and query parameters in top URI bar Parameter field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    When user clicks in the Parameters field of top URI bar
    And clicks the update parameter from URL button to extract the URL www.tryit.com/resource/method;matrix=param1?query=param2
    And close the popup window
    Then the parameter field will change to ;matrix=param1?query=param2
    And a matrix parameter named matrix with value param1 is added in the request parameter list
    And a query parameter named query with value param2 is added in the request parameter list

  Scenario: User can revert all parameters values to default in top URI bar Parameter field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    And user adds two new parameters which are test1=02 and test2=03 from the top URI bar of the request editors
    When user clicks in the Parameters field of top URI bar
    And clicks the reverts value button and confirm it
    And close the popup window
    Then the parameter field will change to ?test=01
    And the parameters named test1 and test2 have empty value in the request parameter list
    And the parameter named test has the value 01