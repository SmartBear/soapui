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

package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.panels.mock.WsdlMockServiceDesktopPanel;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

public class RestMockServicePanelBuilder extends EmptyPanelBuilder<RestMockService> {
    public RestMockServicePanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(RestMockService restMockService) {
        return new WsdlMockServiceDesktopPanel(restMockService);
    }

    @Override
    public boolean hasDesktopPanel() {
        return true;
    }

    public Component buildOverviewPanel(RestMockService mockService) {
        JPropertiesTable<RestMockService> table = new JPropertiesTable<RestMockService>("MockService Properties");
        boolean editable = true;
        table.addProperty("Name", "name", editable);
        table.addProperty("Description", "description", editable);
        table.addProperty("Path", "path", !editable);
        table.addProperty("Port", "port", !editable);
        table.setPropertyObject(mockService);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
