package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.SettingsConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.mock.MockResponse;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WsdlMockResponseTest
{

	MockResponse mockResponse;
	private CompressedStringConfig compressedStringConfig;

	@Before
	public void setUp()
	{
		WsdlMockOperation mockOperation = mock( WsdlMockOperation.class );
		XmlBeansSettingsImpl mockedSettings = mock( XmlBeansSettingsImpl.class );
		when( mockOperation.getSettings() ).thenReturn( mockedSettings );

		compressedStringConfig = mock(CompressedStringConfig.class);

		MockResponseConfig mockResponseConfig = mock( MockResponseConfig.class );
		SettingsConfig mockedResponseSettings = mock( SettingsConfig.class );
		when(mockResponseConfig.getSettings()).thenReturn( mockedResponseSettings );
		when( mockResponseConfig.getResponseContent()).thenReturn( compressedStringConfig );

		mockResponse = new WsdlMockResponse( mockOperation, mockResponseConfig );
	}

	@Test
	public void testGetResponseContentWithGZIPCompression() throws Exception
	{
		when(compressedStringConfig.getCompression()).thenReturn( "gzip" );

		byte[] gZippedBytes =  CompressionSupport.compress("gzip", "Awesomeness".getBytes());
		gZippedBytes = Base64.encodeBase64( gZippedBytes );
		when( compressedStringConfig.getStringValue() ).thenReturn( new String(gZippedBytes) );

		assertThat( mockResponse.getResponseContent(), is( "Awesomeness" ) );
	}

	@Test
	public void testGetResponseContentWithNoCompression() throws Exception
	{
		when(compressedStringConfig.getCompression()).thenReturn( "none" );
		when(compressedStringConfig.getStringValue()).thenReturn( "Awesomeness" );

		assertThat(mockResponse.getResponseContent(), is("Awesomeness"));
	}
}
