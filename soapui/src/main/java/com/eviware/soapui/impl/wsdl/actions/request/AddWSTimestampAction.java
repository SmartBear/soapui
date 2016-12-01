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

package com.eviware.soapui.impl.wsdl.actions.request;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.swing.AbstractAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Prompts to add a WSS Timestamp Token to the specified WsdlRequests
 * requestContent
 *
 * @author Ole.Matzura
 */

public class AddWSTimestampAction extends AbstractAction {
    private final WsdlRequest request;

    public AddWSTimestampAction(WsdlRequest request) {
        super("Add WS-Timestamp");
        this.request = request;
    }

    public void actionPerformed(ActionEvent e) {
        String req = request.getRequestContent();

        try {
            String ttlString = UISupport.prompt("Add WS-Timestamp", "Specify Time-To-Live value", "60");
            if (ttlString == null) {
                return;
            }

            int ttl = 0;
            try {
                ttl = Integer.parseInt(ttlString);
            } catch (Exception ex) {
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(req)));
            WSSecTimestamp addTimestamp = new WSSecTimestamp();
            addTimestamp.setTimeToLive(ttl);

            StringWriter writer = new StringWriter();
            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);
            XmlUtils.serializePretty(addTimestamp.build(doc, secHeader), writer);
            request.setRequestContent(writer.toString());
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        }
    }
}
