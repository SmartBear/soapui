/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui.analytics;

import com.smartbear.analytics.AnalyticsManager;

import static com.eviware.soapui.analytics.ModuleType.LOADUI_NG;
import static com.eviware.soapui.analytics.ModuleType.PROJECTS;
import static com.eviware.soapui.analytics.ModuleType.SECURE;
import static com.eviware.soapui.analytics.ModuleType.SERVICE_V;
import static com.eviware.soapui.analytics.ModuleType.SOAPUI_NG;
import static com.eviware.soapui.analytics.ProductArea.MAIN_EDITOR;
import static com.eviware.soapui.analytics.ProductArea.MAIN_MENU;
import static com.eviware.soapui.analytics.ProductArea.NAVIGATOR_TREE;
import static com.eviware.soapui.analytics.ProductArea.NO;
import static com.eviware.soapui.analytics.ProductArea.STATIC_MAIN_TOOLBAR;

/**
 *
 */
public enum SoapUIActions {

    //Common actions
    PRODUCT_STARTED_FROM_CMD("ProductStartedFromCmd", null, NO),
    PRODUCT_STARTED("ProductStarted", null, NO),
    PRODUCT_STARTED_IN_DEBUGGING_MODE("ProductStartedInDebuggingMode", null, NO),
    EXIT("Exit", null, NO),
    EXIT_WITHOUT_SAVE("ExitWithoutSave", null, NO),
    SAVE_ALL_PROJECTS("MainMenuSaveAllProjects", null, MAIN_MENU),

    //Assertions
    ADD_ASSERTION("AddAssertion", SOAPUI_NG, MAIN_EDITOR),

    //Run actions
    RUN_TEST_STEP_FROM_TOOLBAR("TestCasePanelRunTestStep", SOAPUI_NG, MAIN_EDITOR),
    RUN_TEST_CASE_FROM_TOOLBAR("TestSuitePanelRunTestCase", SOAPUI_NG, MAIN_EDITOR),
    RUN_TEST_SUITE_FROM_TOOLBAR("ProjectPanelRunTestSuite", SOAPUI_NG, MAIN_EDITOR),
    RUN_REQUEST_FROM_REQUEST_EDITOR("RequestEditorRunRequest", PROJECTS, MAIN_EDITOR),
    RUN_TEST_STEP_FROM_PANEL("TestStepPanelRunTestStep", SOAPUI_NG, MAIN_EDITOR),
    SEND_REQUEST("SendRequest", PROJECTS, MAIN_EDITOR),
    RUN_TEST_STEP("RunTestStep", SOAPUI_NG, MAIN_EDITOR),


    //Create Functional Model Items
    CREATE_TEST_SUITE_FROM_PROJECT_PANEL("ProjectPanelCreateTestSuite", SOAPUI_NG, MAIN_EDITOR),
    CREATE_TEST_SUITE("CreateTestSuite", SOAPUI_NG, NO),
    CREATE_TEST_CASE_FROM_TEST_TEST_SUITE_PANEL("TestSuitePanelCreateTestCase", SOAPUI_NG, MAIN_EDITOR),
    CREATE_TEST_CASE("CreateTestCase", SOAPUI_NG, NO),
    ADD_NEW_TEST_STEP("AddNewTestStep", SOAPUI_NG, NO),
    ADD_NEW_TEST_STEP_FROM_TEST_CASE_PANEL("TestCasePanelAddNewTestStep", SOAPUI_NG, MAIN_EDITOR),
    CREATE_REQUEST_FOR_OPERATION("ContextMenuCreateSoapRequestForOperation", PROJECTS, NAVIGATOR_TREE),
    CREATE_REQUEST_FOR_METHOD("ContextMenuCreateRESTRequestForMethod", PROJECTS, NAVIGATOR_TREE),
    ADD_REQUEST_TO_TEST_CASE_FROM_REQUEST_PANEL("TestCasePanelAddRequest", SOAPUI_NG, MAIN_EDITOR),

