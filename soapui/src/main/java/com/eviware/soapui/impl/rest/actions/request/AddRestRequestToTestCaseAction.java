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

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.request.AbstractAddRequestToTestCaseAction;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;

/**
 * Adds a WsdlRequest to a WsdlTestCase as a WsdlTestRequestStep
 *
 * @author Ole.Matzura
 */

public class AddRestRequestToTestCaseAction extends AbstractAddRequestToTestCaseAction<RestRequest> {
    public static final String SOAPUI_ACTION_ID = "AddRestRequestToTestCaseAction";

    private static final String STEP_NAME = "Name";
    private static final String CLOSE_REQUEST = "Close Request Window";
    private static final String SHOW_TESTCASE = "Shows TestCase Editor";
    private static final String SHOW_REQUEST = "Shows Request Editor";

    private XFormDialog dialog;
    private StringToStringMap dialogValues = new StringToStringMap();
    private XFormField closeRequestCheckBox;

    public AddRestRequestToTestCaseAction() {
        super("Add to TestCase", "Adds this REST Request to a TestCase");
    }

    public void perform(RestRequest request, Object param) {
        WsdlProject project = request.getOperation().getInterface().getProject();

        WsdlTestCase testCase = getTargetTestCase(project);
        if (testCase != null) {
            addRequest(testCase, request, -1);
        }
    }

    public boolean addRequest(TestCase testCase, RestRequest request, int position) {
        if (dialog == null) {
            buildDialog();
        }

        dialogValues.put(STEP_NAME, request.getRestMethod().getName() + " - " + request.getName());
        dialogValues.put(CLOSE_REQUEST, "true");
        dialogValues.put(SHOW_TESTCASE, "true");
        dialogValues.put(SHOW_REQUEST, "true");

        SoapUIDesktop desktop = SoapUI.getDesktop();
        closeRequestCheckBox.setEnabled(desktop != null && desktop.hasDesktopPanel(request));

        dialogValues = dialog.show(dialogValues);
        if (dialog.getReturnValue() != XFormDialog.OK_OPTION) {
            return false;
        }

        String name = dialogValues.get(STEP_NAME);

        RestTestRequestStep testStep = (RestTestRequestStep) testCase.insertTestStep(
                RestRequestStepFactory.createConfig(request, name), position);

        if (testStep == null) {
            return false;
        }

        if (dialogValues.getBoolean(CLOSE_REQUEST) && desktop != null) {
            desktop.closeDesktopPanel(request);
        }

        if (dialogValues.getBoolean(SHOW_TESTCASE)) {
            UISupport.selectAndShow(testCase);
        }

        if (dialogValues.getBoolean(SHOW_REQUEST)) {
            UISupport.selectAndShow(testStep);
        }

        return true;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add Request to TestCase");
        XForm mainForm = builder.createForm("Basic");

        mainForm.addTextField(STEP_NAME, "Name of TestStep", XForm.FieldType.URL).setWidth(30);

        closeRequestCheckBox = mainForm.addCheckBox(CLOSE_REQUEST, "(closes the current window for this request)");
        mainForm.addCheckBox(SHOW_TESTCASE, "(opens the TestCase editor for the target TestCase)");
        mainForm.addCheckBox(SHOW_REQUEST, "(opens the Request editor for the created TestStep)");

        dialog = builder.buildDialog(builder.buildOkCancelActions(),
                "Specify options for adding the request to a TestCase", UISupport.OPTIONS_ICON);
    }
}
