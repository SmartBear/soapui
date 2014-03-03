@Manual @Regression
Feature: User can run REST request and get response in request editor.In open source version, the request has the request view and raw view, the response has the XML,JSON,HTML and raw view.

  Scenario: run REST request and get response
    Given a new REST project is created with URI www.tryit.com/resource/method;matrix=param1?query=param2
    When user clicks on submit request button
    Then user will see the GET http://www.tryit.com/resource/method;matrix=param1?query=param2 in RWA request view
    And user will see some contents in XML, JSON,HTML and Raw response view
