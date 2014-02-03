package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.project.Project;

import java.util.List;

public class RestMockService extends AbstractMockService<RestMockAction, RestMockResponse, RESTMockServiceConfig>
{

	public RestMockService( Project project, RESTMockServiceConfig config )
	{
		super( config, project );

		List<RESTMockActionConfig> restActionConfigList = config.getRestMockActionList();
		for( RESTMockActionConfig restActionConfig : restActionConfigList )
		{
			RestMockAction restMockAction = new RestMockAction( this, restActionConfig );
			addMockOperation( restMockAction );
		}

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
		config.setResourcePath( restRequest.getPath() );
		config.setMethod( restRequest.getMethod().name() );
		RestMockAction restMockAction = new RestMockAction( this, config, restRequest );

		addMockOperation( restMockAction );
		fireMockOperationAdded( restMockAction );

		return restMockAction;
	}

}
