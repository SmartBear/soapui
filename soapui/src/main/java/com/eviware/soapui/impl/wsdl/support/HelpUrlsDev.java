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
 * Development 
 */

public interface HelpUrlsDev {


    /*
    Help Sites
    */
    public static final String BASE_URL_PROD = "http://www.soapui.org";

    public static final String BASE_URL_DEV = "http://dev.soapuidocs.sthlm.smartbear.local";

    public static final String BASE_URL_NEXT = "http://next.soapuidocs.sthlm.smartbear.local";

    public static final String MISSING_URL = "/missing_url";

    public static final String THIRD_PARTY_LICENSE_INFO = "/developers_corner/3rd";

    public static final String SMARTBEAR_WEB_SITE_START_PAGE = "http://www.smartbear.com";
    public static final String SMARTBEAR_PRIVACY_POLICY_URL = "/store_info/privacy_policy";

    public static final String ADDMOCKOPERATIONASMOCKRESPONSESTEP_HELP_URL = "/service_mocking/mocking_soap_services";
    public static final String ADDMOCKRESPONSETOTESTCASE_HELP_URL = "/service_mocking/mocking_soap_services";
    public static final String ADDREQUESTASMOCKRESPONSESTEP_HELP_URL = "/service_mocking/mocking_soap_services";
    public static final String ADD_ASSERTION_PANEL = "/functional_testing/validating_messages/getting_started_with_assertions";
    public static final String ADD_AUTHORIZATION = "/oauth/add_authorization";
    public static final String ALERT_SITE_HELP_URL = "http://help.alertsite.com/webhome";
    public static final String AMF_REQUEST_HEADERS_HELP_URL = "/amf/reference/the_amf_request_window";
    public static final String API_TESTING_DOJO_HELP_URL = "/dojo/overview";
    public static final String ATTACHMENTS_HELP_URL = "/soap_and_wsdl/adding_headers_and_attachments";
    public static final String AUTHORIZATION = "/oauth/authorization";
    public static final String AUTHORIZATION_BASIC = "/oauth/basic";
    public static final String AUTHORIZATION_NTLM = "/oauth/ntlm";
    public static final String AUTHORIZATION_OAUTH2 = "/oauth/oauth2";
    public static final String AUTHORIZATION_SPNEGO_KERBEROS = "/oauth/spnego_kerberos";
    public static final String AXIS1X_HELP_URL = "http://ws.apache.org/axis/java/reference.html#wsdl2javareference";
    public static final String AXIS2X_HELP_URL = "http://ws.apache.org/axis2/tools/1_0/codegentoolreference.html";

    public static final String CHANGEMOCKOPERATION_HELP_URL = "/soap_mocking/working_with_mockservices";
    public static final String CHANGEOPERATION_HELP_URL = "/soap_and_wsdl/operations_and_requests";
    public static final String CLONEMOCKSERVICE_HELP_URL = "/service_mocking/mocking_soap_services";
    public static final String CLONETESTCASE_HELP_URL = "/functional_testing/reference/testsuite_reference";
    public static final String CLONETESTSTEP_HELP_URL = "/functional_testing/structuring_and_running_tests";
    public static final String CLONETESTSUITE_HELP_URL = "/functional_testing/structuring_and_running_tests";
    public static final String CREATEMOCKRESPONSESTEP_HELP_URL = "/service_mocking/mocking_soap_services";
    public static final String CRYPTOSWSS_HELP_URL = "/working_with_projects/ws_security";
    public static final String CXFWSDL2JAVA_HELP_URL = "http://cxf.apache.org/docs/wsdl-to-java.html";

    public static final String DEBUGGING_ASSERTION_TEST_STEP = "/functional_testing/assertion_test_step";
    public static final String DEBUGGING_ENVIRONMENT_HANDLING = "/environments/environment_handling_in_soapui";
    public static final String DEBUGGING_TESTCASE_DEBUGGING = "/functional_testing/testcase_debugging";
    public static final String DOTNET_HELP_URL = "https://msdn.microsoft.com/en_us/library/7h3ystb6.aspx";

    public static final String ENDPOINTSEDITOR_HELP_URL = "/soap_and_wsdl/working_with_wsdls";

