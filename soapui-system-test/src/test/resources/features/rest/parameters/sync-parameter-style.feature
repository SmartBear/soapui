@Manual @Regression

Feature: REST parameter style could be synchronized across REST request, method,resource and top URI bar

 Scenario: User can add 5 different styles parameters in resource editor and synchronized with request view
  Given a new REST project is created
  When user adds a parameter in resource editor with name param01 and value value01
  And user adds a parameter in resource editor with name param02 and value value02
  And in resource editor user changes the style to TEMPLATE for parameter with name param02
  And user adds a parameter in resource editor with name param03 and value value03
  And in resource editor user changes the style to HEADER for parameter with name param03
  And user adds a parameter in resource editor with name param04 and value value04
  And in resource editor user changes the style to MATRIX for parameter with name param04
  And user adds a parameter in resource editor with name param05 and value value05
  And in resource editor user changes the style to PLAIN for parameter with name param05
  Then request editor has parameter with name param01 and style QUERY at row 0
  And request editor has parameter with name param02 and style TEMPLATE at row 1
  And request editor has parameter with name param03 and style HEADER at row 2
  And request editor has parameter with name param04 and style MATRIX at row 3
  And request editor has parameter with name param05 and style PLAIN at row 4
  And all the parameters are in the RESOURCE level
  And Resource field in top URI bar has value /{param02}
  And Parameters field in top URI bar has value ;param04=value04?param01=value01

 Scenario: User can add 5 different styles parameters in method editor and synchronize with request view
 Given a new REST project is created
 When user adds a parameter in method editor with name param01 and value value01
 And user adds a parameter in method editor with name param02 and value value02
 And user changes the style to TEMPLATE for parameter with name param02
 And user adds a parameter in method editor with name param03 and value value03
 And user changes the style to HEADER for parameter with name param03
 And user adds a parameter in method editor with name param04 and value value04
 And user changes the style to MATRIX for parameter with name param04
 And user adds a parameter in method editor with name param05 and value value05
 And user changes the style to PLAIN for parameter with name param05
 Then request editor has parameter with name param01 and style QUERY at row 0
 And request editor has parameter with name param02 and style TEMPLATE at row 1
 And request editor has parameter with name param03 and style HEADER at row 2
 And request editor has parameter with name param04 and style MATRIX at row 3
 And request editor has parameter with name param05 and style PLAIN at row 4
 And all the parameters are in the METHOD level
 And Resource field in top URI bar has value /{param02}
 And Parameters field in top URI bar has value ;param04=value04?param01=value01


 Scenario: User can add 5 different styles parameters in request view and synchronize with resource and method editor
 Given a new REST project is created
 When user adds a parameter in request editor with name param01 and value value01
 And user changes the level to METHOD for parameter with name param01
 And user adds a parameter in request editor with name param02 and value value02
 And user changes the style to TEMPLATE for parameter with name param02
 And user changes the level to METHOD for parameter with name param02
 And user adds a parameter in request editor with name param03 and value value03
 And user changes the style to HEADER for parameter with name param03
 And user changes the level to METHOD for parameter with name param03
 And user adds a parameter in request editor with name param04 and value value04
 And user changes the style to MATRIX for parameter with name param04
 And user adds a parameter in request editor with name param05 and value value05
 And user changes the style to PLAIN for parameter with name param05
 Then method editor has parameter with name param01 and style QUERY at row 0
 And method editor has parameter with name param02 and style TEMPLATE at row 1
 And method editor has parameter with name param03 and style HEADER at row 2
 And resource editor has parameter with name param04 and style MATRIX at row 3
 And resource editor has parameter with name param05 and style PLAIN at row 4

 Scenario Outline: User can change parameter styles and synchronize with resource, method editor and topURI bar
 Given a new REST project is created
 And user adds a parameter in request editor with name param01 and value value01
 When  user changes the style to <new style> for parameter with name param01
 Then Parameters field in top URI bar has value <value in resource field>
 And Resource field in top URI bar has value <value in parameter field>
 And resource editor has parameter with name param01 and style <changed style> at row 0

 Examples:
   |new style        |value in resource field     |value in parameter field   |changed style |
   |TEMPLATE         |/{param0}                   |                           |TEMPLATE      |
   |HEADER           |/                           |                           |HEADER        |
   |MATRIX           |/                           |;param01=value01           |MATRIX        |
   |PLAIN            |/                           |                           |PLAIN         |
   |QUERY            |/                           |?param01=value01           |QUERY         |
