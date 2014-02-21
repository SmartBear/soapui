@Manual @Regression
Feature: Extract the REST service or resource in one project as the same as one click REST project creation
  In order to get new service or resource added easily in a SoapUI REST project
  As Calvin
  I want to only paste the URI or path to add the new service or resource

#TODO add the samples and try them in soapUI

Scenario Outline: Add a new REST service from URI
  Given a new REST project is created
  When user adds a new REST service from <URI>
  Then the new REST service is added under the same project
    And the request view is showed up
    And the <endpoint>, <resource> ,<parameters> and <value> are extracted

Examples:
  |URI                                      |endpoint                    |resource           |parameters     |value |
  |https://abc.com/resource/search?param=1  |https://abc.com             |/resource/search   |param          |1     |
  |www.soapui.org/try/search?id=1234        |http://www.soapui.org       |/try/search        |id             |1234  |
  |/path/search                             |                            |/path/search       |               |      |
  |trythis                                  |                            |/trythis           |               |      |


Scenario Outline: Add a new REST resource from URI
  Given a new REST project is created
  When user adds a new REST resource from <URI> started by http or https
  Then the new REST resource is added under the same service <endpoint>
    And the request view is showed up
    And the new <resource> and <parameters>,<value> are extracted

  Examples:
  |URI                                      |endpoint                   |resource       | parameters |value    |
  |http://soapui.org/path/001               |http://soapui.org          |/path/001      |            |         |
  |http://abc.com/path/002                  |http://soapui.org          |/path/002      |            |         |
  |http://trythis.net/search?id=12          |http://soapui.org          |/search        |id          |12       |

Scenario Outline: Add a new REST resource from Path
    Given a new REST project is created
    When user adds a new REST resource from <Path>
    Then the new REST resource is added under the same service <endpoint>
    And the request view is showed up
    And the <resource> and <parameters>,<value> are extracted

Examples:
  |Path                                      |endpoint                   |resource       | parameters |value    |
  |/path/001                                 |http://soapui.org          |/path/001      |            |         |
  |path/002                                  |http://soapui.org          |/path/002      |            |         |
  |try.net/search?id=12                      |http://soapui.org          |try.net/search |id          |12       |

  Scenario Outline: Add a new children REST resource from URI
    Given a new REST project is created with www.soapui.org/parent
    When user adds a new children REST resource from <URI> started by http or https
    Then the new children REST resource is added under the same service <endpoint> and the parent <resource>
    And the request view is showed up
    And the <resource>, <parameters> and <value> are extracted

  Examples:
    |URI                                      |endpoint                   |resource              | parameters |value    |
    |http://soapui.org/path/001               |http://soapui.org          |/parent/path/001      |            |         |
    |http://abc.com/path/002                  |http://soapui.org          |/parent/path/002      |            |         |
    |http://trythis.net/search?id=12          |http://soapui.org          |/parent/search        |id          |12       |

  Scenario Outline: Add a new children REST resource from Path
    Given a new REST project is created with www.soapui.org/parent
    When user adds a new children REST resource from <Path>
    Then the new children REST resource is added under the same service <endpoint> and the parent <resource>
    And the request view is showed up
    And the <resource>, <parameters> and <value> are extracted

  Examples:
    |Path                                      |endpoint                   |resource              | parameters |value    |
    |/path/001                                 |http://soapui.org          |/parent/path/001      |            |         |
    |path/002                                  |http://soapui.org          |/parent/path/002      |            |         |
    |try.net/search?id=12                      |http://soapui.org          |/parenttry.net/search |id          |12       |
