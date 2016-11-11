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

package com.eviware.soapui.impl.wsdl.panels.mock;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * PanelBuilder for WsdlMockServices
 *
 * @author ole.matzura
 */

public class WsdlMockServicePanelBuilder extends EmptyPanelBuilder<WsdlMockService> {
    public WsdlMockServicePanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(WsdlMockService mockService) {
        return new WsdlMockServiceDesktopPanel(mockService);
    }

    @Override
    public boolean hasDesktopPanel() {
        return true;
    }

    public Component buildOverviewPanel(WsdlMockService mockService) {
        JPropertiesTable<WsdlMockService> table = new JPropertiesTable<WsdlMockService>("MockService Properties");
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Path", "path");
        table.addProperty("Port", "port");
        table.addProperty("Match SOAP Version", "requireSoapVersion", JPropertiesTable.BOOLEAN_OPTIONS).setDescription(
                "Matches incoming SOAP Version against corresponding Interface");
        table.addProperty("Require SOAP Action", "requireSoapAction", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Dispatch Responses", "dispatchResponseMessages", JPropertiesTable.BOOLEAN_OPTIONS);

        WsdlProject project = mockService.getProject();
        StringList incomingNames = new StringList(project.getWssContainer().getIncomingWssNames());
        incomingNames.add("");
        table.addProperty("Incoming WSS", "incomingWss", incomingNames.toStringArray());
        StringList outgoingNames = new StringList(project.getWssContainer().getOutgoingWssNames());
        outgoingNames.add("");
        table.addProperty("Default Outgoing WSS", "outgoingWss", outgoingNames.toStringArray());
        table.setPropertyObject(mockService);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
