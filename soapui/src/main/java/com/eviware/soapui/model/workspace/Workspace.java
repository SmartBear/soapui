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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.support.SoapUIException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * SoapUI workspace behaviour
 *
 * @author Ole.Matzura
 */

public interface Workspace extends ModelItem {
    public Project getProjectAt(int index);

    public Project getProjectByName(String projectName);

    public int getProjectCount();

    public SaveStatus onClose();

    public SaveStatus save(boolean workspaceOnly);

    public void addWorkspaceListener(WorkspaceListener listener);

    public void removeWorkspaceListener(WorkspaceListener listener);

    public Project createProject(String name, File file) throws SoapUIException;

    public void removeProject(Project project);

    public Project importProject(String filename) throws SoapUIException;

    public Project importProject(InputStream inputStream);

    public int getIndexOfProject(Project project);

    public String getPath();

    public List<? extends Project> getProjectList();

    public void switchWorkspace(File newPath) throws SoapUIException;

    public Project openProject(Project modelItem) throws SoapUIException;

    public void inspectProjects();

}