    //Static Toolbar Actions
    CREATE_EMPTY_PROJECT_FROM_TOOLBAR("StaticToolbarCreateEmptyProject", null, STATIC_MAIN_TOOLBAR),
    CREATE_SOAP_PROJECT_FROM_TOOLBAR("StaticToolbarCreateSoapProject", SOAPUI_NG, STATIC_MAIN_TOOLBAR),
    CREATE_REST_PROJECT_FROM_TOOLBAR("StaticToolbarCreateRESTProject", SOAPUI_NG, STATIC_MAIN_TOOLBAR),
    IMPORT_PROJECT_FROM_TOOLBAR("StaticToolbarImportProject", SOAPUI_NG, STATIC_MAIN_TOOLBAR),
    SAVE_ALL_PROJECTS_FROM_TOOLBAR("StaticToolbarSaveAllProjects", null, STATIC_MAIN_TOOLBAR),
    OPEN_FORUM_FROM_TOOLBAR("StaticToolbarOpenForum", null, STATIC_MAIN_TOOLBAR),
    APPLY_TRIAL_FROM_TOOLBAR("StaticToolbarApplyTrial", null, STATIC_MAIN_TOOLBAR),
    OPEN_PREFERENCES_FROM_TOOLBAR("StaticToolbarOpenPreferences", null, STATIC_MAIN_TOOLBAR),
    TURN_ON_PROXY_FROM_TOOLBAR("StaticToolbarTurnOnProxy", null, STATIC_MAIN_TOOLBAR),
    TURN_OFF_PROXY_FROM_TOOLBAR("StaticToolbarTurnOffProxy", null, STATIC_MAIN_TOOLBAR),

    //Tools
    USE_JBOSSWS_ARTIFACTS_TOOL("UseJBossWSArtifactsTool", PROJECTS, NO),
    USE_JBOSSWS_JAXWS_ARTIFACTS_TOOL("UseJBossWSJaxWSArtifactsTool", PROJECTS, NO),
    USE_JAX_RPC_ARTIFACTS_TOOL("UseJaxRpcArtifactsTool", PROJECTS, NO),
    USE_JAX_WS_ARTIFACTS_TOOL("UseJaxWsArtifactsTool", PROJECTS, NO),
    USE_AXIS_1_ARTIFACTS_TOOL("UseAxis1ArtifactsTool", PROJECTS, NO),
    USE_AXIS_2_ARTIFACTS_TOOL("UseAxis2ArtifactsTool", PROJECTS, NO),
    USE_APACHE_CXF_TOOL("UseApacheCxfTool", PROJECTS, NO),
    USE_XFIRE_1_STUBS_TOOL("UseXFire1StubsTool", PROJECTS, NO),
    USE_ORACLE_PROXY_ARTIFACTS_TOOL("UseOracleProxyArtifactsTool", PROJECTS, NO),
    USE_XML_BEANS_CLASSES_TOOL("UseXmlBeansClassesTool", PROJECTS, NO),
    USE_JAXB_2_ARTIFACTS_TOOL("UseJaxb2ArtifactsTool", PROJECTS, NO),
    USE_DOT_NET_2_ARTIFACTS_TOOL("UseDotNet2ArtifactTool", PROJECTS, NO),
    USE_GSOAP_ARTIFACTS_TOOL("UseGSoapArtifactsTool", PROJECTS, NO),
    USE_TCP_MON_TOOL("UseTcpMonTool", PROJECTS, NO),
    USE_HERMES_JMS_TOOL("UseHermesJMSTool", PROJECTS, NO),

    //Launch TestRunners From UI
    LAUNCH_FUNCTIONAL_TEST_RUNNER_FROM_UI("LaunchFunctionalTestRunnerFromUI", SOAPUI_NG, NO),
    LAUNCH_SECURITY_TEST_RUNNER_FROM_UI("LaunchSecurityTestRunnerFromUI", SECURE, NO),
    LAUNCH_LOAD_TEST_RUNNER_FROM_UI("LaunchLoadTestRunnerFromUI", LOADUI_NG, NO),

    //Launch TestRunners
    LAUNCH_FUNCTIONAL_TEST_RUNNER("LaunchFunctionalTestRunner", SOAPUI_NG, NO),
    LAUNCH_SECURITY_TEST_RUNNER("LaunchSecurityTestRunner", SECURE, NO),
    LAUNCH_LOAD_TEST_RUNNER("LaunchLoadTestRunner", LOADUI_NG, NO),

