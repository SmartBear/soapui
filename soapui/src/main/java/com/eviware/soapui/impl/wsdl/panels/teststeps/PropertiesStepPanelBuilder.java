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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.JPanel;

/**
 * PanelBuilder for WsdlPropertiesTestStep
 *
 * @author Ole.Matzura
 */

public class PropertiesStepPanelBuilder extends EmptyPanelBuilder<WsdlPropertiesTestStep> {
    public PropertiesStepPanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(WsdlPropertiesTestStep testStep) {
        return new PropertiesStepDesktopPanel(testStep);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    public JPanel buildOverviewPanel(WsdlPropertiesTestStep testStep) {
        JPropertiesTable<WsdlPropertiesTestStep> table = new JPropertiesTable<WsdlPropertiesTestStep>(
                "PropertiesStep Properties");

        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Create Missing on Load", "createMissingOnLoad", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Save before Load", "saveFirst", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Discard Values on Save", "discardValuesOnSave", JPropertiesTable.BOOLEAN_OPTIONS);
        table.setPropertyObject(testStep);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
