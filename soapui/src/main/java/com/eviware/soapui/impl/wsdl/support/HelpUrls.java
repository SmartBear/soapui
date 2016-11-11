/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;

import static com.eviware.soapui.impl.support.HttpUtils.urlEncodeWithUtf8;

/**
 * Help URLS in SoapUI documentation
 *
 * @author ole.matzura
 */
public interface HelpUrls {


    /*
    Help Sites
    */
    public static final String BASE_URL_PROD = "http://www.soapui.org";

    public static final String BASE_URL_DEV = "http://dev.soapuidocs.sthlm.smartbear.local";

    public static final String BASE_URL_NEXT = "http://next.soapuidocs.sthlm.smartbear.local";

    public static final String MISSING_URL = "/missing-url.html";

    public static final String THIRD_PARTY_LICENSE_INFO = "/developers-corner/3rd.html";

    public static final String SMARTBEAR_WEB_SITE_START_PAGE = "http://www.smartbear.com";
    public static final String SMARTBEAR_PRIVACY_POLICY_URL = "/Store-Info/privacy-policy.html";

    public static final String ADDMOCKOPERATIONASMOCKRESPONSESTEP_HELP_URL = "/Service-Mocking/mocking-soap-services.html";
    public static final String ADDMOCKRESPONSETOTESTCASE_HELP_URL = "/Service-Mocking/mocking-soap-services.html";
    public static final String ADDREQUESTASMOCKRESPONSESTEP_HELP_URL = "/Service-Mocking/mocking-soap-services.html";
    public static final String ADD_ASSERTION_PANEL = "/functional-testing/validating-messages/getting-started-with-assertions.html";
    public static final String ADD_AUTHORIZATION = "/OAuth/add-authorization.html";
    public static final String ALERT_SITE_HELP_URL = "http://help.alertsite.com/WebHome";
    public static final String AMF_REQUEST_HEADERS_HELP_URL = "/amf/reference/the-amf-request-window.html";
    public static final String API_TESTING_DOJO_HELP_URL = "/dojo/overview.html";
    public static final String ATTACHMENTS_HELP_URL = "/SOAP-and-WSDL/adding-headers-and-attachments.html";
    public static final String AUTHORIZATION = "/OAuth/authorization.html";
    public static final String AUTHORIZATION_BASIC = "/OAuth/Basic.html";
    public static final String AUTHORIZATION_NTLM = "/OAuth/NTLM.html";
    public static final String AUTHORIZATION_OAUTH2 = "/OAuth/OAuth2.html";
    public static final String AUTHORIZATION_SPNEGO_KERBEROS = "/OAuth/SPNEGO-Kerberos.html";
    public static final String AXIS1X_HELP_URL = "http://ws.apache.org/axis/java/reference.html#WSDL2JavaReference";
    public static final String AXIS2X_HELP_URL = "http://ws.apache.org/axis2/tools/1_0/CodegenToolReference.html";

    public static final String CHANGEMOCKOPERATION_HELP_URL = "/soap-mocking/working-with-mockservices.html";
    public static final String CHANGEOPERATION_HELP_URL = "/soap-and-wsdl/operations-and-requests.html";
    public static final String CLONEMOCKSERVICE_HELP_URL = "/Service-Mocking/mocking-soap-services.html";
    public static final String CLONETESTCASE_HELP_URL = "/functional-testing/reference/testsuite-reference.html";
    public static final String CLONETESTSTEP_HELP_URL = "/functional-testing/structuring-and-running-tests.html";
    public static final String CLONETESTSUITE_HELP_URL = "/Functional-Testing/structuring-and-running-tests.html";

    public static final String COMMUNITY_HELP_URL = "http://community.smartbear.com";
    public static final String COMMUNITY_SEARCH_URL = "http://community.smartbear.com";

