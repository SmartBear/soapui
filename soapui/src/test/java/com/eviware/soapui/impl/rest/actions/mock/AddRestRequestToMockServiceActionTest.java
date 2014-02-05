package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.RestRequest;
import static com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod.*;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AddRestRequestToMockServiceActionTest
{
	AddRestRequestToMockServiceAction action = new AddRestRequestToMockServiceAction();
	RestRequest restRequest;
	Object notUsed = null;
	String mockServiceName = "Mock Service1 1";
	private XDialogs originalDialogs;
	private WsdlProject project;
	private int mockResponseCount;

	@Before
	public void setUp() throws Exception
	{
		restRequest = ModelItemFactory.makeRestRequest();
		restRequest.setMethod( GET );
		restRequest.setPath( "somepath" );
		mockPromptDialog();
		project = restRequest.getRestMethod().getInterface().getProject();
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

		mockResponseCount = project.getRestMockServiceAt( 0 ).getMockOperationAt( 0 ).getMockResponseCount();

		assertThat( mockResponseCount, is(2));
	}

	@Test
	public void shouldCreateNewOperationForDifferentPath() throws SoapUIException
	{
		action.perform( restRequest, notUsed );
		restRequest.setPath( "someotherpath" );
		action.perform( restRequest, notUsed );

		mockResponseCount = project.getRestMockServiceAt( 0 ).getMockOperationAt( 0 ).getMockResponseCount();

		assertThat( mockResponseCount, is(1));
		assertThat( project.getRestMockServiceAt( 0 ).getMockOperationCount(), is(2) );
	}

	private void mockPromptDialog()
	{
		originalDialogs = UISupport.getDialogs();
		StubbedDialogs dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
		dialogs.mockPromptWithReturnValue( mockServiceName );
	}
}
