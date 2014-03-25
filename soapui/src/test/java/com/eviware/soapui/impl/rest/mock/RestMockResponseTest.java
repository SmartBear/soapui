package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class RestMockResponseTest
{

	private RestMockResponse restMockResponse;

	@Before
	public void setUp() throws SoapUIException
	{
		RestMockAction restMockAction = ModelItemFactory.makeRestMockAction();
		restMockAction.getMockService().setName( "REST Mock Service" );
		restMockResponse = restMockAction.addNewMockResponse( "REST Mock Response" );
	}

	@Test
	public void getsCorrectEncodingValue() throws Exception
	{
		String contentType = "application/atom+xml; charset=UTF-8";
		String[] parameters = contentType.split( ";" );
		assertThat( restMockResponse.getEncodingValue( parameters ), is( "UTF-8" ) );
	}

	@Test
	public void getsEncodingValueFromMultipleParameters() throws Exception
	{
		String contentType = "application/atom+xml; charset=UTF-8; type=feed";
		String[] parameters = contentType.split( ";" );
		assertThat( restMockResponse.getEncodingValue( parameters ), is( "UTF-8" ) );
	}

	@Test
	public void getsEncodingValueWhenEncodingValueIsLastParam() throws Exception
	{
		String contentType = "application/atom+xml; type=feed; charset=UTF-8; ";
		String[] parameters = contentType.split( ";" );
		assertThat( restMockResponse.getEncodingValue( parameters ), is( "UTF-8" ) );
	}

	@Test
	public void doesNoGetAnyEncodingValueWhenContentTypeDoesNotHaveEncoding() throws Exception
	{
		String contentType = "application/atom+xml; type=feed; boo=false";
		String[] parameters = contentType.split( ";" );
		assertNull( restMockResponse.getEncodingValue( parameters ) );
	}

}
