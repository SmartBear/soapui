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

import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.config.CredentialsConfig.AuthType.Enum;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpCredentialsProvider;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import org.apache.http.Header;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.NegotiateSchemeFactory;
import org.apache.http.protocol.HttpContext;

/**
 * RequestFilter for setting preemptive authentication and related credentials
 */

public class HttpAuthenticationRequestFilter extends AbstractRequestFilter {

    @Override
    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> wsdlRequest) {
        String username = PropertyExpander.expandProperties(context, wsdlRequest.getUsername());

        // check for authorization prerequisites
        if (username == null || username.length() == 0) {
            return;
        }

        Settings settings = wsdlRequest.getSettings();
        String password = PropertyExpander.expandProperties(context, wsdlRequest.getPassword());
        String domain = PropertyExpander.expandProperties(context, wsdlRequest.getDomain());

        Enum authType = Enum.forString(wsdlRequest.getAuthType());

        registerSpnegoAuthSchemeFactory(authType);

        String wssPasswordType = null;

        if (wsdlRequest instanceof WsdlRequest) {
            wssPasswordType = PropertyExpander.expandProperties(context,
                    ((WsdlRequest) wsdlRequest).getWssPasswordType());
        }

        if (StringUtils.isNullOrEmpty(wssPasswordType)) {
            initRequestCredentials(context, username, settings, password, domain, authType);
        }
    }

    private void registerSpnegoAuthSchemeFactory(Enum authtype) {
        // Due to a bug in apache http client 4.1.1 (HTTPCLIENT-1107) the user must explicitly set the auth type on the request.
        // For more info, see SOAP-1021
        if (authtype == AuthType.NTLM) {
            HttpClientSupport.getHttpClient().getAuthSchemes().register(AuthPolicy.SPNEGO, new NTLMSchemeFactory());
        } else if (authtype == AuthType.SPNEGO_KERBEROS) {
            HttpClientSupport.getHttpClient().getAuthSchemes().register(AuthPolicy.SPNEGO, new NegotiateSchemeFactory(null, true));
        }
    }

    public static void initRequestCredentials(SubmitContext context, String username, Settings settings,
                                              String password, String domain, Enum authType) {
        HttpRequestBase httpMethod = (HttpRequestBase) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);
        HttpContext httpContext = (HttpContext) context.getProperty(SubmitContext.HTTP_STATE_PROPERTY);

        if (!StringUtils.isNullOrEmpty(username) && !StringUtils.isNullOrEmpty(password)) {
            // set preemptive authentication
            if ((authType.equals(AuthType.GLOBAL_HTTP_SETTINGS) && settings.getBoolean(HttpSettings.AUTHENTICATE_PREEMPTIVELY))
                    || authType.equals(AuthType.PREEMPTIVE)) {
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
                Header header = BasicScheme.authenticate(creds, "utf-8", false);
                httpMethod.removeHeaders("Authorization");
                httpMethod.addHeader(header);
            }
        }
        String requestAuthPolicy = getCorrespondingAuthPolicy(authType);
        HttpCredentialsProvider credentialsProvider = new HttpCredentialsProvider();
        credentialsProvider.loadProxyCredentialsFromSettings();
        credentialsProvider.setRequestCredentials(username, password, domain, requestAuthPolicy);
        httpContext.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider);
    }

    private static String getCorrespondingAuthPolicy(Enum authType) {
        String authPolicy = null;
        if (authType == AuthType.NTLM) {
            authPolicy = AuthPolicy.NTLM;
        } else if (authType == AuthType.SPNEGO_KERBEROS) {
            authPolicy = AuthPolicy.SPNEGO;
        }
        return authPolicy;
    }
}
