/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support.wss;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.support.xml.XmlUtils;

public class WssUtils {
    public final static String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public final static String WSU_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    String content;
    SoapVersion soapVersion;

    public static String removeWSSOutgoing(String content, WsaContainer wsaContainer) {
        try {
            SoapVersion soapVersion = wsaContainer.getOperation().getInterface().getSoapVersion();
//			XmlObject xmlContentObject = XmlObject.Factory.parse( content );
            XmlObject xmlContentObject = XmlUtils.createXmlObject(content);
            Element header = (Element) SoapUtils.getHeaderElement(xmlContentObject, soapVersion, true).getDomNode();

            NodeList headerProps = XmlUtils.getChildElements(header);
            for (int i = 0; i < headerProps.getLength(); i++) {
                Node headerChild = headerProps.item(i);
                if (headerChild.getNamespaceURI().equals(WSSE_NAMESPACE)) {
                    header.removeChild(headerChild);
                }
            }
            content = xmlContentObject.xmlText();
        } catch (XmlException e) {
            SoapUI.logError(e);
        }
        return content;
    }
}
