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
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
 * This test covers bug reported in SOAPUI-3935
 */
public class XQueryContainsTest {
    @Mock
    private Assertable assertable;
    @Mock
    private SubmitContext context;

    private String response, testBodyWithDifferentNSPrefix, testBodyWithComments, testResponse;
    private XQueryContainsAssertion assertion, assertionBody;

    @Before
    public void setUp() throws Exception {
        response = readResource("/xqueryassertion/response.xml");
        assertion = new XQueryContainsAssertion(TestAssertionConfig.Factory.newInstance(), assertable);
        
        testBodyWithDifferentNSPrefix = readResource ("/xqueryassertion/testBodyWithDifferentNSPrefix.xml");
        testBodyWithComments = readResource ("/xqueryassertion/testBodyWithComments.xml");
        testResponse = readResource("/testResponse.xml");
        assertionBody = new XQueryContainsAssertion(TestAssertionConfig.Factory.newInstance(), assertable);
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

    @Test(expected = AssertionException.class)
    public void negativeRouteTest() throws AssertionException {
        assertion.setPath("count(DirectionsResponse/route) > 10");
        assertion.setExpectedContent("true");
        assertion.assertContent(response, context, XQueryContainsAssertion.ID);
    }

    @Test
    public void positiveRouteTest() throws AssertionException {
        assertion.setPath("count(DirectionsResponse/route) > 10");
        assertion.setExpectedContent("false");
        String result = assertion.assertContent(response, context, XQueryContainsAssertion.ID);
        assertEquals("Not matched expected!", "XQuery Match matches content for [count(DirectionsResponse/route) > 10]",
                result);
    }

    @Test(expected = AssertionException.class)
    public void negativeLatitudeTest() throws AssertionException {
        assertion.setPath("/DirectionsResponse/route[1]/leg[1]/step[1]/start_location[1]/lat[1]");
        assertion.setExpectedContent("<lat>-35.9286900</lat>");
        assertion.assertContent(response, context, XQueryContainsAssertion.ID);
    }

    @Test
    public void positiveLatitudeTest() throws AssertionException {
        assertion.setPath("/DirectionsResponse/route[1]/leg[1]/step[1]/start_location[1]/lat[1]");
        assertion.setExpectedContent("<lat>-34.9286900</lat>");
        String result = assertion.assertContent(response, context, XQueryContainsAssertion.ID);
        assertEquals("Not matched expected!", "XQuery Match matches content for [/DirectionsResponse/route[1]/leg[1]/step[1]/start_location[1]/lat[1]]",
                result);
    }
    
    @Test
    public void positiveWildcardTest() throws AssertionException {
        assertion.setPath("for $f in //step[1]/html_instructions return $f");
        assertion.setExpectedContent("<html_instructions>Head *</html_instructions>");
        assertion.setAllowWildcards(true);
        assertion.assertContent(response, context, XQueryContainsAssertion.ID);
    }

    @Test(expected = AssertionException.class)
    public void negativeWildcardTest() throws AssertionException {
        assertion.setPath("for $f in //step[1]/html_instructions return $f");
        assertion.setExpectedContent("<html_instructions>ABC *</html_instructions>");
        assertion.setAllowWildcards(true);
        assertion.assertContent(response, context, XQueryContainsAssertion.ID);
    }
    
    @Test(expected = AssertionException.class)
    public void negativeIgnorePrefixTest() throws AssertionException {
        assertionBody.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "//urn:searchResponse");

        assertionBody.setExpectedContent(testBodyWithDifferentNSPrefix);
        assertNotNull(assertionBody.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }
  
    @Test
    public void positiveIgnorePrefixTest() throws AssertionException {
        assertionBody.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "//urn:searchResponse");

        assertionBody.setExpectedContent(testBodyWithDifferentNSPrefix);
        assertionBody.setIgnoreNamespaceDifferences(true);
        assertNotNull(assertionBody.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }
    
    @Test(expected = AssertionException.class)
    public void negativeIgnoreCommentsTest() throws AssertionException {
        assertionBody.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "//urn:searchResponse");

        assertionBody.setExpectedContent(testBodyWithComments);
        assertNotNull(assertionBody.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }
    
    @Test
    public void positiveIgnoreCommentsTest() throws AssertionException {
        assertionBody.setPath("declare namespace urn='urn:schema:v1:companyservice:applications:bis.bonnier.se';"
                + "//urn:searchResponse");

        assertionBody.setExpectedContent(testBodyWithComments);
        assertionBody.setIgnoreComments(true);
        assertNotNull(assertionBody.assertContent(testResponse, new WsdlSubmitContext(null), ""));
    }
}
