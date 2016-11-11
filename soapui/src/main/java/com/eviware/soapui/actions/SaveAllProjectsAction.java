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

package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action to save all projects
 *
 * @author ole.matzura
 */

public class SaveAllProjectsAction extends AbstractSoapUIAction<WorkspaceImpl> implements WorkspaceListener {
    public static final String SOAPUI_ACTION_ID = "SaveAllProjectsAction";

    public SaveAllProjectsAction() {
        super("Save All Projects", "Saves all projects in the current Workspace");

        Workspace workspace = SoapUI.getWorkspace();
        if (workspace == null) {
            setEnabled(true);
        } else {
            setEnabled(workspace.getProjectCount() > 0);
            workspace.addWorkspaceListener(this);
        }
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        workspace.save(false);
    }

    public void projectAdded(Project project) {
        setEnabled(true);
    }

    public void projectChanged(Project project) {
    }

    public void projectRemoved(Project project) {
        setEnabled(project.getWorkspace().getProjectCount() == 0);
    }

    public void workspaceSwitched(Workspace workspace) {
        setEnabled(workspace.getProjectCount() > 0);
    }

    public void workspaceSwitching(Workspace workspace) {
    }

    @Override
    public void projectClosed(Project project) {
    }

    @Override
    public void projectOpened(Project project) {
    }
}
