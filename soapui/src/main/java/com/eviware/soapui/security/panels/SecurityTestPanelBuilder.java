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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * PanelBuilder for SecurityTests
 *
 * @author dragica.soldo
 */

public class SecurityTestPanelBuilder<T extends SecurityTest> extends EmptyPanelBuilder<T> {
    public SecurityTestPanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(T securityTest) {
        return new SecurityTestDesktopPanel(securityTest);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    public Component buildOverviewPanel(T modelItem) {
        JPropertiesTable<SecurityTest> table = new JPropertiesTable<SecurityTest>("SecurityTest Properties", modelItem);

        table.addProperty("Name", "name", true);

        table.setPropertyObject(modelItem);

        return table;
    }
}
