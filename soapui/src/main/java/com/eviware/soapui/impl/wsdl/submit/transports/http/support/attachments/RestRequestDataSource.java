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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import com.eviware.soapui.impl.support.http.HttpRequestInterface;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for an existing WsdlRequest
 *
 * @author ole.matzura
 */

public class RestRequestDataSource implements DataSource {
    private final HttpRequestInterface<?> restRequest;
    private final String requestContent;

    public RestRequestDataSource(HttpRequestInterface<?> restRequest, String requestContent) {
        this.restRequest = restRequest;
        this.requestContent = requestContent;
    }

    public String getContentType() {
        return restRequest.getMediaType();
    }

    public InputStream getInputStream() throws IOException {
        byte[] bytes = requestContent.getBytes("UTF-8");
        return new ByteArrayInputStream(bytes);
    }

    public String getName() {
        return restRequest.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
