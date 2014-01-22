package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.project.Project;

public class RestMockService extends AbstractMockService<RestMockAction, RESTMockServiceConfig>
{

	public RestMockService( Project project, RESTMockServiceConfig config )
	{
		super( config, project );

		if( !getConfig().isSetProperties() )
			getConfig().addNewProperties();

		setPropertiesConfig( config.getProperties() );

	}

	@Override
	public void setPort( int port )
	{

	}

	@Override
	public void setPath( String path )
	{

	}

	@Override
	public MockDispatcher createDispatcher( WsdlMockRunContext mockContext )
	{
		return new RestMockDispatcher( this, mockContext );
	}

	public RestMockAction addNewMockAction( RestRequest restRequest )
	{

		String mockActionName = restRequest.getResource().getName() + " " + restRequest.getName();
		RESTMockActionConfig config = getConfig().addNewRestMockAction();
		config.setName( mockActionName );
		RestMockAction restMockAction = new RestMockAction( this, config );

		addMockOperation( restMockAction );
		fireMockOperationAdded( restMockAction );

		return restMockAction;
	}

	protected void fireMockOperationAdded( RestMockAction mockOperation )
	{
		for( MockServiceListener listener : getMockServiceListeners() )
		{
			listener.mockOperationAdded( mockOperation );
		}
	}

	protected void fireMockResponseAdded( RestMockResponse mockResponse )
	{
		for( MockServiceListener listener : getMockServiceListeners())
		{
			listener.mockResponseAdded( mockResponse );
		}
	}

}
