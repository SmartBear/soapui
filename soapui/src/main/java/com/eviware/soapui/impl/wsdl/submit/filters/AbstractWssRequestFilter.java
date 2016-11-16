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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.xml.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public abstract class AbstractWssRequestFilter extends AbstractRequestFilter {
    private static final String REQUEST_CONTENT_HASH_CODE = "requestContentHashCode";
    public static final String WSS_DOC = "WsSecurityAuthenticationRequestFilter@Document";
    protected static DocumentBuilderFactory dbf;
    protected static DocumentBuilder db;

    static {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            SoapUI.logError(e);
        }
    }

    protected static Document getWssDocument(SubmitContext context) throws SAXException, IOException {
        String request = (String) context.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        Document doc = (Document) context.getProperty(WSS_DOC);

        // this should be solved with pooling for performance-reasons..
        if (doc == null
                || ((Integer) context.getProperty(REQUEST_CONTENT_HASH_CODE)).intValue() != request.hashCode()) {
            synchronized (db) {
                doc = db.parse(new InputSource(new StringReader(request)));
                context.setProperty(REQUEST_CONTENT_HASH_CODE, new Integer(request.hashCode()));
                context.setProperty(WSS_DOC, doc);
            }
        }

        return doc;
    }

    protected static void updateWssDocument(SubmitContext context, Document dom) throws IOException {
        StringWriter writer = new StringWriter();
        XmlUtils.serialize(dom, writer);
        String request = writer.toString();
        context.setProperty(BaseHttpRequestTransport.REQUEST_CONTENT, request);
        context.setProperty(REQUEST_CONTENT_HASH_CODE, new Integer(request.hashCode()));
    }

    public void afterRequest(SubmitContext context, Response response) {
        context.removeProperty(WSS_DOC);
    }
}
