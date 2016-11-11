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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clones a WsdlTestStep
 *
 * @author Ole.Matzura
 */

public class CloneTestStepAction extends AbstractSoapUIAction<WsdlTestStep> {
    private static final String CREATE_NEW_OPTION = "<Create New>";
    private XFormDialog dialog;

    public CloneTestStepAction() {
        super("Clone TestStep", "Clones this TestStep");
    }

    public void perform(WsdlTestStep testStep, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.getFormField(Form.PROJECT).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    if (newValue.equals(CREATE_NEW_OPTION)) {
                        dialog.setOptions(Form.TESTSUITE, new String[]{CREATE_NEW_OPTION});
                        dialog.setOptions(Form.TESTCASE, new String[]{CREATE_NEW_OPTION});
                    } else {
                        Project project = SoapUI.getWorkspace().getProjectByName(newValue);
                        String[] names = ModelSupport.getNames(project.getTestSuiteList(),
                                new String[]{CREATE_NEW_OPTION});
                        dialog.setOptions(Form.TESTSUITE, names);
                        dialog.setValue(Form.TESTSUITE, names[0]);

                        if (names.length > 1) {
                            TestSuite testSuite = project.getTestSuiteByName(names[0]);
                            dialog.setOptions(Form.TESTCASE,
                                    ModelSupport.getNames(testSuite.getTestCaseList(), new String[]{CREATE_NEW_OPTION}));
                        } else {
                            dialog.setOptions(Form.TESTCASE, new String[]{CREATE_NEW_OPTION});
                        }
                    }
                }
            });

            dialog.getFormField(Form.TESTSUITE).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    if (newValue.equals(CREATE_NEW_OPTION)) {
                        dialog.setOptions(Form.TESTCASE, new String[]{CREATE_NEW_OPTION});
                    } else {
                        String projectName = dialog.getValue(Form.PROJECT);
                        Project project = SoapUI.getWorkspace().getProjectByName(projectName);
                        TestSuite testSuite = project.getTestSuiteByName(newValue);
                        dialog.setOptions(Form.TESTCASE, testSuite == null ? new String[]{CREATE_NEW_OPTION}
                                : ModelSupport.getNames(testSuite.getTestCaseList(), new String[]{CREATE_NEW_OPTION}));
                    }
                }
            });

        }

        dialog.setBooleanValue(Form.MOVE, false);
        dialog.setValue(Form.NAME, "Copy of " + testStep.getName());
        WorkspaceImpl workspace = testStep.getTestCase().getTestSuite().getProject().getWorkspace();
        dialog.setOptions(Form.PROJECT,
                ModelSupport.getNames(workspace.getOpenProjectList(), new String[]{CREATE_NEW_OPTION}));

        dialog.setValue(Form.PROJECT, testStep.getTestCase().getTestSuite().getProject().getName());

        dialog.setOptions(Form.TESTSUITE, ModelSupport.getNames(testStep.getTestCase().getTestSuite().getProject()
                .getTestSuiteList(), new String[]{CREATE_NEW_OPTION}));
        dialog.setValue(Form.TESTSUITE, testStep.getTestCase().getTestSuite().getName());

        dialog.setOptions(Form.TESTCASE, ModelSupport.getNames(testStep.getTestCase().getTestSuite().getTestCaseList(),
                new String[]{CREATE_NEW_OPTION}));
        dialog.setValue(Form.TESTCASE, testStep.getTestCase().getName());

        if (dialog.show()) {
            String targetProjectName = dialog.getValue(Form.PROJECT);
            String targetTestSuiteName = dialog.getValue(Form.TESTSUITE);
            String targetTestCaseName = dialog.getValue(Form.TESTCASE);
            String name = dialog.getValue(Form.NAME);

            WsdlProject project = testStep.getTestCase().getTestSuite().getProject();
            WsdlTestSuite targetTestSuite = null;
            WsdlTestCase targetTestCase = null;
            Set<Interface> requiredInterfaces = new HashSet<Interface>();

            // to another project project?
            if (!targetProjectName.equals(project.getName())) {
                // get required interfaces
                requiredInterfaces.addAll(testStep.getRequiredInterfaces());

                project = (WsdlProject) workspace.getProjectByName(targetProjectName);
                if (project == null) {
                    targetProjectName = UISupport.prompt("Enter name for new Project", "Clone TestStep", "");
                    if (targetProjectName == null) {
                        return;
                    }

                    try {
                        project = workspace.createProject(targetProjectName, null);
                    } catch (SoapUIException e) {
                        UISupport.showErrorMessage(e);
                    }

                    if (project == null) {
                        return;
                    }
                }

                if (requiredInterfaces.size() > 0 && project.getInterfaceCount() > 0) {
                    Map<String, Interface> bindings = new HashMap<String, Interface>();
                    for (Interface iface : requiredInterfaces) {
                        bindings.put(iface.getTechnicalId(), iface);
                    }

                    for (Interface iface : project.getInterfaceList()) {
                        bindings.remove(iface.getTechnicalId());
                    }

                    requiredInterfaces.retainAll(bindings.values());
                }

                if (requiredInterfaces.size() > 0) {
                    String msg = "Target project [" + targetProjectName + "] is missing required Interfaces;\r\n\r\n";
                    for (Interface iface : requiredInterfaces) {
                        msg += iface.getName() + " [" + iface.getTechnicalId() + "]\r\n";
                    }
                    msg += "\r\nThese will be cloned to the targetProject as well";

                    if (!UISupport.confirm(msg, "Clone TestStep")) {
                        return;
                    }

                    for (Interface iface : requiredInterfaces) {
                        project.importInterface((AbstractInterface<?>) iface, true, true);
                    }
                }
            }

            targetTestSuite = project.getTestSuiteByName(targetTestSuiteName);
            if (targetTestSuite == null) {
                targetTestSuiteName = UISupport.prompt("Specify name for new TestSuite", "Clone TestStep", "Copy of "
                        + testStep.getTestCase().getTestSuite().getName());
                if (targetTestSuiteName == null) {
                    return;
                }

                targetTestSuite = project.addNewTestSuite(targetTestSuiteName);
            }

            targetTestCase = targetTestSuite.getTestCaseByName(targetTestCaseName);
            if (targetTestCase == null) {
                targetTestCaseName = UISupport.prompt("Specify name for new TestCase", "Clone TestStep", "Copy of "
                        + testStep.getTestCase().getName());
                if (targetTestCaseName == null) {
                    return;
                }

                targetTestCase = targetTestSuite.addNewTestCase(targetTestCaseName);
            }

            boolean move = dialog.getBooleanValue(Form.MOVE);

            WsdlTestStep newTestStep = targetTestCase.importTestStep(testStep, name, -1, !move);
            if (newTestStep == null) {
                return;
            }

            if (dialog.getBooleanValue(Form.OPEN)) {
                UISupport.selectAndShow(newTestStep);
            } else {
                UISupport.select(newTestStep);
            }

            if (move) {
                testStep.getTestCase().removeTestStep(testStep);
            }
        }
    }

    @AForm(description = "Specify target Project/TestSuite/TestCase and name of cloned TestStep", name = "Clone TestStep", helpUrl = HelpUrls.CLONETESTSTEP_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    protected interface Form {
        @AField(name = "TestStep Name", description = "The name of the cloned TestStep", type = AFieldType.STRING)
        public final static String NAME = "TestStep Name";

        @AField(name = "Target Project", description = "The target Project for the cloned TestStep", type = AFieldType.ENUMERATION)
        public final static String PROJECT = "Target Project";

        @AField(name = "Target TestSuite", description = "The target TestSuite for the cloned TestStep", type = AFieldType.ENUMERATION)
        public final static String TESTSUITE = "Target TestSuite";

        @AField(name = "Target TestCase", description = "The target TestCase for the cloned TestStep", type = AFieldType.ENUMERATION)
        public final static String TESTCASE = "Target TestCase";

        @AField(name = "Move Instead", description = "Moves the selected TestStep instead of copying", type = AFieldType.BOOLEAN)
        public final static String MOVE = "Move Instead";

        @AField(name = "Open Editor", description = "Opens the editor for the cloned TestStep", type = AFieldType.BOOLEAN)
        public final static String OPEN = "Open Editor";

    }
}
