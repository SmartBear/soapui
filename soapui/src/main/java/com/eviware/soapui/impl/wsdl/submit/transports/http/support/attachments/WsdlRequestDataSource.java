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

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

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

public class WsdlRequestDataSource implements DataSource {
    private final WsdlRequest wsdlRequest;
    private final String requestContent;
    private final boolean isXOP;

    public WsdlRequestDataSource(WsdlRequest wsdlRequest, String requestContent, boolean isXOP) {
        this.wsdlRequest = wsdlRequest;
        this.requestContent = requestContent;
        this.isXOP = isXOP;
    }

    public String getContentType() {
        SoapVersion soapVersion = wsdlRequest.getOperation().getInterface().getSoapVersion();

        if (isXOP) {
            return AttachmentUtils.buildRootPartContentType(wsdlRequest.getOperation().getName(), soapVersion);
        } else {
            return soapVersion.getContentType() + "; charset=UTF-8";
        }
    }

    public InputStream getInputStream() throws IOException {
        byte[] bytes = requestContent.getBytes("UTF-8");
        return new ByteArrayInputStream(bytes);
    }

    public String getName() {
        return wsdlRequest.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
