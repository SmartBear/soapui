@Manual @Acceptance
Feature: As Mark I can change the MockService method in MockAction editor to synchronize manually with the request method changing

  Scenario: If user change the method in MockAction editor and run a test step with MockService, but not change the origin request method, user will get a '404 not found' in raw response

  Scenario: If user change the method in MockAction editor and run a test step with MockService, and change the origin request method to the same one, user will get the correct MockResponse

  Scenario: User have two different methods under the resource1 and another method under resource2, user generate test steps and mock services according this service. All the test steps can get their correct response.

  Scenario: Using the above project, if user change the request method, user will get a '404 not found' in raw response

  Scenario: Using the above project, if user change the request method, then change the mock service method to the same one, user will get the correct MockResponse