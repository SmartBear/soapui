package com.eviware.soapui.impl.support;

import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.util.*;

public abstract class AbstractMockService<MockOperationType extends MockOperation> extends AbstractTestPropertyHolderWsdlModelItem<MockServiceConfig> implements MockService
{

	protected List<MockOperation> mockOperations = new ArrayList<MockOperation>();
	private Set<MockRunListener> mockRunListeners = new HashSet<MockRunListener>();
	private Set<MockServiceListener> mockServiceListeners = new HashSet<MockServiceListener>();
	private MockServiceIconAnimator iconAnimator;
	private WsdlMockRunner mockRunner;


	protected AbstractMockService( MockServiceConfig config, ModelItem parent )
	{
		super( config, parent, "/mockService.gif" );

		if( !config.isSetPort() || config.getPort() < 1 )
			config.setPort( 8080 );

		if( !config.isSetPath() )
			config.setPath( "/" );

		iconAnimator = new MockServiceIconAnimator();
		addMockRunListener( iconAnimator );
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


	// TODO: think about naming - this does not start nothing.....
	public WsdlMockRunner start( WsdlTestRunContext context ) throws Exception
	{
		String path = getPath();
		if( path == null || path.trim().length() == 0 || path.trim().charAt( 0 ) != '/' )
			throw new Exception( "Invalid path; must start with '/'" );

		mockRunner = new WsdlMockRunner( this, context );
		return mockRunner;
	}

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

	public WsdlMockRunner getMockRunner()
	{
		return mockRunner;
	}

	public MockRunListener[] getMockRunListeners()
    {
        return mockRunListeners.toArray( new MockRunListener[mockRunListeners.size()] );
    }

    public MockServiceListener[] getMockServiceListeners()
    {
        return mockServiceListeners.toArray( new MockServiceListener[mockServiceListeners.size()] );
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

	@Override
	public void release()
	{
		super.release();

		mockServiceListeners.clear();

		if( mockRunner != null )
		{
			if( mockRunner.isRunning() )
				mockRunner.stop();

			if( mockRunner != null )
				mockRunner.release();
		}
	}

	// Implements AbstractWsdlModelItem
	@Override
	public ImageIcon getIcon()
	{
		return iconAnimator.getIcon();
	}

	private class MockServiceIconAnimator extends ModelItemIconAnimator<AbstractMockService<MockOperationType>> implements MockRunListener
	{
		public MockServiceIconAnimator()
		{
			super( AbstractMockService.this, "/mockService.gif", "/mockService", 4, "gif" );
		}

		public MockResult onMockRequest( MockRunner runner, HttpServletRequest request, HttpServletResponse response )
		{
			return null;
		}

		public void onMockResult( MockResult result )
		{
		}

		public void onMockRunnerStart( MockRunner mockRunner )
		{
			start();
		}

		public void onMockRunnerStop( MockRunner mockRunner )
		{
			stop();
			AbstractMockService.this.mockRunner = null;
		}
	}

}
