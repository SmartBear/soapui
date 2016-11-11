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

package com.eviware.soapui.impl.rest.support.handlers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.TypedContent;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HtmlMediaTypeHandler implements MediaTypeHandler {
    public boolean canHandle(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("text/html");
    }

    @Override
    public String createXmlRepresentation(HttpResponse response) {
        return createXmlRepresentation((TypedContent)response);
    }

    public String createXmlRepresentation(TypedContent typedContent) {
        String content = typedContent == null ? null : typedContent.getContentAsString();
        if (!StringUtils.hasContent(content)) {
            return "<xml/>";
        }

        try {
            // XmlObject.Factory.parse( new ByteArrayInputStream(
            // content.getBytes() ) );
            XmlUtils.createXmlObject(new ByteArrayInputStream(content.getBytes()));
            return content;
        } catch (Exception e) {
            // fall through, this wasn't xml
        }

        try {
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);
            tidy.setShowWarnings(false);
            tidy.setErrout(new PrintWriter(new StringWriter()));
            // tidy.setQuiet(true);
            tidy.setNumEntities(true);
            tidy.setQuoteNbsp(true);
            tidy.setFixUri(false);

            Document document = tidy.parseDOM(new ByteArrayInputStream(content.getBytes()), null);
            StringWriter writer = new StringWriter();
            XmlUtils.serializePretty(document, writer);
            return writer.toString();
        } catch (Throwable e) {
            SoapUI.logError(e);
        }
        return null;
    }
}
