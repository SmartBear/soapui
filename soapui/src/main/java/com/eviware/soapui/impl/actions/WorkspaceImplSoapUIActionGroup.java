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

import com.eviware.soapui.actions.CloseOpenProjectsAction;
import com.eviware.soapui.actions.OpenClosedProjectsAction;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlProjects, returns different actions depending on if
 * the project is disabled or not.
 *
 * @author ole.matzura
 */

public class WorkspaceImplSoapUIActionGroup extends DefaultSoapUIActionGroup<WorkspaceImpl> {
    public WorkspaceImplSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    public SoapUIActionMappingList<WorkspaceImpl> getActionMappings(WorkspaceImpl workspace) {
        SoapUIActionMappingList<WorkspaceImpl> mappings = super.getActionMappings(workspace);

        SoapUIActionMapping<WorkspaceImpl> openMapping = mappings.getMapping(OpenClosedProjectsAction.SOAPUI_ACTION_ID);
        openMapping.setEnabled(false);
        SoapUIActionMapping<WorkspaceImpl> closeMapping = mappings.getMapping(CloseOpenProjectsAction.SOAPUI_ACTION_ID);
        closeMapping.setEnabled(false);

        for (Project project : workspace.getProjectList()) {
            if (project.isOpen()) {
                closeMapping.setEnabled(true);
                if (openMapping.isEnabled()) {
                    break;
                }
            } else if (!project.isDisabled()) {
                openMapping.setEnabled(true);
                if (closeMapping.isEnabled()) {
                    break;
                }
            }
        }

        return mappings;
    }
}
