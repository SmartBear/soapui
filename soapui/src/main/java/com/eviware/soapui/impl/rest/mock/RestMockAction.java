package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockService;
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

		//TODO: initData( config );
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
		return 0;
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

		// TODO: Set default response

		( getMockService() ).fireMockResponseAdded( mockResponse );
		notifyPropertyChanged( "mockResponses", null, mockResponse );

		return mockResponse;
	}
}
