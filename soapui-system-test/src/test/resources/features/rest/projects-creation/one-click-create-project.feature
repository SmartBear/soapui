@Manual @Regression
Feature: Extract the REST URI to the Request editor top URI bar
  In order to get started easily with creating a REST project in SoapUI
  As Calvin
  I want to only paste the URI and create a new REST project

Scenario Outline: Reject invalid REST URI
  Given a new REST project is created with URI <invalid URI>
  When SoapUI presents the error message dialogue stating <error message>
  And user clicks OK in the error message dialogue
  Then user is sent back to the 'Create new REST project' dialogue

Examples:
|invalid URI                            | error message                                          |
|ftp://spotify.com/api/?userId=1234     | "URI contains unsupported protocol"                    |
|"http://sp\\sd.com/api/?userId=1234"   | "Invalid endpoint"                                     |
|(empty nothing is in the URI field)    | "Empty URI"                                            |


Scenario Outline: Parse a valid REST URI automatically
  When a new REST project is created with URI <Valid URI>
  Then Method option in top URI bar shows <method>
  And Endpoint field in top URI bar has value <endpoint>
  And Resource field in top URI bar has value <resource path>
  And Parameters field in top URI bar has value <parameters>
  And the navigator tree shows project named REST Project
  And the navigator tree shows resource named combining <method name> and <resource path>
  And the navigator tree shows method named <method name>

Examples:
|Valid URI                                                            |endpoint              |resource path                     |method name     |method| parameters       |
|http://service.com/api/1.2/json/search/search?title=Kill%20me        |http://service.com    |/api/1.2/json/search/search       |Search            |GET   |title=Kill me     |
|http://service.com/api/1.2/json/search/search?title=Kill me          |http://service.com    |/api/1.2/json/search/search       |Search            |GET   |title=Kill me     |
|http://service.com/rest/                                             |http://service.com    |/rest/                            |Rest              |GET   |n/a               |
|http://service.com/xml/api/json                                      |http://service.com    |/xml/api/json                     |Json              |GET   |n/a               |
|http://service.com/books/Fiction?Title=Why we do things?&publ=array  |http://service.com    |/books/Fiction                    |Fiction           |GET   |Title=Why we do things?,publ=array|
|/abc?book=15;column=12                                               |n/a                   |/abc                              |Abc               |GET   |book=15,column=12 |
|/bookandreviews.jsp?book=5                                           |n/a                   |/bookandreviews.jsp               |Bookandreviews.jsp|GET   |book=5            |
|/abc?book_id=12&a-opt                                                |n/a                   |/abc                              |Abc               |GET   |book_id=12,a-opt= |
|http://bokus.se/books/ISBN-5012359                                   |http://bokus.se       |/books/ISBN-5012359               |ISBN-5012359      |GET   |n/a               |
|spotify.com                                                          |http://spotify.com    |n/a                               |n/a               |GET   |n/a               |
|hello.hello                                                          |http://hello.hello    |n/a                               |n/a               |GET   |n/a               |
|/1.57/api/get?id=1234                                                 |n/a                   |/1.57/api/get                     |Get               |GET   |id=1234           |
|/conversations/{id}/{id}/{id}                                        |n/a                   |/conversations/{id}/{id}/{id}     |Conversations     |GET   |id=               |
|/conversation/date/{date}/time/{time}/?userId=1234                   |n/a                   |/conversation/date/{date}/time/{time}/|Time          |GET   |date=,time=,userId=1234|
|/conversation/date-{date}/time-{time}?userId=1234                    |n/a                   |/conversation/date-{date}/time-{time} |Time          |GET   |date=,time=,userId=1234|
|http://consys-qa-m09.websys.aol.com:8090/subscribers/subscriber      |http://consys-qa-m09.websys.aol.com:8090 | /subscribers/subscriber |Subscriber |GET |n/a                   |
|http://soapui.sthlm.smartbear.anytopdomain/rest/getItem?id=1234      |http://soapui.sthlm.smartbear.anytopdomain|/rest/getItem           |GetItem    |GET |id=1234               |
|http://www.youneverknowhowlongcouldtheybeinadomainnameandyoumusttryitinthetestscenarios.com/nowyounerverknowhowlongtheyhaveintheresourcepart/tryitbyyouself/tryitinyourtestscenarios/getItem?id=123;MATRIXpara=value1|http://www.youneverknowhowlongcouldtheybeinadomainnameandyoumusttryitinthetestscenarios.com|/nowyounerverknowhowlongtheyhaveintheresourcepart/tryitbyyouself/tryitinyourtestscenarios/getItem|GetItem|GET|id=123, MATRIXpara=value1|
|10.10.1.230:8090/subscribers/subscriber                              |http://10.10.1.230:8090|/subscribers/subscriber          |Subscriber        |GET   |n/a                    |
|api.soapui.com/services                                              |http://api.soapui.com  |/services                        |Services          |GET   |n/a                |
