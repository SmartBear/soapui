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

package com.eviware.soapui.integration.loadui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.integration.impl.CajoClient;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class IntegrationUtils {

    private static final String NOT_SELECTED = "-";
    public static final String CREATE_NEW_OPTION = "<Create New>";
    public static final String CREATE_ON_PROJECT_LEVEL = "<Project Level>";

    public static final int ADD_TO_PROJECT_LEVEL = 0;
    public static final int ADD_TO_SINGLE_TESTCASE = 1;
    public static final int ADD_TO_SEPARATE_TESTCASES = 2;

    public static final String LOADU_INFO_DIALOG_TITLE = "Target loadUI items info";

    public static String getIntegrationPort(String appName, String whichAppPort, String defaultPort) {
        String cajoPort = SoapUI.getSettings().getString(whichAppPort, defaultPort);
        try {
            Integer.parseInt(cajoPort);
        } catch (NumberFormatException nfe) {
            cajoPort = defaultPort;
            SoapUI.getSettings().setString(whichAppPort, cajoPort);
            SoapUI.log(appName + " integration port was reset to default value " + defaultPort
                    + ", because its value was not correct!");
        }
        return cajoPort;
    }

    public static List<String> getProjectsNames() {
        return invokeRemoteGettingStringList("getProjects", null);
    }

    public static List<String> getTestCasesNames() {
        return invokeRemoteGettingStringList("getTestCases", null);
    }

    public static List<String> getSoapUISamplersNames(String projectName, String testCaseName) {
        return invokeRemoteGettingStringList("getSoapUIRunners", new String[]{projectName, testCaseName});
    }

    public static List<String> getMockServiceRunnersNames(String projectName, String testCaseName) {
        return invokeRemoteGettingStringList("getMockServiceRunners", new String[]{projectName, testCaseName});
    }

    @SuppressWarnings("unchecked")
    private static List<String> invokeRemoteGettingStringList(String methodName, Object args) {
        try {
            return (List<String>) CajoClient.getInstance().invoke(methodName, args);
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
            return Collections.emptyList();
        }
    }

    public static boolean isProjectOpened(String projectName) {
        boolean isOpened = false;
        try {
            isOpened = (Boolean) CajoClient.getInstance().invoke("isProjectOpened", new String[]{projectName});
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return isOpened;
    }

    public static String getOpenedProjectName() {
        String projectName = "";
        try {
            projectName = ((String) CajoClient.getInstance().invoke("getOpenedProjectName", null));
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return projectName;
    }

    public static void bringLoadUIToFront() {
        try {
            CajoClient.getInstance().invoke("bringToFront", null);
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
    }

    public static void removeLoadUILoadedProject(File projectFile) {
        try {
            CajoClient.getInstance().invoke("removeLoadedSoapUIProject", projectFile);
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
    }

    public static HashMap<String, String> createSoapUIRunner(String soapUIProjectPath, String soapUITestSuite,
                                                             String soapUITestCase, String loadUIProject, String loadUITestCase, String loadUISoapUISampler,
                                                             String generatorType, String analisysType) throws IOException {
        HashMap<String, String> samplerSettings = new HashMap<String, String>();
        try {
            SoapUI.log("createSoapUISampler for soapUIProjectPath=\"" + soapUIProjectPath + "\", soapUITestSuite=\""
                    + soapUITestSuite + "\", soapUITestCase=\"" + soapUITestCase + "\", loadUIProject=\"" + loadUIProject
                    + "\", loadUITestCase=\"" + loadUITestCase + ", \"loadUISoapUISampler=\"" + loadUISoapUISampler + "\"");

            HashMap<String, Object> context = new ContextMapping(soapUIProjectPath, soapUITestSuite, soapUITestCase,
                    loadUIProject, loadUITestCase, loadUISoapUISampler).setCreateSoapUIRunnerContext(generatorType,
                    analisysType);
            samplerSettings = (HashMap<String, String>) CajoClient.getInstance().invoke("createSoapUIRunner", context);
            bringLoadUIToFront();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return samplerSettings;
    }

    public static HashMap<String, String> createMockServiceRunner(String soapUIProjectPath, String soapUIMockService,
                                                                  String path, String port, String loadUIProject, String loadUITestCase, String mockServiceRunner)
            throws IOException {
        HashMap<String, String> mockServiceSettings = new HashMap<String, String>();
        try {
            SoapUI.log("createMockRunner for soapUIProjectPath=\"" + soapUIProjectPath + "\", soapUIMockService=\""
                    + soapUIMockService + "\", path=\"" + path + "\", port=\"" + port + "\", loadUIProject=\""
                    + loadUIProject + "\", loadUITestCase=\"" + loadUITestCase + ", \"loadUIMockRunner=\""
                    + mockServiceRunner + "\"");

            HashMap<String, Object> context = new ContextMapping(soapUIProjectPath, soapUIMockService, "", "",
                    loadUIProject, loadUITestCase, mockServiceRunner).setCreateMockServiceRunnerContext();
            mockServiceSettings = (HashMap<String, String>) CajoClient.getInstance().invoke("createMockServiceRunner",
                    context);
            bringLoadUIToFront();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return mockServiceSettings;
    }

    public static HashMap<String, Object> exportLoadTestToLoadUI(WsdlLoadTest loadTest, String loadUIProject,
                                                                 String loadUITestCase, String loadUISoapUISampler) throws IOException {
        HashMap<String, Object> contextSettings = new HashMap<String, Object>();
        try {
            ContextMapping contextMapping = new ContextMapping(loadTest, loadUIProject, loadUITestCase,
                    loadUISoapUISampler);
            HashMap<String, Object> context = contextMapping.setInitExportLoadTestToLoadUIContext();
            contextSettings = (HashMap<String, Object>) CajoClient.getInstance().invoke("exportSoapUILoadTestToLoadUI",
                    context);
            if (contextMapping.isFinalTriggerMappingNeeded()) {
                // export once more to set final values whose setting use
                // previously
                // set default values
                CajoClient.getInstance().invoke("exportSoapUILoadTestToLoadUI",
                        contextMapping.setFinalExportLoadTestToLoadUIContext(contextSettings));
            }
            bringLoadUIToFront();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return contextSettings;
    }

    public static HashMap<String, String> exportMultipleLoadTestToLoadUI(WsdlTestCase testCase, String[] loadTests,
                                                                         String loadUIProject) throws IOException {
        HashMap<String, String> samplerSettings = new HashMap<String, String>();
        try {
            String loadTestName = loadTests[0];
            WsdlLoadTest loadTest = (WsdlLoadTest) testCase.getLoadTestByName(loadTestName);
            HashMap<String, Object> firstSamplerSettings = exportLoadTestToLoadUI(loadTest, loadUIProject,
                    CREATE_NEW_OPTION, CREATE_NEW_OPTION);
            // String loadUITestCaseAddedTo = ( String )firstSamplerSettings.get(
            // ContextMapping.LOADUI_TEST_CASE_NAME );
            String loadUIProjectAddedTo = (String) firstSamplerSettings.get(ContextMapping.LOADUI_PROJECT_NAME);
            for (int i = 1; i < loadTests.length; i++) {
                loadTestName = loadTests[i];
                loadTest = (WsdlLoadTest) testCase.getLoadTestByName(loadTestName);
                exportLoadTestToLoadUI(loadTest, loadUIProjectAddedTo, CREATE_NEW_OPTION, CREATE_NEW_OPTION);
            }
            bringLoadUIToFront();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return samplerSettings;
    }

    public static HashMap<String, String> exportMultipleLoadTestToLoadUI(WsdlTestSuite testSuite, String[] loadTests,
                                                                         String loadUIProject) throws IOException {
        HashMap<String, String> samplerSettings = new HashMap<String, String>();
        try {
            String compositeLoadTestName = loadTests[0];
            String[] names = compositeLoadTestName.split(" - ");
            String testCaseName = names[0];
            String loadTestName = names[1];
            WsdlTestCase testCase = testSuite.getTestCaseByName(testCaseName);
            HashMap<String, Object> firstSamplerSettings;
            // String loadUITestCaseAddedTo = "";
            String loadUIProjectAddedTo = "";
            if (testCase != null) {
                WsdlLoadTest loadTest = (WsdlLoadTest) testCase.getLoadTestByName(loadTestName);
                if (loadTest != null) {
                    firstSamplerSettings = exportLoadTestToLoadUI(loadTest, loadUIProject, CREATE_NEW_OPTION,
                            CREATE_NEW_OPTION);
                    // loadUITestCaseAddedTo = ( String )firstSamplerSettings.get(
                    // ContextMapping.LOADUI_TEST_CASE_NAME );
                    loadUIProjectAddedTo = (String) firstSamplerSettings.get(ContextMapping.LOADUI_PROJECT_NAME);
                }

            }

            for (int i = 1; i < loadTests.length; i++) {
                compositeLoadTestName = loadTests[i];
                loadTestName = loadTests[i];
                WsdlLoadTest loadTest = (WsdlLoadTest) testCase.getLoadTestByName(loadTestName);
                testCaseName = names[0];
                loadTestName = names[1];
                testCase = testSuite.getTestCaseByName(testCaseName);
                if (testCase != null) {
                    loadTest = (WsdlLoadTest) testCase.getLoadTestByName(loadTestName);
                    if (loadTest != null) {
                        exportLoadTestToLoadUI(loadTest, loadUIProjectAddedTo, CREATE_NEW_OPTION, CREATE_NEW_OPTION);
                    }

                }
            }
            bringLoadUIToFront();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return samplerSettings;
    }

    public static void generateTestSuiteLoadTests(String soapUIProjectPath, String soapUITestSuite,
                                                  String[] soapUITestCases, String loadUIProject, int levelToAdd) throws IOException {

        String firstTestCase = soapUITestCases[0];
        HashMap<String, String> firstSamplerSettings;
        String loadUITestCaseAddedTo;
        String loadUIProjectAddedTo;
        switch (levelToAdd) {
            case ADD_TO_PROJECT_LEVEL:
                firstSamplerSettings = createSoapUIRunner(soapUIProjectPath, soapUITestSuite, firstTestCase, loadUIProject,
                        null, CREATE_NEW_OPTION, NOT_SELECTED, NOT_SELECTED);
                loadUIProjectAddedTo = firstSamplerSettings.get(ContextMapping.LOADUI_PROJECT_NAME);
                for (int i = 1; i < soapUITestCases.length; i++) {
                    String testCase = soapUITestCases[i];
                    createSoapUIRunner(soapUIProjectPath, soapUITestSuite, testCase, loadUIProjectAddedTo, null,
                            CREATE_NEW_OPTION, NOT_SELECTED, NOT_SELECTED);
                }
                break;

            case ADD_TO_SINGLE_TESTCASE:
                firstSamplerSettings = createSoapUIRunner(soapUIProjectPath, soapUITestSuite, firstTestCase, loadUIProject,
                        CREATE_NEW_OPTION, CREATE_NEW_OPTION, NOT_SELECTED, NOT_SELECTED);
                loadUITestCaseAddedTo = firstSamplerSettings.get(ContextMapping.LOADUI_TEST_CASE_NAME);
                loadUIProjectAddedTo = firstSamplerSettings.get(ContextMapping.LOADUI_PROJECT_NAME);
                for (int i = 1; i < soapUITestCases.length; i++) {
                    String testCase = soapUITestCases[i];
                    createSoapUIRunner(soapUIProjectPath, soapUITestSuite, testCase, loadUIProjectAddedTo,
                            loadUITestCaseAddedTo, CREATE_NEW_OPTION, NOT_SELECTED, NOT_SELECTED);

                }
                break;
            case ADD_TO_SEPARATE_TESTCASES:
                firstSamplerSettings = createSoapUIRunner(soapUIProjectPath, soapUITestSuite, firstTestCase, loadUIProject,
                        CREATE_NEW_OPTION, CREATE_NEW_OPTION, NOT_SELECTED, NOT_SELECTED);
                loadUIProjectAddedTo = firstSamplerSettings.get(ContextMapping.LOADUI_PROJECT_NAME);
                for (int i = 1; i < soapUITestCases.length; i++) {
                    String testCase = soapUITestCases[i];
                    createSoapUIRunner(soapUIProjectPath, soapUITestSuite, testCase, loadUIProjectAddedTo, CREATE_NEW_OPTION,
                            CREATE_NEW_OPTION, NOT_SELECTED, NOT_SELECTED);
                }
                break;
        }
    }

    /**
     * Closes currently opened project in loadUI.
     *
     * @param saveProject If true project will be saved before closing. If false project
     *                    will be closed without saving.
     */
    public static void closeOpenedLoadUIProject(boolean saveProject) {
        try {
            if (saveProject) {
                CajoClient.getInstance().invoke("saveOpenedProject", null);
            }
            CajoClient.getInstance().invoke("closeOpenedProject", null);
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
    }

    /**
     * Checks if currently opened project in loadUI is dirty.
     *
     * @return true if project is dirty, false if it is not, null if there is no
     *         opened project.
     */
    public static Boolean isOpenedProjectDirty() {
        Boolean result = null;
        try {
            result = (Boolean) CajoClient.getInstance().invoke("isOpenedProjectDirty", null);
        } catch (Exception e) {
            SoapUI.log.error("Error while invoking cajo server in loadui ", e);
        }
        return result;
    }

    /**
     * Checks the state of currently opened loadUI project and asks user what to
     * do with it. When project is dirty user is prompted with the following
     * options: Yes - save and close, No - close without saving, Cancel - don't
     * close project. When project is not dirty available options are: Yes -
     * close project, No - don't close. If there is no opened project user is not
     * prompted since there is no project to close.
     *
     * @return true if operation is canceled, false if not.
     */
    public static boolean checkOpenedLoadUIProjectForClose() {
        String openedProjectName = getOpenedProjectName();
        if (StringUtils.isNullOrEmpty(openedProjectName)) {
            // there is no opened project, so return false (don't cancel operation)
            return false;
        }

        // holds user decision if project should be closed (true) or not
        // (false or null)
        Boolean close;
        // holds user decision if project should be saved before close (true) or
        // not (false or null). This makes sense only if close is true.
        Boolean saveProject = null;

        Boolean isDirty = isOpenedProjectDirty();
        if (isDirty != null && isDirty) {
            // display Yes-No-Cancel dialog. Yes = true (save and close), No =
            // false (close without saving), Cancel = null (dont't close at all)
            saveProject = UISupport.confirmOrCancel("Save currently open [" + openedProjectName
                    + "] loadUI project before closing?", "Save loadUI project");

            // when saveProject is not null user decided to close project (with or
            // without saving). when it is null user clicked cancel so don't close.
            close = saveProject != null;
        } else {
            // project is not dirty so display Yes-No dialog. Yes = true (close),
            // No = false (don't close)
            close = UISupport.confirm("Currently open [" + openedProjectName
                    + "] loadUI project will be closed. Continue?", "Close loadUI project");
        }

        // method result. Set to true if user canceled operation and to false if
        // project was closed.
        boolean quit = true;
        if (close) {
            closeOpenedLoadUIProject(saveProject != null && saveProject);
            quit = false;
        }
        return quit;
    }

    public static String[] getAvailableProjects() {
        List<String> availableProjects = getProjectsNames();
        availableProjects.add(CREATE_NEW_OPTION);
        String[] names = new String[availableProjects.size()];
        for (int c = 0; c < availableProjects.size(); c++) {
            names[c] = availableProjects.get(c);
        }

        return names;
    }

    /*
     * if project with projectName is opened adds all its testcases to list in
     * any case adds {CREATE_ON_PROJECT_LEVEL, CREATE_NEW_OPTION}
     */
    public static String[] getAvailableTestCases(String projectName) {
        List<String> availableTestCases = new ArrayList<String>();
        if (!projectName.equals(CREATE_NEW_OPTION) && isProjectOpened(projectName)) {
            availableTestCases.addAll(getTestCasesNames());
        }
        availableTestCases.add(CREATE_ON_PROJECT_LEVEL);
        availableTestCases.add(CREATE_NEW_OPTION);
        String[] names = new String[availableTestCases.size()];
        for (int c = 0; c < availableTestCases.size(); c++) {
            names[c] = availableTestCases.get(c);
        }

        return names;
    }

    public static String[] getAvailableRunners(String projectName, String testCaseName) {
        List<String> availableSamplers = new ArrayList<String>();
        if (!projectName.equals(CREATE_NEW_OPTION) && isProjectOpened(projectName)) {
            availableSamplers.addAll(getSoapUISamplersNames(projectName, testCaseName));
        }

        availableSamplers.add(CREATE_NEW_OPTION);
        String[] names = new String[availableSamplers.size()];
        for (int c = 0; c < availableSamplers.size(); c++) {
            names[c] = availableSamplers.get(c);
        }

        return names;
    }

    public static String[] getAvailableMockServiceRunners(String projectName, String testCaseName) {
        List<String> availableMockServiceRunners = new ArrayList<String>();
        if (!projectName.equals(CREATE_NEW_OPTION) && isProjectOpened(projectName)) {
            availableMockServiceRunners.addAll(getMockServiceRunnersNames(projectName, testCaseName));
        }

        availableMockServiceRunners.add(CREATE_NEW_OPTION);
        String[] names = new String[availableMockServiceRunners.size()];
        for (int c = 0; c < availableMockServiceRunners.size(); c++) {
            names[c] = availableMockServiceRunners.get(c);
        }

        return names;
    }

    /**
     * When exporting SoapUI project to loadUI, loadUI uses project file to get
     * resources so SoapUI project need to be saved for loadUI be able to pick
     * all changes if there is any.
     *
     * @param project
     * @return
     */
    public static boolean forceSaveProject(WsdlProject project) {

        if (UISupport.confirm("Project needs to be saved before it gets exported! Save it?", "Save Project")) {
            try {
                if (project.save() == SaveStatus.SUCCESS) {
                    return true;
                } else {
                    UISupport.showInfoMessage("Export Operation Aborted!");
                    return false;
                }
            } catch (IOException e) {
                SoapUI.logError(e);
                UISupport.showErrorMessage("Error saving project file!. Export Operation Aborted!");
                return false;
            }
        } else {
            UISupport.showInfoMessage("Export Operation Aborted!");
            return false;
        }

    }

}
