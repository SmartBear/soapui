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

import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.TypedContent;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.codec.binary.Base64;

public class DefaultMediaTypeHandler implements MediaTypeHandler {
    public boolean canHandle(String contentType) {
        return true;
    }

    @Override
    public String createXmlRepresentation(HttpResponse response) {
        return createXmlRepresentation((TypedContent)response);
    }

    public String createXmlRepresentation(TypedContent typedContent) {
        String contentType = typedContent.getContentType();
        String content = typedContent.getContentAsString();

        if (StringUtils.hasContent(contentType) && contentType.toUpperCase().endsWith("XML")) {
            return content;
        }

        if (XmlUtils.seemsToBeXml(content)) {
            return content;
        } else if (content == null) {
            content = "";
        }

        String result = "<data contentType=\"" + contentType + "\" contentLength=\"" + typedContent.getContentLength()
                + "\">";

        for (int c = 0; c < content.length(); c++) {
            if (content.charAt(c) < 8) {
                return result + new String(Base64.encodeBase64(content.getBytes())) + "</data>";
            }
        }

        return result + "<![CDATA[" + content + "]]></data>";
    }
}
