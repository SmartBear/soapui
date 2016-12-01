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

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import org.w3c.dom.Document;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.StringWriter;

/**
 * Prompts to add a WSS Username Token to the specified WsdlRequests
 * requestContent
 *
 * @author Ole.Matzura
 */

public class ApplyOutgoingWSSToMockResponseAction extends AbstractAction {
    private final WsdlMockResponse mockResponse;
    private final OutgoingWss outgoing;

    public ApplyOutgoingWSSToMockResponseAction(WsdlMockResponse mockResponse, OutgoingWss outgoing) {
        super("Apply \" " + outgoing.getName() + " \"");
        this.mockResponse = mockResponse;
        this.outgoing = outgoing;
    }

    public void actionPerformed(ActionEvent e) {
        String req = mockResponse.getResponseContent();

        try {
            UISupport.setHourglassCursor();
            Document dom = XmlUtils.parseXml(req);
            outgoing.processOutgoing(dom, new DefaultPropertyExpansionContext(mockResponse));
            StringWriter writer = new StringWriter();
            XmlUtils.serialize(dom, writer);
            mockResponse.setResponseContent(writer.toString());
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        } finally {
            UISupport.resetCursor();
        }
    }
}
