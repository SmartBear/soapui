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

package com.eviware.soapui.model.workspace;

import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.model.project.Project;

/**
 * Listener for Workspace-related events
 *
 * @author Ole.Matzura
 */

public interface WorkspaceListener extends SoapUIListener {
    public void projectAdded(Project project);

    public void projectRemoved(Project project);

    public void projectChanged(Project project);

    public void projectOpened(Project project);

    public void projectClosed(Project project);

    public void workspaceSwitching(Workspace workspace);

    public void workspaceSwitched(Workspace workspace);
}
