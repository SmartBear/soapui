package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.StringUtils;

import java.beans.PropertyChangeEvent;
import java.util.List;

public class RestMockAction extends AbstractMockOperation<RESTMockActionConfig, RestMockResponse>
{
	private RestResource resource = null;

	public RestMockAction( RestMockService mockService, RESTMockActionConfig config )
	{
		super( config, mockService, RestMockAction.getIconName( config ) );

		mockService.getMockOperationByName( config.getName() );

		List<RESTMockResponseConfig> responseConfigs = config.getResponseList();
		for( RESTMockResponseConfig responseConfig : responseConfigs )
		{
			RestMockResponse restMockResponse = new RestMockResponse( this, responseConfig );
			restMockResponse.addPropertyChangeListener( this );
			addMockResponse( restMockResponse );
		}

		super.setupConfig(config);
	}

	public RestMockAction( RestMockService mockService, RESTMockActionConfig config, RestRequest request )
	{
		this( mockService, config );
		resource = request.getResource();
	}

	public static String getIconName(RESTMockActionConfig methodConfig)
	{
		String method = StringUtils.isNullOrEmpty( methodConfig.getMethod() ) ? "get" : methodConfig.getMethod().toLowerCase();
		return "/" + method + "_method.gif";
	}

	@Override
	public RestMockService getMockService()
	{
		return ( RestMockService )getParent();
	}

	@Override
	public void removeResponseFromConfig( int index )
	{
		getConfig().removeResponse( index );
	}

	@Override
	public Operation getOperation()
	{
		return resource;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{

	}

	public RestMockResponse addNewMockResponse( String name )
	{

		RESTMockResponseConfig restMockResponseConfig = getConfig().addNewResponse();
		restMockResponseConfig.setName( name );

		RestMockResponse mockResponse = new RestMockResponse( this, restMockResponseConfig );
		addMockResponse( mockResponse );


		if( getMockResponseCount() == 1 && restMockResponseConfig.getResponseContent() != null )
		{
			setDefaultResponse( restMockResponseConfig.getResponseContent().toString() );
		}

		( getMockService() ).fireMockResponseAdded( mockResponse );
		notifyPropertyChanged( "mockResponses", null, mockResponse );

		return mockResponse;
	}

	public RestMockResult dispatchRequest( RestMockRequest request ) throws DispatchException
	{
		try
		{
			RestMockResult result = new RestMockResult( request );

			if( getMockResponseCount() == 0 )
				throw new DispatchException( "Missing MockResponse(s) in MockOperation [" + getName() + "]" );

			result.setMockOperation( this );
			RestMockResponse response = getMockResponseAt( 0 ); // TODO in SOAP-1334

			if( response == null )
			{
				// TODO in SOAP-1334 - when there is no matchin response from the dispatcher strategy - use the default
				throw new UnknownError( "not implemented" );
			}

			if( response == null )
			{
				throw new DispatchException( "Failed to find MockResponse" );
			}

			result.setMockResponse( response );
			response.execute( request, result );

			return result;
		}
		catch( Throwable e )
		{
			throw new DispatchException( e );
		}
	}

	public String getPath()
	{
		return getConfig().getResourcePath();
	}

	public RequestMethod getMethod()
	{
		return RequestMethod.valueOf( getConfig().getMethod() );
	}
}
