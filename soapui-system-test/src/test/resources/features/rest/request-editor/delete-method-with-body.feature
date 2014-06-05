@Manual @Acceptance
 Feature: Added new body message support for DELETE method in REST and HTTP request, so that user can send DELETE request with body in HTTP/HTTP request or test step.

 Scenario: Send a body message with DELETE method in REST request editor
      Given user has a DELETE method REST request
      When user attaches a body message with all supported media type:
          | application/json   |
          | application/xml    |
          | text/xml           |
          | multipart/form-data|
          | mulitpart/mixed    |
      And sends the request
      Then user gets the correct content-type and attached body message in raw request editor

   Scenario: Send a body message with DELETE method in REST/HTTP test step editor
     Given user has a DELETE method REST/HTTP test step
     When user attaches a body message with all supported media type
     And sends the test request
     Then user gets the correct content-type and attached body message in raw test step editor

   Scenario: The property expansion can be sent with DELETE method all the editors: like REST request editor or REST/HTTP test step editor
     Given user has a DELETE method REST request and REST/HTTP test step
     When user attaches a body message with a property expansion
     And send the request or test request
     Then user gets the correct content-type and attached body message in raw editors
