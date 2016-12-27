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

/**
 *
 */
public enum SoapUIActions {

    //Start SoapUI pro
    START_SOAPUI_FROM_COMAND_LINE("StartSoapUIFromCommandLine", SoapUIFeatures.AUTOMATE_SOAP_UI),
    START_SOAPUI_PRO("StartSoapUIPro", SoapUIFeatures.USE_SOAP_UI),
    DEBUG_MODE("DebugMode", SoapUIFeatures.USE_SOAP_UI),
    EXIT("Exit", SoapUIFeatures.USE_SOAP_UI),
    EXIT_WITHOUT_SAVE("ExitWithoutSave", SoapUIFeatures.USE_SOAP_UI),

    //Plugin
    INSTALL_PLUGIN("InstallPlugin", SoapUIFeatures.PLUGINS),

    //Service virtualization actions
    CREATE_REST_MOCK("CreateRestMock", SoapUIFeatures.SERVICE_VIRTUALIZATION),
    START_REST_MOCK("StartRestMock", SoapUIFeatures.SERVICE_VIRTUALIZATION),
    CREATE_SOAP_MOCK("CreateSOAPMock", SoapUIFeatures.SERVICE_VIRTUALIZATION),
    START_SOAP_MOCK("StartSOAPMock", SoapUIFeatures.SERVICE_VIRTUALIZATION),
    USE_MOCK_FROM_TEST("UseMockFromTest", SoapUIFeatures.SERVICE_VIRTUALIZATION),
    DEPLOY_REST_MOCK_AS_WAR("DeployRestMockAsWar", SoapUIFeatures.SERVICE_VIRTUALIZATION),
    DEPLOY_SOAP_MOCK_AS_WAR("DeploySoapMockAsWar", SoapUIFeatures.SERVICE_VIRTUALIZATION),

    //Functional testing actions
    CREATE_GENERIC_PROJECT("CreateGenericProject", SoapUIFeatures.FUNCTIONAL_TESTING),
    CREATE_REQUEST("CreateRequest", SoapUIFeatures.FUNCTIONAL_TESTING),
    RUN_REQUEST_FROM_REQUEST_EDITOR("RunRequestFromRequestEditor", SoapUIFeatures.FUNCTIONAL_TESTING),
    CREATE_TEST_CASE("CreateTestCase", SoapUIFeatures.FUNCTIONAL_TESTING),
    RUN_TEST_CASE("RunTestCase", SoapUIFeatures.FUNCTIONAL_TESTING),
    RUN_TEST_SUITE("RunTestSuite", SoapUIFeatures.FUNCTIONAL_TESTING),
    RUN_PROJECT("RunProject", SoapUIFeatures.FUNCTIONAL_TESTING),
    CREATE_TEST_STEP("CreateTestStep", SoapUIFeatures.FUNCTIONAL_TESTING),
    RUN_TEST_STEP("RunTestStep", SoapUIFeatures.FUNCTIONAL_TESTING),
    RUN_REQUEST_FROM_TEST_STEP_EDITOR("RunRequestFromTestStepEditor", SoapUIFeatures.FUNCTIONAL_TESTING),
    ASSIGN_O_AUTH("AssignOAuth", SoapUIFeatures.FUNCTIONAL_TESTING),
    ENABLE_COVERAGE("EnableCoverage", SoapUIFeatures.FUNCTIONAL_TESTING),
    USE_COVERAGE("UseCoverage", SoapUIFeatures.FUNCTIONAL_TESTING),
    ADD_POINT_AND_CLICK_ASSERTION("AddPointAndClickAssertion", SoapUIFeatures.FUNCTIONAL_TESTING),
    ADD_POINT_AND_CLICK_PROPERTY_TRANFER("AddPointAndClickPropertyTransfer", SoapUIFeatures.FUNCTIONAL_TESTING),
    ADD_PROPERTY_TRASNFER_IN_PROPERTY_TRANSFER_TEST_STEP("AddPropertyTransferInPropertyTransferTestStep",
            SoapUIFeatures.FUNCTIONAL_TESTING),

