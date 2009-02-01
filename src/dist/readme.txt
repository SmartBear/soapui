Welcome to soapUI!

2009-02-02 : 2.5.1
---------------------------------------------------------------------------------
A large amount of fixes and minor improvements, including

** Bug
    * [SOAPUI-275] - proxy not working when calling mockservices
    * [SOAPUI-278] - errornous variable export in .sh files
    * [SOAPUI-298] - Update project-nature functionality / popups to support 2.5 features
    * [SOAPUI-310] - Eclipse plugin icons don't work
    * [SOAPUI-311] - MockResponseStep creation shows REST interfaces
    * [SOAPUI-312] - Eclipse plugin desktop tab icons
    * [SOAPUI-313] - Eclipse plugin Project Viewer can be opened twice
    * [SOAPUI-314] - Cache Definitions should be true for new projects
    * [SOAPUI-319] - ProjectListener.afterLoad not called for custom ProjectListeners
    * [SOAPUI-326] - TestCase child icons do not show, and an error is displayed.
    * [SOAPUI-327] - Several sub-menu items are incorrectly named
    * [SOAPUI-332] - NPE with xpath in property-expansion referring to non-existant node(s)
    * [SOAPUI-336] - missing non-cached attachments cause NPE when trying to show file size
    * [SOAPUI-340] - Fix property-expansions support in rest resource paths
    * [SOAPUI-342] - Validating DocLiteral messages does not show errors for superfluous elements in body.

** Improvement
    * [SOAPUI-276] - support PropertyExpansionContainer in WssContainer class
    * [SOAPUI-290] - Add "count" column to soap-monitor log
    * [SOAPUI-308] - Add host property to MockResponse TestStep
    * [SOAPUI-321] - Improve validation error message on empty response
    * [SOAPUI-325] - Support for lists in "recreate request"
    * [SOAPUI-330] - After adding the SoapUI nature to a project, switch to the Project Explorer and select the project.
    * [SOAPUI-331] - Add option to "Add to soapUI project" for WADL.
    * [SOAPUI-341] - Add forum and user-guide buttons to toolbar
    * [SOAPUI-344] - Improve property-expansion functionality for REST/HTTP Requests

** Task
    * [SOAPUI-210] - Create Test for WSTF WS-A Scenario
    * [SOAPUI-280] - Update maven plugins to 2.5 status
    * [SOAPUI-286] - automate maven-plugin build and dist in luntbuild
    * [SOAPUI-296] - update preferences - sync with std soapUI 
    * [SOAPUI-297] - automate build and nightly build 
    * [SOAPUI-305] - upgrade jetty dependency to 6.1.14
    * [SOAPUI-309] - Create 2.5.1 NetBeans Plugin
    * [SOAPUI-329] - Update dependencies for release
    * [SOAPUI-333] - Upgrade to wss4j 1.5.5
    * [SOAPUI-337] - update splash images

** Document
    * [SOAPUI-334] - upgrade maven plugins
    * [SOAPUI-335] - Upgrade eclipse plugin

** Sub-task
    * [SOAPUI-281] - update maven 1.x plugin
    * [SOAPUI-282] - update maven 2.x plugin
    * [SOAPUI-287] - automate core maven plugins

2008-11-18 : 2.5 Final
---------------------------------------------------------------------------------
A bunch of bug-fixes and minor improvements, thanks to our awesome customers and
community for testing and reporting... we owe you another great release!

/eviware-soapui-team

2008-11-05 : 2.5 beta2 release
---------------------------------------------------------------------------------
A bunch of improvements and fixes:
- Much Improved WS-Addressing support
- SOAP-Monitor now support HTTP Tunnel now with SSL support (replaces TCP-Tunnel in 2.0.2)
- REST improvements
  - Improved REST Representation handling
  - Option to not URL-Encode parameter values
- Mock-related improvements
  - MockResponseStep -> open sourced from soapUI Pro, load-test/sync improvements
  - MockOperation Query Dispatch
  - MockService wire log
  - Improved MockService dispatching; support for handling responses and faults
- New Request options:
  - Entitize option for Requests
  - Option to follow redirects
  - MTOM improvements (not require binary datatype)
- Property-Transfer Improvements (entitize, transfer child-nodes)
- Improved Resolve Dialog / Logic
- Command-line improvements
  - Fixed command-line runners to run from other directories
  - Added –G and –P command-line switches to command-line runners
