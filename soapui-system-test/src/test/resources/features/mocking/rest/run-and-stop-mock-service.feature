@Manual @Acceptance
Feature: As Mark I can run and stop Mock service 

Scenario: start and stop  Mock service from Mock service editor 

Scenario: start, stop and restart Mock service from context menu on service level

Scenario: start mock service minimized from context menu( only pro)

#Scenario: You should get a warning if you are trying to run two mock service in same port
#   Given the global setting is checked  for run REST mock 
#   When Creating a REST mock service 
#   Then the mock service is started 
   
#   Given mock service is running 
#   When starting a mock service from command line 
#   Then you should not get any info message 
#   Then you should see an error message in the terminal stating that a mock service is already started
   
#   Given the global setting is checked  for run REST mock 
#   Given mock service is running 
#   When Creating a Mockservice ( from request, from project, from interface)
#   Then a info message is shown stating “ A mock service is running, you can have several mock service started as the same time”

#  Given Mock service is running 
#  When starting a mock service from Editor/context menu
#  Then a info message is shown stating “ A mock service is running, you can have several mock service started as the same time”


Scenario: Run Mock service on different ports

Scenario: start, stop and restart a Mock service from a groovy script 

Scenario: Change responses while mock service is running 

Scenario: Change dispatch strategy while mock service is running ( eg SEQUENCE or Script)

Scenario:Run Mock service with customize response ( scripting)

Scenario: Run Mock service from Command line with different parameter( m,p,a,b,s,x,v,D,G,P,D,f)

#TODO suggestion: 1. check the test steps response during the running 2.multiple mock services running for the different requests/resources or for the different services 3.complicated mock service running( have more resources/children resources, more requests)






