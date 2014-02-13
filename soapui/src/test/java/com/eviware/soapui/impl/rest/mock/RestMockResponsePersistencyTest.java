package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RestMockResponsePersistencyTest
{
	private MockResponse restMockResponse;
	private Integer httpStatusCode = HttpStatus.SC_NOT_FOUND;
	private String projectFileName = "RESTMockResponseTest.xml";
	private String restMockServiceName = "REST Mock Service";

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = new WsdlProject();
		RestMockService restMockService = project.addNewRestMockService( restMockServiceName );
		RestMockAction restMockAction = ModelItemFactory.makeRestMockAction( restMockService );
		restMockResponse = restMockAction.addNewMockResponse( "REST Mock Response" );
		restMockResponse.setResponseHttpStatus( httpStatusCode );
		project.saveAs( projectFileName );

	}

	@After
	public void tearDown()
	{
		File file = new File( projectFileName );
		if( file.exists() )
		{
			file.delete();
		}
	}

	@Test
	public void testResponseHttpStatusIsPersisted() throws Exception
	{
		WsdlProject retrievedProject = new WsdlProject( projectFileName );
		RestMockResponse mockResponse = retrievedProject.getRestMockServiceByName( restMockServiceName ).
				getMockOperationAt( 0 ).getMockResponseAt( 0 );

		assertThat( mockResponse.getResponseHttpStatus(), is(httpStatusCode.toString()));

	}


}
