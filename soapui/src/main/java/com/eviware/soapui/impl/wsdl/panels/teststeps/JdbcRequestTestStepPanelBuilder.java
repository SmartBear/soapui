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
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * PanelBuilder for JdbcRequestTestStep
 *
 * @author dragica.soldo
 */

public class JdbcRequestTestStepPanelBuilder extends EmptyPanelBuilder<JdbcRequestTestStep> {
    public JdbcRequestTestStepPanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(JdbcRequestTestStep testStep) {
        return new JdbcRequestTestStepDesktopPanel(testStep);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    @Override
    public Component buildOverviewPanel(JdbcRequestTestStep modelItem) {
        JPropertiesTable<JdbcRequestTestStep> table = buildDefaultProperties(modelItem, "JdbcRequestTestStep Properties");
        table.addProperty("Max Rows", "maxRows", true);
        table.addProperty("Query Timeout", "queryTimeout", true);
        table.addProperty("Fetch Size", "fetchSize", true);
        table.addProperty("Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS);
        return table;
    }
}
