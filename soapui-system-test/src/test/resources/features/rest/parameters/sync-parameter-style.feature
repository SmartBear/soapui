@Manual @Regression

Feature: REST parameter style could be synchronized across REST request, method,resource and top URI bar

 Scenario: User can add 5 different styles parameters in resource editor and synchronized with request view
  Given a new REST project is created
  When user adds 5 different styles parameters in resource editor
  Then user will see those 5 parameters and their values in request view parameters list
  And they are in resource level
  And user will get the query, template and matrix parameters and their value in top URI bar

 Scenario: User can add 5 different styles parameters in method editor and synchronize with request view
 Given a new REST project is created
 When user add 5 different styles parameters in method editor
 Then user will see those 5 parameters and their values in request view parameters list
 And they are in method level
 And user will get the query, template and matrix parameters and their value in top URI bar

 Scenario: User can add 5 different styles parameters in request view and synchronize with resource and method editor
 Given a new REST project is created
 When user adds 5 different styles parameters in request view with their default resource level
 And user changes the 3 parameters to method level
 Then user will see 2 parameters and their values in resource editor parameters list
 And 3 parameters and their values in method editor parameters list

 Scenario: User could can change parameter styles and synchronize with resource, method editor and topURI bar
 Given a new REST project is created
 And user adds a parameter in request editor with name param01 and value value01
 When user changes the style from query to the other style
 Then user will see the topURI bar fields changed
 And the param01 style in resource editor changed