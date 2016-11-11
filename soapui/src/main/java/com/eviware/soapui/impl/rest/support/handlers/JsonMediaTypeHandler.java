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
import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.TypedContent;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import net.sf.json.JSON;
import net.sf.json.JSONException;

import java.net.URL;

public class JsonMediaTypeHandler implements MediaTypeHandler {

    public boolean canHandle(String contentType) {
        return JsonUtil.seemsToBeJsonContentType(contentType);
    }

    @Override
    public String createXmlRepresentation(HttpResponse response) {
        try {
            if (response == null || response.getContentAsString() == null) {
                return null;
            }
            String content = response.getContentAsString().trim();
            if (!StringUtils.hasContent(content)) {
                return null;
            }
            // remove nulls - workaround for bug in xmlserializer!?
            content = content.replaceAll("\\\\u0000", "");
            JSON json = new JsonUtil().parseTrimmedText(content);
            JsonXmlSerializer serializer = new JsonXmlSerializer();
            serializer.setTypeHintsEnabled(false);
            serializer.setRootName(HttpUtils.isErrorStatus(response.getStatusCode()) ? "Fault" : "Response");
            URL url = response.getURL();
            String originalUri = readOriginalUriFrom(response.getRequest());
            String namespaceUri = originalUri != null ? originalUri : makeNamespaceUriFrom(url);
            serializer.setNamespace("", namespaceUri);
            content = serializer.write(json);
            content = XmlUtils.prettyPrintXml(content);

            return content;
        } catch (JSONException ignore) {
            // if the content is not valid JSON, empty XML will be returned
        } catch (Exception e) {
            SoapUI.logError(e);
        }
        return "<xml/>";

    }

    public String createXmlRepresentation(TypedContent typedContent) {
        try {
            String content = typedContent.getContentAsString().trim();
            if (!StringUtils.hasContent(content)) {
                return null;
            }
            // remove nulls - workaround for bug in xmlserializer!?
            content = content.replaceAll("\\\\u0000", "");
            JSON json = new JsonUtil().parseTrimmedText(content);
            JsonXmlSerializer serializer = new JsonXmlSerializer();
            serializer.setTypeHintsEnabled(false);
            serializer.setRootName("Response");
            serializer.setNamespace("", "json");
            content = serializer.write(json);
            content = XmlUtils.prettyPrintXml(content);

            return content;
        } catch (JSONException ignore) {
            // if the content is not valid JSON, empty XML will be returned
        } catch (Exception e) {
            SoapUI.logError(e);
        }
        return "<xml/>";
    }

    private String readOriginalUriFrom(AbstractHttpRequestInterface<?> request) {
        if (request instanceof RestRequest) {
            AbstractRequestConfig config = ((RestRequest) request).getConfig();
            String originalUri = config.getOriginalUri();
            // if URI contains unexpanded template parameters
            if (originalUri != null && originalUri.contains("{")) {
                return null;
            }
            return originalUri;
        } else {
            return null;
        }
    }

    public static String makeNamespaceUriFrom(URL url) {
        return url.getProtocol() + "://" + url.getHost() + url.getPath();
    }
}
