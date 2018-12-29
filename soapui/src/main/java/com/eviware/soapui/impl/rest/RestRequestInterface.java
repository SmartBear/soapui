/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;

public interface RestRequestInterface extends HttpRequestInterface<RestRequestConfig>, PropertyChangeListener {

    /**
     * Each value in this enumeration represents an officially supported HTTP method ("verb").
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH, PROPFIND, LOCK, UNLOCK, COPY, PURGE;

        public static String[] getMethodsAsStringArray() {
            return new String[]{GET.toString(), POST.toString(), PUT.toString(), DELETE.toString(), HEAD.toString(),
                    OPTIONS.toString(), TRACE.toString(), PATCH.toString(), PROPFIND.toString(), LOCK.toString(), UNLOCK.toString(),
                    COPY.toString(), PURGE.toString()};
        }

        public static HttpMethod[] getMethods() {
            return new HttpMethod[]{GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH, PROPFIND, LOCK, UNLOCK, COPY, PURGE};
        }
    }

    public final static Logger log = Logger.getLogger(RestRequest.class);
    public static final String DEFAULT_MEDIATYPE = "application/xml";
    public static final String REST_XML_REQUEST = "restXmlRequest";

    RestMethod getRestMethod();

    RestRepresentation[] getRepresentations();

    RestRepresentation[] getRepresentations(RestRepresentation.Type type);

    RestRepresentation[] getRepresentations(RestRepresentation.Type type, String mediaType);

    String getAccept();

    void setAccept(String acceptEncoding);

    String[] getResponseMediaTypes();

    RestResource getResource();

    void setPath(String fullPath);

    void setResponse(HttpResponse response, SubmitContext context);

    void release();

    boolean hasEndpoint();

}
