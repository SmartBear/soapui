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

package com.eviware.soapui.impl.support.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

/**
 * XmlDocument for a WsdlRequest
 *
 * @author ole.matzura
 */

public class RequestXmlDocument extends AbstractXmlDocument implements PropertyChangeListener {
    private final WsdlRequest request;
    private boolean updating;

    public RequestXmlDocument(WsdlRequest request) {
        this.request = request;
        request.addPropertyChangeListener(WsdlRequest.REQUEST_PROPERTY, this);
    }

    public String getXml() {
        return request.getRequestContent();
    }

    public void setXml(String xml) {
        if (!updating) {
            updating = true;
            String old = request.getRequestContent();
            request.setRequestContent(xml);
            fireXmlChanged(old, xml);
            updating = false;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!updating) {
            updating = true;
            fireXmlChanged((String) evt.getOldValue(), (String) evt.getNewValue());
            updating = false;
        }
    }

    public SchemaTypeSystem getTypeSystem() {
        WsdlInterface iface = (WsdlInterface) request.getOperation().getInterface();
        WsdlContext wsdlContext = iface.getWsdlContext();
        try {
            return wsdlContext.getSchemaTypeSystem();
        } catch (Exception e1) {
            SoapUI.logError(e1);
            return XmlBeans.getBuiltinTypeSystem();
        }
    }

    public void release() {
        request.removePropertyChangeListener(WsdlRequest.REQUEST_PROPERTY, this);
    }
}