- Scripting-related:
  - Updated to groovy 1.5.7
  - Much Improved Groovy Editor
- And a slew of bug-fixes minor improvements thanks to all our great customers and community!

Thanks to all of you!

2008-09-26 : 2.5-beta1 release!
----------------------------------------------------------------------------------
Finally a new version!
- REST/HTTP Support
  - WADL import / export / generation
  - JSON/HTML to XML conversion for assertions, transfers, etc..
  - REST / HTTP Request TestStep
  - Generate both code and documentation for WADLs
- WS-Addressing support
  - Request, MockResponse, Assertion
- MockService improvements
  - onRequest / afterRequest scripts
  - improved WSDL exposure with ?WSDL endpoint
  - docroot for serving static content
  - HEAD request support
- Encrypted Project Files and hidden password fields
- LoadTest before/afterRun scripts
- Import/Export TestCases/TestSuites for sharing
- Relative paths to project resources
- Improved SOAP Monitor now supports keep-alive and chunked encoding
- Dump-File for response message automatically saves responses to a local file
- Unique keystores on request-level
- Improved XPath Contains Assertion with option to ignore namespace prefixes
- Improved compression algorithm support
- Extended HTTP-related settings
- ..

Backup your existing projects before testing and please don't hesitate to contact
us if you have any issues, suggestions, complaints, etc!


2008-01-28 : 2.0.2 bug-fix release
----------------------------------------------------------------------------------
Some more bugfixes and improvements:
- Fixed -n option for commandline LoadTestRunner (was incorrectly -h)
- Fixed internal initialization of copied/cloned testcases/teststeps and during loadtesting
- Improved Aut/Header inspectors to be visible for form/overview views
- Fixed quoting of SOAP 1.2 Action in content-type header
- Improved opening of local files in external browser (reports, etc)
- Fixed initialization of custom RequestFilters 
- Fixed script-evaluation in MockResponses to allow modification of the responseContent
- Fixed logging/display of failed MockRequests
- Fixed caching of external WSDLs in SchemaComplianceAssertion if another URL was used
- Fixed attribute handling with wildcards
- Fixed NPE on empty response messages
- Fixed a number of typos
- Updated to trunk version of XMLBeans which fixes corruption of project-files on save
- Updated to full version of xercesImpl 2.9.1 for full JAXP functionality
- etc..

As always thanks to you all reporting for making soapUI better and better!

2008-01-15 : 2.0.1 bug-fix release
-----------------------------------------------------------------------------------
A large number of important bug-fixes and a small number of improvements:
- Updated Groovy to 1.5.1
- Fixed Keystore-initialization to use specified provider
- Fixed NPE when initializing properties
- Fixed corruption of PropertyTransfer, ConditionalGoto and RunTestCase teststeps
- Fixed invalid Regular Expression in XSDs to get discarded and show a warning
- Fixed parallell execution of TestCases in TestSuites
- Fixed encoding-problems when compiling Groovy Scripts
- Added support for %20 as space-delimiter in command-line arguments (for unix/linux)
- Fixed check to recreate messages when updating interface
- Fixed global properties as PropertyTransfer targets
- Fixed NPE:s in related to TestCase and MockService logs
- Fixed DnD of requests to TestCases
- Memory fixes
- etc..

soapUI Pro
- Fixed generation of indexed XPath expressions
- Fixed refactoring issues with namespaces and multiple updates
- Improved WSDL Coverage:
  - Added possibility to exclude elements from coverage calculation
  - Fixed handling of empty elements
  - Moved settings to be at project-level
- Added option to skip to closing DataSource Loop when no data is available in a DataSource 
  TestStep  
- Improved import/export of requirements to include testcases and links
- etc..

2007-12-12 : 2.0 final release!
-----------------------------------------------------------------------------------
A bunch of minor improvements and a large number of bug-fixes made it into the final 
release - thanks to all who have reported, tested and helped us out!

2007-12-02 : 2.0 beta2 release!
-----------------------------------------------------------------------------------
Overhauled WS-Security support and many minor improvements;
- WS-Security support has been greatly enhanced and is now managed at Project-level for 
  application to Requests, MockServices/Responses and SOAP Monitors
- Raw message viewer for viewing actual data sent/received
- Aut Request inspector for editing authentication-related settings
- Interface Viewer has been extended with new Overview, Endpoints and WS-I Compliance tabs
- LoadTests can now continuously export statistics for post-processing
- Improved Message-Inspector for logged messages
- TestRun Log has been visually improved

