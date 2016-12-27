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

import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XForm.FieldType;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;

/**
 * Options dialog for testcases
 *
 * @author Ole.Matzura
 */

public class TestCaseOptionsAction extends AbstractSoapUIAction<WsdlTestCase> {
    private static final String KEEP_SESSION = "Session";
    private static final String FAIL_ON_ERROR = "Abort on Error";
    private static final String FAIL_TESTCASE_ON_ERROR = "Fail TestCase on error";
    private static final String DISCARD_OK_RESULTS = "Discard OK Results";
    private static final String SOCKET_TIMEOUT = "Socket timeout";
    private static final String SEARCH_PROPERTIES = "Search Properties";
    public static final String SOAPUI_ACTION_ID = "TestCaseOptionsAction";
    private static final String TESTCASE_TIMEOUT = "TestCase timeout";
    private static final String MAXRESULTS = "Max Results";
    private static final String WS_RM_ENABLED = "WS-RM Enabled";
    private static final String WS_RM_VERSION = "WS-RM Version";
    private static final String WS_RM_ACK_TO = "WS-RM Ack To";
    private static final String WS_RM_EXPIRES = "WS-RM Expires";
    private static final String AMF_LOGIN = "login";
    private static final String AMF_PASSWORD = "password";
    private static final String AMF_AUTHORISATION_ENABLE = "AMF Session";
    private static final String AMF_ENDPOINT = "endpoint";

    private XFormDialog dialog;
    private XForm form;
    private XForm amfForm;
    private XForm wsrmForm;

