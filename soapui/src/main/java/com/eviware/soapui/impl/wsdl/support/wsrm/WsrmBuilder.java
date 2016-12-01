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

package com.eviware.soapui.impl.wsdl.support.wsrm;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

public class WsrmBuilder {
    private static final String WSRM_CREATE_SEQUENCE = "CreateSequence";
    private static final String WSRM_EXPIRES = "Expires";
    private static final String WSRM_ACKNOWLEDGMENTS_TO = "AcksTo";

    private static final String WSRM_CLOSE_SEQUENCE = "CloseSequence";
    private static final String WSRM_IDENTIFIER = "Identifier";
    private static final String WSRM_LAST_MESSAGE = "LastMsgNumber";

    private WsrmConfig wsrmConfig;

    public WsrmBuilder(WsrmConfig wsrmConfig) {
        this.wsrmConfig = wsrmConfig;
    }

    public XmlObject constructSequenceRequest() {
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        // cursor.toNextToken();

        cursor.insertNamespace("wsrm", wsrmConfig.getVersionNameSpace());

        cursor.beginElement(WSRM_CREATE_SEQUENCE, wsrmConfig.getVersionNameSpace());
        cursor.insertElementWithText(WSRM_ACKNOWLEDGMENTS_TO, wsrmConfig.getAckTo());
        if (wsrmConfig.getSequenceExpires() != null) {
            cursor.insertElementWithText(WSRM_EXPIRES, wsrmConfig.getSequenceExpires().toString());
        }

        cursor.dispose();

        return object;
    }

    public XmlObject constructSequenceClose() {
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();

        cursor.insertNamespace("wsrm", wsrmConfig.getVersionNameSpace());

        cursor.beginElement(WSRM_CLOSE_SEQUENCE, wsrmConfig.getVersionNameSpace());
        cursor.insertElementWithText(WSRM_IDENTIFIER, wsrmConfig.getSequenceIdentifier());
        // For a request, there will always be one message
        cursor.insertElementWithText(WSRM_LAST_MESSAGE, "1");

        cursor.dispose();

        return object;
    }
}
