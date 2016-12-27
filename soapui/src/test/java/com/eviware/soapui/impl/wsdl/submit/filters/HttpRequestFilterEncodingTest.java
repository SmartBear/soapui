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

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HttpRequestFilterEncodingTest {

    HttpRequestFilter httpRequestFilter;

    @Before
    public void setUp() {
        httpRequestFilter = new HttpRequestFilter();
    }

    @Test
    public void encValueWithPreEncodedSettingsTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("%257cresource/sub%257cresource",
                httpRequestFilter.getEncodedValue(
                        "%257cresource/sub%257cresource", "UTF-8", false, true));
    }

    @Test
    public void encValueWithDisableUrlEncodingSettingsTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("%257cresource/sub%257cresource",
                httpRequestFilter.getEncodedValue(
                        "%257cresource/sub%257cresource", "UTF-8", true, false));
    }

    @Test
    public void encValueWithPreEncodedAndDisableUrlEncodingSettingsTest()
            throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("%257cresource/sub%257cresource",
                httpRequestFilter.getEncodedValue(
                        "%257cresource/sub%257cresource", "UTF-8", true, true));
    }

    @Test
    public void encPathPreEncodedAndDisableUrlEncodingFalseTest()
            throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("%257cresource/sub%257cresource",
                httpRequestFilter
                        .getEncodedValue(
                                "%257cresource/sub%257cresource", "UTF-8",
                                false, false));
    }

    @Test
    public void decValueWithPreEncodedSettingsTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("resource/subresource|id",
                httpRequestFilter.getEncodedValue(
                        "resource/subresource|id", "UTF-8", false, true));
    }

    @Test
    public void decValueWithDisableUrlEncodingSettingsTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("resource/subresource|id",
                httpRequestFilter.getEncodedValue(
                        "resource/subresource|id", "UTF-8", true, false));
    }

    @Test
    public void decValueWithPreEncodedAndDisableUrlEncodingSettingsTest()
            throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("resource/subresource|id",
                httpRequestFilter.getEncodedValue(
                        "resource/subresource|id", "UTF-8", true, true));
    }

    @Test
    public void pathPreEncodedAndDisableUrlEncodingFalseTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("resource%2Fsubresource%7Cid",
                httpRequestFilter.getEncodedValue(
                        "resource/subresource|id", "UTF-8", false, false));
    }

    @Test
    public void valueWithSpacePreEncodedAndDisableUrlEncodingFalseTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("resource%2Fsub%20resource%7Cid",
                httpRequestFilter.getEncodedValue(
                        "resource/sub resource|id", "UTF-8", false, false));
    }

    @Test
    public void valueWithNoEncodingSchemeTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        assertEquals("resource%2Fsubresource%7Cid",
                httpRequestFilter.getEncodedValue(
                        "resource/subresource|id", null, false, false));
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void valueWithInvalidEncodingTest() throws Exception {
        // String getPathAccordingToSettings(String path, String encoding,
        // boolean isDisableUrlEncoding, boolean isPreEncoded )
        httpRequestFilter.getEncodedValue("resource/subresource|id",
                "ZF", false, false);
    }

    @Test
    public void alreadyEncodedTest() throws Exception {
        assertTrue(httpRequestFilter.isAlreadyEncoded(
                "%257cresource/sub%257cresource", "UTF-8"));
    }

    @Test
    public void alreadyNotEncodedTest() throws Exception {
        assertFalse(httpRequestFilter.isAlreadyEncoded(
                "resource/subresource|id", "UTF-8"));
    }

    @Test
    public void encodingNullValueReturnsEmptyString() throws Exception {
        assertThat(httpRequestFilter.getEncodedValue(null, null, false, false), is(""));
    }
}