2007-11-14 : 2.0 beta1 Release!
-----------------------------------------------------------------------------------
This is the first beta of soapUI 2.0, boasting a large number of new features. Please backup
your existing project-files before testing and report any issues at the sourceforge forums.

* Improved WS-Security support;
- messages can now be signed
- messages can be encrypted
- support for SAML Assertion insertion
- will be further improved for final release
* Built in SOAP Monitor for capturing live traffic
- tunnel / proxy modes
- create request, testcases and mockservices from recorded traffic
* Setup/Teardown scripts can now be specified for both TestCases and TestSuites
* Run TestCase teststep allows parameterized running of testcases from with a testcase 
  with return properties
* Enabled/Disable Assertions / TestCases / TestSuites
* MockService improvements:
- SSL support
- Mocked wsdls are now exposed via a (very) simple web-interface
- Start/Stop scripts
* New Project Overview 
- Project metrics
- Load/Save scripts
* TestSuite Editor run log
* Much improved property-handling; 
- properties can now be specified at testcase, testsuite, project and global level
- property-expansions and property-transfers have been extended accordingly
- drag-and-drop for creating property-expansions
- properties can be shown in navigator tree
- property-refactoring; renaming properties updates associated property expansions
* TestCase Log improvements:
- limit output and logged results to preserve memory under long-running tests
- generate MockServices from TestCase execution
* And many more minor improvements..
- "Start Minimized" action for MockSerivces
- UI improvements
- Auto-save open-sourced

2007-09-26 : 1.7.6 Release!
-----------------------------------------------------------------------------------
The intermediary 1.7.6 release focuses on general functionality and many UI improvements

Improvements:
* Default authentication settings on endpoint level
* XQuery support in assertions and property-transfers
* Dialogs for launching command-line runners
* Apache CXF wsdl2java integration
* Regular expression support in Contains/NotContains assertions
* Improved editors with line-numbers, find-and-replace, etc..
* Greatly improved project/workspace management including support for open/closed projects
* Support for remote projects over http(s)
* Improved/laxed up MTOM functionality
* Global/System-property access in property-expansions
* Very preliminary and inital extension API
* And a large number of UI improvements and minor adjustements

Bug-Fixes:
* Much-improved support for one-way operations
* Property Expansion is now supported in Conditional Goto Steps XPath
* Fixed save of empty properties in Properties Step
* Fixed URL decoding of WSDL port locations
* Fixed correct setting of SOAPAction / Content-Type headers for SOAP 1.2
* Mockservice fault with http response code 500
* Generate TestSuite does not use existing Requests
* OutOfMemory error when creating backup requests

As always we are grateful to our enthusiastic users! You Rock!

2007-08-06 : 1.7.5 Final!
-----------------------------------------------------------------------------------
The final release of soapUI 1.7.5 adds a small number of features and fixes a number
of bugs:

Improvements:
* Action to change the operation of a TestRequest.
* Improved MockService log with own toolbar and options to set size and clear.
* Possibility to set the local address both globally and on a request level.
* Option to pretty-print project files for easier SCM integration.
* Added requestContext variable to MockOperation-dispatch scripts allowing for thread-safe passing of
  values from dispatch script to response script
* Added option to enable interactive utilities when running from command-line.

Bug-Fixes:
* Fixed UpdateInterface to not set all TestRequests to same operation
* Fixed cloning of Assertions to be persistant
* Fixed Memory-Leaks in MockService Log
* Fixed Display of correct Response Message Size
* Fixed Dependencies for Eclipse Plugin
* Fixed PropertyExpansion to support xpath expansion also for Context Properties
* Fixed Form Editor to not pretty-print message and correctly hande nillable values (soapUI Pro)
* Fixed initializing of external libraries to be before intializing of Groovy Script Library when running any of the command-line runners (soapUI Pro)
* Fixed XPath creation when nodes exist with same name at different positions in hierarchy
* etc..

2007-07-11 : 1.7.5 beta2
-----------------------------------------------------------------------------------
soapUI 1.7.5 beta2 contains a small number of improvements and a bunch of bugfixes:

Improvements:
* Rudimentary support for wildcard element/attribute values in the matching 
  XML of XPath Contains assertions, which eases comparisons of large XML blocks.
