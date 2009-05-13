/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support;

/**
 * Help URLS in soapUI documentation
 * 
 * @author ole.matzura
 */

public interface HelpUrls
{
	public static final String HELP_URL_ROOT = "http://www.soapui.org/userguide/";

	public static final String OVERVIEW_HELP_URL = HELP_URL_ROOT + "overview.html";
	public static final String PROJECT_OVERVIEW_HELP_URL = HELP_URL_ROOT + "projects/index.html";
	public static final String INTERFACE_OVERVIEW_HELP_URL = HELP_URL_ROOT + "interfaces/interfaceview.html";
	public static final String WSDL_CONTENT_HELP_URL = HELP_URL_ROOT + "interfaces/interfaceeditor.html";
	public static final String WSI_COMPLIANCE_HELP_URL = HELP_URL_ROOT + "interfaces/wsi.html";

	// TODO option for adding help URl to page section, see
	// buildInterfaceOverviewTab() section operations
	public static final String OPERATION_HELP_URL = HELP_URL_ROOT + "operations.html";
	public static final String INTERFACE_HELP_URL = HELP_URL_ROOT + "interfaces/index.html";

	public static final String HEADERS_HELP_URL = HELP_URL_ROOT + "requests.html#HTTP_Headers";
	// commented ones are not currently used
	public static final String PROJECT_HELP_URL = HELP_URL_ROOT + "projects/index.html#soapui_WSDL_Projects";
	public static final String REQUEST_HELP_URL = HELP_URL_ROOT + "requests.html";
	public static final String ATTACHMENTS_HELP_URL = HELP_URL_ROOT + "attachments.html";

	public static final String TESTSUITE_HELP_URL = HELP_URL_ROOT + "functional/testsuites.html";
	// public static final String TESTCASE_HELP_URL = HELP_URL_ROOT +
	// "functional/testcases.html";
	// public static final String PROPERTYTRANSFER_HELP_URL = HELP_URL_ROOT +
	// "functional/propertytransfers.html";
	// public static final String DELAYSTEP_HELP_URL = HELP_URL_ROOT +
	// "functional/testcases.html#Delay_Test_Step";
	// public static final String GOTOSTEP_HELP_URL = HELP_URL_ROOT +
	// "functional/gotostep.html";
	// public static final String PROPERTIESSTEP_HELP_URL = HELP_URL_ROOT +
	// "functional/propertiesstep.html";
	// public static final String GROOVYSTEP_HELP_URL = HELP_URL_ROOT +
	// "fFunctional/groovystep.html";
	// public static final String TESTREQUEST_HELP_URL = HELP_URL_ROOT +
	// "functional/testrequests.html";
	// public static final String LOADTEST_HELP_URL = HELP_URL_ROOT +
	// "loadtest/index.html";
	// public static final String MOCKRESPONSESTEP_HELP_URL = HELP_URL_ROOT +
	// "functional/mockresponsestep.html";
	public static final String CREATEMOCKRESPONSESTEP_HELP_URL = HELP_URL_ROOT + "functional/mockresponse.html";

	public static final String ADDREQUESTASMOCKRESPONSESTEP_HELP_URL = HELP_URL_ROOT + "functional/mockresponse.html";
	public static final String ADDMOCKRESPONSETOTESTCASE_HELP_URL = HELP_URL_ROOT + "functional/mockresponse.html";
	public static final String ADDMOCKOPERATIONASMOCKRESPONSESTEP_HELP_URL = HELP_URL_ROOT
			+ "functional/mockresponse.html";
	public static final String SETMOCKOPERATION_HELP_URL = HELP_URL_ROOT
			+ "functional/mockresponsestep.html#Set_Mock_Operation";

	public static final String USERGUIDE_HELP_URL = HELP_URL_ROOT + "index.html";
	public static final String GETTINGSTARTED_HELP_URL = "http://www.soapui.org/gettingstarted/index.html";
	public static final String GOTOSTEPEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/gotostep.html#The_Conditional_Goto_Editor";
	public static final String GROOVYSTEPEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/groovystep.html#The_Groovy_Script_Editor";
	public static final String PROPERTIESSTEPEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/propertiesstep.html#Properties_Editor";
	public static final String TRANSFERSTEPEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/propertytransfers.html#The_PropertyTransfer_Editor";
	public static final String TESTCASEEDITOR_HELP_URL = HELP_URL_ROOT + "functional/testcases.html#The_TestCase_Editor";
	public static final String TESTSUITEEDITOR_HELP_URL = HELP_URL_ROOT + "functional/testsuites.html#TestSuite_Runner";
	public static final String LOADTESTEDITOR_HELP_URL = HELP_URL_ROOT + "loadtest/index.html#The_LoadTest_Editor";
	public static final String STATISTICSGRAPH_HELP_URL = HELP_URL_ROOT
			+ "loadtest/diagrams.html#The_Statistics_Diagram";
	public static final String STATISTICSHISTORYGRAPH_HELP_URL = HELP_URL_ROOT
			+ "loadtest/diagrams.html#The_Statistics_History_Diagram";
	public static final String REQUESTEDITOR_HELP_URL = HELP_URL_ROOT + "requests.html#The_Request_Editor";
	public static final String TESTREQUESTEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/testrequests.html#The_TestRequest_Editor";
	public static final String PREFERENCES_HELP_URL = HELP_URL_ROOT + "preferences.html";
	public static final String TESTCASEOPTIONS_HELP_URL = HELP_URL_ROOT + "functional/testcases.html#TestCase_Options";
	public static final String LOADTESTOPTIONS_HELP_URL = HELP_URL_ROOT + "loadtest/index.html#LoadTest_Options";
	public static final String XPATHASSERTIONEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/response-assertions.html#XPath_Match";