    public static final String CREATEMOCKRESPONSESTEP_HELP_URL = "/Service-Mocking/mocking-soap-services.html";
    public static final String CRYPTOSWSS_HELP_URL = "/soapui-projects/ws-security.html";
    public static final String CXFWSDL2JAVA_HELP_URL = "http://cxf.apache.org/docs/wsdl-to-java.html";

    public static final String DEBUGGING_ASSERTION_TEST_STEP = "/Functional-Testing/assertion-test-step.html";
    public static final String DEBUGGING_ENVIRONMENT_HANDLING = "/Environments/environment-handling-in-soapui.html";
    public static final String DEBUGGING_TESTCASE_DEBUGGING = "/functional-testing/testcase-debugging.html";
    public static final String DOTNET_HELP_URL = "https://msdn.microsoft.com/en-us/library/7h3ystb6.aspx";

    public static final String ENDPOINTSEDITOR_HELP_URL = "/SOAP-and-WSDL/working-with-wsdls.html";

    public static final String FUNCTIONAL_TESTING_SETUP_SCRIPT = "/functional-testing/working-with-scripts.html";
    public static final String FUNCTIONAL_TESTING_TEARDOWN_SCRIPT = "/functional-testing/working-with-scripts.html";

    public static final String GENERATE_MOCKSERVICE_HELP_URL = "/Service-Mocking/mocking-soap-services.html ";
    public static final String GENERATE_REST_MOCKSERVICE = "/rest-testing-mocking/rest-mock-service-creation/rest-mock-from-service.html";
    public static final String GENERATE_TESTSUITE_HELP_URL = "/Functional-Testing/structuring-and-running-tests.html";
    public static final String GETTINGSTARTED_HELP_URL = "/Getting-Started/your-first-soapui-project.html";
    public static final String GOTOSTEPEDITOR_HELP_URL = "/functional-testing/teststep-reference/conditional-goto/conditional-goto.html";
    public static final String GROOVYASSERTION_HELP_URL = "/functional-testing/validating-messages/using-script-assertions.html";
    public static final String GROOVYSTEPEDITOR_HELP_URL = "/functional-testing/working-with-scripts.html";
    public static final String ASSERTION_JMS_TIMEOUT_EDITOR_HELP_URL = "/jms/validating-jms-responses.html";
    public static final String GSOAP_HELP_URL = "http://www.cs.fsu.edu/~engelen/soap.html";

    public static final String HTTP_REQUEST_HELP_URL = "/functional-testing/teststep-reference/http-request/http-request.html";
    public static final String HTTP_REQUEST_PARAMS_HELP_URL = "/functional-testing/teststep-reference/http-request/parameters.html";
    public static final String HTTP_REQUEST_HEADERS_HELP_URL = "/functional-testing/teststep-reference/http-request/headers.html";

    public static final String INCOMINGWSS_HELP_URL = "/soapui-projects/ws-security.html";
    public static final String INTERFACE_HELP_URL = "/SOAP-and-WSDL/working-with-wsdls.html";
    public static final String INTERFACE_OVERVIEW_HELP_URL = "/SOAP-and-WSDL/working-with-wsdls.html";

    public static final String JABXJC_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxb/xjc.html";
    public static final String JBOSSWS_WSCONSUME_HELP_URL = "https://developer.jboss.org/wiki/JBossWS-wsconsume";
    public static final String JDBCSTEPEDITOR_HELP_URL = "/JDBC/getting-started.html";
    public static final String JDBC_CONNECTION_HELP_URL = "/Working-with-Projects/jdbc-connections.html";

    public static final String LOADTESTEDITOR_HELP_URL = "/load-testing/getting-started.html";
    public static final String LOADTESTOPTIONS_HELP_URL = "/load-testing/reference/navigation.html";
    public static final String LOADTEST_ASSERTIONS_URL = "/Load-Testing/validating-performance.html";