    public static final String FORUMS_HELP_URL = "http://community.smartbear.com/";
    public static final String FUNCTIONAL_TESTING_SETUP_SCRIPT = "/functional_testing/working_with_scripts";
    public static final String FUNCTIONAL_TESTING_TEARDOWN_SCRIPT = "/functional_testing/working_with_scripts";

    public static final String GENERATE_MOCKSERVICE_HELP_URL = "/service_mocking/mocking_soap_services.html ";
    public static final String GENERATE_REST_MOCKSERVICE = "/rest_testing_mocking/rest_mock_service_creation/rest_mock_from_service";
    public static final String GENERATE_TESTSUITE_HELP_URL = "/functional_testing/structuring_and_running_tests";
    public static final String GETTINGSTARTED_HELP_URL = "/getting_started/your_first_soapui_project";
    public static final String GOTOSTEPEDITOR_HELP_URL = "/functional_testing/teststep_reference/conditional_goto/conditional_goto";
    public static final String GROOVYASSERTION_HELP_URL = "/functional_testing/validating_messages/using_script_assertions";
    public static final String GROOVYSTEPEDITOR_HELP_URL = "/functional_testing/working_with_scripts";
    public static final String GSOAP_HELP_URL = "http://www.cs.fsu.edu/~engelen/soap.html";

    public static final String HTTP_REQUEST_HELP_URL = "/functional_testing/teststep_reference/http_request/http_request";
    public static final String HTTP_REQUEST_PARAMS_HELP_URL = "/functional_testing/teststep_reference/http_request/parameters";
    public static final String HTTP_REQUEST_HEADERS_HELP_URL = "/functional_testing/teststep_reference/http_request/headers";

    public static final String INCOMINGWSS_HELP_URL = "/soapui_projects/ws_security";
    public static final String INTERFACE_HELP_URL = "/soap_and_wsdl/working_with_wsdls";
    public static final String INTERFACE_OVERVIEW_HELP_URL = "/soap_and_wsdl/working_with_wsdls";

    public static final String JABXJC_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxb/xjc.html";
    public static final String JBOSSWS_WSCONSUME_HELP_URL = "https://developer.jboss.org/wiki/jbossws_wsconsume";
    public static final String JDBCSTEPEDITOR_HELP_URL = "/jdbc/getting_started";
    public static final String JDBC_CONNECTION_HELP_URL = "/working_with_projects/jdbc_connections";

    public static final String LOADTESTEDITOR_HELP_URL = "/load_testing/getting_started";
    public static final String LOADTESTOPTIONS_HELP_URL = "/load_testing/reference/navigation";
    public static final String LOADTEST_ASSERTIONS_URL = "/load_testing/validating_performance";

    public static final String MANUALTESTSTEP_HELP_URL = "/functional_testing/teststep_reference/manual_teststep";
    public static final String MAX_ERRORS_LOAD_TEST_ASSERTION_HELP_URL = "/load_testing/reference/assertion";
    public static final String MOCKASWAR_HELP_URL = "/service_mocking/deploying_mock_services_as_war_files";
    public static final String MOCKOPERATION_HELP_URL = "/service_mocking/simulating_complex_behaviour";
    public static final String MOCKOPERATION_QUERYMATCHDISPATCH_HELP_URL = "/soap_mocking/working_with_mockservices";
    public static final String MOCKOPERATION_SCRIPTDISPATCH_HELP_URL = "/soap_mocking/working_with_mockservices";
    public static final String MOCKOPERATION_XPATHDISPATCH_HELP_URL = "/soap_mocking/working_with_mockservices";
    public static final String MOCKRESPONSE_SCRIPT_HELP_URL = "/soap_mocking/mockoperations_and_responses";
    public static final String MOCKSERVICEOPTIONS_HELP_URL = "/soap_mocking/working_with_mockservices";
    public static final String MOCKSERVICE_HELP_URL = "/service_mocking/mocking_soap_services";

    public static final String NEWPROJECT_HELP_URL = "/working_with_projects/new_project";
    public static final String NEWRESTPROJECT_HELP_URL = "/rest_testing/getting_started";
    public static final String NEWRESTSERVICE_HELP_URL = "/rest_testing/working_with_rest_services";
    public static final String NEW_WADL_PROJECT_HELP_URL = "/rest_testing/working_with_rest_services";
    public static final String CREATEWADLDOC_HELP_URL = "/rest_testing/working_with_rest_services";

