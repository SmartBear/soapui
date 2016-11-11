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

package com.eviware.soapui.support.httpclient;

import com.eviware.soapui.impl.wsdl.support.http.NTLMSchemeFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class JCIFSTest {

    @Test
    public void test() throws ParseException, IOException {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            httpClient.getAuthSchemes().register(AuthPolicy.NTLM, new NTLMSchemeFactory());
            httpClient.getAuthSchemes().register(AuthPolicy.SPNEGO, new NTLMSchemeFactory());

            NTCredentials creds = new NTCredentials("testuser", "kebabsalladT357", "", "");
            httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);

            HttpHost target = new HttpHost("dev-appsrv01.eviware.local", 81, "http");
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpget = new HttpGet("/");

            HttpResponse response1 = httpClient.execute(target, httpget, localContext);
            HttpEntity entity1 = response1.getEntity();

            //		System.out.println( "----------------------------------------" );
            //System.out.println( response1.getStatusLine() );
            //		System.out.println( "----------------------------------------" );
            if (entity1 != null) {
                //System.out.println( EntityUtils.toString( entity1 ) );
            }
            //		System.out.println( "----------------------------------------" );

            // This ensures the connection gets released back to the manager
            EntityUtils.consume(entity1);

            Assert.assertEquals(response1.getStatusLine().getStatusCode(), 200);
        } catch (UnknownHostException e) {
            /* ignore */
        } catch (HttpHostConnectException e) {
            /* ignore */
        } catch (SocketException e) {
			/* ignore */
        }

        Assert.assertTrue(true);
    }

}
