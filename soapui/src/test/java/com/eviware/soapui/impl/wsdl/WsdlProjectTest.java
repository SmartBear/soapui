package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestRequest;

import static com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod.*;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class WsdlProjectTest
{
	private final String restMockResponseContent = "Some response";
	private WsdlProject project;
	private final String restMockServiceName = "Another Great Wow";
	private final String restMockResponseName = "Teh Response";

	@Before
	public void setUp() throws Exception
	{
		String fileName = SoapUI.class.getResource( "/soapui-projects/BasicMock-soapui-4.6.3-Project.xml" ).toURI().toString();
		project = new WsdlProject( fileName );
	}

	@Test
	public void shouldSaveAndReloadRestMockServices() throws Exception
	{
		String expectedName = "Teh Awesome Mock Service";
		project.addNewRestMockService( expectedName );

		WsdlProject reloadedProject = saveAndReloadProject( project );

		RestMockService restMockService = reloadedProject.getRestMockServiceByName( expectedName );
		assertThat( restMockService, notNullValue() );
		assertThat( restMockService.getName(), is( expectedName ) );
	}

	@Test
	public void shouldSaveAndReloadRestMockResponses() throws Exception
	{

		addRestMockResponseToProject();

		WsdlProject reloadedProject = saveAndReloadProject( project );

		RestMockService reloadedMockService = reloadedProject.getRestMockServiceByName( restMockServiceName );
		RestMockResponse reloadedMockResponse = reloadedMockService.getMockOperationAt( 0 ).getMockResponseAt( 0 );
		assertThat( reloadedMockResponse, notNullValue() );
		assertThat( reloadedMockResponse.getName(), is( restMockResponseName ) );
		assertThat( reloadedMockResponse.getResponseContent(), is( restMockResponseContent ) );
	}

	private void addRestMockResponseToProject() throws SoapUIException
	{
		RestMockService restMockService = project.addNewRestMockService( restMockServiceName );
		RestRequest restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setMethod( GET );
		restRequest.setName( "REST Mock Action" );
		restRequest.setPath( "Resource/path/subpath" );
		RestMockAction restMockAction = restMockService.addNewMockAction( restRequest );
		RestMockResponse mockResponse = restMockAction.addNewMockResponse( "Response 1" );
		mockResponse.setResponseContent( restMockResponseContent );
		mockResponse.setName( restMockResponseName );
	}

	protected WsdlProject saveAndReloadProject( WsdlProject project ) throws Exception
	{
		File tempFile = File.createTempFile( "soapuitemptestfile", ".xml" );
		tempFile.deleteOnExit();
		project.saveIn( tempFile );
		return new WsdlProject( tempFile.toURI().toString() );
	}
}
