/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.soapui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormTextField;
import com.eviware.x.impl.swing.JTextAreaFormField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Action;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Invokes SoapUI TestRunner tool
 *
 * @author Ole.Matzura
 */

public class LoadTestRunnerAction extends AbstractToolsAction<WsdlProject> {
    private static final String ALL_VALUE = "<all>";
    protected static final String ENVIRONMENT = "Environment";
    protected static final String ENDPOINT = "Endpoint";
    protected static final String HOSTPORT = "Host:Port";
    private static final String LIMIT = "Limit";
    private static final String TESTSUITE = "TestSuite";
    private static final String TESTCASE = "TestCase";
    private static final String LOADTEST = "LoadTest";
    private static final String THREADCOUNT = "ThreadCount";
    protected static final String USERNAME = "Username";
    protected static final String PASSWORD = "Password";
    protected static final String DOMAIN = "Domain";
    private static final String PRINTREPORTSTATISTICS = "Print Report Statistics";
    private static final String ROOTFOLDER = "Root Folder";
    private static final String TESTRUNNERPATH = "TestRunner Path";
    private static final String SAVEPROJECT = "Save Project";
    private static final String ADDSETTINGS = "Add Settings";
    private static final String PROJECTPASSWORD = "Project Password";
    private static final String SAVEAFTER = "Save After";
    protected static final String WSSTYPE = "WSS Password Type";
    private static final String OPEN_REPORT = "Open Report";
    private static final String GENERATEREPORTSEACHTESTCASE = "Report to Generate";
    private static final String REPORTFORMAT = "Report Format(s)";
    private static final String GLOBALPROPERTIES = "Global Properties";
    private static final String SYSTEMPROPERTIES = "System Properties";
    private static final String PROJECTPROPERTIES = "Project Properties";

    private XForm mainForm;
    private final static Logger log = LogManager.getLogger(LoadTestRunnerAction.class);
    public static final String SOAPUI_ACTION_ID = "LoadTestRunnerAction";
    protected XForm advForm;
    private XForm propertyForm;
    private XForm reportForm;

    private boolean updating;
    private boolean proVersion;

    public LoadTestRunnerAction() {
        super("Launch LoadTestRunner", "Launch command-line LoadTestRunner for this project");
    }