* A dedicated Error Log which makes it easier to understand errors and report them back to us :-)
* Action to import global preferences from an existing soapUI installation.
* An "Add Endpoint to Interface" action for easily adding MockService endpoints 
  to their mocked Interfaces. Changing the MockService endpoint will automatically update 
  the associated Interface Endpoint as well.
* Request/MockResponse option option to dynamically remove empty content from outgoing requests, 
  which can be usefull when performing data-driven tests where not all data is available in each iteration.
* Request/MockResponse option for automatically encoding attachments as specified by their associated 
  WSDL Part (base64 or hexBinary)
* Fixed WSDL import to allow redefinition of global types/element (can be turned off in WSDL Settings). 
  Thanks to Lars Borup Jensen!
* Some more dependency updates:
  o log4j to 1.2.14
  o xmlunit to 1.1
  o commons-ssl-0.3.4 to not-yet-commons-ssl-0.3.8
* etc
  
Bug-Fixes:
* Fixed move TestCase up/down with keyboard
* Fixed validation mocking of RPC operation requests with attachments
* Fixed Termination of CommandLine TestRunners
* Fixed null column values in JDBC DataSource results to be replaced with empty string
* Fixed spawning of HTML Reports to use default system browser on Windows
* Fixed stripping of whitespaces to also remove comments
* Fixed attachments tab title update for mock responses
* Fixed skipping of projects with running tests when auto-saving
* Fixed form-editor to insert xsi:nil="true" on empty nillable fields
* etc..

As always, a huge Thank You to our community, and please don't hesitate to report any issues, etc...  

2007-07-02 : 1.7.5 beta1
-----------------------------------------------------------------------------------
soapUI 1.7.5 is another intermediate version which addresses a large number of
community feature requests and stability issues.

Major improvements in soapUI 1.7.5 are

* Extensive support for cloning/moving TestSteps/TestCases/TestSuites withing/between projects
* Workspace management
* Enhanced generation of TestSuites
* Enhanced generation of MockServices
* A new Response SLA Assertion (Contribution by Cory Lewis!)
* Possibility to reorder TestCases within a TestSuite
* Possibiliy to Disable/Enable TestSteps
* Improved "Update Definition" functionality
* And many more... see http://www.soapui.org/new_and_noteworthy_175.html for a more complete list

Also a large number of bugs have been fixed, including
* MimeBinding not read correctly
* Bad mock operation for operation within mimeBinding
* Error referencing included schema types in the default ns
* WsdlMockResult.setRe_ponseContent
* HTTP headers do not get copied to TestCase
* Loadtest thread count has UI limit of 100 threads, 
* soapUI uses startinfo XOP header rather than start-info 
* Junit Report times incorrect
* and many more...

As always we owe great thanks to our users for testing and giving us feedback on 
bug-fixes and improvements... 

2007-05-04 : 1.7.1 release
-----------------------------------------------------------------------------------
This is a bug-fix release which fixes some urgent issues in the 1.7 release

* More performance and memory improvements during load testing
* Fixed attachment-support in MockRequest dispatching, see Bug Report
* Fixed setting of lastSavedDate when WSDL caching is disabled
* Improved focusing when opening/switching desktop windows
* Fixed Check-Box label and creation of TestRequests with default Not SOAP Fault assertion, see Bug Report
* Fixed correct count and termination of LoadTestRunners, see Bug Report
* Fixed TPS/BPS calculation with branched testcases, see Bug Report
* Updated to commons-logging 1.1
* And many more internal fixes..

As always we owe great thanks to our users for testing and giving us feedback on 
bug-fixes and improvements... 

2007-04-10 : 1.7 final release
-----------------------------------------------------------------------------------
Many more major and minor issues have been fixed with the last snapshot releases, see the 
snapshot release page for details. Since the last snapshot, the following have been fixed/added;

* Performance and memory improvements during load testing
* Fixes in dispatching of mocked RPC operations
* Improved importing of services/bindings in imported wsdl's
* Fixed property-expansion in MockResponse HTTP Headers
* Fixed NPE when running on Linux
* Added possibility to add own endpoing when launching TcpMon
* Renamed SOAP Fault / Not SOAP Fault assertions to their correct opposite names
* And many more minor improvements

As always we owe great thanks to our users for testing and giving us feedback on 
bug-fixes and improvements...

