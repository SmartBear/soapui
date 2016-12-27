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

package com.eviware.soapui.impl.support;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by randerson on 15/08/2016.
 */
public class HttpUtilsTest {

    private static final String mixedCaseURL1 = "http://localhost:9001/Test/A001";
    private static final String mixedCaseURLWithoutProtocol = "localhost:9001/Test/A001";
    private static final String trailingSpaceURL1 = "   http://localhost:9001/Test/A001   ";
    private static final String propertyExpansionExample = "${#Project#prop1}";
    private static final String uppercaseURLHTTP = "HTTP://LOCALHOST:9001/TEST/A001";
    private static final String uppercaseURLHTTPS = "HTTPS://LOCALHOST:9001/TEST/A001";

    @Test
    public void nullURLShouldBeReturnNull() throws Exception{
        assertEquals(null, HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(null));
    }

    @Test
    public void emptyURLShouldBeReturnEmpty() throws Exception{
        assertEquals("", HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(""));
    }

    @Test
    public void mixedCaseURLsShouldNotGetForcedToLowerCase() throws Exception {
        assertEquals(mixedCaseURL1, HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(mixedCaseURL1));
    }

    @Test
    public void trailingSpaceURLsShouldStillGetTrimmed() throws Exception {
        assertEquals("http://localhost:9001/Test/A001", HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(trailingSpaceURL1));
    }

    @Test
    public void missingProtocolURLsShouldGetHTTPProtocolAdded() throws Exception {
        assertEquals("http://localhost:9001/Test/A001", HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(mixedCaseURLWithoutProtocol));
    }

    @Test
    public void entirelyUppercaseHTTPURLShouldRemainUnchanged() throws Exception {
        assertEquals("HTTP://LOCALHOST:9001/TEST/A001", HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(uppercaseURLHTTP));
    }

    @Test
    public void entirelyUppercaseHTTPSURLShouldRemainUnchanged() throws Exception {
        assertEquals("HTTPS://LOCALHOST:9001/TEST/A001", HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(uppercaseURLHTTPS));
    }

    @Test
    public void propertyExpansionsShouldNotGoToLowerCaseEither() throws Exception {
        assertEquals(propertyExpansionExample, HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(propertyExpansionExample));
    }
}

