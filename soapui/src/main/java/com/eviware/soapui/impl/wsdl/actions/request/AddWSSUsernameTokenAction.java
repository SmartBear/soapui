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
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.swing.AbstractAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Prompts to add a WSS Username Token to the specified WsdlRequests
 * requestContent
 *
 * @author Ole.Matzura
 */

public class AddWSSUsernameTokenAction extends AbstractAction {
    private final WsdlRequest request;

    public AddWSSUsernameTokenAction(WsdlRequest request) {
        super("Add WSS Username Token");
        this.request = request;
    }

    public void actionPerformed(ActionEvent e) {
        if ((request.getUsername() == null || request.getUsername().length() == 0)
                && (request.getPassword() == null || request.getPassword().length() == 0)) {
            UISupport.showErrorMessage("Request is missing username and password");
            return;
        }

        String req = request.getRequestContent();

        try {
            String passwordType = (String) UISupport.prompt("Add WSS Username Token", "Specify Password Type",
                    new String[]{WsdlRequest.PW_TYPE_TEXT, WsdlRequest.PW_TYPE_DIGEST});

            if (passwordType == null) {
                return;
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(req)));
            WSSecUsernameToken addUsernameToken = new WSSecUsernameToken();

            if (WsdlRequest.PW_TYPE_DIGEST.equals(passwordType)) {
                addUsernameToken.setPasswordType(WSConstants.PASSWORD_DIGEST);
            } else {
                addUsernameToken.setPasswordType(WSConstants.PASSWORD_TEXT);
            }

            addUsernameToken.setUserInfo(request.getUsername(), request.getPassword());
            addUsernameToken.addNonce();
            addUsernameToken.addCreated();

            StringWriter writer = new StringWriter();

            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);
            XmlUtils.serializePretty(addUsernameToken.build(doc, secHeader), writer);
            request.setRequestContent(writer.toString());
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        }
    }
}
