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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * PanelBuilder for WsdlMockOperation
 *
 * @author Ole.Matzura
 */

public class WsdlMockOperationPanelBuilder extends EmptyPanelBuilder<WsdlMockOperation> {
    public boolean hasOverviewPanel() {
        return true;
    }

    public Component buildOverviewPanel(WsdlMockOperation mockOperation) {
        JPropertiesTable<WsdlMockOperation> table = new JPropertiesTable<WsdlMockOperation>("Mock Operation");
        table = new JPropertiesTable<WsdlMockOperation>("MockOperation Properties");
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("WSDL Operation", "wsdlOperationName", false);
        table.addProperty("Dispatch Style", "dispatchStyle", false);
        table.setPropertyObject(mockOperation);

        return table;
    }

    @Override
    public DesktopPanel buildDesktopPanel(WsdlMockOperation mockOperation) {
        return new WsdlMockOperationDesktopPanel(mockOperation);
    }

    @Override
    public boolean hasDesktopPanel() {
        return true;
    }
}