    public static final String MANUALTESTSTEP_HELP_URL = "/functional-testing/teststep-reference/manual-teststep.html";
    public static final String MAX_ERRORS_LOAD_TEST_ASSERTION_HELP_URL = "/load-testing/reference/assertion.html";
    public static final String MOCKASWAR_HELP_URL = "/Service-Mocking/deploying-mock-services-as-war-files.html";
    public static final String MOCKOPERATION_HELP_URL = "/Service-Mocking/simulating-complex-behaviour.html";
    public static final String MOCKOPERATION_QUERYMATCHDISPATCH_HELP_URL = "/soap-mocking/working-with-mockservices.html";
    public static final String MOCKOPERATION_SCRIPTDISPATCH_HELP_URL = "/soap-mocking/working-with-mockservices.html";
    public static final String MOCKOPERATION_XPATHDISPATCH_HELP_URL = "/soap-mocking/working-with-mockservices.html";
    public static final String MOCKRESPONSE_SCRIPT_HELP_URL = "/soap-mocking/mockoperations-and-responses.html";
    public static final String MOCKSERVICEOPTIONS_HELP_URL = "/soap-mocking/working-with-mockservices.html";
    public static final String MOCKSERVICE_HELP_URL = "/Service-Mocking/mocking-soap-services.html";

    public static final String NEWPROJECT_HELP_URL = "/Working-with-Projects/new-project.html";
    public static final String NEWRESTPROJECT_HELP_URL = "/REST-Testing/getting-started.html";
    public static final String NEWRESTSERVICE_HELP_URL = "/REST-Testing/working-with-rest-services.html";
    public static final String NEW_WADL_PROJECT_HELP_URL = "/REST-Testing/working-with-rest-services.html";
    public static final String CREATEWADLDOC_HELP_URL = "/REST-Testing/working-with-rest-services.html";

    public static final String OAUTH_ACCESS_TOKEN_FROM_SERVER = "/OAuth/access-token-from-server.html";
    public static final String OAUTH_ACCESS_TOKEN_RETRIEVAL = "/OAuth/access-token-retrieval.html";
    public static final String OAUTH_ADVANCED_OPTIONS = "/OAuth/advanced-options.html";
    public static final String OAUTH_AUTOMATED_TOKEN_PROFILE_EDITOR = "/OAuth/automated-token-profile-editor.html";
    public static final String OAUTH_AUTOMATING_ACCESS_TOKEN_RETRIEVAL = "/OAuth/automating-access-token-retrieval.html";
    public static final String OAUTH_OVERVIEW = "/OAuth/oauth2-overview.html";
    public static final String ORACLEWSA_HELP_URL = "/soap-and-wsdl/soap-code-generation.html";
    public static final String OUTGOINGWSS_HELP_URL = "/soapui-projects/ws-security.html";

    public static final String PREFERENCES_HELP_URL = "/Working-with-soapUI/preferences.html";
    public static final String PREPAREDPROPERTIES_HELP_URL = "/soapui-projects/jdbc-connections.html";
    public static final String PROJECT_OVERVIEW_HELP_URL = "/Working-with-Projects/working-with-soapui-projects.html";
    public static final String PROPERTIESSTEPEDITOR_HELP_URL = "/functional-testing/properties/working-with-properties.html";
    public static final String PROPERTY_TRANSFER_HELP_URL = "/Functional-Testing/transferring-property-values.html";