2007-03-14 : 1.7 beta2 release
------------------------------------------------------------------------------------
The beta2 release adds the following features above those accumulated fixes in the recent
snapshot releases (http://www.soapui.org/snapshot.html)

* Initial Support for importing SOAP 1.2 bindings
* Improved MockOperation editor and possibility to dispatch to a MockResponse with a Groovy Script
* Possibility to set default Look and Feel
* Possbility to turn off multipart attachments
* Check for external modification of project files before saving
* Option to assign new endpoint to existing requests when updating a WSDL
* Fixed right-button menus on Mac
* Fixed teststep naming when inserting new Request Steps
* Increased maximum number of threads to 9999
* Fixed wstools commandline runner error and updated to wsconsume in alignment with final JBossWS 1.2.0 release
* Fixed closing/release of exported attachments
* Fixed support for quoted charset values both for requests and mockresponses
* ... and more internal refactorings and minor improvements

As always our huge thanks goes out to all our users who have helped us identify and fix 
many of the above issues. Keep your reports coming!

2007-02-09 : 1.7 beta1 release
------------------------------------------------------------------------------------

We are happy to release this intermediate version with several key improvements to soapUI functionality.
		
* Support for Mocking of WebServices from within soapUI. Mock Services can be run either from inside soapUI 
or with one of the IDE/Maven/CommandLine plugins.This opens for a number of usage scenarios, including:
- Rapid Prototyping of WebServices; generate a complete static mock implementation from a WSDL in seconds 
and add dynamic functionality using Groovy.
- Client testing/development; create mock implementations of desired operations set up a number of 
sample responses (including attachments and custom http-headers) so clients can be developed/tested without 
access to the "live" services. Responses can be cycled, randomized or selected with XPath expression from 
incoming request
- Test-Driven Development; Create soapUI TestSuites/TestCases against MockServices before/during the actual
services have been/are being implemented
* New/Improved Tool Integrations for JBossWS "wsimport" and Oracle wsa
* Improved WSDL-inspector shows tree/outline view over complete contract with "drill-down" functionality
* SSL-inspection shows peer certifacte information for responses received using https
* Generate TestSuite action to generate a complete TestSuite for all operation in an interface
* Improved property-expansion allows XPath expression directly in property-expansion syntax
* New Not-SOAP-Fault assertion
* Possibility to expand received MTOM attachments for schema compliance
* Possibility to override attachment Content-IDs
* Possibility to add default schemas for validation
* Possibility to run WS-I validations using commandline tools
* Possibility to set log-tabs history
* Many more minor improvements and bug-fixes

As always, please make backups of your project files before testing and let us know if you have any issues!

2006-11-12 : 1.6 final release
------------------------------------------------------------------------------------

We are extremely happy to finally release soapUI 1.6 final which introduces a large number of 
fixes and many minor improvements since the beta2 release, including;

* Added exclusion list to proxy settings (comma-seperated)
* Added 2 context properties available when load-testing:
- ThreadIndex : the index of the created thread... this value will never change for a given TestCase during its run time.. 
New threads will simply get an incremented index... the mod of this value could for example be used as an index into a data-file 
(to handle changes in number of threads) 
- RunCount: tells how many times the TestCase has been run by its thread (not in total) during the current LoadTest 
* Added initial support for unzipping gzipped response messages, ie messages with a Content-Type or Content-Encoding ending with "gzip". 
Thanks to Diego Banda.
* Added request-level property to inline attachment content in response editor (as in soapUI 1.5)
* Improved saving of projects to not corrupt files on out-of-memory (saving now first saves to an in-memory buffer
which is written to file if no errors occur..)
* Fixed validation of derived types in xml-editors
* Fixed automatic adding of TestStep Status assertion to newly created loadtests,
* Added option to ignore case in Contains/NotContains assertions
* Improved support for automatic validation by adding an option to not send invalid requests 
* Added support for automatic validation of request/response messages in editors  
thanks to Michael Vorburger! ("Preferences / UI Settings / Validate Request / Response")
* Added support for generating rpc-parts for attachments ("Preferences / WSDL Settings / Attachment Parts")
* Added [Tools] button to all Tools dialogs which opens "Preferences / Integrated Tools" for setting tool paths
* General dialog improvements; Escape closes and F1 shows online help if available
* Fixed validation of SOAP mustUnderstand/encodingStyle attributes in entire messages
* Added possibility to edit response message and revalidate with Alt-V (including assertions)
* Added possibility for custom background image in desktop. Just place a soapui-background.gif/jpg/png in the 
soapUI bin directory (at last :-), Thanks to Roger Sundberg
* Added option to open TestCase editor when adding requests to a TestCase
* Updated to commons-ssl-0.3.0.jar
* Fixed caching of attachments to now be saved in the soapui-project file (compressed)... this makes
portability of soapui-projects with attachments much simpler... The attachment-folder setting has been
removed.
* Fixed property-expansion when running xpath-assertions and property-transfers from within their
editors.
* Fixes password-input fields to be masked

2006-09-12 : 1.6 beta 2 release
------------------------------------------------------------------------------------

Welcome to soapUI 1.6 beta2 which introduces a large number of fixes and many minor 
improvements, including;

* Syntax highlighting in groovy editor
* Improved memory management under long running tests
* Support for external log4j configuration
* Greatly improved Groovy script performance and memory usage
* Undo/Redo in most editors
* Extended Property-Expansion to allow specification of step and property
* New tabbed layout mode for request/response editors
* UI "modernization"


2006-07-14 : 1.6 beta 1 release
------------------------------------------------------------------------------------

Welcome to soapUI 1.6! We are happy to finally release this version with several key 
improvements to soapUI functionality... please help us test and verify that old projects
dont stop working and that the new features work as expected...
 
Major Improvements

* Support for Attachments (MTOM / SwA / Inlining)
* 12 Tool integrations for;
      o Code Generation; JBossWS (wstools), JAX-RPC (wscompile), JAX-WS (wsimport), 
        XFire 1.1.X (WsGen), Axis 1.X (wsdl2java), Axis 2 (wsdl2code), JAXB 2.0 (xjc), 
        XmlBeans 2.X (scomp), GSoap 2.7.X and .NET 2.0, including a ToolRunner for 
        running code-generation tools from the command line
      o WSDL-Generation; JBossWS (wstools)
      o WS-I Basic Profile Validations
      o Apache TcpMon
* WSDL Caching and exporting
* JUnit Reports for functional tests from commandline

Minor Improvements

* Support for importing secured wsdls
* Initial support for Property-Expansion in test requests and xpath expressions
* Automatic/Manual generation of WS-Security headers for Authentication
* SOAP 1.2 support for request generation / HTTP headers / validation
* Improved request generation
* Options to sort items in navigator
* Integrated help
* Default assertions when creating request steps
* Support for unverified SSL certificates
* Response-size threshold
* Support for preencoded urls (see Forum discussion)
* Updated xmlbeans/saxon for improved XPath 2.0 support
* More keyboard shortcuts

Bugfixes

* Readonly Project files
* Threads in loadtests shared underlying data
* Circular imports/includes
* Tools run in headless environment

2006-04-06 : 1.5 final release
--------------------------------------------------------------
we are happy to announce the final release of soapUI 1.5. It includes

* a fair number of bug fixes
* improvements to the website (jmeter comparison, more scenarios, etc..)
* a windows installer including the required JRE for those that dont have java installed 
  and/or want start-menu icons and an uninstaller.

The release is as always available at http://sourceforge.net/projects/soapui/?abmode=1

2006-03-13 : 1.5 beta2 release
----------------------------------------------------------------------
This is the second beta for soapUI 1.5, it introduces no new major functionality but:

* fixes a large number of internal issues and adds some improvements in the area of LoadTest timings, etc.
* includes most of the updated documentation both on the site and in the distributions
* includes the first 1.5 beta of the maven-plugin

As always huge thanks for all feedback and encouragement! Keep it coming :-)

