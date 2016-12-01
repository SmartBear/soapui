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
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

public class RestMockActionPanelBuilder extends EmptyPanelBuilder<RestMockAction> {
    public boolean hasOverviewPanel() {
        return true;
    }

    public Component buildOverviewPanel(RestMockAction mockAction) {
        JPropertiesTable<RestMockAction> table = new JPropertiesTable<RestMockAction>("MockAction Properties");
        boolean editable = true;
        table.addProperty("Name", "name", editable);
        table.addProperty("Description", "description", editable);
        table.addProperty("Resource path", "resourcePath", editable);
        table.addProperty("Method", "method", RestRequestInterface.HttpMethod.values());
        table.setPropertyObject(mockAction);

        return table;
    }

    @Override
    public DesktopPanel buildDesktopPanel(RestMockAction mockOperation) {
        return new RestMockActionDesktopPanel(mockOperation);
    }

    @Override
    public boolean hasDesktopPanel() {
        return true;
    }
}