    public static final String OAUTH_ACCESS_TOKEN_FROM_SERVER = "/oauth/access_token_from_server";
    public static final String OAUTH_ACCESS_TOKEN_RETRIEVAL = "/oauth/access_token_retrieval";
    public static final String OAUTH_ADVANCED_OPTIONS = "/oauth/advanced_options";
    public static final String OAUTH_AUTOMATED_TOKEN_PROFILE_EDITOR = "/oauth/automated_token_profile_editor";
    public static final String OAUTH_AUTOMATING_ACCESS_TOKEN_RETRIEVAL = "/oauth/automating_access_token_retrieval";
    public static final String OAUTH_OVERVIEW = "/oauth/oauth2_overview";
    public static final String ORACLEWSA_HELP_URL = "/soap_and_wsdl/soap_code_generation";
    public static final String OUTGOINGWSS_HELP_URL = "/soapui_projects/ws_security";

    public static final String PREFERENCES_HELP_URL = "/working_with_soapui/preferences";
    public static final String PREPAREDPROPERTIES_HELP_URL = "/soapui_projects/jdbc_connections";
    public static final String PROJECT_OVERVIEW_HELP_URL = "/working_with_projects/working_with_soapui_projects";
    public static final String PROPERTIESSTEPEDITOR_HELP_URL = "/functional_testing/properties/working_with_properties";
    public static final String PROPERTY_TRANSFER_HELP_URL = "/functional_testing/transferring_property_values";

    public static final String REQUESTEDITOR_HELP_URL = "/soap_and_wsdl/working_with_messages";
    public static final String RESPONSE_ASSERTIONS_HELP_URL = "/functional_testing/getting_started_with_assertions";
    public static final String RESTMETHODEDITOR_HELP_URL = "/rest_testing/rest_resources_and_methods";
    public static final String RESTREQUESTEDITOR_HELP_URL = "/rest_testing/working_with_rest_requests";
    public static final String RESTRESOURCEEDITOR_HELPURL = "/rest_testing/rest_resources_and_methods";
    public static final String REST_MOCKSERVICE_ACTION = "/rest_service_mocking/mock_action_editor";
    public static final String REST_MOCKSERVICE_HELP_URL = "/rest_service_mocking/mock_service_editor";
    public static final String REST_MOCKSERVICE_OPTIONS = "/rest_service_mocking/mock_service_options";
    public static final String REST_MOCK_RESPONSE_EDITOR = "/rest_service_mocking/mock_response_editor";
    public static final String REST_MOCK_RESPONSE_EDITOR_BODY = "/rest_testing_mocking/reference/mock_response_editor";
    public static final String REST_MOCK_RESPONSE_EDITOR_HEADER = "/rest_testing_mocking/reference/mock_response_editor";
    public static final String REST_MOCK_RESPONSE_SCRIPT = "/rest_testing_mocking/reference/mock_response_editor";
    public static final String REST_MOCK_SCRIPTDISPATCH = "/rest_testing_mocking/reference/mock_response_editor";
    public static final String RUNTESTCASESTEP_HELP_URL = "/functional_testing/modularizing_your_tests";

    public static final String SECURITYTESTEDITOR_HELP_URL = "/security/working_with_security_tests";
    public static final String SECURITY_ASSERTION_HELP = "/security_testing/overview_of_security_scans";
    public static final String SECURITY_INVALID_HTTP_CODES_ASSERTION_HELP = "/security_testing/overview_of_security_scans";
    public static final String SECURITY_MALICIOUS_ATTACHMENT_HELP = "/security/malicious_attachment";
    public static final String SECURITY_SCANS_OVERVIEW = "/security/security_scans_overview";
    public static final String SECURITY_SENSITIVE_INFORMATION_EXPOSURE_ASSERTION_HELP = "/security_testing/overview_of_security_scans";
    public static final String SECURITY_VALID_HTTP_CODES_ASSERTION_HELP = "/security_testing/overview_of_security_scans";
    public static final String SECURITY_XSS_ASSERTION_HELP = "/security/cross_site_scripting";
    public static final String SETMOCKOPERATION_HELP_URL = "/soap_mocking/reference/mockresponse";
    public static final String SIMPLE_CONTAINS_HELP_URL = "/functional_testing/validating_messages/getting_started_with_assertions";
    public static final String SIMPLE_NOT_CONTAINS_HELP_URL = "/functional_testing/validating_messages/getting_started_with_assertions";
    public static final String SOAPMONITOR_GENERAL_OPTIONS = "/http_recording/general_options";
    public static final String SOAPMONITOR_MONITOR = "/http_recording/monitor";
    public static final String SOAPMONITOR_MONITOR_OPTIONS = "/http_recording/reference/general_options";
    public static final String STATISTICSGRAPH_HELP_URL = "/load_testing/reference/loadtest_graph";
    public static final String STEP_AVERAGE_LOAD_TEST_ASSERTION_HELP_URL = "/load_testing/reference/assertion";
    public static final String STEP_MAXIMUM_LOAD_TEST_ASSERTION_HELP_URL = "/load_testing/reference/assertion";
    public static final String STEP_STATUS_LOAD_TEST_ASSERTION_HELP_URL = "/load_testing/reference/assertion";
    public static final String STEP_TPS_LOAD_TEST_ASSERTION_HELP_URL = "/load_testing/reference/assertion";
    public static final String STAY_TUNED = "/getting_started/help_in_soapui/stay_tuned";

