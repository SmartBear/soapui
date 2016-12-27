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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

public class MockResponseStepPanelBuilder extends EmptyPanelBuilder<WsdlMockResponseTestStep> {
    public MockResponseStepPanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(WsdlMockResponseTestStep mockResponseStep) {
        return new WsdlMockResponseStepDesktopPanel(mockResponseStep);
    }

    @Override
    public boolean hasDesktopPanel() {
        return true;
    }

    public boolean hasOverviewPanel() {
        return true;
    }

    public Component buildOverviewPanel(WsdlMockResponseTestStep mockResponseStep) {
        JPropertiesTable<WsdlMockResponseTestStep> table = new JPropertiesTable<WsdlMockResponseTestStep>(
                "MockResponse Properties");
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Message Size", "contentLength", false);
        table.addProperty("Encoding", "encoding", new String[]{null, "UTF-8", "iso-8859-1"});

        StringList outgoingNames = new StringList(mockResponseStep.getTestCase().getTestSuite().getProject()
                .getWssContainer().getOutgoingWssNames());
        outgoingNames.add("");
        table.addProperty("Outgoing WSS", "outgoingWss", outgoingNames.toStringArray());

        // attachments
        table.addProperty("Enable MTOM", "mtomEnabled", JPropertiesTable.BOOLEAN_OPTIONS);

        // preprocessing
        table.addProperty("Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS);

        // others
        table.addProperty("Timeout", "timeout", true);

        String[] names = ModelSupport.getNames(new String[]{""}, mockResponseStep.getTestCase().getTestStepList());

        table.addProperty("Start Step", "startStep", names);

        table.addProperty("Port", "port", true);
        table.addProperty("Path", "path", true);
        table.addProperty("Host", "host", true);
        table.setPropertyObject(mockResponseStep);

        return table;
    }
}
