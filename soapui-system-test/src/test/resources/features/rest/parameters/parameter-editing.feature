@Manual @Regression
Feature:User can manage the parameters in request view, form view, method editor, resource editor and top URI bar

Scenario: add parameters in request view, the value will be blank in resource editor
  Given a new REST project is created
  When user adds a new parameter named param01 with value value01 in request view
  Then the new parameter with correct name and value is saved in the parameter list
  And the same parameter is showed in Parameters field of top URI bar
  And the same parameter is showed in resource editor but the value is blank

Scenario: add parameters in method or resource editor, the value will be added as well in request view
  Given a new REST project is created
  When user adds a new parameter named param01 with value value01 in method editor
  And user adds a new parameter named param02 with value value02 in resource editor
  Then the new parameters with correct names and values are saved in the resource editor and method editor
  And the same parameters are showed in Parameters field of top URI bar
  And the same parameters are showed in request view

Scenario: delete parameters in request view, it will synchronize in resource editor and top URI bar
  Given a new REST project is created with URI www.tryit.com/resource/method;matrix=param1?query=param2
  When user deletes the two parameters in request view
  Then the parameters list is blank in request view
  And the parameters list is blank in resource editor
  And the parameters list is blank in Parameters field of top URI bar

Scenario: delete parameters in method or resource editor, it will synchronize in request view and top URI bar
  Given a new REST project is created with URI www.tryit.com/resource/method;matrix=param1?query=param2
  And user changes the level to method for parameter with name matrix
  When user deletes the parameter named query in resource editor
  And user deleted the parameter named matrix in method editor
  Then the parameter list is blank in resource editor
  And the parameter list is blank in method editor
  And the parameters list is blank in request view
  And the parameters list is blank in Parameters field of top URI bar

Scenario: reorder parameters in request view, it will only change the order in request view
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2&query3=param3
  When user changes the order of the parameters
  Then the changed order is saved in parameters list even when reopen the request view

Scenario: reorder parameters in method or resource editor, it will only change the order in method or resource editor
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2&query3=param3&query4=param4
  And user changes the level to method for parameter with name query3
  And user changes the level to method for parameter with name query4
  When user changes the order of the parameters in resource editor
  And user changes the order of the parameters in method editor
  Then the changed order is saved even when reopen the method or resource editor

Scenario: edit parameters name and their value in request view, the value not changed in resource editor
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  When user changes the parameter name query1 to change1
  And user changes the parameter value param2 to 002
  Then the changed parameters are saved in the request view
  And the changed parameter name change1 is saved in the resource editor, but the changed value still keeps as param2
  And the changed parameters saved in Parameters field of top URI bar

Scenario: edit parameters name and their value in method or resource editor, the value are also changed in request view
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user changes the level to method for parameter with name query2
  When user changes the parameter name query1 to change1 in resource editor
  And user changes the parameter value param2 to 002 in method editor
  Then the changed parameters are saved in the resource editor
  And the changed parameters are saved in the method editor
  And the changed parameters are saved in the request view
  And the changed parameters are saved in Parameters field of top URI bar

Scenario: revert the parameter value to default in request view
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user changes the parameters value to 01 and 02
  When user clicks the revert all parameters values button
  And verify the action
  Then all the parameters value will be change back to param1 and param2

Scenario: parameters value could be property expansion
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user adds a project custom property named prop1 with value 001
  When user changes the value of parameter query2 to ${#Project#prop1}
  And run the REST request
  Then the value of parameter query2 changes to 001 in the raw view

Scenario: two requests under the same resource but different method only inherit the same resource level parameters
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user creates a new method method1 under the same resource
  When user changes the query1 as a method level parameter in method1 method
  Then the query1 parameter will show up in method1 method editor and its request view
  And the query1 parameter will be deleted in the other request, only the query2 parameter is left

Scenario: two requests under the same resource and method inherit the same resource level and method level parameters
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user creates a new request request2 under the same method
  When user changes the query1 as a method level parameter in request1
  Then the query1 parameter will show up in method editor
  And the query1 parameter still keeps in request1 and request2 as method level

  Scenario: User can edit parameters in top URI bar
    Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
    When user clicks on the Parameters field of top URI bar
    And user deletes the current two parameters in the popup window
    And user add a new parameter newparam with value 001
    Then user will get only one query parameter named newparam with value 001 in the request view
    And the parameter will show up in the resource editor but the value is blank



