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

package com.eviware.soapui.impl.wsdl.panels.project;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.JPanel;

/**
 * PanelBuilder for WsdlProject. Only builds an overview panel.
 *
 * @author Ole.Matzura
 */

public class WsdlProjectPanelBuilder extends EmptyPanelBuilder<WsdlProject> {
    public WsdlProjectPanelBuilder() {
    }

    public JPanel buildOverviewPanel(WsdlProject project) {
        JPropertiesTable<WsdlProject> table = new JPropertiesTable<WsdlProject>("Project Properties", project);

        if (project.isOpen()) {
            table.addProperty("Name", "name", true);
            table.addProperty("Description", "description", true);
            table.addProperty("File", "path");

            if (!project.isDisabled()) {
                table.addProperty("Resource Root", "resourceRoot",
                        new String[]{null, "${projectDir}", "${workspaceDir}"});
                table.addProperty("Cache Definitions", "cacheDefinitions", JPropertiesTable.BOOLEAN_OPTIONS);
                table.addPropertyShadow("Project Password", "shadowPassword", true);
                table.addProperty("Script Language", "defaultScriptLanguage",
                        SoapUIScriptEngineRegistry.getAvailableEngineIds());
                table.addProperty("Hermes Config", "hermesConfig", true);
            }
        } else {
            table.addProperty("File", "path");
        }

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    public DesktopPanel buildDesktopPanel(WsdlProject modelItem) {
        return new WsdlProjectDesktopPanel(modelItem);
    }
}
