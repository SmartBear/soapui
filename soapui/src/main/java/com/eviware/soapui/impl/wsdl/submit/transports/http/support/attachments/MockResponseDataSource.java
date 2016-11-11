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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.mock.MockResponse;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for an existing WsdlMockResponse
 *
 * @author ole.matzura
 */

public class MockResponseDataSource implements DataSource {
    private final String responseContent;
    private final boolean isXOP;
    private final MockResponse mockResponse;

    public MockResponseDataSource(MockResponse mockResponse, String responseContent, boolean isXOP) {
        this.mockResponse = mockResponse;
        this.responseContent = responseContent;
        this.isXOP = isXOP;
    }

    public String getContentType() {
        if (mockResponse instanceof WsdlMockResponse) {
            SoapVersion soapVersion = ((WsdlMockResponse) mockResponse).getSoapVersion();

            if (isXOP) {
                return AttachmentUtils.buildRootPartContentType(mockResponse.getMockOperation().getOperation().getName(),
                        soapVersion);
            } else {
                return soapVersion.getContentType() + "; charset=UTF-8";
            }
        } else {
            throw new IllegalStateException("Multipart support is only available for SOAP");
        }
    }

    public InputStream getInputStream() throws IOException {
        byte[] bytes = responseContent.getBytes("UTF-8");
        return new ByteArrayInputStream(bytes);
    }

    public String getName() {
        return mockResponse.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