2006-02-27 : 1.5 beta1 release
----------------------------------------------------------------------
This is the first beta for soapUI 1.5, read all about it on the soapui.org website.

1.0.3 release 2005 11 24
--------------------------------------------------------------
This release fixes the following issues:

* Fixed inclusion of relative xsds in sub wsdls, see bug 1362652
* Fixed lockup on failed authentication, see bug 1362888
* Fixed handling of overloaded operations, see bug 1362929
* Fixed handling of wsdls containing muliple inline schemas in the types element

And adds a small number of improvements:

* Added "Show Editor" menu options to relevant popup menus in navigator
* Added possibility to move test-steps in the testcase editor with Ctrl-Up/Down
* Added possibility to open test-step editors by selecting their step in the 
  TestCase editor and pressing Enter
* Added a "Clone TestCase" option to the TestCase popup menu allowing cloning of an 
  entire testCase including all its test steps.  
* Related site/doc updates

Thanks to all that have helped in reporting/suggesting/confirming the above issues!

1.0.2 release 2005 11 20
--------------------------------------------------------------
This release fixes the following issues:

* Fixed inclusion of schemas when adding WSDL from the file system; see bug 1350372
* Fixed scrollbar-behaviour in xml-editors for long-lines
* Fixed pick-up of proxy-settings from standard system properties; see feature request 1346544
* Fixed source-distribution so that it compiles... (duh!)
  - see the site documentation on how to compile

