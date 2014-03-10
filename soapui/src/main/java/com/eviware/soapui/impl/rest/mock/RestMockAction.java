package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;

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

	public static String getIconName( RESTMockActionConfig methodConfig )
	{
		if( methodConfig.isSetMethod() )
		{
			return getIconName( methodConfig.getMethod() );
		}
		return getDefaultIcon();
	}

	private static String getIconName( String method )
	{
		return "/mock_" + method.toLowerCase() + "_method.gif";
	}

	public static String getDefaultIcon()
	{
		return getIconName( HttpMethod.GET.name() );
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
		if( getMockResponseCount() == 0 )
			throw new DispatchException( "Missing MockResponse(s) in MockOperation [" + getName() + "]" );

		try
		{
			RestMockResult result = new RestMockResult( request );

			MockResponse mockResponse = getDispatcher().selectMockResponse( request, result );

			result.setMockResponse( mockResponse );

			result.setMockOperation( this );

			if( mockResponse == null)
			{
				mockResponse = getMockResponseByName( this.getDefaultResponse() );
			}

			if( mockResponse == null )
			{
				throw new DispatchException( "Failed to find MockResponse" );
			}

			result.setMockResponse( mockResponse );
			mockResponse.execute( request, result );

			return result;
		}
		catch( Exception e )
		{
			throw new DispatchException( e );
		}
	}

	public String getResourcePath()
	{
		return getConfig().getResourcePath();
	}

	public void setMethod( HttpMethod method)
	{
		getConfig().setMethod( method.name() );
		setIcon( UISupport.createImageIcon( getIconName( method.name() ) ));

		notifyPropertyChanged( "httpMethod", null, this );
	}

	public HttpMethod getMethod()
	{
		return HttpMethod.valueOf( getConfig().getMethod() );
	}

	public void setResourcePath( String path )
	{
		getConfig().setResourcePath( path );
		notifyPropertyChanged( "resourcePath", null, this );
	}

	public void setResource( RestResource resource )
	{
		this.resource = resource;
	}

}
