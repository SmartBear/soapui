@Manual @Regression
Feature: Extract the REST service or resource in one project as the same as one click REST project creation
  In order to get new service or resource added easily in a SoapUI REST project
  As Calvin
  I want to only paste the URI or path to add the new service or resource

#TODO add the samples and try them in soapUI

Scenario Outline: Add a new REST service from URI
  Given SoapUI is started
    And a new REST project is created
  When user adds a new REST service from <URI>
  Then the new REST service is added under the same project
    And the request view is showed up
    And the <endpoint>, <resource> and <parameters> are extracted

Examples:
  |  URI    |    endpoint | resource  | parameters |

Scenario Outline: Add a new REST resource from URI  (doesn't work!!!)
  Given SoapUI is started
    And a new REST project is created
  When user adds a new REST resource from <URI>
  Then the new REST resource is added under the same service <endpoint>
    And the request view is showed up
    And the new <resource> and <parameters> are extracted

  Examples:
    |  URI    |    endpoint | resource  | parameters |
  #same URL, different URL

Scenario Outline: Add a new REST resource from Path
    Given SoapUI is started
    And a new REST project is created
    When user adds a new REST resource from <Path>
    Then the new REST resource is added under the same service <endpoint>
    And the request view is showed up
    And the <resource> and <parameters> are extracted

  Examples:
    |  Path    |    endpoint | resource  | parameters |

  Scenario Outline: Add a new children REST resource from URI
    Given SoapUI is started
    And a new REST project is created
    When user adds a new children REST resource from <URI>
    Then the new children REST resource is added under the same service <endpoint> and <resource>
    And the request view is showed up
    And the <parameters> are extracted

  Examples:
    |  URI    |    endpoint | resource  | parameters |
  #same URL, different URL

  Scenario Outline: Add a new children REST resource from Path
    Given SoapUI is started
    And a new REST project is created
    When user adds a new children REST resource from <Path>
    Then the new children REST resource is added under the same service <endpoint> and <resource>
    And the request view is showed up
    And the <parameters> are extracted

  Examples:
    |  Path    |    endpoint | resource  | parameters |




# TODO
# add new REST service ---right click the project name          same as create REST project
# add new resource    ---right click the REST service name      SoapUI show the endpoint before
# add new children resource ---right click the resource name    SoapUI show the endpoint and resources before