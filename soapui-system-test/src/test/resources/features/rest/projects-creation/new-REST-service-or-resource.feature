@Manual @Regression
Feature: Extract the REST service or resource in one project as the same as one click REST project creation
  In order to get new service or resource added easily in a SoapUI REST project
  As Calvin
  I want to only paste the URI or path to add the new service or resource

Scenario Outline: Add a new REST service from URI
  Given a new REST project is created
  When user creates a service from <URI> under the project named REST project
  Then the navigator tree shows service named <endpoint>
  And Endpoint field in top URI bar has value <endpoint>
  And Resource field in top URI bar has value <resource>
  And Parameters field in top URI bar has value <parameters>

Examples:
  |URI                                      |endpoint                    |resource           |parameters     |
  |https://abc.com/resource/search?param=1  |https://abc.com             |/resource/search   |?param=1       |
  |www.soapui.org/try/search?id=1234        |http://www.soapui.org       |/try/search        |?id=1234       |
  |/path/search                             |                            |/path/search       |               |
  |trythis                                  |                            |/trythis           |               |


Scenario Outline: Add a new REST resource from URI
  Given a new REST project is created
  When user creates a resource from <URI> under the service named http://www.soapui.org
  Then the navigator tree shows resource named <method> under the service named http://www.soapui.org
  And Endpoint field in top URI bar has value <endpoint>
  And Resource field in top URI bar has value <resource>
  And Parameters field in top URI bar has value <parameters>

  Examples:
  |URI                                      |endpoint                   |resource       |parameters  |method   |
  |http://soapui.org/path/001               |http://soapui.org          |/path/001      |            |001      |
  |http://abc.com/path/002                  |http://soapui.org          |/path/002      |            |002      |
  |http://trythis.net/search?id=12          |http://soapui.org          |/search        |?id=12      |Search   |

Scenario Outline: Add a new REST resource from Path
  Given a new REST project is created
  When user creates a resource from <Path> under the service named http://www.soapui.org
  Then the navigator tree shows resource named <name> under the service named http://www.soapui.org
  And Endpoint field in top URI bar has value <endpoint>
  And Resource field in top URI bar has value <resource>
  And Parameters field in top URI bar has value <parameters>

Examples:
  |Path                                      |endpoint                   |resource       |parameters |name   |
  |/path/001                                 |http://soapui.org          |/path/001      |           |001      |
  |path/002                                  |http://soapui.org          |/path/002      |           |002      |
  |try.net/search?id=12                      |http://soapui.org          |try.net/search |?id=12      |Search   |

  Scenario Outline: Add a new children REST resource from URI
    Given a new REST project is created with URI www.soapui.org/parent
    When user creates a children resource from <URI> under the resource named Parent
    Then the navigator tree shows children resource named <name> under the resource named Parent
    And Endpoint field in top URI bar has value <endpoint>
    And Resource field in top URI bar has value <resource>
    And Parameters field in top URI bar has value <parameters>

  Examples:
    |URI                                      |endpoint                   |resource              | parameters |name     |
    |http://soapui.org/path/001               |http://soapui.org          |/parent/path/001      |            |001      |
    |http://abc.com/path/002                  |http://soapui.org          |/parent/path/002      |            |002      |
    |http://trythis.net/search?id=12          |http://soapui.org          |/parent/search        |?id=12      |Search   |

  Scenario Outline: Add a new children REST resource from Path
    Given a new REST project is created with URI www.soapui.org/parent
    When user creates a children resource from <Path> under the resource named Parent
    Then the navigator tree shows children resource named <name> under the resource named Parent
    And Endpoint field in top URI bar has value <endpoint>
    And Resource field in top URI bar has value <resource>
    And Parameters field in top URI bar has value <parameters>

  Examples:
    |Path                                      |endpoint                   |resource              | parameters |name    |
    |/path/001                                 |http://soapui.org          |/parent/path/001      |            |001     |
    |path/002                                  |http://soapui.org          |/parent/path/002      |            |002     |
    |try.net/search?id=12                      |http://soapui.org          |/parenttry.net/search |id          |Search  |
