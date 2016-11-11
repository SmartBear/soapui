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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.util.ArrayList;
import java.util.List;

public class SetMockOperationAction extends AbstractSoapUIAction<WsdlMockResponseTestStep> {
    private XFormDialog dialog;
    private WsdlProject project;

    public SetMockOperationAction() {
        super("Set MockOperation", "Sets which Operation to Mock");
    }

    public void perform(WsdlMockResponseTestStep mockResponseTestStep, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(CreateForm.class);
            dialog.getFormField(CreateForm.INTERFACE).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    updateOperations(newValue);
                }
            });
        }

        project = mockResponseTestStep.getTestCase().getTestSuite().getProject();
        List<Interface> interfaces = new ArrayList<Interface>();
        for (int c = 0; c < project.getInterfaceCount(); c++) {
            if (project.getInterfaceAt(c).getOperationCount() > 0) {
                interfaces.add(project.getInterfaceAt(c));
            }
        }

        dialog.setOptions(CreateForm.INTERFACE, new ModelItemNames<Interface>(interfaces).getNames());
        String ifaceName = mockResponseTestStep.getOperation().getInterface().getName();
        updateOperations(ifaceName);

        dialog.setValue(CreateForm.INTERFACE, ifaceName);
        dialog.setValue(CreateForm.OPERATION, mockResponseTestStep.getOperation().getName());

        if (dialog.show()) {
            mockResponseTestStep.setInterface(dialog.getValue(CreateForm.INTERFACE));
            mockResponseTestStep.setOperation(dialog.getValue(CreateForm.OPERATION));
        }
    }

    private void updateOperations(String interfaceName) {
        WsdlInterface iface = (WsdlInterface) project.getInterfaceByName(interfaceName);
        dialog.setOptions(CreateForm.OPERATION, new ModelItemNames<Operation>(iface.getOperationList()).getNames());
    }

    @AForm(description = "Set the Operation to mock (required for dispatch and validations)", name = "Set MockOperation", helpUrl = HelpUrls.SETMOCKOPERATION_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private interface CreateForm {
        @AField(description = "Specifies the operation to be mocked", name = "Operation", type = AFieldType.ENUMERATION)
        public final static String OPERATION = "Operation";

        @AField(description = "Specifies the interface containing the operation to be mocked", name = "Interface", type = AFieldType.ENUMERATION)
        public final static String INTERFACE = "Interface";
    }
}