    protected XFormDialog buildDialog(WsdlProject modelItem) {
        if (modelItem == null) {
            return null;
        }

        proVersion = isProVersion(modelItem);

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Launch LoadTestRunner");

        mainForm = builder.createForm("Basic");
        mainForm.addComboBox(TESTSUITE, new String[]{}, "The TestSuite to run").addFormFieldListener(
                new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        updateCombos();
                    }
                });

        mainForm.addComboBox(TESTCASE, new String[]{}, "The TestCase to run").addFormFieldListener(
                new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        updateCombos();
                    }
                });
        mainForm.addComboBox(LOADTEST, new String[]{}, "The LoadTest to run");
        mainForm.addSeparator();

        XFormTextField path = mainForm.addTextField(TESTRUNNERPATH, "Folder containing TestRunner.bat to use",
                XForm.FieldType.FOLDER);
        path.setValue(System.getProperty("soapui.home", ""));
        mainForm.addCheckBox(SAVEPROJECT, "Saves project before running").setEnabled(!modelItem.isRemote());
        mainForm.addCheckBox(SAVEAFTER, "Sets to save the project file after tests have been run");
        mainForm.addCheckBox(ADDSETTINGS, "Adds global settings to command-line");
        mainForm.addSeparator();
        mainForm.addTextField(PROJECTPASSWORD, "Set project password", XForm.FieldType.PASSWORD);
        mainForm.addTextField(SOAPUISETTINGSPASSWORD, "Set soapui-settings.xml password", XForm.FieldType.PASSWORD);

        advForm = builder.createForm("Overrides");
        advForm.addComboBox(ENVIRONMENT, new String[]{"Default"}, "The environment to set for all requests")
                .setEnabled(proVersion);
        advForm.addComboBox(ENDPOINT, new String[]{""}, "endpoint to forward to");
        advForm.addTextField(HOSTPORT, "Host:Port to use for requests", XForm.FieldType.TEXT);
        advForm.addTextField(LIMIT, "Limit for LoadTest", XForm.FieldType.TEXT);
        advForm.addTextField(THREADCOUNT, "ThreadCount for LoadTest", XForm.FieldType.TEXT);
        advForm.addSeparator();
        advForm.addTextField(USERNAME, "The username to set for all requests", XForm.FieldType.TEXT);
        advForm.addTextField(PASSWORD, "The password to set for all requests", XForm.FieldType.PASSWORD);
        advForm.addTextField(DOMAIN, "The domain to set for all requests", XForm.FieldType.TEXT);
        advForm.addComboBox(WSSTYPE, new String[]{"", "Text", "Digest"}, "The username to set for all requests");

        reportForm = builder.createForm("Reports");
        createReportTab();

        propertyForm = builder.createForm("Properties");
        propertyForm.addComponent(GLOBALPROPERTIES, createTextArea());
        propertyForm.addComponent(SYSTEMPROPERTIES, createTextArea());
        propertyForm.addComponent(PROJECTPROPERTIES, createTextArea());

        setToolsSettingsAction(null);
        buildArgsForm(builder, false, "TestRunner");

        return builder.buildDialog(buildDefaultActions(HelpUrls.TESTRUNNER_HELP_URL, modelItem),
                "Specify arguments for launching SoapUI LoadTestRunner", UISupport.TOOL_ICON);
    }

    /**
     *
     */
    private void createReportTab() {
        reportForm.addCheckBox(PRINTREPORTSTATISTICS, "Creates a report statistics in the specified folder");
        reportForm.addTextField(ROOTFOLDER, "Folder for reporting", XForm.FieldType.FOLDER);
        reportForm.addCheckBox(OPEN_REPORT, "Opens generated report(s) in browser (SoapUI Pro only)").setEnabled(
                proVersion);
        reportForm.addTextField(GENERATEREPORTSEACHTESTCASE, "Report to Generate (SoapUI Pro only)",
                XForm.FieldType.TEXT).setEnabled(proVersion);
        reportForm.addTextField(REPORTFORMAT, "Choose report format(s), comma-separated (SoapUI Pro only)",
                XForm.FieldType.TEXT).setEnabled(proVersion);
    }

    private JTextAreaFormField createTextArea() {
        JTextAreaFormField textArea = new JTextAreaFormField();
        textArea.setWidth(40);
        textArea.getTextArea().setRows(4);
        textArea.setToolTip("name=value pairs separated by space or enter");
        return textArea;
    }

    protected Action createRunOption(WsdlProject modelItem) {
        Action action = super.createRunOption(modelItem);
        action.putValue(Action.NAME, "Launch");
        return action;
    }

    protected StringToStringMap initValues(WsdlProject modelItem, Object param) {
        if (modelItem != null && mainForm != null) {
            List<String> endpoints = new ArrayList<String>();

            for (Interface iface : modelItem.getInterfaceList()) {
                for (String endpoint : iface.getEndpoints()) {
                    if (!endpoints.contains(endpoint)) {
                        endpoints.add(endpoint);
                    }
                }
            }

            endpoints.add(0, null);
            advForm.setOptions(ENDPOINT, endpoints.toArray());
            List<TestSuite> testSuites = modelItem.getTestSuiteList();
            for (int c = 0; c < testSuites.size(); c++) {
                int cnt = 0;

                for (TestCase testCase : testSuites.get(c).getTestCaseList()) {
                    cnt += testCase.getLoadTestCount();
                }

                if (cnt == 0) {
                    testSuites.remove(c);
                    c--;
                }
            }

            mainForm.setOptions(TESTSUITE, ModelSupport.getNames(new String[]{ALL_VALUE}, testSuites));
        } else if (mainForm != null) {
            mainForm.setOptions(ENDPOINT, new String[]{null});
        }

        initEnvironment(modelItem);

        StringToStringMap values = super.initValues(modelItem, param);
        updateCombos();

        if (mainForm != null && param instanceof WsdlLoadTest) {
            mainForm.getFormField(TESTSUITE).setValue(((WsdlLoadTest) param).getTestCase().getTestSuite().getName());
            mainForm.getFormField(TESTCASE).setValue(((WsdlLoadTest) param).getTestCase().getName());
            mainForm.getFormField(LOADTEST).setValue(((WsdlLoadTest) param).getName());

            values.put(TESTSUITE, mainForm.getComponentValue(TESTSUITE));
            values.put(TESTCASE, mainForm.getComponentValue(TESTCASE));
            values.put(LOADTEST, mainForm.getComponentValue(LOADTEST));

            mainForm.getComponent(SAVEPROJECT).setEnabled(!modelItem.isRemote());
        }

        return values;
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, WsdlProject modelItem) throws Exception {
        String testRunnerDir = mainForm.getComponentValue(TESTRUNNERPATH);

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(modelItem);
        builder.command(args.getArgs());
        if (StringUtils.isNullOrEmpty(testRunnerDir)) {
            builder.directory(new File("."));
        } else {
            builder.directory(new File(testRunnerDir));
        }

        if (mainForm.getComponentValue(SAVEPROJECT).equals(Boolean.TRUE.toString())) {
            modelItem.save();
        } else if (StringUtils.isNullOrEmpty(modelItem.getPath())) {
            UISupport.showErrorMessage("Project [" + modelItem.getName() + "] has not been saved to file.");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Launching loadtestrunner in directory [" + builder.directory() + "] with arguments ["
                    + args.toString() + "]");
        }

        toolHost.run(new ProcessToolRunner(builder, "SoapUI LoadTestRunner", modelItem, args));
    }

    private ArgumentBuilder buildArgs(WsdlProject modelItem) throws IOException {
        XFormDialog dialog = getDialog();
        if (dialog == null) {
            ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
            builder.startScript("loadtestrunner", ".bat", ".sh");
            return builder;
        }

        StringToStringMap values = dialog.getValues();

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript("loadtestrunner", ".bat", ".sh");

        builder.addString(ENDPOINT, "-e", "");
        builder.addString(HOSTPORT, "-h", "");

        if (!values.get(TESTSUITE).equals(ALL_VALUE)) {
            builder.addString(TESTSUITE, "-s", "");
        }

        if (!values.get(TESTCASE).equals(ALL_VALUE)) {
            builder.addString(TESTCASE, "-c", "");
        }

        if (!values.get(LOADTEST).equals(ALL_VALUE)) {
            builder.addString(LOADTEST, "-l", "");
        }

        builder.addString(LIMIT, "-m", "");
        builder.addString(THREADCOUNT, "-n", "");
        builder.addString(USERNAME, "-u", "");
        builder.addStringShadow(PASSWORD, "-p", "");
        builder.addString(DOMAIN, "-d", "");

        builder.addBoolean(PRINTREPORTSTATISTICS, "-r");
        builder.addString(ROOTFOLDER, "-f", "");

        builder.addStringShadow(PROJECTPASSWORD, "-x", "");
        builder.addStringShadow(SOAPUISETTINGSPASSWORD, "-v", "");
        builder.addBoolean(SAVEAFTER, "-S");
        builder.addString(WSSTYPE, "-w", "");

        if (proVersion) {
            builder.addBoolean(OPEN_REPORT, "-o");
            builder.addString(GENERATEREPORTSEACHTESTCASE, "-R", "");
            builder.addStrings(REPORTFORMAT, "-F", ",");
            builder.addString(ENVIRONMENT, "-E", "");
        }

        addPropertyArguments(builder);

        if (dialog.getBooleanValue(ADDSETTINGS)) {
            try {
                builder.addBoolean(ADDSETTINGS, "-t" + SoapUI.saveSettings());
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        builder.addArgs(new String[]{modelItem.getPath()});

        addToolArgs(values, builder);

        return builder;
    }

    private void updateCombos() {
        if (updating) {
            return;
        }

        updating = true;

        List<String> testCases = new ArrayList<String>();
        List<String> loadTests = new ArrayList<String>();

        TestSuite ts = getModelItem().getTestSuiteByName(mainForm.getComponentValue(TESTSUITE));
        String testCaseName = mainForm.getComponentValue(TESTCASE);
        if (ALL_VALUE.equals(testCaseName)) {
            testCaseName = null;
        }

        for (TestSuite testSuite : getModelItem().getTestSuiteList()) {
            if (ts != null && testSuite != ts) {
                continue;
            }

            for (TestCase testCase : testSuite.getTestCaseList()) {
                if (testCase.getLoadTestCount() == 0) {
                    continue;
                }

                if (!testCases.contains(testCase.getName())) {
                    testCases.add(testCase.getName());
                }

                if (testCaseName != null && !testCase.getName().equals(testCaseName)) {
                    continue;
                }

                for (LoadTest loadTest : testCase.getLoadTestList()) {
                    if (!loadTests.contains(loadTest.getName())) {
                        loadTests.add(loadTest.getName());
                    }
                }
            }
        }

        testCases.add(0, ALL_VALUE);
        mainForm.setOptions(TESTCASE, testCases.toArray());

        loadTests.add(0, ALL_VALUE);
        mainForm.setOptions(LOADTEST, loadTests.toArray());

        updating = false;
    }

    /**
     * check whether this is Pro or Core version
     *
     * @param modelItem
     * @return
     */
    private boolean isProVersion(WsdlProject modelItem) {
        if (modelItem.getClass().getName().contains("WsdlProjectPro")) {
            return true;
        }
        return false;
    }

    private void addPropertyArguments(ArgumentBuilder builder) {
        List<String> propertyArguments = new ArrayList<String>();

        addProperties(propertyArguments, GLOBALPROPERTIES, "-G");
        addProperties(propertyArguments, SYSTEMPROPERTIES, "-D");
        addProperties(propertyArguments, PROJECTPROPERTIES, "-P");

        builder.addArgs(propertyArguments.toArray(new String[propertyArguments.size()]));
    }

    private void addProperties(List<String> propertyArguments, String propertyDomain, String arg) {
        StringTokenizer tokenizer = new StringTokenizer(getDialog().getValue(propertyDomain));

        while (tokenizer.hasMoreTokens()) {
            propertyArguments.add(arg + tokenizer.nextToken());
        }
    }

    protected void initEnvironment(final WsdlProject modelItem) {
    }
}
