package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.MockOperationConfig;
import com.eviware.soapui.config.RESTMockActionConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockService;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class RestMockAction  extends AbstractWsdlModelItem<RESTMockActionConfig> implements MockOperation,
		PropertyChangeListener
{
	protected RestMockAction( RESTMockActionConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
	}

	@Override
	public MockService getMockService()
	{
		return null;
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
}
