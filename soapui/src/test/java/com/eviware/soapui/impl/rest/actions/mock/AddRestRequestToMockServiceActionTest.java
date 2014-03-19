package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Null;

import java.util.List;

import static com.eviware.soapui.impl.rest.HttpMethod.GET;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AddRestRequestToMockServiceActionTest
{
	private static final String ONE_HEADER = "oneHeader";
	private static final String ANOTHER_HEADER = "anotherHeader";
	private static final String HEADER_STATUS = "#status#";
	private static final String HEADER_CONTENT_LENGTH = "Content-Length";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private final String requestPath = "/somepath";
	AddRestRequestToMockServiceAction action = new AddRestRequestToMockServiceAction();
	RestRequest restRequest;
	Object notUsed = null;
	String mockServiceName = "Mock Service1 1";
	private XDialogs originalDialogs;
	private WsdlProject project;

	@Before
	public void setUp() throws Exception
	{
		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setMethod( GET );
		restRequest.setPath( requestPath );
		mockPromptDialog();
		project = restRequest.getRestMethod().getInterface().getProject();

		setUpResponse();
	}

	public void setUpResponse()
	{
		HttpResponse response = mock( HttpResponse.class );

		StringToStringsMap headers = new StringToStringsMap(  );
		headers.add( ONE_HEADER, "oneValue" );
		headers.add( ANOTHER_HEADER, "anotherValue" );
		headers.add( HEADER_STATUS, "HTTP/1.1 200 OK" );
		headers.add( HEADER_CONTENT_LENGTH, "456" );
		headers.add( HEADER_CONTENT_TYPE, "application/xml" );

		when( response.getResponseHeaders() ).thenReturn( headers );
		when( response.getContentType() ).thenReturn( "application/xml" );

		restRequest.setResponse( response, null );
	}

	@After
	public void tearDown()
	{
		UISupport.setDialogs( originalDialogs );
	}

	@Test
	public void shouldSaveRestMockWithSetNameToProject()
	{
		action.perform( restRequest, notUsed );
		List<RestMockService> serviceList = project.getRestMockServiceList();
		assertThat( serviceList.size(), is( 1 ) );

		RestMockService service = project.getRestMockServiceByName( mockServiceName );
		assertThat( service.getName(), is( mockServiceName ) );
	}

	@Test
	public void shouldFireProjectChangedEvent()
	{
		ProjectListenerAdapter listener = mock( ProjectListenerAdapter.class );
		project.addProjectListener( listener );
		action.perform( restRequest, notUsed );
		verify( listener, times( 1 ) ).mockServiceAdded( any( RestMockService.class ) );
	}

	@Test
	public void shouldAddASecondResponseToAnOperationForTheSamePath() throws SoapUIException
	{
		action.perform( restRequest, notUsed );
		action.perform( restRequest, notUsed );

		int mockResponseCount = getFirstMockOperation().getMockResponseCount();

		assertThat( mockResponseCount, is(2));
	}

	@Test
	public void shouldCreateNewOperationForDifferentPath() throws SoapUIException
	{
		action.perform( restRequest, notUsed );
		restRequest.setPath( "someotherpath" );
		action.perform( restRequest, notUsed );

		int mockResponseCount = getFirstMockOperation().getMockResponseCount();

		assertThat( mockResponseCount, is(1));
		assertThat( getFirstRestMockService().getMockOperationCount(), is(2) );
	}

	public RestMockAction getFirstMockOperation()
	{
		return getFirstRestMockService().getMockOperationAt( 0 );
	}

	public RestMockService getFirstRestMockService()
	{
		return project.getRestMockServiceAt( 0 );
	}

	@Test
	public void shouldCreateNewOperationForDifferentVerb()
	{
		action.perform( restRequest, notUsed );
		int mockOperationCount = getFirstRestMockService().getMockOperationCount();
		assertThat( mockOperationCount, is(1));

		restRequest.setMethod( HttpMethod.TRACE );
		action.perform( restRequest, notUsed );
		mockOperationCount = getFirstRestMockService().getMockOperationCount();
		assertThat( mockOperationCount, is( 2 ) );
	}

	@Test
	public void shouldSaveHeadersOnMockResponse()
	{
		action.perform( restRequest, notUsed );

		StringToStringsMap responseHeaders = getActualResponseHeaders();
		assertThat( responseHeaders.get( ONE_HEADER ).get(0), is( "oneValue" ) );
		assertThat( responseHeaders.get( ANOTHER_HEADER ).get( 0 ), is( "anotherValue" ) );
	}

	public StringToStringsMap getActualResponseHeaders()
	{
		return getFirstMockOperation().getMockResponseAt( 0 ).getResponseHeaders();
	}

	@Test
	public void shouldNotSaveSomeHeaders()
	{
		String[] headersNotToSave = new String[]{ HEADER_STATUS, HEADER_CONTENT_TYPE, HEADER_CONTENT_LENGTH };

		action.perform( restRequest, notUsed );

		StringToStringsMap responseHeaders = getActualResponseHeaders();

		for( String header : headersNotToSave)
		{
			assertThat( responseHeaders.get( header ), is( Null.NULL ) );
		}
	}

	@Test
	public void shouldAddEmptyResponses()
	{
		restRequest.setResponse( null, null );
		action.perform( restRequest, notUsed );

		assertThat( getFirstMockOperation().getMockResponseCount(), is(1));
	}

    @Test
	public void shouldExpandPathParameters() throws SoapUIException
	{
		RestService restService = (RestService)project.addNewInterface( "a rest resource", RestServiceFactory.REST_TYPE );

		RestResource restResource = restService.addNewResource( "resource", "http://some.path.example.com" );

		RestMethod restMethod = restResource.addNewMethod("get");
        RestRequest anotherRestRequest = createRestRequest(restMethod, "/template/{id}/path");
		anotherRestRequest.setPropertyValue( "id", "42" );

		action.perform( anotherRestRequest, notUsed );

		assertThat( getFirstMockOperation().getResourcePath(), is( "/template/42/path" ) );
	}

    @Test
    public void shouldExpandMultiplePathParameters() throws SoapUIException
    {
        RestService restService = (RestService)project.addNewInterface( "a rest resource", RestServiceFactory.REST_TYPE );

        RestResource restResource = restService.addNewResource( "resource", "http://some.path.example.com" );

        RestMethod restMethod = restResource.addNewMethod( "get" );
        RestRequest anotherRestRequest = createRestRequest(restMethod, "/template/{id}/path/{version}");
        anotherRestRequest.setPropertyValue( "id", "42" );
        anotherRestRequest.setPropertyValue( "version", "3.1" );

        action.perform( anotherRestRequest, notUsed );

        assertThat( getFirstMockOperation().getResourcePath(), is( "/template/42/path/3.1" ) );
    }

    private RestRequest createRestRequest(RestMethod restMethod, String path ) {
        RestRequest anotherRestRequest = restMethod.addNewRequest( "another" );
        anotherRestRequest.setPath( path );
        anotherRestRequest.setMethod(HttpMethod.GET);
        return anotherRestRequest;
    }

    private void mockPromptDialog()
	{
		originalDialogs = UISupport.getDialogs();
		StubbedDialogs dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
		dialogs.mockPromptWithReturnValue( mockServiceName );
	}
}
