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

package com.eviware.soapui.impl;

import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.components.JPropertiesTable;

import javax.swing.JPanel;

/**
 * PanelBuilder for default Workspace implementation
 *
 * @author ole.matzura
 */

public class WorkspaceImplPanelBuilder extends EmptyPanelBuilder<WorkspaceImpl> {
    public static final MessageSupport messages = MessageSupport.getMessages(WorkspaceImplPanelBuilder.class);

    public WorkspaceImplPanelBuilder() {
    }

    public JPanel buildOverviewPanel(WorkspaceImpl workspace) {
        JPropertiesTable<WorkspaceImpl> table = buildDefaultProperties(workspace, messages.get("OverviewPanel.Title"));
        table.addProperty(messages.get("OverviewPanel.File.Label"), "path", false);
        table.addProperty(messages.get("OverviewPanel.ProjectRoot.Label"), "projectRoot",
                new String[]{null, "${workspaceDir}"}).setDescription(
                messages.get("OverviewPanel.ProjectRoot.Description"));
        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