    public static final String TCPMON_HELP_URL = "http://ws.apache.org/commons/tcpmon/";
    public static final String TEST_AMF_REQUEST_EDITOR_HELP_URL = "/amf/getting_started";
    public static final String TESTCASEEDITOR_HELP_URL = "/functional_testing/structuring_and_running_tests";
    public static final String TESTCASEOPTIONS_HELP_URL = "/functional_testing/reference/testcase_window";
    public static final String TESTREQUESTEDITOR_HELP_URL = "/soap_and_wsdl/reference/request_interface";
    public static final String TESTRUNNER_HELP_URL = "/test_automation/launch_testrunner";
    public static final String TESTSUITEEDITOR_HELP_URL = "/functional_testing/reference/testsuite_reference";
    public static final String TESTSUITELIST_HELP_URL = "/functional_testing/project_testsuites_tab";
    public static final String TESTSUITE_HELP_URL = "/functional_testing/structuring_and_running_tests";
    public static final String TRANSFERSTEPEDITOR_HELP_URL = "/functional_testing/transferring_property_values";
    public static final String TRIAL_URL = "/downloads/download_soapui_pro_trial";

    public static final String UPDATE_INTERFACE_HELP_URL = "/soap_and_wsdl/wsdl_refactoring";
    public static final String USERGUIDE_HELP_URL = "/getting_started/help_in_soapui/help_in_soapui";

    public static final String WADL2JAVA_HELP_URL = "https://wadl.java.net/wadl2java.html";
    public static final String WADL_PARAMS_HELP_URL = "/rest_testing/rest_resources_and_methods";
    public static final String WSCOMPILE_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxrpc/jaxrpc_tools.html#wp80809";
    public static final String WSDL_CONTENT_HELP_URL = "/soap_and_wsdl/working_with_wsdls";
    public static final String WSIMPORT_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxws/wsimport.html";
    public static final String WSI_COMPLIANCE_HELP_URL = "/soap_and_wsdl/working_with_wsdls";
    public static final String WSS_HELP_URL = "/soap_and_wsdl/applying_ws_security";
    public static final String WSTOOLS_HELP_URL = "http://jbossas.jboss.org/docs";

    public static final String XFIRE_HELP_URL = "http://xfire.codehaus.org/client+and+server+stub+generation+from+wsdl";
    public static final String XMLBEANS_HELP_URL = "http://xmlbeans.apache.org/docs/2.0.0/guide/tools.html#scomp";
    public static final String XPATHASSERTIONEDITOR_HELP_URL = "/functional_testing/validating_messages/validating_xml_messages";
    public static final String XQUERYASSERTIONEDITOR_HELP_URL = "/functional_testing/validating_messages/validating_xml_messages";

    public static final String REST_DISCOVERY_WITH_INTERNAL_BROWSER = "/rest_discovery/api_with_internal_browser";
    public static final String SOAPUI_WELCOME_PAGE = "/downloads/thank_you_for_downloading_soapui";
    public static final String STARTER_PAGE_URL = "http://soapui.org/appindex/soapui_starterpage.html?version=" + urlEncodeWithUtf8(SoapUI.SOAPUI_VERSION);
} 