    public static final String REQUESTEDITOR_HELP_URL = "/soap-and-wsdl/working-with-messages.html";
    public static final String RESPONSE_ASSERTIONS_HELP_URL = "/Functional-Testing/getting-started-with-assertions.html";
    public static final String RESTMETHODEDITOR_HELP_URL = "/REST-Testing/rest-resources-and-methods.html";
    public static final String RESTREQUESTEDITOR_HELP_URL = "/REST-Testing/working-with-rest-requests.html";
    public static final String RESTRESOURCEEDITOR_HELPURL = "/REST-Testing/rest-resources-and-methods.html";
    public static final String REST_MOCKSERVICE_ACTION = "/REST-Service-Mocking/mock-action-editor.html";
    public static final String REST_MOCKSERVICE_HELP_URL = "/REST-Service-Mocking/mock-service-editor.html";
    public static final String REST_MOCKSERVICE_OPTIONS = "/REST-Service-Mocking/mock-service-options.html";
    public static final String REST_MOCK_RESPONSE_EDITOR = "/REST-Service-Mocking/mock-response-editor.html";
    public static final String REST_MOCK_RESPONSE_EDITOR_BODY = "/rest-testing-mocking/reference/mock-response-editor.html";
    public static final String REST_MOCK_RESPONSE_EDITOR_HEADER = "/rest-testing-mocking/reference/mock-response-editor.html";
    public static final String REST_MOCK_RESPONSE_SCRIPT = "/rest-testing-mocking/reference/mock-response-editor.html";
    public static final String REST_MOCK_SCRIPTDISPATCH = "/rest-testing-mocking/reference/mock-response-editor.html";
    public static final String RUNTESTCASESTEP_HELP_URL = "/Functional-Testing/modularizing-your-tests.html";

    public static final String SECURITYTESTEDITOR_HELP_URL = "/Security/working-with-security-tests.html";
    public static final String SECURITY_ASSERTION_HELP = "/security-testing/overview-of-security-scans.html";
    public static final String SECURITY_INVALID_HTTP_CODES_ASSERTION_HELP = "/security-testing/overview-of-security-scans.html";
    public static final String SECURITY_MALICIOUS_ATTACHMENT_HELP = "/Security/malicious-attachment.html";
    public static final String SECURITY_SCANS_OVERVIEW = "/Security/security-scans-overview.html";
    public static final String SECURITY_SENSITIVE_INFORMATION_EXPOSURE_ASSERTION_HELP = "/security-testing/overview-of-security-scans.html";
    public static final String SECURITY_VALID_HTTP_CODES_ASSERTION_HELP = "/security-testing/overview-of-security-scans.html";
    public static final String SECURITY_XSS_ASSERTION_HELP = "/Security/cross-site-scripting.html";
    public static final String SETMOCKOPERATION_HELP_URL = "/soap-mocking/reference/mockresponse.html";
    public static final String SIMPLE_CONTAINS_HELP_URL = "/functional-testing/validating-messages/getting-started-with-assertions.html";
    public static final String SIMPLE_NOT_CONTAINS_HELP_URL = "/functional-testing/validating-messages/getting-started-with-assertions.html";
    public static final String SOAPMONITOR_GENERAL_OPTIONS = "/HTTP-Recording/general-options.html";
    public static final String SOAPMONITOR_MONITOR = "/HTTP-Recording/monitor.html";
    public static final String SOAPMONITOR_MONITOR_OPTIONS = "/http-recording/reference/general-options.html";
    public static final String STATISTICSGRAPH_HELP_URL = "/load-testing/reference/loadtest-graph.html";
    public static final String STEP_AVERAGE_LOAD_TEST_ASSERTION_HELP_URL = "/load-testing/reference/assertion.html";
    public static final String STEP_MAXIMUM_LOAD_TEST_ASSERTION_HELP_URL = "/load-testing/reference/assertion.html";
    public static final String STEP_STATUS_LOAD_TEST_ASSERTION_HELP_URL = "/load-testing/reference/assertion.html";
    public static final String STEP_TPS_LOAD_TEST_ASSERTION_HELP_URL = "/load-testing/reference/assertion.html";
    public static final String STAY_TUNED = "/getting-started/help-in-soapui/help-in-soapui.html";
    public static final String START_HERMES_HELP_URL = "/jms/getting-started.html";

