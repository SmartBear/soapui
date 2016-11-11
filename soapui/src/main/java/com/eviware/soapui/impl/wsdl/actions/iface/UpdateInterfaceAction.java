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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.components.ModelItemListDesktopPanel;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Updates the definition of a WsdlInterface.
 *
 * @author Ole.Matzura
 */

public class UpdateInterfaceAction extends AbstractSoapUIAction<WsdlInterface> {
    public static final String SOAPUI_ACTION_ID = "UpdateInterfaceAction";
    private XFormDialog dialog = null;

    public UpdateInterfaceAction() {
        this("Update Definition", "Reloads the definition for this interface and its operations");
    }

    protected UpdateInterfaceAction(String name, String description) {
        super(name, description);
    }

    public void perform(WsdlInterface iface, Object param) {
        if (RemoveInterfaceAction.hasRunningDependingTests(iface)) {
            UISupport.showErrorMessage("Cannot update Interface due to running depending tests");
            return;
        }

        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.setBooleanValue(Form.CREATE_REQUESTS, true);
            dialog.getFormField(Form.CREATE_BACKUPS).setEnabled(false);
            dialog.getFormField(Form.RECREATE_OPTIONAL).setEnabled(false);
            dialog.getFormField(Form.KEEP_EXISTING).setEnabled(false);
            dialog.getFormField(Form.RECREATE_REQUESTS).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    boolean enabled = dialog.getBooleanValue(Form.RECREATE_REQUESTS);

                    dialog.getFormField(Form.CREATE_BACKUPS).setEnabled(enabled);
                    dialog.getFormField(Form.RECREATE_OPTIONAL).setEnabled(enabled);
                    dialog.getFormField(Form.KEEP_EXISTING).setEnabled(enabled);
                }
            });
        }

        dialog.setValue(Form.DEFINITION_URL, iface.getDefinition());
        dialog.getFormField(Form.DEFINITION_URL).setToolTip(PathUtils.expandPath(iface.getDefinition(), iface));
        if (!dialog.show()) {
            return;
        }

        String url = dialog.getValue(Form.DEFINITION_URL);
        if (url == null || url.trim().length() == 0) {
            return;
        }

        String expUrl = PathUtils.expandPath(url, iface);
        if (expUrl.trim().length() == 0) {
            return;
        }

        try {
            File file = new File(expUrl);
            if (file.exists()) {
                expUrl = file.toURI().toURL().toString();
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        boolean createRequests = dialog.getBooleanValue(Form.CREATE_REQUESTS);

        try {
            UISupport.setHourglassCursor();
            if (iface.updateDefinition(expUrl, createRequests)) {
                afterUpdate(iface);
                if (!url.equals(expUrl)) {
                    iface.setDefinition(url, false);
                }
            } else {
                UISupport.showInfoMessage("Update of interface failed", "Update Definition");
            }
        } catch (Exception e1) {
            UISupport.showInfoMessage("Failed to update interface: [" + e1 + "]", "Update Definition");
            SoapUI.logError(e1);
        } finally {
            UISupport.resetCursor();
        }
    }

    protected void afterUpdate(WsdlInterface iface) throws Exception {
        if (dialog.getBooleanValue(Form.RECREATE_REQUESTS)) {
            boolean buildOptional = dialog.getBooleanValue(Form.RECREATE_OPTIONAL);
            boolean createBackups = dialog.getBooleanValue(Form.CREATE_BACKUPS);
            boolean keepExisting = dialog.getBooleanValue(Form.KEEP_EXISTING);
            boolean keepHeaders = dialog.getBooleanValue(Form.KEEP_HEADERS);

            List<ModelItem> updated = new ArrayList<ModelItem>();

            updated.addAll(recreateRequests(iface, buildOptional, createBackups, keepExisting, keepHeaders));

            if (dialog.getBooleanValue(Form.UPDATE_TESTREQUESTS)) {
                updated.addAll(recreateTestRequests(iface, buildOptional, createBackups, keepExisting, keepHeaders));
            }

            UISupport.showInfoMessage("Update of interface successfull, [" + updated.size()
                    + "] Requests/TestRequests have" + " been updated.", "Update Definition");

            if (dialog.getBooleanValue(Form.OPEN_LIST)) {
                UISupport
                        .showDesktopPanel(new ModelItemListDesktopPanel("Updated Requests/TestRequests",
                                "The following Request/TestRequests where updated", updated.toArray(new ModelItem[updated
                                .size()])));
            }
        } else {
            UISupport.showInfoMessage("Update of interface successful", "Update Definition");
        }
    }

    public static List<Request> recreateRequests(WsdlInterface iface, boolean buildOptional, boolean createBackups,
                                                 boolean keepExisting, boolean keepHeaders) {
        int count = 0;

        List<Request> result = new ArrayList<Request>();

        // first check operations
        for (int c = 0; c < iface.getOperationCount(); c++) {
            WsdlOperation operation = iface.getOperationAt(c);
            String newRequest = operation.createRequest(buildOptional);
            List<Request> requests = operation.getRequestList();

            for (Request request : requests) {
                String requestContent = request.getRequestContent();

                if (keepHeaders) {
                    newRequest = SoapUtils.transferSoapHeaders(requestContent, newRequest, iface.getSoapVersion());
                }

                String req = XmlUtils.transferValues(requestContent, newRequest);

                // changed?
                if (!req.equals(requestContent)) {
                    if (!XmlUtils.prettyPrintXml(req).equals(XmlUtils.prettyPrintXml(requestContent))) {
                        if (createBackups) {
                            WsdlRequest backupRequest = operation.addNewRequest("Backup of [" + request.getName() + "]");
                            ((WsdlRequest) request).copyTo(backupRequest, false, false);
                        }

                        ((WsdlRequest) request).setRequestContent(req);
                        count++;

                        result.add(request);
                    }
                }
            }
        }

        return result;
    }

    public static List<WsdlTestRequestStep> recreateTestRequests(WsdlInterface iface, boolean buildOptional,
                                                                 boolean createBackups, boolean keepExisting, boolean keepHeaders) {
        int count = 0;

        List<WsdlTestRequestStep> result = new ArrayList<WsdlTestRequestStep>();

        // now check testsuites..
        for (TestSuite testSuite : iface.getProject().getTestSuiteList()) {
            for (TestCase testCase : testSuite.getTestCaseList()) {
                int testStepCount = testCase.getTestStepCount();
                for (int c = 0; c < testStepCount; c++) {
                    WsdlTestStep testStep = (WsdlTestStep) testCase.getTestStepAt(c);
                    if (testStep instanceof WsdlTestRequestStep) {
                        WsdlTestRequest testRequest = ((WsdlTestRequestStep) testStep).getTestRequest();
                        if (testRequest != null && testRequest.getOperation() != null
                                && testRequest.getOperation().getInterface() == iface) {
                            String newRequest = testRequest.getOperation().createRequest(buildOptional);

                            if (keepHeaders) {
                                newRequest = SoapUtils.transferSoapHeaders(testRequest.getRequestContent(), newRequest,
                                        iface.getSoapVersion());
                            }

                            if (keepExisting) {
                                newRequest = XmlUtils.transferValues(testRequest.getRequestContent(), newRequest);
                            }

                            // changed?
                            if (!newRequest.equals(testRequest.getRequestContent())) {
                                if (createBackups) {
                                    ((WsdlTestCase) testCase).importTestStep(testStep,
                                            "Backup of [" + testStep.getName() + "]", -1, true).setDisabled(true);
                                }

                                ((WsdlRequest) testRequest).setRequestContent(newRequest);
                                count++;

                                result.add((WsdlTestRequestStep) testStep);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @AForm(description = "Specify Update Definition options", name = "Update Definition", helpUrl = HelpUrls.UPDATE_INTERFACE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    protected interface Form {
        @AField(name = "Definition URL", description = "The URL or file for the updated definition", type = AFieldType.FILE)
        public final static String DEFINITION_URL = "Definition URL";

        @AField(name = "Create New Requests", description = "Create default requests for new methods", type = AFieldType.BOOLEAN)
        public final static String CREATE_REQUESTS = "Create New Requests";

        @AField(name = "Recreate Requests", description = "Recreate existing request with the new schema", type = AFieldType.BOOLEAN)
        public final static String RECREATE_REQUESTS = "Recreate Requests";

        @AField(name = "Recreate Optional", description = "Recreate optional content when updating requests", type = AFieldType.BOOLEAN)
        public final static String RECREATE_OPTIONAL = "Recreate Optional";

        @AField(name = "Keep Existing", description = "Keeps existing values when recreating requests", type = AFieldType.BOOLEAN)
        public final static String KEEP_EXISTING = "Keep Existing";

        @AField(name = "Keep SOAP Headers", description = "Keeps any SOAP Headers when recreating requests", type = AFieldType.BOOLEAN)
        public final static String KEEP_HEADERS = "Keep SOAP Headers";

        @AField(name = "Create Backups", description = "Create backup copies of changed requests", type = AFieldType.BOOLEAN)
        public final static String CREATE_BACKUPS = "Create Backups";

        @AField(name = "Update TestRequests", description = "Updates all TestRequests for operations in this Interface also", type = AFieldType.BOOLEAN)
        public final static String UPDATE_TESTREQUESTS = "Update TestRequests";

        @AField(name = "Open Request List", description = "Opens a list of all requests that have been updated", type = AFieldType.BOOLEAN)
        public final static String OPEN_LIST = "Open Request List";
    }
}
