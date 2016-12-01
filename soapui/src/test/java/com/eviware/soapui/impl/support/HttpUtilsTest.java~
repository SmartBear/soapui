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
    public void propertyExpansionsShouldNotGoToLowerCaseEither() throws Exception {
        assertEquals(propertyExpansionExample, HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(propertyExpansionExample));
    }
}
