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

package com.eviware.soapui.analytics;

import com.smartbear.analytics.AnalyticsManager;

/**
 *
 */
public enum SoapUIActions {

    //Common actions
    PRODUCT_STARTED_FROM_CMD("ProductStartedFromCmd", null, ProductArea.NO),
    PRODUCT_STARTED("ProductStarted", null, ProductArea.NO),
    PRODUCT_STARTED_IN_DEBUGGING_MODE("ProductStartedInDebuggingMode", null, ProductArea.NO),
    EXIT("Exit", null, ProductArea.NO),
    EXIT_WITHOUT_SAVE("ExitWithoutSave", null, ProductArea.NO),
    SAVE_ALL_PROJECTS("MainMenuSaveAllProjects", null, ProductArea.MAIN_MENU),

    //Assertions
    ADD_ASSERTION("AddAssertion", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),

    //Run actions
    RUN_TEST_STEP_FROM_TOOLBAR("TestCasePanelRunTestStep", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    RUN_TEST_CASE_FROM_TOOLBAR("TestSuitePanelRunTestCase", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    RUN_TEST_SUITE_FROM_TOOLBAR("ProjectPanelRunTestSuite", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    RUN_REQUEST_FROM_REQUEST_EDITOR("RequestEditorRunRequest", ModuleType.PROJECTS, ProductArea.MAIN_EDITOR),
    RUN_TEST_STEP_FROM_PANEL("TestStepPanelRunTestStep", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),

    //Create Functional Model Items
    CREATE_TEST_SUITE_FROM_PROJECT_PANEL("ProjectPanelCreateTestSuite", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    CREATE_TEST_SUITE("CreateTestSuite", ModuleType.SOAPUI_NG, ProductArea.NO),
    CREATE_TEST_CASE_FROM_TEST_TEST_SUITE_PANEL("TestSuitePanelCreateTestCase", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    CREATE_TEST_CASE("CreateTestCase", ModuleType.SOAPUI_NG, ProductArea.NO),
    ADD_NEW_TEST_STEP_FROM_CONTEXT_MENU("ContextMenuAddNewTestStep", ModuleType.SOAPUI_NG, ProductArea.NAVIGATOR_TREE),
    ADD_NEW_TEST_STEP_FROM_TEST_CASE_PANEL("TestCasePanelAddNewTestStep", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    CREATE_REQUEST_FOR_OPERATION("ContextMenuCreateSoapRequestForOperation", ModuleType.PROJECTS, ProductArea.NAVIGATOR_TREE),
    CREATE_REQUEST_FOR_METHOD("ContextMenuCreateRestRequestForMethod", ModuleType.PROJECTS, ProductArea.NAVIGATOR_TREE),
    ADD_REQUEST_TO_TEST_CASE_FROM_REQUEST_PANEL("TestCasePanelAddRequest", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),

    //Static Toolbar Actions
    CREATE_EMPTY_PROJECT_FROM_TOOLBAR("StaticToolbarCreateEmptyProject", null, ProductArea.STATIC_MAIN_TOOLBAR),
    CREATE_SOAP_PROJECT_FROM_TOOLBAR("StaticToolbarCreateSoapProject", ModuleType.SOAPUI_NG, ProductArea.STATIC_MAIN_TOOLBAR),
    CREATE_REST_PROJECT_FROM_TOOLBAR("StaticToolbarCreateRestProject", ModuleType.SOAPUI_NG, ProductArea.STATIC_MAIN_TOOLBAR),
    IMPORT_PROJECT_FROM_TOOLBAR("StaticToolbarImportProject", ModuleType.SOAPUI_NG, ProductArea.STATIC_MAIN_TOOLBAR),
    SAVE_ALL_PROJECTS_FROM_TOOLBAR("StaticToolbarSaveAllProjects", null, ProductArea.STATIC_MAIN_TOOLBAR),
    OPEN_FORUM_FROM_TOOLBAR("StaticToolbarOpenForum", null, ProductArea.STATIC_MAIN_TOOLBAR),
    APPLY_TRIAL_FROM_TOOLBAR("StaticToolbarApplyTrial", null, ProductArea.STATIC_MAIN_TOOLBAR),
    OPEN_PREFERENCES_FROM_TOOLBAR("StaticToolbarOpenPreferences", null, ProductArea.STATIC_MAIN_TOOLBAR),
    TURN_ON_PROXY_FROM_TOOLBAR("StaticToolbarTurnOnProxy", null, ProductArea.STATIC_MAIN_TOOLBAR),
    TURN_OFF_PROXY_FROM_TOOLBAR("StaticToolbarTurnOffProxy", null, ProductArea.STATIC_MAIN_TOOLBAR),

    //Tools
    USE_JBOSSWS_ARTIFACTS_TOOL("ToolsMenuUseJBossWSArtifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_JBOSSWS_JAXWS_ARTIFACTS_TOOL("ToolsMenuUseJBossWSJaxWSArtifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_JAX_RPC_ARTIFACTS_TOOL("ToolsMenuUseJaxRpcArtifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_JAX_WS_ARTIFACTS_TOOL("ToolsMenuUseJaxWsArtifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_AXIS_1_ARTIFACTS_TOOL("ToolsMenuUseAxis1Artifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_AXIS_2_ARTIFACTS_TOOL("ToolsMenuUseAxis2Artifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_APACHE_CXF_TOOL("ToolsMenuUseApacheCxf", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_XFIRE_1_STUBS_TOOL("ToolsMenuUseXFire1Stubs", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_ORACLE_PROXY_ARTIFACTS_TOOL("ToolsMenuUseOracleProxyArtifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_XML_BEANS_CLASSES_TOOL("ToolsMenuUseXmlBeansClasses", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_JAXB_2_ARTIFACTS_TOOL("ToolsMenuUseJaxb2Artifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_DOT_NET_2_ARTIFACTS_TOOL("ToolsMenuUseDotNet2Artifact", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_GSOAP_ARTIFACTS_TOOL("UseGSoapArtifacts", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_TCP_MON_TOOL("ToolsMenuUseTcpMon", ModuleType.PROJECTS, ProductArea.MAIN_MENU),
    USE_HERMES_JMS_TOOL("ToolsMenuUseHermesJMS", ModuleType.PROJECTS, ProductArea.MAIN_MENU),

    //Launch TestRunners From UI
    LAUNCH_FUNCTIONAL_TEST_RUNNER_FROM_UI("LaunchFunctionalTestRunnerFromUI", ModuleType.SOAPUI_NG, ProductArea.NAVIGATOR_TREE),
    LAUNCH_SECURITY_TEST_RUNNER_FROM_UI("LaunchSecurityTestRunnerFromUI", ModuleType.SECURE, ProductArea.NAVIGATOR_TREE),
    LAUNCH_LOAD_TEST_RUNNER_FROM_UI("LaunchLoadTestRunnerFromUI", ModuleType.LOADUI_NG, ProductArea.NAVIGATOR_TREE),

    //Launch TestRunners
    LAUNCH_FUNCTIONAL_TEST_RUNNER("LaunchFunctionalTestRunner", ModuleType.SOAPUI_NG, ProductArea.NO),
    LAUNCH_SECURITY_TEST_RUNNER("LaunchSecurityTestRunner", ModuleType.SECURE, ProductArea.NO),
    LAUNCH_LOAD_TEST_RUNNER("LaunchLoadTestRunner", ModuleType.LOADUI_NG, ProductArea.NO),

    //Service virtualization actions
    CREATE_REST_MOCK_FROM_CONTEXT_MENU("ContextMenuCreateRestMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    START_REST_MOCK_FROM_MOCK_PANEL("MockPanelStartRestMock", ModuleType.SERVICE_V, ProductArea.MAIN_EDITOR),
    CREATE_SOAP_MOCK_FROM_CONTEXT_MENU("ContextMenuCreateSOAPMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    START_SOAP_MOCK_FROM_MOCK_PANEL("MockPanelStartSOAPMock", ModuleType.SERVICE_V, ProductArea.MAIN_EDITOR),
    DEPLOY_REST_MOCK_AS_WAR("DeployRestMockAsWar", ModuleType.SERVICE_V, ProductArea.NO),
    DEPLOY_SOAP_MOCK_AS_WAR("DeploySoapMockAsWar", ModuleType.SERVICE_V, ProductArea.NO),
    STOP_REST_MOCK_FROM_MOCK_PANEL("MockPanelStopRestMock", ModuleType.SERVICE_V, ProductArea.MAIN_EDITOR),
    STOP_SOAP_MOCK_FROM_MOCK_PANEL("MockPanelStopSOAPMock", ModuleType.SERVICE_V, ProductArea.MAIN_EDITOR),
    STOP_REST_MOCK_FROM_NAVIGATOR("ContextMenuStopRestMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    STOP_SOAP_MOCK_FROM_NAVIGATOR("ContextMenuStopSOAPMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    START_REST_MOCK_FROM_NAVIGATOR("ContextMenuStartRestMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    START_SOAP_MOCK_FROM_NAVIGATOR("ContextMenuStartSOAPMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    GENERATE_REST_MOCK_FROM_NAVIGATOR("ContextMenuGenerateRestMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    GENERATE_SOAP_MOCK_FROM_NAVIGATOR("ContextMenuGenerateSOAPMock", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    ADD_SOAP_REQUEST_TO_SOAP_MOCK_SERVICE("ContextMenuAddSoapRequestToSoapMockService", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    ADD_REST_REQUEST_TO_REST_MOCK_SERVICE("ContextMenuAddRestRequestToRestMockService", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    ADD_OPERATION_AS_MOCK_RESPONSE_STEP("ContextMenuAddOperationAsMockResponseStep", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),
    ADD_REQUEST_AS_MOCK_RESPONSE_STEP("ContextMenuAddRequestAsMockResponseStep", ModuleType.SERVICE_V, ProductArea.NAVIGATOR_TREE),

    //Functional testing actions
    ASSIGN_O_AUTH20("AssignOAuth20", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    ASSIGN_O_AUTH10("AssignOAuth10", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    ASSIGN_NTLM_AUTH("AssignNTLMAuth", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    ASSIGN_SPNEGO_KERBEROS_AUTH("AssignSPNEGOKerberosAuth", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    ASSIGN_WSS_AUTH("AssignWssAuth", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    ASSIGN_BASIC_AUTH("AssignBasicAuth", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),
    ADD_PROPERTY_TRANSFER_IN_PROPERTY_TRANSFER_TEST_STEP("AddPropertyTransferInPropertyTransferTestStep", ModuleType.SOAPUI_NG, ProductArea.MAIN_EDITOR),

    //Definitions Import/Export
    IMPORT_WADL("ImportWADL", ModuleType.PROJECTS, ProductArea.NO),
    IMPORT_WSDL("ImportWSDL", ModuleType.PROJECTS, ProductArea.NO),
    IMPORT_SWAGGER("ImportSwagger", ModuleType.PROJECTS, ProductArea.NO),
    EXPORT_SWAGGER("ExportSwagger", ModuleType.PROJECTS, ProductArea.NO),
    IMPORT_RAML("ImportRAML", ModuleType.PROJECTS, ProductArea.NO),
    EXPORT_RAML("ExportRAML", ModuleType.PROJECTS, ProductArea.NO),
    ADD_REST_SERVICE_FROM_URI("AddRestServiceFromUri", ModuleType.PROJECTS, ProductArea.NO),

    //Project Import/Export
    IMPORT_PROJECT("ImportProject", null, ProductArea.NO),
    IMPORT_PACKED_PROJECT("MainMenuImportPackedProject", null, ProductArea.MAIN_MENU),
    IMPORT_REMOTE_PROJECT("MainMenuImportRemoteProject", null, ProductArea.MAIN_MENU),
    EXPORT_PROJECT("MainMenuExportProject", null, ProductArea.MAIN_MENU),

    //Security testing actions
    CREATE_SECURITY_TEST("CreateSecurityTest", ModuleType.SECURE, ProductArea.NO),
    CREATE_SECURITY_TEST_FROM_TEST_CASE_PANEL("TestCasePanelCreateSecurityTest", ModuleType.SECURE, ProductArea.MAIN_EDITOR),
    RUN_SECURITY_TEST_FROM_SECURITY_TEST_PANEL("SecurityTestPanelRunSecurityTest", ModuleType.SECURE, ProductArea.MAIN_EDITOR),

    //Load testing actions
    CREATE_LOAD_TEST("CreateLoadTest", ModuleType.LOADUI_NG, ProductArea.NO),
    CREATE_LOAD_TEST_FROM_TEST_CASE_PANEL("TestCasePanelCreateLoadTest", ModuleType.LOADUI_NG, ProductArea.MAIN_EDITOR),
    RUN_LOAD_TEST_FROM_LOAD_TEST_PANEL("LoadTestPanelRunLoadTest", ModuleType.LOADUI_NG, ProductArea.MAIN_EDITOR),
    ADD_LOAD_TEST_ASSERTION("AddLoadTestAssertion", ModuleType.LOADUI_NG, ProductArea.MAIN_EDITOR),

    //Monitoring
    TEST_ON_DEMAND("TestCasePanelTestOnDemand", ModuleType.PROJECTS, ProductArea.MAIN_EDITOR),
    LAUNCH_HTTP_MONITOR("LaunchHttpMonitor", ModuleType.PROJECTS, ProductArea.NO),

    //Workspace actions
    SWITCH_WORKSPACE("SwitchWorkspace", null, ProductArea.NO),
    NEW_WORKSPACE("NewWorkspace", null, ProductArea.NO),
    RENAME_WORKSPACE("RenameWorkspace", null, ProductArea.NO),

    //Preferences
    SAVE_PREFERENCES("MainMenuSavePreferences", null, ProductArea.MAIN_MENU),
    IMPORT_PREFERENCES("MainMenuImportPreferences", null, ProductArea.MAIN_MENU),

    //Tool
    RUN_TOOL_FROM_COMMAND_LINE("RunToolFromCmdLine", ModuleType.PROJECTS, ProductArea.NO);

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
