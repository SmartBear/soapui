/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;

public class HttpCompressionRequestFilter extends AbstractRequestFilter {
    private final static Logger log = LogManager.getLogger(HttpCompressionRequestFilter.class);

    @Override
    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> httpRequest) {
        Settings settings = httpRequest.getSettings();
        String compressionAlg = settings.getString(HttpSettings.REQUEST_COMPRESSION, "None");
        if (!"None".equals(compressionAlg)) {
            try {
                ExtendedHttpMethod method = (ExtendedHttpMethod) context
                        .getProperty(BaseHttpRequestTransport.HTTP_METHOD);
                if (method instanceof HttpEntityEnclosingRequest) {
                    HttpEntity requestEntity = ((HttpEntityEnclosingRequest) method).getEntity();
                    if (requestEntity != null) {
                        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
                        requestEntity.writeTo(tempOut);

                        byte[] compressedData = CompressionSupport.compress(compressionAlg, tempOut.toByteArray());
                        ((HttpEntityEnclosingRequest) method).setEntity(new ByteArrayEntity(compressedData));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
