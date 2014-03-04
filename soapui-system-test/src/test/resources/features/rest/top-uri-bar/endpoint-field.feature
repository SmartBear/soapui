@Manual @Regression
Feature: User can add, delete and edit endpoint in top URI bar of REST request. The property expansion woks in top URI bar endpoint field.

Scenario: edit endpoint in top URI bar endpoint field will only affect this request
   Given a new REST project is created with URI www.example.com/resource/path/?test=01
   When user change the endpoint to www.newex.net in top URI bar endpoint field
   Then only the endpoint field text changed to www.newex.net in the request editor
   And  the endpoint in service editor is still www.example.com
   And the dropdown list of endpoints in top URI bar has no www.newex.net endpoint in it

Scenario: edit current in drop down list  will be synchronized to service editor and dropdown list
  Given a new REST project is created with URI www.example.com/resource/path/?test=01
  When user uses 'edit current' in dropdown list of the endpoint field to change the endpoint to www.newex.net
  Then the endpoint field text changed to www.newex.net in the request editor
  And  the endpoint in service editor is changed to www.newex.net
  And the dropdown list of endpoints in top URI bar has www.newex.net endpoint in it
  But the dropdown list of endpoints in top URI bar has no www.example.com endpoint in it

Scenario: add new endpoint in dropdown list will be synchronized to service editor and dropdown list
  Given a new REST project is created with URI www.example.com/resource/path/?test=01
  When user uses 'add new endpoint' in dropdown list of the endpoint field to add an endpoint www.newex.net
  Then the endpoint field text changed to www.newex.net in the request editor
  And  the endpoints in service editor are www.newex.net and www.example.com
  And the dropdown list of endpoints in top URI bar has www.newex.net endpoint in it
  And the dropdown list of endpoints in top URI bar has www.example.com endpoint in it

Scenario: delete current in dropdown list will be synchronized to service editor and dropdown list
  Given a new REST project is created with URI www.example.com/resource/path/?test=01
  When user uses 'delete current' in dropdown list of the endpoint field to delete the current endpoint
  Then the endpoint field text changed to '- no endpoint set -' in the request editor
  And  the endpoint in service editor is blank
  And the dropdown list of endpoints in top URI bar has no endpoint in it

Scenario: the property expansion works in top URI bar endpoint field
  Given a new REST project is created with URI www.tryit.com/resource/method/?query1=param1
  And user creates a project custom property named endpoint with value http://www.example.com
  When user changes the endpoint to ${#Project#endpoint} in top URI bar endpoint field
  And submit the request
  Then user will get the URI http://www.example.com/resource/method/?query1=123 in raw request view

