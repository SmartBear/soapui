package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.config.RESTMockServiceConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.model.mock.*;
import com.eviware.soapui.model.project.Project;

import java.util.List;

public class RestMockService extends AbstractTestPropertyHolderWsdlModelItem<RESTMockServiceConfig> implements MockService
{
	public RestMockService( Project project, RESTMockServiceConfig config )
	{
		super( config, project, "/mockService.gif" );

        if( !getConfig().isSetProperties() )
            getConfig().addNewProperties();

        setPropertiesConfig(config.getProperties());

    }

	@Override
	public Project getProject()
	{
		return (Project) getParent();
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
