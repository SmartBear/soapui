package com.eviware.soapui.impl.support;

import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.*;

import java.util.*;

public abstract class AbstractMockService<MockOperationType extends MockOperation> extends AbstractTestPropertyHolderWsdlModelItem<MockServiceConfig> implements MockService
{

	protected List<MockOperation> mockOperations = new ArrayList<MockOperation>();
	private Set<MockRunListener> mockRunListeners = new HashSet<MockRunListener>();
	private Set<MockServiceListener> mockServiceListeners = new HashSet<MockServiceListener>();

	protected AbstractMockService( MockServiceConfig config, ModelItem parent )
	{
		super( config, parent, "/mockService.gif" );

		if( !config.isSetPort() || config.getPort() < 1 )
			config.setPort( 8080 );

		if( !config.isSetPath() )
			config.setPath( "/" );

	}

	// Implements MockService
	@Override
	public WsdlProject getProject()
	{
		return ( WsdlProject )getParent();
	}

	@Override
	public MockOperationType getMockOperationAt( int index )
	{
		return ( MockOperationType )mockOperations.get( index );
	}

	@Override
	public MockOperation getMockOperationByName( String name )
	{

		for( MockOperation operation : mockOperations )
		{
			if( operation.getName() != null && operation.getName().equals( name ) )
				return operation;
		}

		return null;
	}

	public void addMockOperation( MockOperation mockOperation )
	{
		mockOperations.add( mockOperation );
	}

	@Override
	public int getMockOperationCount()
	{
		return mockOperations.size();
	}


	@Override
	public int getPort()
	{
		return getConfig().getPort();
	}

	@Override
	public String getPath()
	{
		return getConfig().getPath();
	}

	@Override
	public abstract MockRunner start() throws Exception;

	@Override
	public void addMockRunListener( MockRunListener listener )
	{
		mockRunListeners.add( listener );
	}

	@Override
	public void removeMockRunListener( MockRunListener listener )
	{
		mockRunListeners.remove( listener );
	}

	@Override
	public void addMockServiceListener( MockServiceListener listener )
	{
		mockServiceListeners.add( listener );
	}


	@Override
	public void removeMockServiceListener( MockServiceListener listener )
	{
		mockServiceListeners.remove( listener );
	}

	@Override
	public List<MockOperation> getMockOperationList()
	{
		return Collections.unmodifiableList( new ArrayList<MockOperation>( mockOperations ) );
	}

	protected List<MockOperation> getMockOperations()
	{
		return mockOperations;
	}
}
