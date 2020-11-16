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
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
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
import com.eviware.x.impl.swing.JTextAreaFormField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Action;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Invokes SoapUI TestRunner tool
 *
 * @author Ole.Matzura
 */

public class TestRunnerAction extends AbstractToolsAction<WsdlProject> {
    protected static final String ALL_VALUE = "<all>";
    protected static final String ENVIRONMENT = "Environment";
    protected static final String ENDPOINT = "Endpoint";
    protected static final String HOSTPORT = "Host:Port";
    protected static final String TESTSUITE = "TestSuite";
    protected static final String TESTCASE = "TestCase";
    protected static final String USERNAME = "Username";
    protected static final String PASSWORD = "Password";
    protected static final String WSSTYPE = "WSS Password Type";
    protected static final String DOMAIN = "Domain";
    protected static final String PRINTREPORT = "Print Report";
    protected static final String ROOTFOLDER = "Root Folder";
    protected static final String EXPORTJUNITRESULTS = "Export JUnit Results";
    protected static final String EXPORTJUNITRESULTSWITHPROPERTIES = "Export JUnit Results with test properties";
    protected static final String EXPORTALL = "Export All";
    protected static final String ENABLEUI = "Enable UI";
    protected static final String TESTRUNNERPATH = "TestRunner Path";
    protected static final String SAVEPROJECT = "Save Project";
    protected static final String ADDSETTINGS = "Add Settings";
    protected static final String OPEN_REPORT = "Open Report";
    protected static final String COVERAGE = "Coverage Report";
    protected static final String PROJECTPASSWORD = "Project Password";
    protected static final String IGNOREERRORS = "Ignore Errors";
    protected static final String GENERATEREPORTSEACHTESTCASE = "Select Report Type";
    protected static final String REPORTFORMAT = "Report Format(s)";
    protected static final String SAVEAFTER = "Save After";
    protected static final String GLOBALPROPERTIES = "Global Properties";
    protected static final String SYSTEMPROPERTIES = "System Properties";
    protected static final String PROJECTPROPERTIES = "Project Properties";

    protected XForm mainForm;

    private final static Logger log = LogManager.getLogger(TestRunnerAction.class);

    public static final String SOAPUI_ACTION_ID = "TestRunnerAction";

    protected XForm advForm;
    protected XForm propertyForm;
    protected XForm reportForm;

    protected List<TestSuite> testSuites;

    protected boolean proVersion;

    public TestRunnerAction() {
        super("Launch TestRunner", "Launch command-line TestRunner for this project");
    }

    public TestRunnerAction(String name, String description) {
        super(name, description);
    }

    protected XFormDialog buildDialog(WsdlProject modelItem) {
        if (modelItem == null) {
            return null;
        }

        proVersion = isProVersion(modelItem);

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Launch TestRunner");

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

        mainForm.addComboBox(TESTCASE, new String[]{}, "The TestCase to run");
        mainForm.addSeparator();

        mainForm.addCheckBox(ENABLEUI, "Enables UI components in scripts");
        mainForm.addTextField(TESTRUNNERPATH, "Folder containing TestRunner.bat to use", XForm.FieldType.FOLDER);
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
        reportForm.addCheckBox(EXPORTJUNITRESULTSWITHPROPERTIES, "Exports results to a JUnit-Style report with test properties");
        reportForm.addCheckBox(EXPORTALL, "Exports all results (not only errors)");
        reportForm.addTextField(ROOTFOLDER, "Folder to export to", XForm.FieldType.FOLDER);
        reportForm.addSeparator();
        reportForm.addCheckBox(COVERAGE, "Generate WSDL Coverage report (SoapUI Pro only)").setEnabled(proVersion);
        reportForm.addCheckBox(OPEN_REPORT, "Opens generated report(s) in browser (SoapUI Pro only)").setEnabled(
                proVersion);
        reportForm.addComboBox(GENERATEREPORTSEACHTESTCASE, new String[0],
                "Template used to generate report (SoapUI Pro only)").setEnabled(proVersion);
        reportForm.addTextField(REPORTFORMAT, "Choose report format(s), comma-separated (SoapUI Pro only)",
                XForm.FieldType.TEXT).setEnabled(proVersion);

        propertyForm = builder.createForm("Properties");
        propertyForm.addComponent(GLOBALPROPERTIES, createTextArea());
        propertyForm.addComponent(SYSTEMPROPERTIES, createTextArea());
        propertyForm.addComponent(PROJECTPROPERTIES, createTextArea());

        setToolsSettingsAction(null);
        buildArgsForm(builder, false, "TestRunner");

        return builder.buildDialog(buildDefaultActions(HelpUrls.TESTRUNNER_HELP_URL, modelItem),
                "Specify arguments for launching SoapUI TestRunner", UISupport.TOOL_ICON);
    }

    protected JTextAreaFormField createTextArea() {
        JTextAreaFormField textArea = new JTextAreaFormField();
        textArea.setWidth(40);
        textArea.getTextArea().setRows(4);
        textArea.setToolTip("name=value pairs separated by space or enter");
        return textArea;
    }

    /**
     * check whether this is Pro or Core version
     *
     * @param modelItem
     * @return boolean
     */
    protected boolean isProVersion(WsdlProject modelItem) {
        if (modelItem.getClass().getName().contains("WsdlProjectPro")) {
            return true;
        }
        return false;
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

    protected ArgumentBuilder buildArgs(WsdlProject modelItem) throws IOException {
        XFormDialog dialog = getDialog();
        if (dialog == null) {
            ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
            builder.startScript("testrunner", ".bat", ".sh");
            return builder;
        }

        StringToStringMap values = dialog.getValues();

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript("testrunner", ".bat", ".sh");

        builder.addString(ENDPOINT, "-e", "");
        builder.addString(HOSTPORT, "-h", "");

        if (!values.get(TESTSUITE).equals(ALL_VALUE)) {
            builder.addString(TESTSUITE, "-s", "");
        }

        if (!values.get(TESTCASE).equals(ALL_VALUE)) {
            builder.addString(TESTCASE, "-c", "");
        }

        builder.addString(USERNAME, "-u", "");
        builder.addStringShadow(PASSWORD, "-p", "");
        builder.addString(DOMAIN, "-d", "");
        builder.addString(WSSTYPE, "-w", "");

        builder.addBoolean(PRINTREPORT, "-r");
        builder.addBoolean(EXPORTALL, "-a");
        builder.addBoolean(EXPORTJUNITRESULTS, "-j");
        builder.addBoolean(EXPORTJUNITRESULTSWITHPROPERTIES, "-J");
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

    protected void addPropertyArguments(ArgumentBuilder builder) {
        List<String> propertyArguments = new ArrayList<String>();

        addProperties(propertyArguments, GLOBALPROPERTIES, "-G");
        addProperties(propertyArguments, SYSTEMPROPERTIES, "-D");
        addProperties(propertyArguments, PROJECTPROPERTIES, "-P");

        builder.addArgs(propertyArguments.toArray(new String[propertyArguments.size()]));
    }

    protected void addProperties(List<String> propertyArguments, String propertiyDomain, String arg) {
        StringTokenizer tokenizer = new StringTokenizer(getDialog().getValue(propertiyDomain));

        while (tokenizer.hasMoreTokens()) {
            propertyArguments.add(arg + tokenizer.nextToken());
        }
    }

    protected void initEnvironment(final WsdlProject modelItem) {
    }

}
