/*
 * Copyright 2004-2014 SmartBear Software
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
package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

public class RestMockServiceOptionsAction extends AbstractSoapUIAction<RestMockService> {
    private XFormDialog dialog;

    public RestMockServiceOptionsAction() {
        super("Options", "Sets options for this MockService");
    }

    public void perform(RestMockService mockService, Object param) {
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
        dialog.setValue(OptionsForm.DOCROOT, mockService.getDocroot());

        if (dialog.show()) {
            mockService.setPath(dialog.getValue(OptionsForm.PATH));
            mockService.setPort(dialog.getIntValue(OptionsForm.PORT, mockService.getPort()));
            mockService.setHost(dialog.getValue(OptionsForm.HOST));
            mockService.setDocroot(dialog.getValue(OptionsForm.DOCROOT));
        }
    }

    @AForm(name = "REST MockService Options", description = "Set options for this REST mock service", helpUrl = HelpUrls.REST_MOCKSERVICE_OPTIONS, icon = UISupport.OPTIONS_ICON_PATH)
    private class OptionsForm {
        @AField(name = "Path", description = "The path this MockService will mount on")
        public final static String PATH = "Path";

        @AField(name = "Port", description = "The port this MockService will mount on", type = AField.AFieldType.INT)
        public final static String PORT = "Port";

        @AField(name = "Host", description = "The local host to bind to and use in Port endpoints")
        public final static String HOST = "Host";

        @AField(name = "Host Only", description = "Only binds to specified host", type = AField.AFieldType.BOOLEAN)
        public final static String HOSTONLY = "Host Only";

        @AField(name = "Docroot", description = "The document root to serve (empty = none)", type = AField.AFieldType.FOLDER)
        public final static String DOCROOT = "Docroot";
    }

}