    public TestCaseOptionsAction() {
        super("Options", "Sets options for this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        if (dialog == null) {
            XFormDialogBuilder builder = XFormFactory.createDialogBuilder("TestCase Options");
            form = builder.createForm("Basic");
            form.addCheckBox(SEARCH_PROPERTIES, "Search preceding TestSteps for property values");
            form.addCheckBox(KEEP_SESSION, "Maintain HTTP session");
            form.addCheckBox(FAIL_ON_ERROR, "Abort test if an error occurs").addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    form.getFormField(FAIL_TESTCASE_ON_ERROR).setEnabled(!Boolean.parseBoolean(newValue));
                }
            });

            form.addCheckBox(FAIL_TESTCASE_ON_ERROR, "Fail TestCase if it has failed TestSteps");
            form.addCheckBox(DISCARD_OK_RESULTS, "Discards successful TestStep results to preserve memory");
            form.addTextField(SOCKET_TIMEOUT, "Socket timeout in milliseconds", FieldType.TEXT);
            form.addTextField(TESTCASE_TIMEOUT, "Timeout in milliseconds for entire TestCase", FieldType.TEXT);
            form.addTextField(MAXRESULTS, "Maximum number of TestStep results to keep in memory during a run",
                    FieldType.TEXT);

            wsrmForm = builder.createForm("WS-RM");
            wsrmForm.addCheckBox(WS_RM_ENABLED, "Use WS-Reliable Messaging");
            wsrmForm.addComboBox(WS_RM_VERSION, new String[]{WsrmVersionTypeConfig.X_1_0.toString(),
                    WsrmVersionTypeConfig.X_1_1.toString(), WsrmVersionTypeConfig.X_1_2.toString()},
                    "The  property for managing WS-RM version");
            wsrmForm.addTextField(WS_RM_ACK_TO, "Acknowledgments To", FieldType.TEXT);
            wsrmForm.addTextField(WS_RM_EXPIRES, "Expires after", FieldType.TEXT);

            amfForm = builder.createForm("AMF");
            amfForm.addCheckBox(AMF_AUTHORISATION_ENABLE, "Enable AMF Session").addFormFieldListener(
                    new AMFXFormFieldListener());
            amfForm.addTextField(AMF_ENDPOINT, "AMF Authorization endpoint", FieldType.TEXT);
            amfForm.addTextField(AMF_LOGIN, "AMF Authorization usernmae", FieldType.TEXT);
            amfForm.addTextField(AMF_PASSWORD, "AMF Authorization password", FieldType.PASSWORD);

            dialog = builder.buildDialog(builder.buildOkCancelHelpActions(HelpUrls.TESTCASEOPTIONS_HELP_URL),
                    "Specify general options for this TestCase", UISupport.OPTIONS_ICON);
        }

        StringToStringMap values = new StringToStringMap();

        values.put(SEARCH_PROPERTIES, String.valueOf(testCase.getSearchProperties()));
        values.put(KEEP_SESSION, String.valueOf(testCase.getKeepSession()));
        values.put(FAIL_ON_ERROR, String.valueOf(testCase.getFailOnError()));
        values.put(FAIL_TESTCASE_ON_ERROR, String.valueOf(testCase.getFailTestCaseOnErrors()));
        values.put(DISCARD_OK_RESULTS, String.valueOf(testCase.getDiscardOkResults()));
        values.put(SOCKET_TIMEOUT, String.valueOf(testCase.getSettings().getString(HttpSettings.SOCKET_TIMEOUT, "")));
        values.put(TESTCASE_TIMEOUT, String.valueOf(testCase.getTimeout()));
        values.put(MAXRESULTS, String.valueOf(testCase.getMaxResults()));

        values.put(WS_RM_ENABLED, String.valueOf(testCase.getWsrmEnabled()));
        values.put(WS_RM_VERSION, String.valueOf(testCase.getWsrmVersion()));
        if (testCase.getWsrmAckTo() != null) {
            values.put(WS_RM_ACK_TO, String.valueOf(testCase.getWsrmAckTo()));
        }
        if (testCase.getWsrmExpires() != 0) {
            values.put(WS_RM_EXPIRES, String.valueOf(testCase.getWsrmExpires()));
        }

        values.put(AMF_AUTHORISATION_ENABLE, String.valueOf(testCase.getAmfAuthorisation()));
        values.put(AMF_ENDPOINT, String.valueOf(testCase.getAmfEndpoint()));
        values.put(AMF_LOGIN, String.valueOf(testCase.getAmfLogin()));
        values.put(AMF_PASSWORD, String.valueOf(testCase.getAmfPassword()));

        dialog.getFormField(FAIL_TESTCASE_ON_ERROR).setEnabled(
                !Boolean.parseBoolean(String.valueOf(testCase.getFailOnError())));

        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                testCase.setSearchProperties(Boolean.parseBoolean(values.get(SEARCH_PROPERTIES)));
                testCase.setKeepSession(Boolean.parseBoolean(values.get(KEEP_SESSION)));
                testCase.setDiscardOkResults(Boolean.parseBoolean(values.get(DISCARD_OK_RESULTS)));
                testCase.setFailOnError(Boolean.parseBoolean(values.get(FAIL_ON_ERROR)));
                testCase.setFailTestCaseOnErrors(Boolean.parseBoolean(values.get(FAIL_TESTCASE_ON_ERROR)));
                testCase.setTimeout(Long.parseLong(values.get(TESTCASE_TIMEOUT)));
                testCase.setMaxResults(Integer.parseInt(values.get(MAXRESULTS)));
                testCase.setWsrmEnabled(Boolean.parseBoolean(values.get(WS_RM_ENABLED)));
                testCase.setWsrmVersion(values.get(WS_RM_VERSION));
                testCase.setWsrmAckTo(values.get(WS_RM_ACK_TO));
                if (values.get(WS_RM_EXPIRES) != null && values.get(WS_RM_EXPIRES).length() > 0) {
                    testCase.setWsrmExpires(Long.parseLong(values.get(WS_RM_EXPIRES)));
                }

                String timeout = values.get(SOCKET_TIMEOUT);
                if (timeout.trim().length() == 0) {
                    testCase.getSettings().clearSetting(HttpSettings.SOCKET_TIMEOUT);
                } else {
                    testCase.getSettings().setString(HttpSettings.SOCKET_TIMEOUT, timeout);
                }

                testCase.setAmfAuthorisation(Boolean.parseBoolean(values.get(AMF_AUTHORISATION_ENABLE)));
                testCase.setAmfEndpoint(values.get(AMF_ENDPOINT));
                testCase.setAmfLogin(values.get(AMF_LOGIN));
                testCase.setAmfPassword(values.get(AMF_PASSWORD));
            } catch (Exception e1) {
                UISupport.showErrorMessage(e1.getMessage());
            }
        }
    }

    private class AMFXFormFieldListener implements XFormFieldListener {

        public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
            amfForm.getFormField(AMF_ENDPOINT).setEnabled(Boolean.parseBoolean(newValue));
            amfForm.getFormField(AMF_LOGIN).setEnabled(Boolean.parseBoolean(newValue));
            amfForm.getFormField(AMF_PASSWORD).setEnabled(Boolean.parseBoolean(newValue));
        }

    }
}
