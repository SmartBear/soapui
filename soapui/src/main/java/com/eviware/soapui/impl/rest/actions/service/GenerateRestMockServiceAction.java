package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

public class GenerateRestMockServiceAction extends AbstractSoapUIAction<RestService>
{
	XFormDialog dialog = null;

	public GenerateRestMockServiceAction()
	{
		super( "Generate REST Mock Service", "Generates a REST mock service containing all resources of this REST service" );
	}

	@Override
	public void perform( RestService restService, Object param )
	{
		createDialog( restService );

		if( dialog.show() )
		{
			String mockServiceName = dialog.getValue( Form.MOCKSERVICENAME );
			MockService mockService = getMockService( mockServiceName, restService.getProject() );

			if( mockService != null )
			{
				populateMockService( restService, mockService );
				restService.addEndpoint( mockService.getLocalEndpoint() );

				UISupport.showDesktopPanel( mockService );
				maybeStart( mockService );
			}
		}
	}

	private void createDialog( RestService restService )
	{
		if( dialog == null)
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
		}
		String nextMockServiceName = nextMockServiceName( restService );
		dialog.setValue( Form.MOCKSERVICENAME, nextMockServiceName );
	}

	private void maybeStart( MockService mockService )
	{
		try
		{
			if( SoapUI.getSettings().getBoolean( HttpSettings.START_MOCK_SERVICE ))
			{
				mockService.start();
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	private String nextMockServiceName( RestService restService )
	{
		int nextMockServiceCount = restService.getProject().getRestMockServiceCount() + 1;
		return "REST MockService " + nextMockServiceCount;
	}

	private void populateMockService( RestService restService, MockService mockService )
	{
		mockService.setPath( "/" );
		mockService.setPort( 8080 );
		addMockOperations( restService, mockService );
	}

	private MockService getMockService( String mockServiceName, WsdlProject project )
	{
		if( StringUtils.isNullOrEmpty( mockServiceName ) )
		{
			UISupport.showInfoMessage( "The mock service name can not be empty" );
			return null;
		}

		if( project.getRestMockServiceByName( mockServiceName ) == null )
		{
			return project.addNewRestMockService( mockServiceName );
		}
		else
		{
			UISupport.showInfoMessage( "The mock service name need to be unique. '" + mockServiceName + "' already exists." );
			return null;
		}
	}

	private void addMockOperations( RestService restService, MockService mockService )
	{
		for( RestResource oneResource : restService.getAllResources() )
		{
			MockOperation mockOperation = mockService.addNewMockOperation( oneResource );
			if( mockOperation != null )
			{
				mockOperation.addNewMockResponse( "Response 1" );
			}
		}
	}

	/*
	 * only for injacting the dialog when testing
	 */
	protected void setFormDialog( XFormDialog dialog )
	{
		this.dialog = dialog;
	}

	@AForm( name = "Generate REST Mock Service", description = "Set name for the new REST Mock Service", helpUrl = HelpUrls.GENERATE_MOCKSERVICE_HELP_URL )
	protected interface Form
	{
		@AField( name = "MockServiceName", description = "The Mock Service name", type = AField.AFieldType.STRING )
		public final static String MOCKSERVICENAME = "MockServiceName";
	}
}
