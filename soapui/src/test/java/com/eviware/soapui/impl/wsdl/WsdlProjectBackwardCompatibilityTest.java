package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WsdlProjectBackwardCompatibilityTest
{

	WsdlProject project;

	@Before
	public void setUp() throws URISyntaxException, XmlException, IOException, SoapUIException
	{

		String fileName = SoapUI.class.getResource( "/soapui-projects/BasicMock-soapui-4.6.3-Project.xml" ).toURI().toString();
		project = new WsdlProject( fileName );
	}

	@Test
	public void verifyBackwardCompatibilityForBasic462ProjectWithMockService() throws XmlException, IOException, SoapUIException, URISyntaxException
	{

		assertThat( project.getMockServiceCount(), is( 2 ) );
		MockService mockService = project.getMockServiceByName( "MockService 1" );
		MockOperation mockOperation = mockService.getMockOperationByName( "ConversionRate" );
		MockResponse mockResponse = mockOperation.getMockResponseByName( "Response 1" );
		assertTrue( mockResponse.getResponseContent().contains( "10" ) );


	}

}
