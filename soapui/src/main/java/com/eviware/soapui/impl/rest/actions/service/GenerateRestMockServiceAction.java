package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

public class GenerateRestMockServiceAction extends AbstractSoapUIAction<RestService>
{
	public GenerateRestMockServiceAction()
	{
		super( "Generate REST Mock Service", "Generates a REST mock service containing this REST services all resources" );
	}

	@Override
	public void perform( RestService target, Object param )
	{
		generateMockService( target );
	}

	public void generateMockService( RestService restService )
	{
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );

		MockService mockService = null;

		while( mockService == null && dialog.show() )
		{
			mockService = getMockService( dialog.getValue( Form.MOCKSERVICENAME ), restService.getProject() );
		}

		populateMockService( restService, mockService );
		restService.addEndpoint( mockService.getLocalEndpoint() );

		UISupport.showDesktopPanel( mockService );
	}

	public void populateMockService( RestService restService, MockService mockService )
	{
		mockService.setPath( "/" );
		mockService.setPort( 8080 );
		addMockOperations( restService, mockService );
	}

	public MockService getMockService( String mockServiceName, WsdlProject project )
	{

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

	public void addMockOperations( RestService restService, MockService mockService )
	{
		for( int i = 0; i < restService.getOperationCount(); i++ )
		{
			Operation operation = restService.getOperationAt( i );

			MockOperation mockOperation = mockService.addNewMockOperation( operation );
			if( mockOperation != null )
				mockOperation.addNewMockResponse( "Response 1" );
		}
	}

	@AForm( name = "Generate REST Mock Service", description = "Set name for the new REST Mock Service", helpUrl = HelpUrls.GENERATE_MOCKSERVICE_HELP_URL )
	private interface Form
	{
		@AField( name = "Mock Service name", description = "The Mock Service name", type = AField.AFieldType.STRING )
		public final static String MOCKSERVICENAME = "MockServiceName";
	}
}
