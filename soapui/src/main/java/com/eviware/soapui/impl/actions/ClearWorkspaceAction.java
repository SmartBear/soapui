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
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action to clear the current workspace
 *
 * @author ole.matzura
 */

public class ClearWorkspaceAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "ClearWorkspaceAction";
    public static final MessageSupport messages = MessageSupport.getMessages(ClearWorkspaceAction.class);

    public ClearWorkspaceAction() {
        super(messages.get("ClearWorkspaceAction.Title"), messages.get("ClearWorkspaceAction.Description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        if (SoapUI.getTestMonitor().hasRunningTests()) {
            UISupport.showErrorMessage(messages.get("ClearWorkspaceAction.WhileTestsAreRunningError"));
            return;
        }

        if (!UISupport.confirm(messages.get("ClearWorkspaceAction.ConfirmQuestion"),
                messages.get("ClearWorkspaceAction.Title"))) {
            return;
        }

        if (SoapUI.getDesktop().closeAll()) {
            while (workspace.getProjectCount() > 0) {
                workspace.removeProject(workspace.getProjectAt(0));
            }
        }
    }
}
