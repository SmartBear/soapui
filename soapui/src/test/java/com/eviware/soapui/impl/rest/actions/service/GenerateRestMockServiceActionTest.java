package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.x.form.XFormDialog;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.NotNull;
import org.mockito.internal.matchers.Null;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static java.lang.Boolean.*;

public class GenerateRestMockServiceActionTest
{

	private RestService restService;
	private String restMockServiceName;
	private XFormDialog dialog;
	private GenerateRestMockServiceAction action;

	@Before
	public void setUp() throws Exception
	{
		restService = ModelItemFactory.makeRestService();
		restMockServiceName = "My Mock Service";
		action = new GenerateRestMockServiceAction();

		mockFormDialog();

		SoapUI.getSettings().setBoolean( HttpSettings.START_MOCK_SERVICE, TRUE );
	}

	public void mockFormDialog()
	{
		dialog = mock( XFormDialog.class );
		when( dialog.getValue( GenerateRestMockServiceAction.Form.MOCKSERVICENAME ) ).thenReturn( restMockServiceName );
		when( dialog.show() ).thenReturn( true ).thenReturn( false );
		action.setFormDialog( dialog );
	}

	@Test
	public void shouldGenerateRestMockService() throws SoapUIException
	{
		action.perform( restService, null );

		RestMockService restMockService = getResultingRestMockService();
		assertThat( restMockService, is( NotNull.NOT_NULL ) );
		assertThat( restMockService.getName(), is( restMockServiceName ) );
		assertThat( restMockService.getMockRunner().isRunning(), is( TRUE ) );
	}

	@Test
	public void shouldGenerateNonStartedRestMockServiceIfSettingIsOff() throws SoapUIException
	{
		SoapUI.getSettings().setBoolean( HttpSettings.START_MOCK_SERVICE, FALSE );

		action.perform( restService, null );

		RestMockService restMockService = getResultingRestMockService();
		assertThat( restMockService.getMockRunner(), is( Null.NULL ) );
	}

	public RestMockService getResultingRestMockService()
	{
		return restService.getProject().getRestMockServiceByName( restMockServiceName );
	}

	@Test
	public void shouldGenerateRestMockServiceWithResources()
	{
		restService.addNewResource( "one", "/one" );
		restService.addNewResource( "two", "/two" );

		action.perform( restService, null );

		RestMockService restMockService = getResultingRestMockService();
		assertThat( restMockService.getMockOperationCount(), is( 2 ));
		assertThat( restMockService.getMockOperationAt( 1 ).getName(), is( "/two" ) );

		for( MockOperation mockAction : restMockService.getMockOperationList() )
		{
			assertThat( mockAction.getMockResponseCount(), is( 1 ) );
		}
	}

	@Test
	public void shouldGenerateRestMockServiceForNestedResources()
	{
		RestResource one = restService.addNewResource( "one", "/one" );

		RestResourceConfig nestedResourceConfig = one.getConfig().addNewResource();
		nestedResourceConfig.setPath( "/path/again" );

		RestResource three = one.addNewChildResource( "three", "/will/be/overwritten" );
		three.setConfig( nestedResourceConfig );

		restService.addNewResource( "two", "/two" );

		action.perform( restService, null );

		RestMockService restMockService = getResultingRestMockService();
		assertThat( restMockService.getMockOperationCount(), is( 3 ));
		assertMockActionWithPath( restMockService, "/one" );
		assertMockActionWithPath( restMockService, "/one/path/again" );
		assertMockActionWithPath( restMockService, "/two" );
	}

	private void assertMockActionWithPath( RestMockService restMockService, String expectedPath )
	{
		boolean foundMatch = false;

		for( MockOperation mockOperation : restMockService.getMockOperationList() )
		{
			RestMockAction mockAction = ( RestMockAction )mockOperation;

			if( mockAction.getResourcePath().equals( expectedPath ))
			{
				foundMatch = true;
				break;
			}
		}
		assertTrue( "Did not find a match for " + expectedPath, foundMatch );
	}


}
