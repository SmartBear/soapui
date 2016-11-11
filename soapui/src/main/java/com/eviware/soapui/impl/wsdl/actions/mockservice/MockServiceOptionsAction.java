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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

/**
 * Displays the options for the specified WsdlMockService
 *
 * @author ole.matzura
 */

public class MockServiceOptionsAction extends AbstractSoapUIAction<WsdlMockService> {
    private XFormDialog dialog;

    public MockServiceOptionsAction() {
        super("Options", "Sets options for this MockService");
    }

    public void perform(WsdlMockService mockService, Object param) {
        if (mockService.getMockRunner() != null && mockService.getMockRunner().isRunning()) {
            UISupport.showErrorMessage("Can not set MockService options while running");
            return;
        }

        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(OptionsForm.class);
        }

        dialog.setValue(OptionsForm.PATH, mockService.getPath());
        dialog.setValue(OptionsForm.HOST, mockService.getHost());
        dialog.setIntValue(OptionsForm.PORT, mockService.getPort());
        dialog.setBooleanValue(OptionsForm.HOSTONLY, mockService.getBindToHostOnly());
        dialog.setValue(OptionsForm.DOCROOT, mockService.getDocroot());
        dialog.setOptions(OptionsForm.FAULT_OPERATION,
                ModelSupport.getNames(new String[]{"- none -"}, mockService.getMockOperationList()));
        dialog.setValue(OptionsForm.FAULT_OPERATION, String.valueOf(mockService.getFaultMockOperation()));

        if (dialog.show()) {
            mockService.setPath(dialog.getValue(OptionsForm.PATH));
            mockService.setPort(dialog.getIntValue(OptionsForm.PORT, mockService.getPort()));
            mockService.setHost(dialog.getValue(OptionsForm.HOST));
            mockService.setBindToHostOnly(dialog.getBooleanValue(OptionsForm.HOSTONLY));
            mockService.setDocroot(dialog.getValue(OptionsForm.DOCROOT));
            mockService.setFaultMockOperation((WsdlMockOperation) mockService.getMockOperationByName(dialog
                    .getValue(OptionsForm.FAULT_OPERATION)));
        }
    }

    @AForm(name = "SOAP MockService Options", description = "Set options for this SOAP mock service", helpUrl = HelpUrls.MOCKSERVICEOPTIONS_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH)
    private class OptionsForm {
        @AField(name = "Path", description = "The path this MockService will mount on")
        public final static String PATH = "Path";

        @AField(name = "Port", description = "The port this MockService will mount on", type = AFieldType.INT)
        public final static String PORT = "Port";

        @AField(name = "Host", description = "The local host to bind to and use in Port endpoints")
        public final static String HOST = "Host";

        @AField(name = "Host Only", description = "Only binds to specified host", type = AFieldType.BOOLEAN)
        public final static String HOSTONLY = "Host Only";

        @AField(name = "Docroot", description = "The document root to serve (empty = none)", type = AFieldType.FOLDER)
        public final static String DOCROOT = "Docroot";

        @AField(name = "Fault Operation", description = "The MockOperation that should handle incoming SOAP Faults", type = AFieldType.ENUMERATION)
        public final static String FAULT_OPERATION = "Fault Operation";
    }
}
