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

package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JTextFieldFormField;

public class AddEmptyRestMockResourceAction extends AbstractSoapUIAction<RestMockService> {
    public static final String SOAPUI_ACTION_ID = "AddEmptyRestMockResourceAction";

    public AddEmptyRestMockResourceAction() {
        super("Add new mock action", "Add a new REST mock action to this mock service");
    }


    @Override
    public void perform(RestMockService mockService, Object param) {
        XFormDialog dialog = ADialogBuilder.buildDialog(Form.class);
        dialog.setOptions(Form.HTTP_METHOD, RestRequestInterface.HttpMethod.getMethodsAsStringArray());
        dialog.setValue(Form.HTTP_METHOD, RestRequestInterface.HttpMethod.GET.name());

        JTextFieldFormField formField = (JTextFieldFormField) dialog.getFormField(Form.RESOURCE_PATH);
        formField.getComponent().requestFocus();

        while (dialog.show()) {
            String resourcePath = dialog.getValue(Form.RESOURCE_PATH);
            String httpMethod = dialog.getValue(Form.HTTP_METHOD);

            if (StringUtils.hasContent(resourcePath)) {
                mockService.addEmptyMockAction(RestRequestInterface.HttpMethod.valueOf(httpMethod), resourcePath);
                break;
            }
            UISupport.showInfoMessage("The resource path can not be empty");
        }
    }

    @AForm(name = "Add new mock action",
            description = "Enter path and HTTP method for your new mock action",
            helpUrl = HelpUrls.MOCKOPERATION_HELP_URL)
    public interface Form {
        @AField(description = "Select HTTP method", type = AField.AFieldType.COMBOBOX)
        public final static String HTTP_METHOD = "Method";

        @AField(description = "Enter resource path", type = AField.AFieldType.STRING)
        public final static String RESOURCE_PATH = "Resource path";
    }

}
