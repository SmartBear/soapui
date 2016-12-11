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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Action class to create new REST project.
 *
 * @author Shadid Chowdhury
 */

public class NewRestProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "NewRestProjectAction";

    private static final String DEFAULT_PROJECT_NAME = "REST Project";
    private static final MessageSupport messages = MessageSupport.getMessages(NewRestProjectAction.class);


    private RestUriDialogHandler dialogBuilder = new RestUriDialogHandler();
    private XFormDialog dialog;
    private RestServiceBuilder serviceBuilder = new RestServiceBuilder();


    public NewRestProjectAction() {
        super(messages.get("Title"), messages.get("Description"));
    }


    public void perform(WorkspaceImpl workspace, Object param) {
        dialog = dialogBuilder.buildDialog(messages, new AbstractAction("Import WADL...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                SoapUI.getActionRegistry().getAction(NewWadlProjectAction.SOAPUI_ACTION_ID).perform(SoapUI.getWorkspace(), null);
                Analytics.trackAction(SoapUIActions.IMPORT_WADL.getActionName());
            }
        });
        while (dialog.show()) {
            WsdlProject project = null;
            try {
                String uri = dialogBuilder.getUri();
                if (uri != null) {
                    project = workspace.createProject(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), null);
                    serviceBuilder.createRestService(project, uri);
                }
                // If there is no exception or error we break out

                Analytics.trackAction(SoapUIActions.CREATE_REST_PROJECT.getActionName());
                break;

            } catch (Exception ex) {
                UISupport.showErrorMessage(ex.getMessage());
                if (project != null) {
                    workspace.removeProject(project);
                }
                dialogBuilder.resetUriField();
            }
        }
    }


}
