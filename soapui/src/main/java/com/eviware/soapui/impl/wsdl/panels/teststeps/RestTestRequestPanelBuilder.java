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
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;

import javax.swing.JPanel;

/**
 * PanelBuilder for RestTestRequest
 *
 * @author Ole.Matzura
 */

public class RestTestRequestPanelBuilder extends EmptyPanelBuilder<RestTestRequestStep> {
    public RestTestRequestPanelBuilder() {
    }

    public RestTestRequestDesktopPanel buildDesktopPanel(RestTestRequestStep testStep) {
        return new RestTestRequestDesktopPanel(testStep);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    public JPanel buildOverviewPanel(RestTestRequestStep testStep) {
        RestTestRequestInterface request = testStep.getTestRequest();
        JPropertiesTable<RestTestRequestInterface> table = new JPropertiesTable<RestTestRequestInterface>(
                "REST TestRequest Properties");

        // basic properties
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        // table.addProperty( "Message Size", "contentLength", false );
        table.addProperty("Encoding", "encoding", new String[]{null, "UTF-8", "iso-8859-1"});

        if (request.getOperation() == null) {
            table.addProperty("Path", "path", true);
        } else {
            table.addProperty("Endpoint", "endpoint", request.getInterface() == null ? new String[0] : request
                    .getInterface().getEndpoints());
            table.addProperty("Service", "serviceName");
            table.addProperty("Resource", "path");
            table.addProperty("Method", "restMethodName");
        }

        table.addProperty("Timeout", "timeout", true);

        table.addProperty("Bind Address", "bindAddress", true);
        table.addProperty("Follow Redirects", "followRedirects", JPropertiesTable.BOOLEAN_OPTIONS);

        StringList keystores = new StringList(request.getTestStep().getTestCase().getTestSuite().getProject()
                .getWssContainer().getCryptoNames());
        keystores.add("");
        table.addProperty("SSL Keystore", "sslKeystore", keystores.toStringArray());

        table.addProperty("Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Entitize Properties", "entitizeProperties", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Multi-Value Delimiter", "multiValueDelimiter", true);

        // post-processing
        table.addProperty("Pretty Print", "prettyPrint", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Dump File", "dumpFile", true).setDescription("Dumps response message to specified file");
        table.addProperty("Max Size", "maxSize", true).setDescription("The maximum number of bytes to receive");
        table.addProperty("Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS);

        table.setPropertyObject(request);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
