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

package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

/**
 * Actions for importing an existing SoapUI project file into the current
 * workspace
 *
 * @author Ole.Matzura
 */

public class NewRestRequestAction extends AbstractSoapUIAction<RestMethod> {
    public static final String SOAPUI_ACTION_ID = "NewRestRequestAction";
    public static final MessageSupport messages = MessageSupport.getMessages(NewRestRequestAction.class);
    private XFormDialog dialog;

    public NewRestRequestAction() {
        super(messages.get("title"), messages.get("description"));
    }

    public void perform(RestMethod method, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        } else {
            dialog.setValue(Form.RESOURCENAME, "");
        }

        if (dialog.show()) {
            RestRequest request = method.addNewRequest(dialog.getValue(Form.RESOURCENAME));

            UISupport.select(request);

            if (dialog.getBooleanValue(Form.OPENSREQUEST)) {
                UISupport.showDesktopPanel(request);
            }

            Analytics.trackAction(SoapUIActions.CREATE_REQUEST.getActionName(), "RequestType", "REST");
        }
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(description = "Form.ResourceName.Description", type = AFieldType.STRING)
        public final static String RESOURCENAME = messages.get("Form.ResourceName.Label");

        @AField(description = "Form.OpenRequest.Description", type = AFieldType.BOOLEAN)
        public final static String OPENSREQUEST = messages.get("Form.OpenRequest.Label");
    }
}
