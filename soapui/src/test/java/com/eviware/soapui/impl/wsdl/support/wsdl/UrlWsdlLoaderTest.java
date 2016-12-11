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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.config.DefinitionCacheConfig;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class UrlWsdlLoaderTest {

    @Test
    public void cachesWsdl() throws Exception {
        File file = new File(UrlWsdlLoaderTest.class.getResource("/test6/TestService.wsdl").toURI());
        WsdlLoader loader = new UrlWsdlLoader(file.toURI().toURL().toString());

        DefinitionCacheConfig cachedWsdl = WsdlUtils.cacheWsdl(loader);
        assertThat(cachedWsdl.sizeOfPartArray(), is(4));
    }

    @Test
    public void urlWithoutBasicAuthentication() throws Exception {
        URL url = new URL("http://test/test6/TestService.wsdl");
        WsdlLoader loader = new UrlWsdlLoader(url.toString());

        assertNull(loader.getUsername());
        assertNull(loader.getPassword());
    }
    
    @Test
    public void urlWithBasicAuthentication() throws Exception {
        URL url = new URL("http://username:password@test/test6/TestService.wsdl");
        WsdlLoader loader = new UrlWsdlLoader(url.toString());
        
        assertThat(loader.getUsername(), is("username"));
        assertThat(loader.getPassword(), is("password"));
    }
    
    @Test
    public void urlWithBasicAuthenticationAndAtSymbol() throws Exception {
        URL url = new URL("http://username:passw@rd@test/test6/TestService.wsdl");
        WsdlLoader loader = new UrlWsdlLoader(url.toString());
        
        assertThat(loader.getUsername(), is("username"));
        assertThat(loader.getPassword(), is("passw@rd"));
    }
}
