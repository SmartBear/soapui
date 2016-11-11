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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * PanelBuilder for AMFRequestTestStep
 *
 * @author nebojsa.tasic
 */

public class AMFRequestTestStepPanelBuilder extends EmptyPanelBuilder<AMFRequestTestStep> {
    public AMFRequestTestStepPanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(AMFRequestTestStep testStep) {
        return new AMFRequestTestStepDesktopPanel(testStep);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    @Override
    public Component buildOverviewPanel(AMFRequestTestStep modelItem) {
        JPropertiesTable<AMFRequestTestStep> table = buildDefaultProperties(modelItem, "AMFRequestTestStep Properties");
        table.addProperty("Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS);
        return table;
    }
}
