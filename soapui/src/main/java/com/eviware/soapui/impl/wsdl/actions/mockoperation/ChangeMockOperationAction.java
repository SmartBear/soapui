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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

/**
 * Prompts to change the WsdlOperation of a WsdlMockOperation
 *
 * @author Ole.Matzura
 */

public class ChangeMockOperationAction extends AbstractSoapUIAction<WsdlMockOperation> {
    private XFormDialog dialog;
    private WsdlMockOperation testStep;

    public ChangeMockOperationAction() {
        super("Change Operation", "Changes the Interface Operation for this MockOperation");
    }

    public void perform(WsdlMockOperation target, Object param) {
        this.testStep = target;

        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.getFormField(Form.INTERFACE).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    WsdlProject project = testStep.getMockService().getProject();
                    dialog.setOptions(Form.OPERATION,
                            ModelSupport.getNames(project.getInterfaceByName(newValue).getOperationList()));
                    WsdlOperation operation = testStep.getOperation();
                    dialog.setValue(Form.OPERATION, operation == null ? "" : operation.getName());
                }
            });

            dialog.getFormField(Form.RECREATE_REQUEST).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    boolean enabled = Boolean.parseBoolean(newValue);

                    dialog.getFormField(Form.CREATE_OPTIONAL).setEnabled(enabled);
                    dialog.getFormField(Form.KEEP_EXISTING).setEnabled(enabled);
                }
            });

            dialog.getFormField(Form.CREATE_OPTIONAL).setEnabled(false);
            dialog.getFormField(Form.KEEP_EXISTING).setEnabled(false);
        }

        WsdlOperation operation = testStep.getOperation();
        WsdlProject project = testStep.getMockService().getProject();
        String[] interfaceNames = ModelSupport.getNames(project.getInterfaceList(),
                new ModelSupport.InterfaceTypeFilter(WsdlInterfaceFactory.WSDL_TYPE));
        dialog.setOptions(Form.INTERFACE, interfaceNames);
        dialog.setValue(Form.INTERFACE, operation == null ? interfaceNames[0] : operation.getInterface().getName());

        dialog.setOptions(Form.OPERATION,
                ModelSupport.getNames(project.getInterfaceByName(dialog.getValue(Form.INTERFACE)).getOperationList()));
        dialog.setValue(Form.OPERATION, operation == null ? null : operation.getName());
        dialog.setValue(Form.NAME, target.getName());

        if (dialog.show()) {
            String ifaceName = dialog.getValue(Form.INTERFACE);
            String operationName = dialog.getValue(Form.OPERATION);

            WsdlInterface iface = (WsdlInterface) project.getInterfaceByName(ifaceName);
            operation = iface.getOperationByName(operationName);
            target.setOperation(operation);

            String name = dialog.getValue(Form.NAME).trim();
            if (name.length() > 0 && !target.getName().equals(name)) {
                target.setName(name);
            }

            if (dialog.getBooleanValue(Form.RECREATE_REQUEST)) {
                String req = operation.createResponse(dialog.getBooleanValue(Form.CREATE_OPTIONAL));
                if (req == null) {
                    UISupport.showErrorMessage("Response creation failed");
                } else {
                    for (int c = 0; c < target.getMockResponseCount(); c++) {
                        String msg = req;
                        WsdlMockResponse mockResponse = target.getMockResponseAt(c);

                        if (dialog.getBooleanValue(Form.KEEP_EXISTING)) {
                            msg = XmlUtils.transferValues(mockResponse.getResponseContent(), req);
                        }

                        mockResponse.setResponseContent(msg);
                    }
                }
            }
        }
    }

    @AForm(description = "Specify Interface/Operation for MockOperation", name = "Change Operation", helpUrl = HelpUrls.CHANGEMOCKOPERATION_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    protected interface Form {
        @AField(name = "Name", description = "The Name of the MockOperation", type = AFieldType.STRING)
        public final static String NAME = "Name";

        @AField(name = "Interface", description = "The MockOperations Interface", type = AFieldType.ENUMERATION)
        public final static String INTERFACE = "Interface";

        @AField(name = "Operation", description = "The MockOperations Operation", type = AFieldType.ENUMERATION)
        public final static String OPERATION = "Operation";

        @AField(name = "Recreate Responses", description = "Recreates all MockResponses content from the new Operations Definition", type = AFieldType.BOOLEAN)
        public final static String RECREATE_REQUEST = "Recreate Responses";

        @AField(name = "Create Optional", description = "Creates optional content when recreating the response", type = AFieldType.BOOLEAN)
        public final static String CREATE_OPTIONAL = "Create Optional";

        @AField(name = "Keep Existing", description = "Tries to keep existing values when recreating the response", type = AFieldType.BOOLEAN)
        public final static String KEEP_EXISTING = "Keep Existing";
    }
}
