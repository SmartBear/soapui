package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.*;
import com.eviware.soapui.model.project.Project;

import java.util.List;

public class RestMockService extends AbstractTestPropertyHolderWsdlModelItem<RESTMockServiceConfig> implements MockService
{
	protected RestMockService( RESTMockServiceConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
	}

	@Override
	public Project getProject()
	{
		return null;
	}

	@Override
	public int getMockOperationCount()
	{
		return 0;
	}

	@Override
	public MockOperation getMockOperationAt( int index )
	{
		return null;
	}

	@Override
	public MockOperation getMockOperationByName( String name )
	{
		return null;
	}

	@Override
	public String getPath()
	{
		return null;
	}

	@Override
	public int getPort()
	{
		return 0;
	}

	@Override
	public MockRunner start() throws Exception
	{
		return null;
	}

	@Override
	public void addMockRunListener( MockRunListener listener )
	{

	}

	@Override
	public void removeMockRunListener( MockRunListener listener )
	{

	}

	@Override
	public void addMockServiceListener( MockServiceListener listener )
	{

	}

	@Override
	public void removeMockServiceListener( MockServiceListener listener )
	{

	}

	@Override
	public List<MockOperation> getMockOperationList()
	{
		return null;
	}
}