    //Service virtualization actions
    CREATE_REST_MOCK("CreateRESTMock", SERVICE_V, NO),
    START_REST_MOCK_FROM_MOCK_PANEL("MockPanelStartRESTMock", SERVICE_V, MAIN_EDITOR),
    CREATE_SOAP_MOCK("CreateSOAPMock", SERVICE_V, NO),
    START_SOAP_MOCK_FROM_MOCK_PANEL("MockPanelStartSOAPMock", SERVICE_V, MAIN_EDITOR),
    DEPLOY_REST_MOCK_AS_WAR("DeployRESTMockAsWar", SERVICE_V, NO),
    DEPLOY_SOAP_MOCK_AS_WAR("DeploySoapMockAsWar", SERVICE_V, NO),
    STOP_REST_MOCK_FROM_MOCK_PANEL("MockPanelStopRESTMock", SERVICE_V, MAIN_EDITOR),
    STOP_SOAP_MOCK_FROM_MOCK_PANEL("MockPanelStopSOAPMock", SERVICE_V, MAIN_EDITOR),
    STOP_REST_MOCK_FROM_NAVIGATOR("ContextMenuStopRESTMock", SERVICE_V, NAVIGATOR_TREE),
    STOP_SOAP_MOCK_FROM_NAVIGATOR("ContextMenuStopSOAPMock", SERVICE_V, NAVIGATOR_TREE),
    START_REST_MOCK_FROM_NAVIGATOR("ContextMenuStartRESTMock", SERVICE_V, NAVIGATOR_TREE),
    START_SOAP_MOCK_FROM_NAVIGATOR("ContextMenuStartSOAPMock", SERVICE_V, NAVIGATOR_TREE),
    GENERATE_REST_MOCK_FROM_NAVIGATOR("ContextMenuGenerateRESTMock", SERVICE_V, NAVIGATOR_TREE),
    GENERATE_SOAP_MOCK_FROM_NAVIGATOR("ContextMenuGenerateSOAPMock", SERVICE_V, NAVIGATOR_TREE),
    ADD_SOAP_REQUEST_TO_SOAP_MOCK_SERVICE("ContextMenuAddSoapRequestToSoapMockService", SERVICE_V, NAVIGATOR_TREE),
    ADD_REST_REQUEST_TO_REST_MOCK_SERVICE("ContextMenuAddRESTRequestToRESTMockService", SERVICE_V, NAVIGATOR_TREE),
    ADD_OPERATION_AS_MOCK_RESPONSE_STEP("ContextMenuAddOperationAsMockResponseStep", SERVICE_V, NAVIGATOR_TREE),
    ADD_REQUEST_AS_MOCK_RESPONSE_STEP("ContextMenuAddRequestAsMockResponseStep", SERVICE_V, NAVIGATOR_TREE),

    //Functional testing actions
    ASSIGN_O_AUTH20("AssignOAuth20", PROJECTS, MAIN_EDITOR),
    ASSIGN_O_AUTH20_FOR_TEST_REQUEST("AssignOAuth20", SOAPUI_NG, MAIN_EDITOR),
    ASSIGN_O_AUTH10("AssignOAuth10", PROJECTS, MAIN_EDITOR),
    ASSIGN_O_AUTH10_FOR_TEST_REQUEST("AssignOAuth10", SOAPUI_NG, MAIN_EDITOR),
    ASSIGN_NTLM_AUTH("AssignNTLMAuth", PROJECTS, MAIN_EDITOR),
    ASSIGN_NTLM_AUTH_FOR_TEST_REQUEST("AssignNTLMAuth", SOAPUI_NG, MAIN_EDITOR),
    ASSIGN_SPNEGO_KERBEROS_AUTH("AssignSPNEGOKerberosAuth", PROJECTS, MAIN_EDITOR),
    ASSIGN_SPNEGO_KERBEROS_AUTH_FOR_TEST_REQUEST("AssignSPNEGOKerberosAuth", SOAPUI_NG, MAIN_EDITOR),
    ASSIGN_BASIC_AUTH("AssignBasicAuth", PROJECTS, MAIN_EDITOR),
    ASSIGN_BASIC_AUTH_FOR_TEST_REQUEST("AssignBasicAuth", SOAPUI_NG, MAIN_EDITOR),
    ADD_PROPERTY_TRANSFER_IN_PROPERTY_TRANSFER_TEST_STEP("AddPropertyTransferInPropertyTransferTestStep", SOAPUI_NG, MAIN_EDITOR),

