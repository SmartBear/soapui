Feature: Export a wadl

  Scenario: An imported wadl keeps the changes when exported
    Given a user opens an empty project
    And imports a wadl
    And make some changes to the project
    When user saves the project
    And exports the wadl back
    Then the exported wadl keeps the changes

  Scenario: A generated wadl keeps the changes when exported
    Given a user opens an existing rest project with generated wadl
    And make some changes to the project
    When user saves the project
    And exports the wadl back
    Then the generated wadl keeps the changes

  Scenario: An imported wadl doesn't change original wadl
    Given a user opens an empty project
    And imports a wadl
    And make some changes to the project
    When user saves the project
    And exports the wadl back
    Then the original imported wadl remains same