    //REST Discovery actions
    START_REST_DISCOVERY("StartRestDiscovery", SoapUIFeatures.DISCOVERY),
    GENERATE_REST_REQ_FROM_REST_DISCOVERY("GenerateRequestFromRestDiscovery", SoapUIFeatures.DISCOVERY),
    GENERATE_TEST_SUITE_FROM_REST_DISCOVERY("GenerateTestSuiteFromRequestDiscovery", SoapUIFeatures.DISCOVERY),

    //REST
    CREATE_REST_PROJECT("CreateRESTProject", SoapUIFeatures.REST),
    IMPORT_WADL("ImportWADL", SoapUIFeatures.REST),
    IMPORT_SWAGGER("ImportSwagger", SoapUIFeatures.REST),
    EXPORT_SWAGGER("ExportSwagger", SoapUIFeatures.REST),
    IMPORT_RAML("ImportRAML", SoapUIFeatures.REST),
    EXPORT_RAML("ExportRAML", SoapUIFeatures.REST),

    //SOAP
    CREATE_SOAP_PROJECT("CreateSOAPProject", SoapUIFeatures.SOAP),
    IMPORT_WSDL("ImportWSDL", SoapUIFeatures.SOAP),

    //Security testing actions
    CREATE_SECURITY_TEST("CreateSecurityTest", SoapUIFeatures.SECURITY_TESTING),
    RUN_SECURITY_TEST("RunSecurityTest", SoapUIFeatures.SECURITY_TESTING),

    //load testing actions
    CREATE_LOAD_TEST("CreateLoadTest", SoapUIFeatures.PERFORMANCE_TESTING),
    RUN_LOAD_TEST("RunLoadTest", SoapUIFeatures.PERFORMANCE_TESTING),
    ADD_LOAD_TEST_ASSERTION("AddLoadTestAssertion", SoapUIFeatures.PERFORMANCE_TESTING),

    //Monitoring
    TEST_ON_DEMAND("TestOnDemand", SoapUIFeatures.MONITORING),


    //Install/Uninstall actions
    INSTALL_SOFTWARE("InstallSoftware", SoapUIFeatures.INSTALL),
    UNINSTALL_SOFTWARE("UninstallSoftware", SoapUIFeatures.INSTALL),
    REINSTALL_SOFTWARE("ReInstallSoftware", SoapUIFeatures.INSTALL),

    //Licensing
    INSTALL_LICENSE("InstallLicense", SoapUIFeatures.LICENSE),
    LICENSE_UPDATED("LicenseUpdated", SoapUIFeatures.LICENSE),
    LICENSE_EXPIRED("LicenseExpired", SoapUIFeatures.LICENSE),
    DEACTIVATE_LICENSE("DeactivateLicense", SoapUIFeatures.LICENSE),
    SHOW_RENEWAL_PAGE("ShowRenewalPage", SoapUIFeatures.LICENSE),
    SHOW_LICENSE_EXPIRED_PAGE("ShowLicenseExpiredPage", SoapUIFeatures.LICENSE),
    SHOW_PRO_LICENSE_INSTALLED_PAGE("ShowProLicenseInstalledPage", SoapUIFeatures.LICENSE),
    SHOW_TRIAL_LICENSE_INSTALLED_PAGE("ShowTrialInstalledPage", SoapUIFeatures.LICENSE),

    //Tool
    RUN_TOOL("RunTool", SoapUIFeatures.TOOL),
    RUN_TOOL_FROM_COMMAND_LINE("RunToolFromCmdLine", SoapUIFeatures.TOOL),

    //Reporting
    CREATE_REPORT("CreateReport", SoapUIFeatures.REPORTS);

    private String actionName;
    private SoapUIFeatures feature;

    SoapUIActions(String actionName, SoapUIFeatures feature) {
        this.actionName = actionName;
        this.feature = feature;
    }

    public SoapUIFeatures getFeature() {
        return feature;
    }

    public String getActionName() {
        return actionName;
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
