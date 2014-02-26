package com.eviware.soapui.impl.support;

import com.eviware.soapui.config.BaseMockOperationConfig;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMockOperation
		<ModelItemConfigType extends ModelItemConfig, MockResponseType extends MockResponse>
		extends AbstractWsdlModelItem<ModelItemConfigType>
		implements MockOperation, PropertyChangeListener
{
	private List<MockResponseType> responses = new ArrayList<MockResponseType>();

	protected AbstractMockOperation( ModelItemConfigType config, AbstractMockService parent, String icon )
	{
		super( config, parent, icon );
	}

	protected void setupConfig( ModelItemConfigType config )
	{
		BaseMockOperationConfig baseConfig = (BaseMockOperationConfig)config;
		Operation operation = getOperation();
		if( !config.isSetName() )
			config.setName( operation == null ? "<missing operation>" : operation.getName() );

		if( !baseConfig.isSetDefaultResponse() && getMockResponseCount() > 0 )
			setDefaultResponse( getMockResponseAt( 0 ).getName() );
	}

	public void addMockResponse(MockResponseType response)
	{
		responses.add( response );
	}

	public List<MockResponse> getMockResponses()
	{
		return new ArrayList<MockResponse>( responses );
	}

	public MockResponseType getMockResponseAt( int index )
	{
		return responses.get( index );
	}

	public int getMockResponseCount()
	{
		return responses.size();
	}

	public MockResponseType getMockResponseByName( String name )
	{
		return ( MockResponseType )getWsdlModelItemByName( getMockResponses(), name );
	}

	public MockResult getLastMockResult()
	{
		MockResult result = null;

		for( MockResponse response : getMockResponses() )
		{
			MockResult mockResult = response.getMockResult();
			if( mockResult != null )
			{
				if( result == null || result.getTimestamp() > mockResult.getTimestamp() )
					result = mockResult;
			}
		}

		return result;
	}

	public void removeMockResponse( MockResponse mockResponse )
	{
		int ix = responses.indexOf( mockResponse );
		responses.remove( ix );
		mockResponse.removePropertyChangeListener( this );

		try
		{
			( getMockService() ).fireMockResponseRemoved( mockResponse );
		}
		finally
		{
			mockResponse.release();
			removeResponseFromConfig( ix ); // wow - do we really know that the ordering is the same.....
		}
	}

	public abstract void removeResponseFromConfig(int index);

	public String getDefaultResponse()
	{
		return ((BaseMockOperationConfig)getConfig()).getDefaultResponse();
	}

	public void setDefaultResponse( String defaultResponse )
	{
		String old = getDefaultResponse();
		((BaseMockOperationConfig)getConfig()).setDefaultResponse( defaultResponse );
		// noone is listening? notifyPropertyChanged( WsdlMockOperation.DEFAULT_RESPONSE_PROPERTY, old, defaultResponse );
	}


}
