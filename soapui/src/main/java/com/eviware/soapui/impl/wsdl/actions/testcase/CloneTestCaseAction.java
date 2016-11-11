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

package com.eviware.soapui.impl.wsdl.actions.testcase;

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
 * Clones a WsdlTestSuite
 *
 * @author Ole.Matzura
 */

/*
 * There is a separate class for the pro version ProCloneTestCaseAction any
 * changes made here should reflect to that class too TODO refactor these two
 * classes so that only one class needs to be changed in case of changing the
 * core functionality
 */
public class CloneTestCaseAction extends AbstractSoapUIAction<WsdlTestCase> {
    private static final String CREATE_NEW_OPTION = "<Create New>";
    private XFormDialog dialog;

    public CloneTestCaseAction() {
        super("Clone TestCase", "Clones this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.getFormField(Form.PROJECT).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    if (newValue.equals(CREATE_NEW_OPTION)) {
                        dialog.setOptions(Form.TESTSUITE, new String[]{CREATE_NEW_OPTION});
                    } else {
                        Project project = SoapUI.getWorkspace().getProjectByName(newValue);
                        dialog.setOptions(Form.TESTSUITE,
                                ModelSupport.getNames(project.getTestSuiteList(), new String[]{CREATE_NEW_OPTION}));
                    }
                }
            });
            dialog.getFormField(Form.CLONE_DESCRIPTION).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    if (dialog.getBooleanValue(Form.CLONE_DESCRIPTION)) {
                        dialog.getFormField(Form.DESCRIPTION).setEnabled(false);
                    } else {
                        dialog.getFormField(Form.DESCRIPTION).setEnabled(true);
                    }

                }
            });
        }

        dialog.setBooleanValue(Form.MOVE, false);
        dialog.setBooleanValue(Form.CLONE_DESCRIPTION, true);
        dialog.getFormField(Form.DESCRIPTION).setEnabled(false);
        dialog.setValue(Form.DESCRIPTION, testCase.getDescription());
        dialog.setValue(Form.NAME, "Copy of " + testCase.getName());
        WorkspaceImpl workspace = testCase.getTestSuite().getProject().getWorkspace();
        dialog.setOptions(Form.PROJECT,
                ModelSupport.getNames(workspace.getOpenProjectList(), new String[]{CREATE_NEW_OPTION}));

        dialog.setValue(Form.PROJECT, testCase.getTestSuite().getProject().getName());

        dialog.setOptions(Form.TESTSUITE, ModelSupport.getNames(
                testCase.getTestSuite().getProject().getTestSuiteList(), new String[]{CREATE_NEW_OPTION}));

        dialog.setValue(Form.TESTSUITE, testCase.getTestSuite().getName());

        boolean hasLoadTests = testCase.getLoadTestCount() > 0;
        dialog.setBooleanValue(Form.CLONE_LOADTESTS, hasLoadTests);
        dialog.getFormField(Form.CLONE_LOADTESTS).setEnabled(hasLoadTests);

        boolean hasSecurityTests = testCase.getSecurityTestCount() > 0;
        dialog.setBooleanValue(Form.CLONE_SECURITYTESTS, hasSecurityTests);
        dialog.getFormField(Form.CLONE_SECURITYTESTS).setEnabled(hasSecurityTests);

        if (dialog.show()) {
            String targetProjectName = dialog.getValue(Form.PROJECT);
            String targetTestSuiteName = dialog.getValue(Form.TESTSUITE);
            String name = dialog.getValue(Form.NAME);

            WsdlProject project = testCase.getTestSuite().getProject();
            WsdlTestSuite targetTestSuite = null;
            Set<Interface> requiredInterfaces = new HashSet<Interface>();

            // to another project project?
            if (!targetProjectName.equals(project.getName())) {
                // get required interfaces
                for (int y = 0; y < testCase.getTestStepCount(); y++) {
                    WsdlTestStep testStep = testCase.getTestStepAt(y);
                    requiredInterfaces.addAll(testStep.getRequiredInterfaces());
                }

                project = (WsdlProject) workspace.getProjectByName(targetProjectName);
                if (project == null) {
                    targetProjectName = UISupport.prompt("Enter name for new Project", "Clone TestCase", "");
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
                    msg += "\r\nShould these will be cloned to the targetProject as well?";

                    Boolean result = UISupport.confirmOrCancel(msg, "Clone TestCase");
                    if (result == null) {
                        return;
                    }

                    if (result) {
                        for (Interface iface : requiredInterfaces) {
                            project.importInterface((AbstractInterface<?>) iface, true, true);
                        }
                    }
                }
            }

            targetTestSuite = project.getTestSuiteByName(targetTestSuiteName);
            if (targetTestSuite == null) {
                targetTestSuiteName = UISupport.prompt("Specify name for new TestSuite", "Clone TestCase", "Copy of "
                        + testCase.getTestSuite().getName());
                if (targetTestSuiteName == null) {
                    return;
                }

                targetTestSuite = project.addNewTestSuite(targetTestSuiteName);
            }

            boolean move = dialog.getBooleanValue(Form.MOVE);
            WsdlTestCase newTestCase = targetTestSuite.importTestCase(testCase, name, -1,
                    dialog.getBooleanValue(Form.CLONE_LOADTESTS), dialog.getBooleanValue(Form.CLONE_SECURITYTESTS),
                    !move);
            UISupport.select(newTestCase);

            if (move) {
                testCase.getTestSuite().removeTestCase(testCase);
            }
            boolean cloneDescription = dialog.getBooleanValue(Form.CLONE_DESCRIPTION);
            if (!cloneDescription) {
                newTestCase.setDescription(dialog.getValue(Form.DESCRIPTION));
            }
        }
    }

    @AForm(description = "Specify target Project/TestSuite and name of cloned TestCase", name = "Clone TestCase", helpUrl = HelpUrls.CLONETESTCASE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    protected interface Form {
        @AField(name = "TestCase Name", description = "The name of the cloned TestCase", type = AFieldType.STRING)
        public final static String NAME = "TestCase Name";

        @AField(name = "Target Project", description = "The target Project for the cloned TestCase", type = AFieldType.ENUMERATION)
        public final static String PROJECT = "Target Project";

        @AField(name = "Target TestSuite", description = "The target TestSuite for the cloned TestCase", type = AFieldType.ENUMERATION)
        public final static String TESTSUITE = "Target TestSuite";

        @AField(name = "Clone LoadTests", description = "Clone contained LoadTests", type = AFieldType.BOOLEAN)
        public final static String CLONE_LOADTESTS = "Clone LoadTests";

        @AField(name = "Clone SecurityTests", description = "Clone contained SecurityTests", type = AFieldType.BOOLEAN)
        public final static String CLONE_SECURITYTESTS = "Clone SecurityTests";

        @AField(name = "Move instead", description = "Moves the selected TestCase instead of copying", type = AFieldType.BOOLEAN)
        public final static String MOVE = "Move instead";

        @AField(name = "Clone description", description = "Clones the description of selected TestCase", type = AFieldType.BOOLEAN)
        public final static String CLONE_DESCRIPTION = "Clone description";

        @AField(name = "Description", description = "Description of new TestCase", type = AFieldType.STRINGAREA)
        public final static String DESCRIPTION = "Description";
    }
}
