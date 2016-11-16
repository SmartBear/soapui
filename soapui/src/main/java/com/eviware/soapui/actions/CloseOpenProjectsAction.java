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
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to save all projects
 *
 * @author ole.matzura
 */

public class CloseOpenProjectsAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "CloseOpenProjectsAction";
    public static final String NAME = "Close All Open Projects";

    public CloseOpenProjectsAction() {
        super(NAME, "Closes all open projects in the current Workspace");
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        List<Project> openProjects = new ArrayList<Project>();
        for (Project project : workspace.getProjectList()) {
            if (project.isOpen()) {
                openProjects.add(project);
            }
        }

        if (openProjects.isEmpty()) {
            UISupport.showErrorMessage("No open projects in workspace");
            return;
        }

        Boolean coc = UISupport.confirmOrCancel("Save projects before closing?", getName());
        if (coc == null) {
            return;
        }

        for (Project project : openProjects) {
            try {
                if (coc) {
                    project.save();
                }

                workspace.closeProject(project);
            } catch (IOException e) {
                SoapUI.logError(e);
            }
        }
    }
}