	public static final String ENDPOINTSEDITOR_HELP_URL = HELP_URL_ROOT + "interfaces/endpoints.html";

	public static final String AXIS1X_HELP_URL = "http://ws.apache.org/axis/java/reference.html#WSDL2JavaReference";
	public static final String AXIS2X_HELP_URL = "http://ws.apache.org/axis2/tools/1_0/CodegenToolReference.html";
	public static final String DOTNET_HELP_URL = "http://msdn2.microsoft.com/en-us/library/7h3ystb6.aspx";
	public static final String GSOAP_HELP_URL = "http://www.cs.fsu.edu/~engelen/soap.html";

	public static final String JABXJC_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxb/xjc.html";
	public static final String WSTOOLS_HELP_URL = "http://labs.jboss.com/portal/jbossws/user-guide/en/html/index.html";
	public static final String TCPMON_HELP_URL = "http://ws.apache.org/commons/tcpmon/";
	public static final String WSCOMPILE_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxrpc/jaxrpc-tools.html#wp80809";
	public static final String WSIMPORT_HELP_URL = "http://java.sun.com/webservices/docs/2.0/jaxws/wsimport.html";
	public static final String CXFWSDL2JAVA_HELP_URL = "http://cwiki.apache.org/CXF20DOC/wsdl-to-java.html";
	public static final String XFIRE_HELP_URL = "http://xfire.codehaus.org/Client+and+Server+Stub+Generation+from+WSDL";
	public static final String XMLBEANS_HELP_URL = "http://xmlbeans.apache.org/docs/2.0.0/guide/tools.html#scomp";
	public static final String ORACLEWSA_HELP_URL = HELP_URL_ROOT + "tools/topdown.html#Oracle_WSA";
	public static final String JBOSSWS_WSCONSUME_HELP_URL = "http://jbws.dyndns.org/mediawiki/index.php/Wsconsume";

	public static final String MAX_ERRORS_LOAD_TEST_ASSERTION_HELP_URL = HELP_URL_ROOT
			+ "loadtest/assertions.html#Max_Errors_Assertion";
	public static final String STEP_MAXIMUM_LOAD_TEST_ASSERTION_HELP_URL = HELP_URL_ROOT
			+ "loadtest/assertions.html#Step_Maximum_Assertion";
	public static final String STEP_STATUS_LOAD_TEST_ASSERTION_HELP_URL = HELP_URL_ROOT
			+ "loadtest/assertions.html#Step_Status_Assertion";
	public static final String STEP_TPS_LOAD_TEST_ASSERTION_HELP_URL = HELP_URL_ROOT
			+ "loadtest/assertions.html#Step_TPS_Assertion";
	public static final String STEP_AVERAGE_LOAD_TEST_ASSERTION_HELP_URL = HELP_URL_ROOT
			+ "loadtest/assertions.html#Step_Average_Assertion";

	public static final String SIMPLE_CONTAINS_HELP_URL = HELP_URL_ROOT
			+ "functional/response-assertions.html#Simple_Contains";
	public static final String SIMPLE_NOT_CONTAINS_HELP_URL = HELP_URL_ROOT
			+ "functional/response-assertions.html#Simple_NotContains_Assertion";
	public static final String GROOVYASSERTION_HELP_URL = HELP_URL_ROOT
			+ "functional/response-assertions.html#Script_Assertion";

	public static final String MOCKRESPONSE_HELP_URL = HELP_URL_ROOT + "mock/responses.html";
	public static final String MOCKSERVICE_HELP_URL = HELP_URL_ROOT + "mock/services.html";
	public static final String MOCKRESPONSE_SCRIPT_HELP_URL = HELP_URL_ROOT + "mock/responses.html#Response_Scripts";

