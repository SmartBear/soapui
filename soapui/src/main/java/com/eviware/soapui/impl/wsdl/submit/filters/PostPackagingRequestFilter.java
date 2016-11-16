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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpVersion;
import org.apache.http.entity.AbstractHttpEntity;

public class PostPackagingRequestFilter extends AbstractRequestFilter {

    @Override
    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> request) {
        ExtendedHttpMethod httpMethod = (ExtendedHttpMethod) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);
        Settings settings = request.getSettings();

        // chunking?
        if (httpMethod.getProtocolVersion().equals(HttpVersion.HTTP_1_1)
                && httpMethod instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityEnclosingMethod = ((HttpEntityEnclosingRequest) httpMethod);
            long limit = settings.getLong(HttpSettings.CHUNKING_THRESHOLD, -1);
            HttpEntity requestEntity = entityEnclosingMethod.getEntity();
            if (requestEntity != null && requestEntity instanceof AbstractHttpEntity) {
                ((AbstractHttpEntity) requestEntity).setChunked(limit >= 0 ? requestEntity.getContentLength() > limit
                        : false);
            }
        }
    }
}
