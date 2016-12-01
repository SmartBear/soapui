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
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

/**
 * Action to swtich the current workspace
 *
 * @author ole.matzura
 */

public class SwitchWorkspaceAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "SwitchWorkspaceAction";
    public static final MessageSupport messages = MessageSupport.getMessages(SwitchWorkspaceAction.class);

    public SwitchWorkspaceAction() {
        super(messages.get("SwitchWorkspaceAction.Title"), messages.get("SwitchWorkspaceAction.Description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        if (SoapUI.getTestMonitor().hasRunningTests()) {
            UISupport.showErrorMessage(messages.get("SwitchWorkspaceAction.WhileTestsAreRunningError"));
            return;
        }

        File newPath = null;

        if (param != null) {
            newPath = new File(param.toString());
        } else {
            newPath = UISupport.getFileDialogs().open(this, messages.get("SwitchWorkspaceAction.FileOpenTitle"),
                    ".xml", "SoapUI Workspace (*.xml)", workspace.getPath());
        }

        if (newPath != null) {
            if (SoapUI.getDesktop().closeAll()) {
                boolean save = true;

                if (!newPath.exists()) {
                    if (!UISupport.confirm(messages.get("SwitchWorkspaceAction.Confirm.Label", newPath.getName()),
                            messages.get("SwitchWorkspaceAction.Confirm.Title"))) {
                        return;
                    }

                    save = false;
                } else if (workspace.getOpenProjectList().size() > 0) {
                    Boolean val = UISupport.confirmOrCancel(messages.get("SwitchWorkspaceAction.SaveOpenProjects.Label"),
                            messages.get("SwitchWorkspaceAction.SaveOpenProjects.Title"));
                    if (val == null) {
                        return;
                    }

                    save = val.booleanValue();
                }

                workspace.save(!save);

                try {
                    workspace.switchWorkspace(newPath);
                    SoapUI.getSettings().setString(SoapUI.CURRENT_SOAPUI_WORKSPACE, newPath.getAbsolutePath());
                    UISupport.select(workspace);
                } catch (SoapUIException e) {
                    UISupport.showErrorMessage(e);
                }
            }
        }
    }
}
