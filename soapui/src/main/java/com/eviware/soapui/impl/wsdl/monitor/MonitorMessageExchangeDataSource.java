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

package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.Tools;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for a MockRequest
 *
 * @author ole.matzura
 */

public class MonitorMessageExchangeDataSource implements DataSource {
    private byte[] data;
    private String contentType;
    private String name;

    public MonitorMessageExchangeDataSource(String name, InputStream in, String contentType) {
        try {
            data = Tools.readAll(in, 0).toByteArray();
            this.contentType = contentType;
            this.name = name;
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    public String getName() {
        return name;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
