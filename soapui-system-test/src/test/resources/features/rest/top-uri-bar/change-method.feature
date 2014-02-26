@Manual @Regression
Feature: The http method can be changed to GET,POST,PUT,delete,head,options,trace and patch in REST request. Those changed will be synchronized to the navigator tree and method editor.

Scenario Outline: change the REST request method and run
   Given a new REST project is created with URI www.example.com/resource/path/?test=01
   When user changes the method from GET to a <new method> on top URI bar in request editor
   And submits the request
   Then the method will be <new method> in raw request
   And the logo before method in navigator tree will change to <new method>
   And the method will change to <new method>  in method editor

Examples:
  |new method |
  |POST       |
  |PUT        |
  |DELETE     |
  |HEAD       |
  |OPTIONS    |
  |TRACE      |
  |PATCH      |

 Scenario: change the method in one request will be reflected to the other requests under the same method
  Given a new REST project is created with URI www.example.com/resource/path/?test=01
  And user adds a new request named request2 under the same method
  When user changes the method from GET to OPTIONS on top URI bar in request editor of request1
  Then the method on top URI bar in request editor of request1 will change to OPTIONS as well

 Scenario: the changed method will be saved when user save the project
  Given a new REST project is created with URI www.example.com/resource/path/?test=01
  And user changes the method from GET to TRACE on top URI bar in request editor
  When user close and save the project
  And reopen the project
  Then the http method of the request is still TRACE