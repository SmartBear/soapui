@Manual @Regression
Feature: User can change the resource path in top URI bar of REST request.The property expansion woks in top URI bar endpoint field.
  Scenario: the changes of resource path in top URI bar of REST request can be synchronized to navigator tree, resource editor and raw view
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    When user changes the resource path to /newres/search in top URI bar of request editor
    And submit the request
    Then the resource path in navigator tree is changed to /newres/search
    And the resource path in resource editor is changed to /newres/search
    And the URI in raw view changed to www.example.com/newres/search?test=01

  Scenario:the property expansion works in top URI bar resource field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    And user creates a project custom property named resourcepath with value newres/search
    When user changes the resource path to ${#Project#resourcepath} in top URI bar of request editor
    And submits the request
    Then the resource path in navigator tree is changed to /${#Project#resourcepath}
    And the resource path in resource editor is changed to /${#Project#resourcepath}
    And the URI in raw view changed to www.example.com/newres/search?test=01

  Scenario: user can add template parameter through top URI bar resource field
    Given a new REST project is created with URI www.example.com/resource/path?test=01
    When user adds {abc} at the end of the resource path in top URI bar of request editor
    And submits the request
    And inputs value 002 in the pop up window
    Then one template parameter named abc with value 002 is added in the request parameter list
    And the resource path in navigator tree is changed to /resource/path{abc}
    And the resource path in resource editor is changed to /resource/path{abc}
    And the URI in raw view changed to www.example.com/resource/path002?test=01