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

import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;

import javax.swing.JPanel;

/**
 * PanelBuilder for WsdlTestRequest
 *
 * @author Ole.Matzura
 */

public class WsdlTestRequestPanelBuilder extends EmptyPanelBuilder<WsdlTestRequestStep> {
    public WsdlTestRequestPanelBuilder() {
    }

    public WsdlTestRequestDesktopPanel buildDesktopPanel(WsdlTestRequestStep testStep) {
        return new WsdlTestRequestDesktopPanel(testStep);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    public JPanel buildOverviewPanel(WsdlTestRequestStep testStep) {
        WsdlTestRequest request = testStep.getTestRequest();
        JPropertiesTable<WsdlTestRequest> table = new JPropertiesTable<WsdlTestRequest>("TestRequest Properties");

        // basic properties
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Message Size", "contentLength", false);
        table.addProperty("Encoding", "encoding", new String[]{null, "UTF-8", "iso-8859-1"});
        table.addProperty("Endpoint", "endpoint", request.getInterface() == null ? new String[0] : request
                .getInterface().getEndpoints());
        table.addProperty("Timeout", "timeout", true);
        table.addProperty("Bind Address", "bindAddress", true);
        table.addProperty("Follow Redirects", "followRedirects", JPropertiesTable.BOOLEAN_OPTIONS);

        table.addProperty("Interface", "interfaceName");
        table.addProperty("Operation", "operationName");

        // security / authentication
        table.addProperty("Username", "username", true);
        table.addPropertyShadow("Password", "password", true);
        table.addProperty("Domain", "domain", true);
        table.addProperty("Authentication Type", "authType", new String[]{AuthType.GLOBAL_HTTP_SETTINGS.toString(),
                AuthType.PREEMPTIVE.toString(), AuthType.SPNEGO_KERBEROS.toString(), AuthType.NTLM.toString()});

        table.addProperty("WSS-Password Type", "wssPasswordType", new String[]{WsdlRequest.PW_TYPE_NONE,
                WsdlRequest.PW_TYPE_TEXT, WsdlRequest.PW_TYPE_DIGEST});
        table.addProperty("WSS TimeToLive", "wssTimeToLive", true);

        StringList keystores = new StringList(request.getOperation().getInterface().getProject().getWssContainer()
                .getCryptoNames());
        keystores.add(0, null);
        table.addProperty("SSL Keystore", "sslKeystore", keystores.toStringArray());

        table.addProperty("Skip SOAP Action", "skipSoapAction", JPropertiesTable.BOOLEAN_OPTIONS);

        // mtom / attachments
        table.addProperty("Enable MTOM", "mtomEnabled", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Force MTOM", "forceMtom", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Inline Response Attachments", "inlineResponseAttachments", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Expand MTOM Attachments", "expandMtomResponseAttachments", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Disable multiparts", "multipartEnabled", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Encode Attachments", "encodeAttachments", JPropertiesTable.BOOLEAN_OPTIONS);

        // preprocessing
        table.addProperty("Enable Inline Files", "inlineFilesEnabled", JPropertiesTable.BOOLEAN_OPTIONS)
                .setDescription("Enables inline file references in elements with binary content; file:<path>");
        table.addProperty("Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Entitize Properties", "entitizeProperties", JPropertiesTable.BOOLEAN_OPTIONS);

        // post-processing
        table.addProperty("Pretty Print", "prettyPrint", JPropertiesTable.BOOLEAN_OPTIONS);
        table.addProperty("Dump File", "dumpFile", true).setDescription("Dumps response message to specified file");
        table.addProperty("Max Size", "maxSize", true).setDescription("The maximum number of bytes to receive");

        table.addProperty("WS-Addressing", "wsAddressing", JPropertiesTable.BOOLEAN_OPTIONS);

        table.addProperty("Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS);

        table.setPropertyObject(request);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
