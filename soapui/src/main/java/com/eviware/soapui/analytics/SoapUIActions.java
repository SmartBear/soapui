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
    PRODUCT_STARTED_FROM_CMD("ProductStartedFromCmd", SoapUIFeatures.USE_SOAP_UI, ProductArea.NO),
    PRODUCT_STARTED_IN_DEBUGGING_MODE("ProductStartedInDebuggingMode", SoapUIFeatures.USE_SOAP_UI, ProductArea.NO),
    EXIT("Exit", SoapUIFeatures.USE_SOAP_UI, ProductArea.NO),
    EXIT_WITHOUT_SAVE("ExitWithoutSave", SoapUIFeatures.USE_SOAP_UI, ProductArea.NO),
    INSTALL_SOFTWARE("InstallSoftware", SoapUIFeatures.INSTALL, ProductArea.NO),
    UNINSTALL_SOFTWARE("UninstallSoftware", SoapUIFeatures.INSTALL, ProductArea.NO),

    //Assertions
    ADD_ASSERTION("AddAssertion", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),

    //Run actions
    RUN_TEST_CASE_FROM_TOOLBAR("TestCasePanelRunTestCase", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    RUN_TEST_SUITE_FROM_TOOLBAR("TestSuitePanelRunTestSuite", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    RUN_PROJECT_FROM_TOOLBAR("ProjectPanelRunProject", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    RUN_REQUEST_FROM_REQUEST_EDITOR("RequestEditorRunRequest", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    RUN_TEST_STEP_FROM_PANEL("TestStepPanelRunTestStep", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),

    //Create Functional Model Items
    CREATE_TEST_SUITE_FROM_PROJECT_PANEL("ProjectPanelCreateTestSuite", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    CREATE_TEST_SUITE_FROM_CONTEXT_MENU("ContextMenuCreateTestSuite", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.NAVIGATOR_TREE),
    CREATE_TEST_CASE_FROM_TEST_TEST_SUITE_PANEL("TestSuitePanelCreateTestCase", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    CREATE_TEST_CASE_FROM_CONTEXT_MENU("ContextMenuCreateTestCase", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.NAVIGATOR_TREE),
    ADD_NEW_TEST_STEP_FROM_CONTEXT_MENU("ContextMenuAddNewTestStep", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.NAVIGATOR_TREE),
    ADD_NEW_TEST_STEP_FROM_TEST_CASE_PANEL("TestCasePanelAddNewTestStep", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    CREATE_REQUEST_FOR_OPERATION("ContextMenuCreateSoapRequestForOperation", SoapUIFeatures.SOAP, ProductArea.NAVIGATOR_TREE),
    CREATE_REQUEST_FOR_METHOD("ContextMenuCreateRestRequestForMethod", SoapUIFeatures.REST, ProductArea.NAVIGATOR_TREE),
    ADD_REQUEST_TO_TEST_CASE_FROM_REQUEST_PANEL("TestCasePanelAddRequest", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),

    //Static Toolbar Actions
    CREATE_EMPTY_PROJECT_FROM_TOOLBAR("StaticToolbarCreateEmptyProject", SoapUIFeatures.USE_SOAP_UI, ProductArea.STATIC_MAIN_TOOLBAR),
    CREATE_SOAP_PROJECT_FROM_TOOLBAR("StaticToolbarCreateSoapProject", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    CREATE_REST_PROJECT_FROM_TOOLBAR("StaticToolbarCreateRestProject", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    IMPORT_PROJECT_FROM_TOOLBAR("StaticToolbarImportProject", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    SAVE_ALL_PROJECTS_FROM_TOOLBAR("StaticToolbarSaveAllProjects", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    OPEN_FORUM_FROM_TOOLBAR("StaticToolbarOpenForum", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    APPLY_TRIAL_FROM_TOOLBAR("StaticToolbarApplyTrial", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    OPEN_PREFERENCES_FROM_TOOLBAR("StaticToolbarOpenPreferences", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    TURN_ON_PROXY_FROM_TOOLBAR("StaticToolbarTurnOnProxy", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),
    TURN_OFF_PROXY_FROM_TOOLBAR("StaticToolbarTurnOffProxy", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.STATIC_MAIN_TOOLBAR),

    //Tools
    USE_JBOSSWS_ARTIFACTS_TOOL("ToolsMenuUseJBossWSArtifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_JBOSSWS_JAXWS_ARTIFACTS_TOOL("ToolsMenuUseJBossWSJaxWSArtifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_JAX_RPC_ARTIFACTS_TOOL("ToolsMenuUseJaxRpcArtifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_JAX_WS_ARTIFACTS_TOOL("ToolsMenuUseJaxWsArtifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_AXIS_1_ARTIFACTS_TOOL("ToolsMenuUseAxis1Artifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_AXIS_2_ARTIFACTS_TOOL("ToolsMenuUseAxis2Artifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_APACHE_CXF_TOOL("ToolsMenuUseApacheCxf", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_XFIRE_1_STUBS_TOOL("ToolsMenuUseXFire1Stubs", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_ORACLE_PROXY_ARTIFACTS_TOOL("ToolsMenuUseOracleProxyArtifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_XML_BEANS_CLASSES_TOOL("ToolsMenuUseXmlBeansClasses", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_JAXB_2_ARTIFACTS_TOOL("ToolsMenuUseJaxb2Artifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_DOT_NET_2_ARTIFACTS_TOOL("ToolsMenuUseDotNet2Artifact", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_GSOAP_ARTIFACTS_TOOL("UseGSoapArtifacts", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_TCP_MON_TOOL("ToolsMenuUseTcpMon", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),
    USE_HERMES_JMS_TOOL("ToolsMenuUseHermesJMS", SoapUIFeatures.TOOL, ProductArea.MAIN_MENU),

    //Launch TestRunners From UI
    LAUNCH_FUNCTIONAL_TEST_RUNNER_FROM_UI("LaunchFunctionalTestRunnerFromUI", SoapUIFeatures.AUTOMATE_SOAP_UI, ProductArea.NAVIGATOR_TREE),
    LAUNCH_SECURITY_TEST_RUNNER_FROM_UI("LaunchSecurityTestRunnerFromUI", SoapUIFeatures.AUTOMATE_SOAP_UI, ProductArea.NAVIGATOR_TREE),
    LAUNCH_LOAD_TEST_RUNNER_FROM_UI("LaunchLoadTestRunnerFromUI", SoapUIFeatures.AUTOMATE_SOAP_UI, ProductArea.NAVIGATOR_TREE),

    //Launch TestRunners
    LAUNCH_FUNCTIONAL_TEST_RUNNER("LaunchFunctionalTestRunner", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.NO),
    LAUNCH_SECURITY_TEST_RUNNER("LaunchSecurityTestRunner", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.NO),
    LAUNCH_LOAD_TEST_RUNNER("LaunchLoadTestRunner", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.NO),

    //Service virtualization actions
    CREATE_REST_MOCK_FROM_CONTEXT_MENU("ContextMenuCreateRestMock", SoapUIFeatures.SERVICE_VIRTUALIZATION, ProductArea.NAVIGATOR_TREE),
    START_REST_MOCK_FROM_MOCK_PANEL("MockPanelStartRestMock", SoapUIFeatures.SERVICE_VIRTUALIZATION, ProductArea.MAIN_EDITOR),
    CREATE_SOAP_MOCK_FROM_CONTEXT_MENU("ContextMenuCreateSOAPMock", SoapUIFeatures.SERVICE_VIRTUALIZATION, ProductArea.NAVIGATOR_TREE),
    START_SOAP_MOCK_FROM_MOCK_PANEL("MockPanelStartSOAPMock", SoapUIFeatures.SERVICE_VIRTUALIZATION, ProductArea.MAIN_EDITOR),
    DEPLOY_REST_MOCK_AS_WAR("DeployRestMockAsWar", SoapUIFeatures.SERVICE_VIRTUALIZATION, ProductArea.NAVIGATOR_TREE),
    DEPLOY_SOAP_MOCK_AS_WAR("DeploySoapMockAsWar", SoapUIFeatures.SERVICE_VIRTUALIZATION, ProductArea.NAVIGATOR_TREE),

    //Functional testing actions
    ASSIGN_O_AUTH("AssignOAuth", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),
    ADD_PROPERTY_TRASNFER_IN_PROPERTY_TRANSFER_TEST_STEP("AddPropertyTransferInPropertyTransferTestStep", SoapUIFeatures.FUNCTIONAL_TESTING, ProductArea.MAIN_EDITOR),

    //Definitions Import/Export
    IMPORT_WADL("ImportWADL", SoapUIFeatures.REST, ProductArea.NO),
    IMPORT_WSDL("ImportWSDL", SoapUIFeatures.SOAP, ProductArea.NO),
    IMPORT_SWAGGER("ImportSwagger", SoapUIFeatures.REST, ProductArea.NO),
    EXPORT_SWAGGER("ExportSwagger", SoapUIFeatures.REST, ProductArea.NO),
    IMPORT_RAML("ImportRAML", SoapUIFeatures.REST, ProductArea.NO),
    EXPORT_RAML("ExportRAML", SoapUIFeatures.REST, ProductArea.NO),
    ADD_REST_SERVICE_FROM_URI_CONTEXT_MENU("ContextMenuAddRestServiceFromUri", SoapUIFeatures.REST, ProductArea.NAVIGATOR_TREE),

    //Project Import/Export
    IMPORT_PROJECT("MainMenuImportProject", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),
    IMPORT_PACKED_PROJECT("MainMenuImportPackedProject", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),
    IMPORT_REMOTE_PROJECT("MainMenuImportRemoteProject", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),
    EXPORT_PROJECT("MainMenuExportProject", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),

    //Security testing actions
    CREATE_SECURITY_TEST_FROM_CONTEXT_MENU("ContextMenuCreateSecurityTest", SoapUIFeatures.SECURITY_TESTING, ProductArea.NAVIGATOR_TREE),
    CREATE_SECURITY_TEST_FROM_TEST_CASE_PANEL("TestCasePanelCreateSecurityTest", SoapUIFeatures.SECURITY_TESTING, ProductArea.MAIN_EDITOR),
    RUN_SECURITY_TEST_FROM_SECURITY_TEST_PANEL("SecurityTestPanelRunSecurityTest", SoapUIFeatures.SECURITY_TESTING, ProductArea.MAIN_EDITOR),

    //Load testing actions
    CREATE_LOAD_TEST_FROM_CONTEXT_MENU("ContextMenuCreateLoadTest", SoapUIFeatures.PERFORMANCE_TESTING, ProductArea.NAVIGATOR_TREE),
    CREATE_LOAD_TEST_FROM_TEST_CASE_PANEL("TestCasePanelCreateLoadTest", SoapUIFeatures.PERFORMANCE_TESTING, ProductArea.MAIN_EDITOR),
    RUN_LOAD_TEST_FROM_LOAD_TEST_PANEL("LoadTestPanelRunLoadTest", SoapUIFeatures.PERFORMANCE_TESTING, ProductArea.MAIN_EDITOR),
    ADD_LOAD_TEST_ASSERTION("AddLoadTestAssertion", SoapUIFeatures.PERFORMANCE_TESTING, ProductArea.MAIN_EDITOR),

    //Monitoring
    TEST_ON_DEMAND("TestCasePanelTestOnDemand", SoapUIFeatures.MONITORING, ProductArea.MAIN_EDITOR),
    LAUNCH_HTTP_MONITOR("ContextMenuLaunchHttpMonitor", SoapUIFeatures.MONITORING, ProductArea.NAVIGATOR_TREE),

    //Workspace actions
    SWITCH_WORKSPACE("MainMenuSwitchWorkspace", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),
    NEW_WORKSPACE("MainMenuNewWorkspace", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),
    RENAME_WORKSPACE("MainMenuRenameWorkspace", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),

    //Preferences
    SAVE_PREFERENCES("MainMenuSavePreferences", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),
    IMPORT_PREFERENCES("MainMenuImportPreferences", SoapUIFeatures.USE_SOAP_UI, ProductArea.MAIN_MENU),

    //Tool
    RUN_TOOL_FROM_COMMAND_LINE("RunToolFromCmdLine", SoapUIFeatures.TOOL, ProductArea.NO);

    private String actionName;
    private SoapUIFeatures feature;
    private ProductArea productArea;
    private final AnalyticsManager.Category category;

    SoapUIActions(String actionName, SoapUIFeatures feature, ProductArea productArea) {
        this.actionName = actionName;
        this.feature = feature;
        this.productArea = productArea;
        this.category = AnalyticsManager.Category.ACTION;
    }

    public SoapUIFeatures getFeature() {
        return feature;
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
