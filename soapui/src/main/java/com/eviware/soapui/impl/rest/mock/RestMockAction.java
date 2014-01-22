package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.*;
import com.eviware.soapui.support.StringUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RestMockAction extends AbstractWsdlModelItem<RESTMockActionConfig> implements MockOperation,
		PropertyChangeListener
{
	private RestResource resource = null;
	private List<RestMockResponse> responses = new ArrayList<RestMockResponse>();

	public RestMockAction( RestMockService mockService, RESTMockActionConfig config )
	{
		super( config, mockService, RestMockAction.getIconName( config ) );

		Interface iface = mockService.getProject().getInterfaceByName( mockService.getName() );
		if( iface == null )
		{
			SoapUI.log.warn( "Missing interface [" + mockService.getName() + "] for MockOperation in project" );
		}
		else
		{
			resource = ( RestResource )iface.getOperationByName( mockService.getName() );
		}

		List<RESTMockResponseConfig> responseConfigs = config.getRestMockResponseList();
		for( RESTMockResponseConfig responseConfig : responseConfigs )
		{
			RestMockResponse restMockResponse = new RestMockResponse( this, responseConfig );
			restMockResponse.addPropertyChangeListener( this );
			responses.add( restMockResponse );
		}

		//TODO: split WsdlMockOperation.initData( config ); into several parts moving most of them to superclass call them from here
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
	public int getMockResponseCount()
	{
		return responses.size();
	}

	@Override
	public MockResponse getMockResponseAt( int index )
	{
		return null;
	}

	@Override
	public MockResponse getMockResponseByName( String name )
	{
		return null;
	}

	@Override
	public Operation getOperation()
	{
		return null;
	}

	@Override
	public MockResult getLastMockResult()
	{
		return null;
	}

	@Override
	public List<MockResponse> getMockResponses()
	{
		return null;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{

	}

	public RestMockResponse addNewMockResponse( RESTMockResponseConfig responseConfig )
	{
		RestMockResponse mockResponse = new RestMockResponse( this, responseConfig );

		responses.add( mockResponse );

		// TODO: Set default response - reuse if necessary

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
			RestMockResponse response = responses.get( 0 ); // TODO in SOAP-1334
			if( response == null )
			{
				throw new UnknownError( "not implemented" ); // FIXME use the default response when no response if found
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
			if( e instanceof DispatchException )
				throw ( DispatchException )e;
			else
				throw new DispatchException( e );
		}
	}
}
