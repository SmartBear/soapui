package com.eviware.soapui.impl.wsdl.submit.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpRequestFilterPathEncodingTestCase {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(
				HttpRequestFilterPathEncodingTestCase.class);
	}

	HttpRequestFilter httpRequestFilter;

	@Before
	public void setUp() {
		httpRequestFilter = new HttpRequestFilter();
	}

	@After
	public void tearDown() {
		httpRequestFilter = null;
	}

	@Test
	public void encPathWithPreEncodedSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("%257cresource/sub%257cresource",
				httpRequestFilter.getPathAccordingToSettings(
						"%257cresource/sub%257cresource", "UTF-8", false, true));
	}

	@Test
	public void encPathWithDisableUrlEncodingSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("%257cresource/sub%257cresource",
				httpRequestFilter.getPathAccordingToSettings(
						"%257cresource/sub%257cresource", "UTF-8", true, false));
	}

	@Test
	public void encPathWithPreEncodedAndDisableUrlEncodingSettingsTest()
			throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("%257cresource/sub%257cresource",
				httpRequestFilter.getPathAccordingToSettings(
						"%257cresource/sub%257cresource", "UTF-8", true, true));
	}

	@Test
	public void encPathPreEncodedAndDisableUrlEncodingFalseTest()
			throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("%257cresource/sub%257cresource",
				httpRequestFilter
						.getPathAccordingToSettings(
								"%257cresource/sub%257cresource", "UTF-8",
								false, false));
	}

	@Test
	public void decPathWithPreEncodedSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource/subresource|id",
				httpRequestFilter.getPathAccordingToSettings(
						"resource/subresource|id", "UTF-8", false, true));
	}

	@Test
	public void decPathWithDisableUrlEncodingSettingsTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource/subresource|id",
				httpRequestFilter.getPathAccordingToSettings(
						"resource/subresource|id", "UTF-8", true, false));
	}

	@Test
	public void decPathWithPreEncodedAndDisableUrlEncodingSettingsTest()
			throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource/subresource|id",
				httpRequestFilter.getPathAccordingToSettings(
						"resource/subresource|id", "UTF-8", true, true));
	}

	@Test
	public void PathPreEncodedAndDisableUrlEncodingFalseTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource%2Fsubresource%7Cid",
				httpRequestFilter.getPathAccordingToSettings(
						"resource/subresource|id", "UTF-8", false, false));
	}
	
	@Test
	public void PathWithNoEncodingSchemeTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		assertEquals("resource%2Fsubresource%7Cid",
				httpRequestFilter.getPathAccordingToSettings(
						"resource/subresource|id", null, false, false));
	}

	@Test(expected = UnsupportedEncodingException.class)
	public void PathWithInvalidEncodingTest() throws Exception {
		// String getPathAccordingToSettings(String path, String encoding,
		// boolean isDisableUrlEncoding, boolean isPreEncoded )
		httpRequestFilter.getPathAccordingToSettings("resource/subresource|id",
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

}