And adds a fair number of improvements:

* Added 2 new Test steps (see the user guide for details):
  - a "Delay" step allowing a pause of xx milliseconds
  - a "Conditional Goto" step allowing xpath-based conditions to trigger jumping to any other
    test step opening for conditional test steps, monitoring, restarts, etc.
* Improvements to ValueTransfers;
  - Possibility to select values from the previous steps request message (instead of response) or one of its
    endpoint, domain, username or password properties.
  - Possibility to assign the source value to one of the following steps endpoint, domain, username or password properties.
* Improved logging in TestCase desktop panel to show info on performed value transfers and goto conditions
* Improved WSDL importing to handle WSDLs with invalid XSDs. A warning will be shown and request-generation/
  schema-validation will not work.
* Upgraded to xmlbeans 2.1.0  
* Improved keyboard navigation in navigator so selected items can be opened by pressing enter
* Improved endpoint combobox in request editors to include an option for adding a new endpoint
* Added a number of unit tests for wsdl importing and valuetransfer test steps
* Numerous minor adjustements/improvements (icons, messages, etc.)
* Added documentation on how to integrate soapUI in other projects
* Related site/doc updates

Thanks to all that have helped in reporting/suggesting/confirming the above issues!

1.0.1 release 2005 10 30
--------------------------------------------------------------
Maintainance release:

* Fixed naming of value transfers; see bug 1342557 
* Fixed captalization of Content-type; see bug 1328568 
* Fixed closing of dependant panels when removing model objects; see bug 1342555
* Minor improvements to documentation (added PDF)

Thanks to all reports / comments! Focus is now on the first 1.1 beta containing load-test functionality, 
scheduled for mid-november.

1.0 release - 2005 10 17
--------------------------------------------------------------
1.0 final contains the following fixes and improvements;

* Fixed setting of empty SOAP Action header; see bug 1314885
* Fixed schema validation of SOAP Faults
* Fixed ValueTransfer editor related exception; see bug 1312942
* Updated to commons-httpclient-3.0rc4
* Minor refactorings and updates to the documentation

Thanks to all reports / comments so far! Focus is now on fixing any new bugs and on the next version..

1.0b2 release - 2005 10 02
------------------------------------------
beta2 contains the following fixes/improvements;

* Improved "Update Definition" functionality; fixed bug 1296190 and added the possibility to choose a 
  new binding if the old one has been renamed
* Added the possibility to set username/password/domain for all requests in a testcase via a "Set Credentials" 
  button in the TestCase Panel
* Added username/password/domain properties to the maven plugin to allow overriding of these during CI test runs
* Improved and added documentation for the CommandLine TestRunner
* Added a SimpleNotContainsAssertions checking that a specified token is missing from the response
* Fixed bug 1305583 which reused authentication credentials
* Fixed bug 1304913 which did not terminate soapUI correctly under webstart runtime
* Modified splitpane behaviour so that dragged splitpanes do not automatically resize themselves (see "bug" 1304920)
* Added a sample-project to the offline distribtion containing some tests, assertions, etc.
* Improved Service Endpoints management
* Fixed problems when importing WSDLs containing REST-style bindings
* Added a sample project containing 2 interfaces and a small number of tests for getting started.
* Related and additional updates to the documentation

Thanks to Jesper Brandt for suggesting/helping out with several of the above issues!

1.0b1 release - 2005 09 14
------------------------------------------
This is the first public release and I'm sure there will problems related to 
WSDL importing, XML Validation, etc. (soapUI has been tested rather extensively
only with doc/literal services in a J2EE environment and running under WinXP). 

Please have patience and notify me any problems together with WSDL's, messages, 
project-files, etc. so these can be fixed as soon as possible. Thanks!

-------------------------------------------------------------------------

Get started by running one of the soapUI scripts in the bin folder..

Good Luck and happy soaping!

Ole
ole@eviware.com
