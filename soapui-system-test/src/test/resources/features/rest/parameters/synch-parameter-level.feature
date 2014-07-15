@AutomatedWithFest @Regression
Feature: REST request parameter synchronization across REST request, method and resource

  Scenario: By default a parameter is added to RESOURCE level and it is synchronized between request and resource editors
    Given a new REST project is created
    When user adds a parameter in request editor with name reqParam and value reqParamValue
    Then request editor has parameter with name reqParam and value reqParamValue at row 0
    And resource editor has parameter with name reqParam and with empty value at row 0

  Scenario: A new parameter in resource editor is synchronized with request editor
    Given a new REST project is created
    When user adds a parameter in resource editor with name resParam and value resParamValue
    Then resource editor has parameter with name resParam and value resParamValue at row 0
    And request editor has parameter with name resParam and value resParamValue at row 0

  Scenario: A new parameter in method editor is synchronized with request editor
    Given a new REST project is created
    When user adds a parameter in method editor with name methodParam and value methodParamValue
    Then method editor has parameter with name methodParam and value methodParamValue at row 0
    And request editor has parameter with name methodParam and value methodParamValue at row 0

  Scenario: A parameter is moved from method to resource when the level is changed from METHOD to RESOURCE in request editor
    Given a new REST project is created
    When user adds a parameter in method editor with name methodParam and value methodParamValue
    And  user adds a parameter in request editor with name reqParam and value reqParamValue
    And user changes the level to RESOURCE for parameter with name methodParam
    Then method editor has no parameters
    And resource editor has parameter with name reqParam and with empty value at row 0
    And resource editor has parameter with name methodParam and value methodParamValue at row 1