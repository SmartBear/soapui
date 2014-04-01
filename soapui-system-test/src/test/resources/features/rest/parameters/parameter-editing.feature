@Manual @Regression
Feature:User can manage the parameters in request view, form view, method editor, resource editor and top URI bar

Scenario: add parameters in request view, the value will be blank in resource editor
  Given a new REST project is created
  When user adds a parameter in request editor with name param01 and value value01
  Then request editor has parameter with name param01 and value value01 at row 0
  And  Parameters field in top URI bar has value ?param01=value01
  And resource editor has parameter with name param01 and with empty value at row 0

Scenario: add parameters in method or resource editor, the value will be added as well in request view
  Given a new REST project is created
  When user adds a parameter in method editor with name param01 and value value01
  And user adds a parameter in resource editor with name param02 and value value02
  Then method editor has parameter with name param01 and value value01 at row 0
  And resource editor has parameter with name param02 and value value02 at row 0
  And Parameters field in top URI bar has value ?param01=value01&param02=value02
  And request editor has parameter with name param01 and value value01 at row 0
  And request editor has parameter with name param02 and value value02 at row 1

Scenario: delete parameters in request view, it will synchronize in resource editor and top URI bar
  Given a new REST project is created with URI www.tryit.com/resource/method;matrix=param1?query=param2
  When user deletes the parameter in request editor at row 1
  And  user deletes the parameter in request editor at row 0
  Then request editor has no parameters
  And resource editor has no parameters
  And Parameters field is empty in top URI bar

Scenario: delete parameters in method or resource editor, it will synchronize in request view and top URI bar
  Given a new REST project is created with URI www.tryit.com/resource/method;matrix=param1?query=param2
  And user changes the level to method for parameter with name matrix
  When user deletes the parameter in resource editor with name query
  And user deletes the parameter in method editor with name matrix
  Then resource editor has no parameters
  And method editor has no parameters
  And request editor has no parameters
  And Parameters field is empty in top URI bar

Scenario: reorder parameters in request view, it will only change the order in request view
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2&query3=param3
  When user move up the parameter in request editor with name query2
  And user move down the parameter in request editor with name query1
  Then request editor has parameter with name query2 and value param2 at row 0
  And request editor has parameter with name query3 and value param3 at row 1
  And request editor has parameter with name query1 and value param1 at row 2
  And resource editor has parameter with name query1 and value param1 at row 0
  And resource editor has parameter with name query2 and value param2 at row 1
  And resource editor has parameter with name query3 and value param3 at row 2

Scenario: reorder parameters in method or resource editor, it will only change the order in method or resource editor
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2&query3=param3&query4=param4
  And user changes the level to METHOD for parameter with name query3
  And user changes the level to METHOD for parameter with name query4
  When user move up the parameter in resource editor with name query2
  And user move down the parameter in method editor with name query3
  Then resource editor has parameter with name query2 and value param2 at row 0
  And resource editor has parameter with name query1 and value param1 at row 1
  And method editor has parameter with name query4 and value param4 at row 0
  And method editor has parameter with name query3 and value param3 at row 1
  And request editor has parameter with name query1 and value param1 at row 0
  And request editor has parameter with name query2 and value param2 at row 1
  And request editor has parameter with name query3 and value param3 at row 2
  And request editor has parameter with name query4 and value param4 at row 3

Scenario: edit parameters name and their value in request view, the value not changed in resource editor
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  When user changes the name to change1 for parameter with name query1
  And user changes the value to 002 for parameter with value param2
  Then request editor has parameter with name change1 and value param1 at row 0
  And request editor has parameter with name query2 and value 002 at row 1
  And resource editor has parameter with name change1 and value param1 at row 0
  And resource editor has parameter with name query2 and value param2 at row 1
  And Parameters field in top URI bar has value ?change1=param1&query2=002

Scenario: edit parameters name and their value in method or resource editor, the value are also changed in request view
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user changes the level to METHOD for parameter with name query2
  When in resource editor user changes the name to change1 for parameter with name query1
  And in method editor user changes the value to 002 for parameter with value param2
  Then resource editor has parameter with name change1 and value param1 at row 0
  And method editor has parameter with name query2 and value 002 at row 0
  And request editor has parameter with name change1 and value param1 at row 0
  And request editor has parameter with name query2 and value 002 at row 1
  And Parameters field in top URI bar has value ?change1=param1&query2=002

Scenario: revert the parameter value to default in request view
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user changes the value to 01 for parameter with value param1
  And user changes the value to 02 for parameter with value param2
  When user clicks the revert all parameters values button
  Then request editor has parameter with name query1 and value param1 at row 0
  And request editor has parameter with name query2 and value param2 at row 1

Scenario: parameters value could be property expansion
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user adds a custom property to project with name prop1 and value 001
  When user changes the value to ${#Project#prop1} for parameter with value param2
  And run the REST request
  Then in the raw view the submitted URI is www.tryit.com/resource/method/?query1=param1&query2=001

Scenario: two requests under the same resource but different method only inherit the same resource level parameters
  Given a new REST project is created with URI www.tryit.com/backlog/search/?query1=param1&query2=param2
  And user creates a method with name List under the resource with name Search [/backlog/search/] for interface http://www.tryit.com
  When user changes the parameter level to METHOD for parameter with name query1 in request editor for request with path http://www.tryit.com##Search [/backlog/search/]##List##Request 1
  Then under method named Search request editor user has parameter with name query1 and value param1 at row 0
  And under method named Search method editor user has parameter with name query1 and value param1 at row 0
  And under method named List request editor user has parameter with name query2 and value param2 at row 0
  And under method named List method editor user has no parameters

Scenario: two requests under the same resource and method inherit the same resource level and method level parameters
  Given a new REST project is created with URI www.tryit.com/backlog/search/?query1=param1&query2=param2
  And user creates a new request Request2 under the same method
  When user changes the query1 as a method level parameter in Request1
  Then in method editor user has parameter with name query1 and value param1 at row 0
  And in Request1 request editor user has parameter with name query1 and value param1 at row 0
  And in Request1 request editor user has parameter with name query2 and value param2 at row 1
  And in Request2 request editor user has parameter with name query1 and value param1 at row 0
  And in Request2 request editor user has parameter with name query2 and value param2 at row 1

  Scenario: User can edit query parameters in top URI bar
    Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
    When user clicks on the Parameters field of top URI bar
    And user deletes the current two parameters in the popup window
    And user add a new parameter newparam with value 001
    Then request editor has parameter with name newparam and value 001 at row 0
    And resource editor has parameter with name newparam and with empty value at row 0



