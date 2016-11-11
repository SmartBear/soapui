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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import org.apache.commons.httpclient.URI;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * RequestFilter that adds SOAP specific headers
 *
 * @author Ole.Matzura
 */

public class EndpointRequestFilter extends AbstractRequestFilter {
    @Override
    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> request) {
        HttpRequestBase httpMethod = (HttpRequestBase) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);

        String strURL = request.getEndpoint();
        strURL = PropertyExpander.expandProperties(context, strURL);
        try {
            if (StringUtils.hasContent(strURL)) {
                URI uri = new URI(strURL, request.getSettings().getBoolean(HttpSettings.ENCODED_URLS));
                context.setProperty(BaseHttpRequestTransport.REQUEST_URI, uri);
                httpMethod.setURI(HttpUtils.createUri(uri));
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }
}
