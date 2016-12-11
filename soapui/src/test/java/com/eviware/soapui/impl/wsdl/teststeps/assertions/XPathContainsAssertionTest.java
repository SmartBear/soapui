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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.model.testsuite.AssertionException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class XPathContainsAssertionTest {
    private String testResponse;
    private XPathContainsAssertion assertion;
    private String testBody;

    @Before
    public void setUp() throws Exception {
        testResponse = readResource("/testResponse.xml");
        testBody = readResource("/testBody.xml");
        assertion = new XPathContainsAssertion(TestAssertionConfig.Factory.newInstance(), null);
    }

    @Test
    public void idIsSetOnCreation() throws Exception {
        assertThat(assertion.getConfig().isSetId(), is(true));
        assertThat(assertion.getId(), is(notNullValue()));
    }

    @Test
    public void testCreate() throws Exception {
        TestAssertionConfig config = createConfig("testPath", "testContent");

        XPathContainsAssertion assertion = new XPathContainsAssertion(config, null);

        assertEquals("testPath", assertion.getPath());
        assertEquals("testContent", assertion.getExpectedContent());

        XmlObject conf = assertion.createConfiguration();
        String str = conf.xmlText();
    }

    @Test
    public void testFullContentMatch() throws Exception {
        assertion.setPath("/");
        assertion.setExpectedContent(testResponse);

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testFullBodyMatch() throws Exception {
        assertion.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "//urn:searchResponse");

        assertion.setExpectedContent(testBody);

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testAttributeMatch() throws Exception {
        assertion.setPath("declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';"
                + "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/@hitCount");
        assertion.setExpectedContent("131");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testElementMatch() throws Exception {
        assertion.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "//urn:searchResponse/urn1:searchResult/company[2]/companyName");
        assertion.setExpectedContent("<companyName>Bonnier Otto Karl Adam</companyName>");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testElementTextMatch() throws Exception {
        assertion.setPath("declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';"
                + "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/company[2]/companyName/text()");
        assertion.setExpectedContent("Bonnier Otto Karl Adam");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testFragmentMatch() throws Exception {
        assertion.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "//urn:searchResponse/urn1:searchResult/company[4]");
        assertion.setExpectedContent(readResource("/testFragment.xml"));
        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testFragmentWithCommentMatch() throws Exception {
        assertion.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "//urn:searchResponse/urn1:searchResult/company[4]");

        assertion.setAllowWildcards(true);
        assertion.setExpectedContent(readResource("/testFragmentWithCommentAndWildcard.xml"));

        try {
            assertion.assertContent(testResponse, new WsdlSubmitContext(null), "");
            assertFalse("Assertion should have failed", true);
        } catch (AssertionException e) {
        }

        assertion.setIgnoreComments(true);

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testAnyFragmentMatch() throws Exception {
        assertion.setExpectedContent(readResource("/testFragment.xml"));
        assertion.setPath("//company");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    // TODO Disabling for now due to character encoding hell
    @Ignore
    @Test
    public void testLastElementTextMatch() throws Exception {
        assertion.setPath("//company[last()]/companyName/text()");
        assertion.setExpectedContent("Bonnier Zoo Förlag AB");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testElementCountMatch() throws Exception {
        assertion.setPath("count(//company)");
        assertion.setExpectedContent("20");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testAnyElementTextMatch() throws Exception {
        assertion.setPath("declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';"
                + "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/company/companyName/text()");
        assertion.setExpectedContent("Bonnier Otto Karl Adam");

        assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }

    @Test
    public void testAnyElementTextFail() throws Exception {
        assertion.setPath("declare namespace env='http://schemas.xmlsoap.org/soap/envelope/';"
                + "declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "declare namespace urn1='urn:v1:companysearch:common:bis.bonnier.se';"
                + "/env:Envelope/env:Body/urn:searchResponse/urn1:searchResult/company/companyName/text()");
        assertion.setExpectedContent("Bonnier Otto Karl Adams");

        try {
            assertNotNull(assertion.assertContent(testResponse, new WsdlSubmitContext(null), ""));
            assertFalse("assertion should have failed", true);
        } catch (Exception e) {
        }
    }

    @Test
    public void testComplexMatch() throws Exception {
        String response = "<response><book>" + "<bookID>1012</bookID>" + "<author type=\"humanBeing\" href=\"#ID_1\"/>"
                + "<title type=\"string\">Birds</title>" + "</book>" + "<humanBeing id=\"ID_1\">"
                + "<name>Stephen King</name>" + "</humanBeing></response>";

        assertion.setExpectedContent("Stephen King");
        // assertion.setPath(
        // "//*[@id=substring(//book/bookID[text()='1012']/following-sibling::author/@href,2)]"
        // );

        assertion
                .setPath("//*[@id=substring(//book/bookID[text()='1012']/following-sibling::author/@href,2)]/name/text()");
        // assertion.setPath( "//*[@id='ID_1']/name/text()" );
        assertNotNull(assertion.assertContent(response, new WsdlSubmitContext(null), ""));
    }

    private String readResource(String string) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(string)));
        StringBuffer result = new StringBuffer();

        String line = reader.readLine();
        while (line != null) {
            result.append(line);
            line = reader.readLine();
        }

        return result.toString();
    }

    private TestAssertionConfig createConfig(String path, String content) throws XmlException {
        return TestAssertionConfig.Factory.parse("<con:configuration xmlns:con=\"http://eviware.com/soapui/config\">"
                + "<path>" + path + "</path><content>" + content + "</content></con:configuration>");
    }

}