    public static final String TCPMON_HELP_URL = "http://ws.apache.org/commons/tcpmon/";
    public static final String TEST_AMF_REQUEST_EDITOR_HELP_URL = "/amf/getting-started.html";
    public static final String TESTCASEEDITOR_HELP_URL = "/functional-testing/structuring-and-running-tests.html";
    public static final String TESTCASEOPTIONS_HELP_URL = "/functional-testing/reference/testcase-window.html";
    public static final String TESTREQUESTEDITOR_HELP_URL = "/soap-and-wsdl/reference/request-interface.html";
    public static final String TESTRUNNER_HELP_URL = "/Test-Automation/launch-testrunner.html";
    public static final String TESTRUNNER_SECURITY_HELP_URL = "/test-automation/running-from-soapui/security-tests.html";
    public static final String TESTSUITEEDITOR_HELP_URL = "/functional-testing/reference/testsuite-reference.html";
    public static final String TESTSUITELIST_HELP_URL = "/Functional-Testing/project-testsuites-tab.html";
    public static final String TESTSUITE_HELP_URL = "/Functional-Testing/structuring-and-running-tests.html";
    public static final String TRANSFERSTEPEDITOR_HELP_URL = "/Functional-Testing/transferring-property-values.html";
    public static final String TRIAL_URL = "/Downloads/download-soapui-pro-trial.html?utm_source=soapui&utm_medium=inproductupgrade&utm_campaign=protrial";

    public static final String UPDATE_INTERFACE_HELP_URL = "/soap-and-wsdl/wsdl-refactoring.html";
    public static final String USERGUIDE_HELP_URL = "/getting-started/help-in-soapui/help-in-soapui.html";

    public static final String WADL2JAVA_HELP_URL = "https://wadl.java.net/wadl2java.html";
    public static final String WADL_PARAMS_HELP_URL = "/rest-testing/rest-resources-and-methods.html";
    public static final String WSCOMPILE_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxrpc/jaxrpc-tools.html#wp80809";
    public static final String WSDL_CONTENT_HELP_URL = "/SOAP-and-WSDL/working-with-wsdls.html";
    public static final String WSIMPORT_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxws/wsimport.html";
    public static final String WSI_COMPLIANCE_HELP_URL = "/SOAP-and-WSDL/working-with-wsdls.html";
    public static final String WSS_HELP_URL = "/SOAP-and-WSDL/applying-ws-security.html";
    public static final String WSTOOLS_HELP_URL = "http://jbossas.jboss.org/docs";

    public static final String XFIRE_HELP_URL = "http://xfire.codehaus.org/Client+and+Server+Stub+Generation+from+WSDL";
    public static final String XMLBEANS_HELP_URL = "http://xmlbeans.apache.org/docs/2.0.0/guide/tools.html#scomp";
    public static final String ASSERTION_XPATH_CONTENT = "/functional-testing/validating-messages/validating-xml-messages.html";
    public static final String ASSERTION_XQUERY = "/functional-testing/validating-messages/validating-xml-messages.html";

    public static final String ASSERTION_JSON_CONTENT = "/functional-testing/validating-messages/validating-json-messages.html";
    public static final String ASSERTION_JSON_COUNT = "/functional-testing/validating-messages/validating-json-messages.html";
    public static final String ASSERTION_JSON_EXIST = "/functional-testing/validating-messages/validating-json-messages.html";
    public static final String ASSERTION_JSON_REGEX = "/functional-testing/validating-messages/validating-json-messages.html";
    public static final String ASSERTION_JSON_REGEX_CONFIG = "/functional-testing/validating-messages/validating-json-messages.html";



    public static final String REST_DISCOVERY_WITH_INTERNAL_BROWSER = "/REST-Discovery/api-with-internal-browser.html";
    public static final String SOAPUI_WELCOME_PAGE = "/Downloads/thank-you-for-downloading-soapui.html";
    public static final String STARTER_PAGE_URL = "http://soapui.org/Appindex/soapui-starterpage.html?version=" + urlEncodeWithUtf8(SoapUI.SOAPUI_VERSION);
}
