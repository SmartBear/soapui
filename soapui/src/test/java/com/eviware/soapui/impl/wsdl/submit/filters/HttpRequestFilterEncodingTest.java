package com.eviware.soapui.impl.wsdl.submit.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpRequestFilterEncodingTest
{

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
						"%257cresource/sub%257cresource", "UTF-8", false, true ));
	}

	@Test
	public void encValueWithDisableUrlEncodingSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("%257cresource/sub%257cresource",
				httpRequestFilter.getEncodedValue(
						"%257cresource/sub%257cresource", "UTF-8", true, false ));
	}

	@Test
	public void encValueWithPreEncodedAndDisableUrlEncodingSettingsTest()
			throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("%257cresource/sub%257cresource",
				httpRequestFilter.getEncodedValue(
						"%257cresource/sub%257cresource", "UTF-8", true, true ));
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
								false, false ));
	}

	@Test
	public void decValueWithPreEncodedSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource/subresource|id",
				httpRequestFilter.getEncodedValue(
						"resource/subresource|id", "UTF-8", false, true ));
	}

	@Test
	public void decValueWithDisableUrlEncodingSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource/subresource|id",
				httpRequestFilter.getEncodedValue(
						"resource/subresource|id", "UTF-8", true, false ));
	}

	@Test
	public void decValueWithPreEncodedAndDisableUrlEncodingSettingsTest()
			throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource/subresource|id",
				httpRequestFilter.getEncodedValue(
						"resource/subresource|id", "UTF-8", true, true ));
	}

	@Test
	public void pathPreEncodedAndDisableUrlEncodingFalseTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource%2Fsubresource%7Cid",
				httpRequestFilter.getEncodedValue(
						"resource/subresource|id", "UTF-8", false, false ));
	}

	@Test
	public void valueWithSpacePreEncodedAndDisableUrlEncodingFalseTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource%2Fsub%20resource%7Cid",
				httpRequestFilter.getEncodedValue(
						"resource/sub resource|id", "UTF-8", false, false ));
	}

	@Test
	public void valueWithNoEncodingSchemeTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource%2Fsubresource%7Cid",
				httpRequestFilter.getEncodedValue(
						"resource/subresource|id", null, false, false ));
	}

	@Test(expected = UnsupportedEncodingException.class)
	public void valueWithInvalidEncodingTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		httpRequestFilter.getEncodedValue( "resource/subresource|id",
				"ZF", false, false );
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

}
