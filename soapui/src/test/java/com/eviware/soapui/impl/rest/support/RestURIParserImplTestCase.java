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

package com.eviware.soapui.impl.rest.support;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;

import static com.eviware.soapui.utils.CommonMatchers.anEmptyString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests RestURIParserImpl
 *
 * @author Shadid Chowdhury
 */
public class RestURIParserImplTestCase {
    private RestURIParserImpl restURIParser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() {
        restURIParser = null;
        thrown = null;
    }

    @Test
    public void encodedParamURITest() throws MalformedURLException {
        String uri = "http://service.com/api/1.2/json/search/search?title=Kill%20me";
        String expectedEndpoint = "http://service.com";
        String expectedPath = "/api/1.2/json/search/search";
        String expectedResourceName = "Search";
        String expectedQuery = "title=Kill me";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void decodedParamURITest() throws MalformedURLException {
        String uri = "http://service.com/api/1.2/json/search/search?title=Kill me";
        String expectedEndpoint = "http://service.com";
        String expectedPath = "/api/1.2/json/search/search";
        String expectedResourceName = "Search";
        String expectedQuery = "title=Kill me";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void noParameterTest() throws MalformedURLException {
        String uri = "http://service.com/rest/";
        String expectedEndpoint = "http://service.com";
        String expectedPath = "/rest/";
        String expectedResourceName = "Rest";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void noEndpointTest() throws MalformedURLException {
        String uri = "/abc?book=15;column=12";
        String expectedEndpoint = "";
        String expectedPath = "/abc";
        String expectedResourceName = "Abc";
        String expectedQuery = "book=15;column=12";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void numericResourceTest() throws MalformedURLException {
        String uri = "/1.2/json.search/search?title=Kill%20me";
        String expectedEndpoint = "";
        String expectedPath = "/1.2/json.search/search";
        String expectedResourceName = "Search";
        String expectedQuery = "title=Kill me";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void parametereizedURITest() throws MalformedURLException {
        String uri = "/conversation/date/{date}/time/{time}/?userId=1234";
        String expectedEndpoint = "";
        String expectedPath = "/conversation/date/{date}/time/{time}/";
        String expectedResourceName = "Time";
        String expectedQuery = "userId=1234";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void parametereizedURIWithEndpointTest() throws MalformedURLException {
        String uri = "/conversation/{date}";
        String expectedEndpoint = "";
        String expectedPath = "/conversation/{date}";
        String expectedResourceName = "Date";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void parametereizedFullURITest() throws MalformedURLException {
        String uri = "http://servo.com/conversation/date/{date}/time/{time}/?userId=1234";
        String expectedEndpoint = "http://servo.com";
        String expectedPath = "/conversation/date/{date}/time/{time}/";
        String expectedResourceName = "Time";
        String expectedQuery = "userId=1234";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void numbersInResourcePathTest() throws MalformedURLException {
        String uri = "http://bokus.se/books/ISBN-5012359";
        String expectedEndpoint = "http://bokus.se";
        String expectedPath = "/books/ISBN-5012359";
        String expectedResourceName = "ISBN-5012359";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void httpPrefixAddedWhenOmittedTest() throws MalformedURLException {
        String uri = "soapui.com";
        String expectedEndpoint = "http://soapui.com";
        String expectedPath = "";
        String expectedResourceName = "";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void onlyEndPointWithPortTest() throws MalformedURLException {
        String uri = "soapui.com:8080";
        String expectedEndpoint = "http://soapui.com:8080";
        String expectedPath = "";
        String expectedResourceName = "";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void withoutHTTPPrefixPortTest() throws MalformedURLException {
        String uri = "soapui.com:8080/services";
        String expectedEndpoint = "http://soapui.com:8080";
        String expectedPath = "/services";
        String expectedResourceName = "Services";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void uriWithLargeDomainTest() throws MalformedURLException {
        String uri = "soapui.local";
        String expectedEndpoint = "http://soapui.local";
        String expectedPath = "";
        String expectedResourceName = "";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void domainWithHyphenAndPortTest() throws MalformedURLException {
        String uri = "http://consys-qa-m09.websys.aol.com:8090/subscribers/subscriber";
        String expectedEndpoint = "http://consys-qa-m09.websys.aol.com:8090";
        String expectedPath = "/subscribers/subscriber";
        String expectedResourceName = "Subscriber";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }

    @Test
    public void endpointsWithSubDomainTest() throws MalformedURLException {
        String uri = "api.soapui.com/services";
        String expectedEndpoint = "http://api.soapui.com";
        String expectedPath = "/services";
        String expectedResourceName = "Services";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }


    @Test
    public void queryParamRightAfterSlashTest() throws MalformedURLException {
        String uri = "http://ws.spotify.com/lookup/1/?uri=spotify:artist:4YrKBkKSVeqDamzBPWVnSJ";
        String expectedEndpoint = "http://ws.spotify.com";
        String expectedPath = "/lookup/1/";
        String expectedResourceName = "1";
        String expectedQuery = "uri=spotify:artist:4YrKBkKSVeqDamzBPWVnSJ";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }

    @Test
    public void ipv4AddressWithPortTest() throws MalformedURLException {
        String uri = "http://10.10.1.230:8090/subscribers/subscriber";
        String expectedEndpoint = "http://10.10.1.230:8090";
        String expectedPath = "/subscribers/subscriber";
        String expectedResourceName = "Subscriber";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }

    @Test
    public void ipv4AddressWithoutSchemeTest() throws MalformedURLException {
        String uri = "10.10.1.230:8090/subscribers/subscriber";
        String expectedEndpoint = "http://10.10.1.230:8090";
        String expectedPath = "/subscribers/subscriber";
        String expectedResourceName = "Subscriber";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }

    @Test
    public void parseTemplateParameterAndMatirxParametersCorrectly() throws MalformedURLException {
        String uri = "http://soapui.org/{templateParam};matrixParam=matrixValue?queryParam=value";
        String expectedEndpoint = "http://soapui.org";
        String expectedPath = "/{templateParam};matrixParam=matrixValue";
        String expectedResourceName = "TemplateParam";
        String expectedQuery = "queryParam=value";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }

    @Test
    public void ipv6AddressWithPortTest() throws MalformedURLException {
        String uri = "http://2001:0db8:85a3:0000:0000:8a2e:0370:7334:8090/subscribers/subscriber";
        String expectedEndpoint = "http://2001:0db8:85a3:0000:0000:8a2e:0370:7334:8090";
        String expectedPath = "/subscribers/subscriber";
        String expectedResourceName = "Subscriber";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);

    }

    @Test
    public void onlyEndpointTest() throws MalformedURLException {
        String uri = "http://www.google.se";
        String expectedEndpoint = "http://www.google.se";
        String expectedPath = "";
        String expectedResourceName = "";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void onlyEndpointWithSlashAtEndTest() throws MalformedURLException {
        String uri = "http://www.google.se/";
        String expectedEndpoint = "http://www.google.se";
        String expectedPath = "/";
        String expectedResourceName = "";
        String expectedQuery = "";

        restURIParser = new RestURIParserImpl(uri);

        assertURIParsedCorrectly(expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser);
    }

    @Test
    public void invalidProtocol() throws MalformedURLException {
        String uri = "ftp://spotify.com/api/?userId=1234";

        thrown.expect(MalformedURLException.class);
        thrown.expectMessage("unsupported protocol");

        restURIParser = new RestURIParserImpl(uri);

    }

    @Test
    public void invalidHost() throws MalformedURLException {
        String uri = "http://sp\\sd.com/api/?userId=1234";

        thrown.expect(MalformedURLException.class);
        thrown.expectMessage("Invalid");

        restURIParser = new RestURIParserImpl(uri);

    }

    @Test
    public void nullURI() throws MalformedURLException {
        String uri = null;

        thrown.expect(MalformedURLException.class);
        thrown.expectMessage("Empty");

        restURIParser = new RestURIParserImpl(uri);

    }

    @Test
    public void emptyURI() throws MalformedURLException {
        String uri = "";

        thrown.expect(MalformedURLException.class);
        thrown.expectMessage("Empty");

        restURIParser = new RestURIParserImpl(uri);

    }

    @Test
    public void handlesEmptyNameWithMatrixParameters() throws Exception {
        restURIParser = new RestURIParserImpl("http://example.com/;JSESSIONID=abc");

        assertThat(restURIParser.getResourceName(), is(anEmptyString()));
    }

    @Test
    public void handlesEmptyNameWithQueryParameters() throws Exception {
        restURIParser = new RestURIParserImpl("http://example.com/?articleId=234");

        assertThat(restURIParser.getResourceName(), is(anEmptyString()));
    }

    private void assertURIParsedCorrectly(String expectedEndpoint,
                                          String expectedPath,
                                          String expectedResourceName,
                                          String expectedQuery, RestURIParserImpl restURIParser) throws MalformedURLException {
        assertEquals(expectedEndpoint, restURIParser.getEndpoint());
        assertEquals(expectedPath, restURIParser.getResourcePath());
        assertEquals(expectedResourceName, restURIParser.getResourceName());
        assertEquals(expectedQuery, restURIParser.getQuery());
    }
}
