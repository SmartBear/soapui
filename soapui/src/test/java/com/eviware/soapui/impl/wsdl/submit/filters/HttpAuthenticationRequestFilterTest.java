/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.auth.SPNegoScheme;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Anders Jaensson
 */
public class HttpAuthenticationRequestFilterTest {
    private HttpAuthenticationRequestFilter filter;
    private WsdlRequest wsdlRequest;

    @Before
    public void setup() {
        filter = new HttpAuthenticationRequestFilter();
        wsdlRequest = Mockito.mock(WsdlRequest.class);
        when(wsdlRequest.getUsername()).thenReturn("Uwe");
        when(wsdlRequest.getWssPasswordType()).thenReturn(WsdlRequest.PW_TYPE_NONE);
    }

    @Test
    public void usingNtlmWhenBothNtlmAndNegotiateRequested() {
        selectAuthMethod(CredentialsConfig.AuthType.NTLM);

        CredentialsProvider credentialsProvider = makeCredentialsProvider();
        AuthScope authScope = Mockito.mock(AuthScope.class);

        when(authScope.getScheme()).thenReturn(AuthPolicy.SPNEGO);
        assertNull(credentialsProvider.getCredentials(authScope));

        when(authScope.getScheme()).thenReturn(AuthPolicy.NTLM);
        assertThat(credentialsProvider.getCredentials(authScope), instanceOf(NTCredentials.class));
    }

    @Test
    public void usingNegotiateWhenBothNtlmAndNegotiateRequested() {
        selectAuthMethod(CredentialsConfig.AuthType.SPNEGO_KERBEROS);

        CredentialsProvider credentialsProvider = makeCredentialsProvider();
        AuthScope authScope = Mockito.mock(AuthScope.class);

        when(authScope.getScheme()).thenReturn(AuthPolicy.NTLM);
        assertNull(credentialsProvider.getCredentials(authScope));

        when(authScope.getScheme()).thenReturn(AuthPolicy.SPNEGO);
        assertThat(credentialsProvider.getCredentials(authScope), instanceOf(UsernamePasswordCredentials.class));
    }

    private CredentialsProvider makeCredentialsProvider() {
        SubmitContext context = Mockito.mock(WsdlSubmitContext.class);
        HttpContext httpContext = new BasicHttpContext();
        when(context.getProperty(SubmitContext.HTTP_STATE_PROPERTY)).thenReturn(httpContext);
        when(wsdlRequest.getWssPasswordType()).thenReturn(null);

        filter.filterAbstractHttpRequest(context, wsdlRequest);

        return (CredentialsProvider) httpContext.getAttribute(ClientContext.CREDS_PROVIDER);
    }

    @Test
    public void selectingAuthTypeNtlmReturnsNtlmIfNtlmRequested() {
        selectAuthMethod(CredentialsConfig.AuthType.NTLM);

        filter.filterAbstractHttpRequest(null, wsdlRequest);

        AuthScheme scheme = getSchemeFor(AuthPolicy.NTLM);
        assertThat(scheme, instanceOf(NTLMScheme.class));
    }

    @Test
    public void selectingAuthTypeSpnegoReturnsSpnegoIfSpnegoRequested() {
        selectAuthMethod(CredentialsConfig.AuthType.SPNEGO_KERBEROS);

        filter.filterAbstractHttpRequest(null, wsdlRequest);

        AuthScheme scheme = getSchemeFor(AuthPolicy.SPNEGO);
        assertThat(scheme, instanceOf(SPNegoScheme.class));
    }

    @Test
    public void selectingAuthTypeSpnegoReturnsNtlmIfNtlmRequested() {
        selectAuthMethod(CredentialsConfig.AuthType.SPNEGO_KERBEROS);

        filter.filterAbstractHttpRequest(null, wsdlRequest);

        AuthScheme scheme = getSchemeFor(AuthPolicy.NTLM);
        assertThat(scheme, instanceOf(NTLMScheme.class));
    }

    private AuthScheme getSchemeFor(String schemeName) {
        return HttpClientSupport.getAuthScheme(schemeName);
    }

    private void selectAuthMethod(CredentialsConfig.AuthType.Enum authType) {
        when(wsdlRequest.getAuthType()).thenReturn(authType.toString());
    }
}