    //Definitions Import/Export
    IMPORT_WADL("ImportWADL", PROJECTS, NO),
    IMPORT_WSDL("ImportWSDL", PROJECTS, NO),
    IMPORT_SWAGGER("ImportSwagger", PROJECTS, NO),
    EXPORT_SWAGGER("ExportSwagger", PROJECTS, NO),
    IMPORT_RAML("ImportRAML", PROJECTS, NO),
    EXPORT_RAML("ExportRAML", PROJECTS, NO),
    ADD_REST_SERVICE_FROM_URI("AddRESTServiceFromUri", PROJECTS, NO),

    //Project Import/Export
    IMPORT_PROJECT("ImportProject", null, NO),
    IMPORT_PACKED_PROJECT("ImportPackedProject", null, NO),
    IMPORT_REMOTE_PROJECT("ImportRemoteProject", null, NO),
    EXPORT_PROJECT("ExportProject", null, NO),

    //Security testing actions
    CREATE_SECURITY_TEST("CreateSecurityTest", SECURE, NO),
    CREATE_SECURITY_TEST_FROM_TEST_CASE_PANEL("TestCasePanelCreateSecurityTest", SECURE, MAIN_EDITOR),
    RUN_SECURITY_TEST_FROM_SECURITY_TEST_PANEL("SecurityTestPanelRunSecurityTest", SECURE, MAIN_EDITOR),

    //Load testing actions
    CREATE_LOAD_TEST("CreateLoadTest", LOADUI_NG, NO),
    CREATE_LOAD_TEST_FROM_TEST_CASE_PANEL("TestCasePanelCreateLoadTest", LOADUI_NG, MAIN_EDITOR),
    RUN_LOAD_TEST_FROM_LOAD_TEST_PANEL("LoadTestPanelRunLoadTest", LOADUI_NG, MAIN_EDITOR),
    ADD_LOAD_TEST_ASSERTION("AddLoadTestAssertion", LOADUI_NG, MAIN_EDITOR),

    //Monitoring
    TEST_ON_DEMAND("TestCasePanelTestOnDemand", PROJECTS, MAIN_EDITOR),
    LAUNCH_HTTP_MONITOR("LaunchHttpMonitor", PROJECTS, NO),

    //Workspace actions
    SWITCH_WORKSPACE("SwitchWorkspace", null, NO),
    NEW_WORKSPACE("NewWorkspace", null, NO),
    RENAME_WORKSPACE("RenameWorkspace", null, NO),

    //Preferences
    SAVE_PREFERENCES("MainMenuSavePreferences", null, MAIN_MENU),
    IMPORT_PREFERENCES("MainMenuImportPreferences", null, MAIN_MENU),

    //Tool
    RUN_TOOL_FROM_COMMAND_LINE("RunToolFromCmdLine", PROJECTS, NO);

    private String actionName;
    private ModuleType moduleType;
    private ProductArea productArea;
    private final AnalyticsManager.Category category;

    SoapUIActions(String actionName, ModuleType moduleType, ProductArea productArea) {
        this.actionName = actionName;
        this.moduleType = moduleType;
        this.productArea = productArea;
        this.category = AnalyticsManager.Category.ACTION;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public String getActionName() {
        return actionName;
    }

    public AnalyticsManager.Category getCategory() {
        return category;
    }

    public ProductArea getProductArea() {
        return productArea;
    }

    public static SoapUIActions getByActionName(String actionName) {
        for (SoapUIActions currentAction : SoapUIActions.values()) {
            if (currentAction.getActionName().equals(actionName)) {
                return currentAction;
            }
        }
        return null;
    }
}
