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

import javax.swing.Action;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Invokes SoapUI Security TestRunner tool
 *
 * @author nebojsa.tasic
 */
@Deprecated
// use com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SecurityTestRunnerAction instead
public class SecurityTestRunnerAction extends AbstractToolsAction<WsdlProject> {
    private static final String SH = ".sh";
    private static final String BAT = ".bat";
    private static final String SECURITYTESTRUNNER = "securitytestrunner";
    private static final String ALL_VALUE = "<all>";
    private static final String TESTSUITE = "TestSuite";
    private static final String TESTCASE = "TestCase";
    private static final String TESTRUNNERPATH = "Security TestRunner Path";
    private static final String SECURITY_TEST_NAME = "SecurityTestName";
    private static final String SAVEPROJECT = "Save Project";

    private XForm mainForm;

    private final static Logger log = LogManager.getLogger(SecurityTestRunnerAction.class);

    public static final String SOAPUI_ACTION_ID = "SecurityTestRunnerAction";

    private List<TestSuite> testSuites;

    public SecurityTestRunnerAction() {
        super("Launch SecurityTestRunner", "Launch command-line SecurityTestRunner for this project");
    }

    protected XFormDialog buildDialog(WsdlProject modelItem) {
        if (modelItem == null) {
            return null;
        }

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Launch Security TestRunner");

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

        mainForm.addComboBox(TESTCASE, new String[]{}, "TestCase").addFormFieldListener(new XFormFieldListener() {

            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                List<String> securityTests = new ArrayList<String>();
                String st = mainForm.getComponentValue(SECURITY_TEST_NAME);

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
                mainForm.setOptions(SECURITY_TEST_NAME, securityTests.toArray());

                if (securityTests.contains(st)) {
                    mainForm.getFormField(SECURITY_TEST_NAME).setValue(st);
                }
            }
        });

        mainForm.addComboBox(SECURITY_TEST_NAME, new String[]{}, "The Security Test to run");
        mainForm.addCheckBox(SAVEPROJECT, "Saves project before running").setEnabled(!modelItem.isRemote());
        mainForm.addSeparator();
        mainForm.addTextField(TESTRUNNERPATH, "Folder containing SecurityTestRunner.bat to use", XForm.FieldType.FOLDER);

        setToolsSettingsAction(null);
        buildArgsForm(builder, false, "TestRunner");

        return builder.buildDialog(buildDefaultActions(HelpUrls.TESTRUNNER_SECURITY_HELP_URL, modelItem),
                "Specify arguments for launching SoapUI Security TestRunner", UISupport.TOOL_ICON);
    }

    protected XForm buildArgsForm(XFormDialogBuilder builder, boolean addJavaArgs, String toolName) {
        return null;
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

            mainForm.getComponent(SAVEPROJECT).setEnabled(!modelItem.isRemote());
        }

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
            log.debug("Launching security testrunner in directory [" + builder.directory() + "] with arguments ["
                    + args.toString() + "]");
        }

        toolHost.run(new ProcessToolRunner(builder, "SoapUI Security TestRunner", modelItem, args));
    }

    private ArgumentBuilder buildArgs(WsdlProject modelItem) throws IOException {
        XFormDialog dialog = getDialog();
        if (dialog == null) {
            ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
            builder.startScript(SECURITYTESTRUNNER, BAT, SH);
            return builder;
        }

        StringToStringMap values = dialog.getValues();

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript(SECURITYTESTRUNNER, BAT, SH);

        if (!values.get(TESTSUITE).equals(ALL_VALUE)) {
            builder.addString(TESTSUITE, "-s", "");
        }

        if (!values.get(TESTCASE).equals(ALL_VALUE)) {
            builder.addString(TESTCASE, "-c", "");
        }

        if (!values.get(SECURITY_TEST_NAME).equals(ALL_VALUE)) {
            builder.addString(SECURITY_TEST_NAME, "-n", "");
        }

        builder.addArgs(new String[]{modelItem.getPath()});

        addToolArgs(values, builder);

        return builder;
    }
}
