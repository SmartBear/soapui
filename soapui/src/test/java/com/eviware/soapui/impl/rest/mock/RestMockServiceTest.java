package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod.GET;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class RestMockServiceTest
{
	private RestMockService restMockService;
	private RestRequest restRequest;

	@Before
	public void setup() throws SoapUIException
	{
		restMockService = ModelItemFactory.makeRestMockService();
		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setPath( "aNicePath" );
		restRequest.setMethod( GET );
	}

	@Test
	public void shouldAddNewMockAction() throws SoapUIException
	{
		restMockService.addNewMockAction( restRequest );

		RestMockAction restMockAction = restMockService.getMockOperationAt( 0 );
		assertEquals( "aNicePath", restMockAction.getPath() );
		assertEquals( GET, restMockAction.getMethod() );
	}

	@Test
	public void isConstructedWithActionsAndResponses() throws SoapUIException
	{
		Project project = ModelItemFactory.makeWsdlProject();
		RESTMockServiceConfig config = createRestMockServiceConfig();

		RestMockService mockService = new RestMockService( project, config );

		assertEquals( config.getName(), mockService.getName() );
		RestMockAction mockOperation = mockService.getMockOperationAt( 0 );
		RestMockResponse mockResponse = mockOperation.getMockResponseAt( 0 );
		assertEquals( "Some content", mockResponse.getResponseContent() );
	}

	private RESTMockServiceConfig createRestMockServiceConfig()
	{
		RESTMockServiceConfig config = RESTMockServiceConfig.Factory.newInstance();
		config.setName( "Da service" );
		RESTMockActionConfig mockActionConfig = config.addNewRestMockAction();
		mockActionConfig.setName( "Da action" );
		RESTMockResponseConfig mockResponseConfig = mockActionConfig.addNewResponse();
		mockResponseConfig.setName( "Da response" );
		CompressedStringConfig responseContent = CompressedStringConfig.Factory.newInstance();
		responseContent.setStringValue( "Some content" );
		mockResponseConfig.setResponseContent( responseContent );
		return config;
	}
}
