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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.actions.RestUriDialogHandler;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;

/**
 * Action for adding a new REST URI to a project.
 */

public class AddRestUriAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "AddRestUriAction";

    private static final MessageSupport messages = MessageSupport.getMessages(AddRestUriAction.class);


    private RestUriDialogHandler dialogBuilder = new RestUriDialogHandler();
    private RestServiceBuilder serviceBuilder = new RestServiceBuilder();

    public AddRestUriAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    public void perform(WsdlProject project, Object param) {
        XFormDialog dialog = dialogBuilder.buildDialog(messages);
        while (dialog.show()) {
            try {
                String uri = dialogBuilder.getUri();
                if (uri != null) {
                    serviceBuilder.createRestService(project, uri);
                }
                // If there is no exception or error we break out
                break;

            } catch (Exception ex) {
                UISupport.showErrorMessage(ex.getMessage());

                dialogBuilder.resetUriField();
            }
        }
    }


}