	public static final String MOCKOPERATION_HELP_URL = HELP_URL_ROOT + "mock/operations.html";
	public static final String MOCKOPERATION_SCRIPTDISPATCH_HELP_URL = HELP_URL_ROOT
			+ "mock/operations.html#Groovy_Script_Dispatching";
	public static final String MOCKOPERATION_XPATHDISPATCH_HELP_URL = HELP_URL_ROOT
			+ "mock/operations.html#XPath_Dispatching";

	public static final String RESPONSE_ASSERTIONS_HELP_URL = HELP_URL_ROOT + "functional/response-assertions.html";

	public static final String TESTRUNNER_HELP_URL = HELP_URL_ROOT + "commandline/functional.html#Launch_Dialog";
	public static final String XQUERYASSERTIONEDITOR_HELP_URL = HELP_URL_ROOT
			+ "functional/response-assertions.html#XQuery_Match_Assertion";

	public static final String REQUEST_ASSERTIONS_HELP_URL = HELP_URL_ROOT + "functional/response-assertions.html";
	public static final String LOADTEST_ASSERTIONS_URL = HELP_URL_ROOT + "loadtest/assertions.html";

	public static final String UPDATE_INTERFACE_HELP_URL = HELP_URL_ROOT
			+ "interfaces/index.html#Updating_the_Interface_Definition";

	public static final String NEWPROJECT_HELP_URL = HELP_URL_ROOT + "projects/index.html#Creating_a_WSDL_Projects";

	public static final String GENERATE_MOCKSERVICE_HELP_URL = HELP_URL_ROOT + "mock/services.html";

	public static final String GENERATE_TESTSUITE_HELP_URL = HELP_URL_ROOT + "functional/testsuites.html";

	public static final String CHANGEMOCKOPERATION_HELP_URL = HELP_URL_ROOT + "mock/operations.html#Change_Operation";

	public static final String CLONEMOCKSERVICE_HELP_URL = HELP_URL_ROOT + "mock/services.html";

	public static final String MOCKOPERATIONOPTIONS_HELP_URL = HELP_URL_ROOT + "mock/operations.html";

	public static final String MOCKSERVICEOPTIONS_HELP_URL = HELP_URL_ROOT + "mock/services.html#MockService_Options";

	public static final String CLONETESTCASE_HELP_URL = HELP_URL_ROOT + "functional/testcases.html#Clone_TestCase";

	public static final String CLONETESTSTEP_HELP_URL = HELP_URL_ROOT + "functional/testcases.html#Clone_TestSteps";

	public static final String CLONETESTSUITE_HELP_URL = HELP_URL_ROOT + "functional/testsuites.html";

	public static final String CHANGEOPERATION_HELP_URL = HELP_URL_ROOT
			+ "functional/testrequests.html#Change_Operation";

	public static final String RUNTESTCASESTEP_HELP_URL = HELP_URL_ROOT + "functional/runtestcasestep.html";

	public static final String SOAPMONITOR_HELP_URL = HELP_URL_ROOT + "monitor/index.html";

	public static final String WSS_HELP_URL = HELP_URL_ROOT + "projects/wss.html";

	public static final String OUTGOINGWSS_HELP_URL = HELP_URL_ROOT + "projects/wss.html#Outgoing_WSS";

	public static final String INCOMINGWSS_HELP_URL = HELP_URL_ROOT + "projects/wss.html#Incoming_WSS";

	public static final String CRYPTOSWSS_HELP_URL = HELP_URL_ROOT + "projects/wss.html#Keystores";

	public static final String WSIREPORT_HELP_URL = HELP_URL_ROOT + "interfaces/wsi.html#Creating_WS-I_Reports";

	public static final String RESOLVEPROJECT_HELP_URL = HELP_URL_ROOT + "projects/resolving.html";

	public static final String NEWRESTSERVICE_HELP_URL = HELP_URL_ROOT + "rest/index.html";

	public static final String RESTREQUESTEDITOR_HELP_URL = HELP_URL_ROOT + "rest/requests.html";

	public static final String CREATEWADLDOC_HELP_URL = HELP_URL_ROOT + "rest/index.html";

	public static final String WADL2JAVA_HELP_URL = "https://wadl.dev.java.net/wadl2java.html";

	public static final String WADL_PARAMS_HELP_URL = HELP_URL_ROOT + "rest/params.html";

	public static final String RESTRESOURCEEDITOR_HELPURL = HELP_URL_ROOT + "rest/resources.html";

	public static final String MOCKOPERATION_QUERYMATCHDISPATCH_HELP_URL = HELP_URL_ROOT
			+ "mock/operations.html#Query/Match_Dispatching";

	public static final String FORUMS_HELP_URL = "http://www.eviware.com/forums";

	public static final String TRIAL_URL = "http://www.eviware.com/soapui/trial";

}
