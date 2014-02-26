@Manual @Regression
Feature: User can change the resource path in top URI bar of REST request.The property expansion woks in top URI bar endpoint field.
  Scenario: the changes of resource path in top URI bar of REST request can be synchronized to navigator tree, resource editor and raw view
    Given a new REST project is created with URI www.example.com/resource/path/?test=01
    When user changes the resource path to /newres/search in top URI bar of request editor
    And submit the request
    Then the resource path in navigator tree is changed to /newres/search
    And the resource path in resource editor is changed to /newres/search
    And the URI in raw view changed to www.example.com/newres/search/?test=01

  Scenario:the property expansion works in top URI bar resource field
    Given

  Scenario: user can add template parameter through top URI bar resource field
  #add template parameter at the end ({ para1}), it should ask for confirmation to add as a template parameter to resource. If confirmed, it should add it to paramter table and it should be added only once in the resource field