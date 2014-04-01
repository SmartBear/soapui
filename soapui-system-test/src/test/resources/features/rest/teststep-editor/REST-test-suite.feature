@Manual @Regression
Feature: User can create ,edit and run test suite in a REST project. The changes in REST request will be synchronized to test step, but the test step can have the different parameter value after creation.

Scenario: Create a test step from a REST request and check the resource, method and parameters are inherited from its REST request
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  When user creates a test step from the REST request
  Then the new created test step editor is showed up
  And the endpoint is http://www.tryit.com
  And the resource path is /resource/method/?query1=param1&query2=param2 on test step editor top URI bar
  And the parameters are query1 with value param1 and query2 with value param2


Scenario: The changes of parameter name in REST request will be inherited to test step, but not their values
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user creates a test step from the REST request
  When user changes the parameter query2 with value param2 to change2 with value value2 in request editor
  Then the parameter query2 with value param2 is changed to change2 with value param2 in test step editor
  And the parameter changes will be synchronized to top URI bar in test step editor

Scenario: The new added parameters name in REST request will be inherited to test step, but not their values
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user creates a test step from the REST request
  When user adds a parameter in request editor with name query3 and value param3
  Then the parameter query3 with blank value is showed up in test step editor
  And the parameter changes will be synchronized to top URI bar in test step editor

Scenario: The deleted parameters from REST request will be deleted from test step as well
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user creates a test step from the REST request
  When user deletes a parameter in request editor with name query1 and value param1
  Then only one parameter named query2 with value param2 in test step editor
  And the parameter changes will be synchronized to top URI bar in test step editor

Scenario: The changes of REST resource or method path will be synchronized to test step
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1&query2=param2
  And user creates a test step from the REST request
  When user changes the resource path to /resource1/method1 in request editor top URI bar
  Then the resource path will change to /resource1/method1/?query1=param1&query2=param2 on top URI bar in test step editor

Scenario: The changes of test step parameters value will not synchronized upward to its REST request
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1
  And user creates a test step from the REST request
  When user changes the parameter value from param1 to change1 in test step editor
  Then the resource path will change to /resource/method/?query1=change1 on top URI bar in test step editor
  Then the parameter is query1 with value param1 in request editor

Scenario: User can set the parameter value by property expansion
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1
  And user creates a test step from the REST request
  And user creates a test suite custom property named prop with value 123
  When user changes the parameter value from param1 to ${#TestSuite#prop}
  And run the test step in test step editor
  Then user will get the URI www.tryit.com/resource/method/?query1=123 in raw request view

Scenario: Create a test case with four different REST requests under different methods or different resources and run it
  Given a new REST project is created with URI http://www.example.com/resource1/search/?query1=param1
  And a new REST resource is created with URI http://www.example.com//resource2/watch/?query2-1=param1
  And a new POST method named Log is created under the resource named Search
  And a new request named Request2 is created under the method named Search
  When user creates four test steps with the above four requests under the same test case
  And run the test case named TestCase1
  Then user will get the test case with four test steps running with status [FINISHED] in TestCase log


Scenario: Add assertions in test steps and run the test suite
  Given a new REST project is created with URI http://www.example.com/resource/search/?query1=param1
  And user creates a test step from the REST request
  When user adds an assertion Contains with value ABCtry in the test step
  And user adds an assertion Response SLA with value 1 in the test step
  And run the test suite with TestSuite1
  Then user will get the test suite running with status failed in test suite log


