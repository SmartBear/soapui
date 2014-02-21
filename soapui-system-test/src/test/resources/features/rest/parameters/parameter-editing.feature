@Manual @Regression
Feature:User can manage the parameters in request view, form view, method editor, resource editor and top URI bar

Scenario: add parameters in request view
  Given a new REST project is created
  When user adds a new parameter named param01 with value value01 in request view
  Then the new parameter with correct name and value is saved in the parameter list
  And the same parameter is showed in Parameters field of top URI bar
  And the same parameter is showed in resource editor but the value is blank

Scenario: add parameters in method or resource editor
  Given a new REST project is created
  When user adds a new parameter named param01 with value value01 in method editor
  And user adds a new parameter named param02 with value value02 in resource editor
  Then the new parameters with correct names and values are saved in the resource editor and method editor
  And the same parameters are showed in Parameters field of top URI bar
  And the same parameters are showed in request view

Scenario: delete parameters in request view
  Given a new REST project is created with URI www.tryit.com/resource/;matrix=param1?query=param2
  When user deletes the two parameters in request view
  Then the parameters list is blank in request view
  And the parameters list is blank in resource editor
  And the parameters list is blank in Parameters field of top URI bar

Scenario: delete parameters in method or resource editor
  Given a new REST project is created with URI www.tryit.com/resource/;matrix=param1?query=param2
  And user changes the level to method for parameter with name matrix
  When user deletes the parameter named query in resource editor
  And user deleted the parameter named matrix in method editor
  Then the parameter list is blank in resource editor
  And the parameter list is blank in method editor
  And the parameters list is blank in request view
  And the parameters list is blank in Parameters field of top URI bar

Scenario: reorder parameters in request view
  Given a new REST project is created with URI www.tryit.com/resource/?query1=param1&query2=param2&query3=param3
  When user changes the order of the parameters
  Then the changed order is saved in parameters list even when reopen the request view

Scenario: reorder parameters in method or resource editor
  Given a new REST project is created with URI www.tryit.com/resource/?query1=param1&query2=param2&query3=param3&query4=param4
  And user changes the level to method for parameter with name query3
  And user changes the level to method for parameter with name query4
  When user changes the order of the parameters in resource editor
  And user changes the order of the parameters in method editor
  Then the changed order is saved even when reopen the method or resource editor

Scenario: edit parameters name and their value in request view
  Given a new REST project is created with URI www.tryit.com/resource/?query1=param1&query2=param2
  When user changes the parameter name query1 to change1
  And user changes the parameter value param2 to 002
  Then the changed parameters saved in the request view
  And the changed parameters saved in the resource editor
  And the changed parameters saved in Parameters field of top URI bar

Scenario: edit parameters name and their value in method or resource editor
  Given a new REST project is created with URI www.tryit.com/resource/?query1=param1&query2=param2
  And user changes the level to method for parameter with name query2
  When user changes the parameter name query1 to change1 in resource editor
  And user changes the parameter value param2 to 002 in method editor
  Then the changed parameters saved in the resource editor
  And the changed parameters saved in the method editor
  And the changed parameters saved in the request view
  And the changed parameters saved in Parameters field of top URI bar

Scenario: revert the parameter value to default in request view

Scenario: parameters value could be property expansion

Scenario: user can use 'get data' feature to get a project-level property

Scenario: two requests under the same resource but different method only inherit the same resource level parameters

Scenario: two requests under the same resource and method inherit the same resource level and method level parameters

  Scenario: User can edit parameters in top URI bar
    Given a new REST project is created with URI www.tryit.com/resource/;matrix=param1?query=param2
    When user clicks on the Parameters field of top URI bar
    And user deletes the current two parameters in the popup window
    And user add a new parameter newparam with value 001
    Then user will get only one query parameter named newparam with value 001 in the request view
    And the parameter will show up in the resource editor



