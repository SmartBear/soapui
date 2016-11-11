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
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.settings.HttpSettings;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DataSource for a standard POST response
 *
 * @author ole.matzura
 */

public class PostResponseDataSource implements DataSource {
    private final ExtendedHttpMethod postMethod;
    private byte[] data;

    public PostResponseDataSource(ExtendedHttpMethod postMethod) {
        this.postMethod = postMethod;

        try {
            data = postMethod.getResponseBody();

            if (!SoapUI.getSettings().getBoolean(HttpSettings.DISABLE_RESPONSE_DECOMPRESSION)
                    && postMethod.hasHttpResponse()) {
                String compressionAlg = HttpClientSupport.getResponseCompressionType(postMethod.getHttpResponse());
                if (compressionAlg != null) {
                    data = CompressionSupport.decompress(compressionAlg, data);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public long getDataSize() {
        return data == null ? -1 : data.length;
    }

    public String getContentType() {
        return postMethod.getResponseContentType();
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    public String getName() {
        return postMethod.getRequestLine().getMethod() + " response for "
                + postMethod.getRequestLine().getUri().toString();
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public byte[] getData() {
        return data;
    }
}
