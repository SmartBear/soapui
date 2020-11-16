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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.soapui.TestRunnerAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecurityTestRunnerAction extends TestRunnerAction {
    private static final String SH = ".sh";
    private static final String BAT = ".bat";
    private static final String SECURITYTESTRUNNER = "securitytestrunner";
    private static final String SECURITYTEST = "SecurityTest";
    protected static final String TESTRUNNERPATH = "SecurityTestRunner Path";
    public static final String SOAPUI_ACTION_ID = "SecurityTestRunnerAction";
    private static final String ALL_VALUE = "<all>";

    private final static Logger log = LogManager.getLogger(SecurityTestRunnerAction.class);

    public SecurityTestRunnerAction() {
        super("Launch Security TestRunner", "Launch command-line SecurityTestRunner for this project");
    }

    protected XFormDialog buildDialog(WsdlProject modelItem) {
        if (modelItem == null) {
            return null;
        }

        proVersion = isProVersion(modelItem);

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Launch SecurityTestRunner");
        createTestCaseRunnerTabs(modelItem, builder);

        return builder.buildDialog(buildDefaultActions(HelpUrls.TESTRUNNER_HELP_URL, modelItem),
                "Specify arguments for launching SoapUI Security TestRunner", UISupport.TOOL_ICON);
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

            testSuites = modelItem.getTestSuiteList();
            for (int c = 0; c < testSuites.size(); c++) {
                if (testSuites.get(c).getTestCaseCount() == 0) {
                    testSuites.remove(c);
                    c--;
                }
            }

            mainForm.setOptions(TESTSUITE, ModelSupport.getNames(new String[]{ALL_VALUE}, testSuites));

            List<String> testCases = new ArrayList<String>();

            for (TestSuite testSuite : testSuites) {
                for (TestCase testCase : testSuite.getTestCaseList()) {
                    if (!testCases.contains(testCase.getName())) {
                        testCases.add(testCase.getName());
                    }
                }
            }

            testCases.add(0, ALL_VALUE);
            mainForm.setOptions(TESTCASE, testCases.toArray());

            List<String> securityTests = new ArrayList<String>();

            for (TestSuite testSuite : testSuites) {
                for (TestCase testCase : testSuite.getTestCaseList()) {
                    for (SecurityTest securityTest : testCase.getSecurityTestList()) {
                        if (!securityTests.contains(securityTest.getName())) {
                            securityTests.add(securityTest.getName());
                        }
                    }
                }
            }

            securityTests.add(0, ALL_VALUE);
            mainForm.setOptions(SECURITYTEST, securityTests.toArray());
        } else if (mainForm != null) {
            mainForm.setOptions(ENDPOINT, new String[]{null});
        }

        initEnvironment(modelItem);

        StringToStringMap values = super.initValues(modelItem, param);

        if (mainForm != null) {
            if (param instanceof WsdlTestCase) {
                mainForm.getFormField(TESTSUITE).setValue(((WsdlTestCase) param).getTestSuite().getName());
                mainForm.getFormField(TESTCASE).setValue(((WsdlTestCase) param).getName());

                values.put(TESTSUITE, ((WsdlTestCase) param).getTestSuite().getName());
                values.put(TESTCASE, ((WsdlTestCase) param).getName());
            } else if (param instanceof WsdlTestSuite) {
                mainForm.getFormField(TESTSUITE).setValue(((WsdlTestSuite) param).getName());
                values.put(TESTSUITE, ((WsdlTestSuite) param).getName());
            }

            mainForm.getComponent(SAVEPROJECT).setEnabled(!modelItem.isRemote());
        }

        return values;
    }

    private void createTestCaseRunnerTabs(WsdlProject modelItem, XFormDialogBuilder builder) {
        mainForm = builder.createForm("Basic");
        mainForm.addComboBox(TESTSUITE, new String[]{}, "The TestSuite to run").addFormFieldListener(
                new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        List<String> testCases = new ArrayList<String>();
                        String tc = mainForm.getComponentValue(TESTCASE);

                        if (newValue.equals(ALL_VALUE)) {
                            for (TestSuite testSuite : testSuites) {
                                for (TestCase testCase : testSuite.getTestCaseList()) {
                                    if (!testCases.contains(testCase.getName())) {
                                        testCases.add(testCase.getName());
                                    }
                                }
                            }
                        } else {
                            TestSuite testSuite = getModelItem().getTestSuiteByName(newValue);
                            if (testSuite != null) {
                                testCases.addAll(Arrays.asList(ModelSupport.getNames(testSuite.getTestCaseList())));
                            }
                        }

                        testCases.add(0, ALL_VALUE);
                        mainForm.setOptions(TESTCASE, testCases.toArray());

                        if (testCases.contains(tc)) {
                            mainForm.getFormField(TESTCASE).setValue(tc);
                        }
                    }
                });

        mainForm.addComboBox(TESTCASE, new String[]{}, "The TestCase to run").addFormFieldListener(
                new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        List<String> securityTests = new ArrayList<String>();
                        String st = mainForm.getComponentValue(SECURITYTEST);

                        if (newValue.equals(ALL_VALUE)) {
                            for (TestSuite testSuite : testSuites) {
                                for (TestCase testCase : testSuite.getTestCaseList()) {
                                    for (SecurityTest securityTest : testCase.getSecurityTestList()) {
                                        if (!securityTests.contains(securityTest.getName())) {
                                            securityTests.add(securityTest.getName());
                                        }
                                    }
                                }
                            }
                        } else {
                            TestCase testCase = null;
                            try {
                                testCase = getModelItem().getTestSuiteByName(mainForm.getComponentValue(TESTSUITE))
                                        .getTestCaseByName(mainForm.getComponentValue(TESTCASE));
                            } catch (NullPointerException npe) {
                            }
                            if (testCase != null) {
                                securityTests.addAll(Arrays.asList(ModelSupport.getNames(testCase.getSecurityTestList())));
                            }
                        }

                        securityTests.add(0, ALL_VALUE);
                        mainForm.setOptions(SECURITYTEST, securityTests.toArray());

                        if (securityTests.contains(st)) {
                            mainForm.getFormField(SECURITYTEST).setValue(st);
                        }
                    }
                });
        mainForm.addComboBox(SECURITYTEST, new String[]{}, "The Security Test to run");
        mainForm.addSeparator();

        mainForm.addCheckBox(ENABLEUI, "Enables UI components in scripts");
        mainForm.addTextField(TESTRUNNERPATH, "Folder containing SecurityTestRunner.bat to use", XForm.FieldType.FOLDER);
        mainForm.addCheckBox(SAVEPROJECT, "Saves project before running").setEnabled(!modelItem.isRemote());
        mainForm.addCheckBox(ADDSETTINGS, "Adds global settings to command-line");
        mainForm.addSeparator();
        mainForm.addTextField(PROJECTPASSWORD, "Set project password", XForm.FieldType.PASSWORD);
        mainForm.addTextField(SOAPUISETTINGSPASSWORD, "Set soapui-settings.xml password", XForm.FieldType.PASSWORD);
        mainForm.addCheckBox(IGNOREERRORS, "Do not stop if error occurs, ignore them");
        mainForm.addCheckBox(SAVEAFTER, "Sets to save the project file after tests have been run");

        advForm = builder.createForm("Overrides");
        advForm.addComboBox(ENVIRONMENT, new String[]{"Default"}, "The environment to set for all requests")
                .setEnabled(proVersion);
        advForm.addComboBox(ENDPOINT, new String[]{""}, "endpoint to forward to");
        advForm.addTextField(HOSTPORT, "Host:Port to use for requests", XForm.FieldType.TEXT);
        advForm.addSeparator();
        advForm.addTextField(USERNAME, "The username to set for all requests", XForm.FieldType.TEXT);
        advForm.addTextField(PASSWORD, "The password to set for all requests", XForm.FieldType.PASSWORD);
        advForm.addTextField(DOMAIN, "The domain to set for all requests", XForm.FieldType.TEXT);
        advForm.addComboBox(WSSTYPE, new String[]{"", "Text", "Digest"}, "The username to set for all requests");

        reportForm = builder.createForm("Reports");
        reportForm.addCheckBox(PRINTREPORT, "Prints a summary report to the console");
        reportForm.addCheckBox(EXPORTJUNITRESULTS, "Exports results to a JUnit-Style report");
        reportForm.addCheckBox(EXPORTALL, "Exports all results (not only errors)");
        reportForm.addTextField(ROOTFOLDER, "Folder to export to", XForm.FieldType.FOLDER);
        reportForm.addSeparator();
        reportForm.addCheckBox(COVERAGE, "Generate WSDL Coverage report (SoapUI Pro only)").setEnabled(proVersion);
        reportForm.addCheckBox(OPEN_REPORT, "Opens generated report(s) in browser (SoapUI Pro only)").setEnabled(
                proVersion);
        reportForm.addTextField(GENERATEREPORTSEACHTESTCASE, "Report to Generate (SoapUI Pro only)",
                XForm.FieldType.TEXT).setEnabled(proVersion);
        reportForm.addTextField(REPORTFORMAT, "Choose report format(s), comma-separated (SoapUI Pro only)",
                XForm.FieldType.TEXT).setEnabled(proVersion);

        propertyForm = builder.createForm("Properties");
        propertyForm.addComponent(GLOBALPROPERTIES, createTextArea());
        propertyForm.addComponent(SYSTEMPROPERTIES, createTextArea());
        propertyForm.addComponent(PROJECTPROPERTIES, createTextArea());

        setToolsSettingsAction(null);
        buildArgsForm(builder, false, "TestRunner");
    }

    protected ArgumentBuilder buildArgs(WsdlProject modelItem) throws IOException {
        XFormDialog dialog = getDialog();
        if (dialog == null) {
            ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
            builder.startScript(SECURITYTESTRUNNER, BAT, SH);
            return builder;
        }

        StringToStringMap values = dialog.getValues();

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript(SECURITYTESTRUNNER, BAT, SH);

        builder.addString(ENDPOINT, "-e", "");
        builder.addString(HOSTPORT, "-h", "");

        if (!values.get(TESTSUITE).equals(ALL_VALUE)) {
            builder.addString(TESTSUITE, "-s", "");
        }

        if (!values.get(TESTCASE).equals(ALL_VALUE)) {
            builder.addString(TESTCASE, "-c", "");
        }

        if (!values.get(SECURITYTEST).equals(ALL_VALUE)) {
            builder.addString(SECURITYTEST, "-n", "");
        }

        builder.addString(USERNAME, "-u", "");
        builder.addStringShadow(PASSWORD, "-p", "");
        builder.addString(DOMAIN, "-d", "");
        builder.addString(WSSTYPE, "-w", "");

        builder.addBoolean(PRINTREPORT, "-r");
        builder.addBoolean(EXPORTALL, "-a");
        builder.addBoolean(EXPORTJUNITRESULTS, "-j");
        builder.addString(ROOTFOLDER, "-f", "");

        if (proVersion) {
            builder.addBoolean(OPEN_REPORT, "-o");
            builder.addBoolean(COVERAGE, "-g");
            builder.addString(GENERATEREPORTSEACHTESTCASE, "-R", "");
            builder.addString(REPORTFORMAT, "-F", "");
            builder.addString(ENVIRONMENT, "-E", "");
        }

        builder.addStringShadow(PROJECTPASSWORD, "-x", "");
        builder.addStringShadow(SOAPUISETTINGSPASSWORD, "-v", "");
        builder.addBoolean(IGNOREERRORS, "-I");
        builder.addBoolean(SAVEAFTER, "-S");

        addPropertyArguments(builder);

        if (dialog.getBooleanValue(ADDSETTINGS)) {
            try {
                builder.addBoolean(ADDSETTINGS, "-t" + SoapUI.saveSettings());
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }

        builder.addBoolean(ENABLEUI, "-i");
        builder.addArgs(new String[]{modelItem.getPath()});

        addToolArgs(values, builder);

        return builder;
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
            log.debug("Launching testrunner in directory [" + builder.directory() + "] with arguments ["
                    + args.toString() + "]");
        }

        toolHost.run(new ProcessToolRunner(builder, "SoapUI TestRunner", modelItem, args));
    }

}
