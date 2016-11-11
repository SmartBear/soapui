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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.monitor.CaptureInputStream;
import com.eviware.soapui.settings.UISettings;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for a MockRequest
 *
 * @author ole.matzura
 */

public class MockRequestDataSource implements DataSource {
    private String contentType;
    private String name;
    private final HttpServletRequest request;
    private CaptureInputStream capture = null;

    public MockRequestDataSource(HttpServletRequest request) {
        this.request = request;
        try {
            contentType = request.getContentType();
            name = "Request for " + request.getPathInfo();
            capture = new CaptureInputStream(request.getInputStream(), SoapUI.getSettings().getLong(
                    UISettings.RAW_REQUEST_MESSAGE_SIZE, 0));
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    public String getName() {
        return name;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public byte[] getData() {
        return capture.getCapturedData();
    }
}
