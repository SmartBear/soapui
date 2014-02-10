package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.MockServiceExternalDependency;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.*;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.util.*;

public abstract class AbstractMockService<MockOperationType extends MockOperation> extends AbstractTestPropertyHolderWsdlModelItem<MockServiceConfig> implements MockService
{
	public final static String START_SCRIPT_PROPERTY = AbstractMockService.class.getName() + "@startScript";
	public final static String STOP_SCRIPT_PROPERTY = AbstractMockService.class.getName() + "@stopScript";

	protected List<MockOperation> mockOperations = new ArrayList<MockOperation>();
	private Set<MockRunListener> mockRunListeners = new HashSet<MockRunListener>();
	private Set<MockServiceListener> mockServiceListeners = new HashSet<MockServiceListener>();
	private MockServiceIconAnimator iconAnimator;
	private WsdlMockRunner mockRunner;

	private SoapUIScriptEngine startScriptEngine;
	private SoapUIScriptEngine stopScriptEngine;
	private BeanPathPropertySupport docrootProperty;
	private ScriptEnginePool onRequestScriptEnginePool;
	private ScriptEnginePool afterRequestScriptEnginePool;


	protected AbstractMockService( MockServiceConfig config, ModelItem parent )
	{
		super( config, parent, "/mockService.gif" );

		if( !config.isSetPort() || config.getPort() < 1 )
			config.setPort( 8080 );

		if( !config.isSetPath() )
			config.setPath( "/" );

		docrootProperty = new BeanPathPropertySupport( this, "docroot" );

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

		if( onRequestScriptEnginePool != null )
			onRequestScriptEnginePool.release();

		if( afterRequestScriptEnginePool != null )
			afterRequestScriptEnginePool.release();

		if( startScriptEngine != null )
			startScriptEngine.release();

		if( stopScriptEngine != null )
			stopScriptEngine.release();

	}

	public void setStartScript( String script )
	{
		String oldScript = getStartScript();

		if( !getConfig().isSetStartScript() )
			getConfig().addNewStartScript();

		getConfig().getStartScript().setStringValue( script );

		if( startScriptEngine != null )
			startScriptEngine.setScript( script );

		notifyPropertyChanged( START_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getStartScript()
	{
		return getConfig().isSetStartScript() ? getConfig().getStartScript().getStringValue() : null;
	}

	public void setStopScript( String script )
	{
		String oldScript = getStopScript();

		if( !getConfig().isSetStopScript() )
			getConfig().addNewStopScript();

		getConfig().getStopScript().setStringValue( script );
		if( stopScriptEngine != null )
			stopScriptEngine.setScript( script );

		notifyPropertyChanged( STOP_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getStopScript()
	{
		return getConfig().isSetStopScript() ? getConfig().getStopScript().getStringValue() : null;
	}

	public Object runStartScript( WsdlMockRunContext runContext, WsdlMockRunner runner ) throws Exception
	{
		String script = getStartScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( startScriptEngine == null )
		{
			startScriptEngine = SoapUIScriptEngineRegistry.create( this );
			startScriptEngine.setScript( script );
		}

		startScriptEngine.setVariable( "context", runContext );
		startScriptEngine.setVariable( "mockRunner", runner );
		startScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return startScriptEngine.run();
	}

	public Object runStopScript( WsdlMockRunContext runContext, WsdlMockRunner runner ) throws Exception
	{
		String script = getStopScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( stopScriptEngine == null )
		{
			stopScriptEngine = SoapUIScriptEngineRegistry.create( this );
			stopScriptEngine.setScript( script );
		}

		stopScriptEngine.setVariable( "context", runContext );
		stopScriptEngine.setVariable( "mockRunner", runner );
		stopScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return stopScriptEngine.run();
	}

	public void setOnRequestScript( String script )
	{
		String oldScript = getOnRequestScript();

		if( !getConfig().isSetOnRequestScript() )
			getConfig().addNewOnRequestScript();

		getConfig().getOnRequestScript().setStringValue( script );

		if( onRequestScriptEnginePool != null )
			onRequestScriptEnginePool.setScript( script );

		notifyPropertyChanged( "onRequestScript", oldScript, script );
	}

	public String getOnRequestScript()
	{
		return getConfig().isSetOnRequestScript() ? getConfig().getOnRequestScript().getStringValue() : null;
	}

	public void setAfterRequestScript( String script )
	{
		String oldScript = getAfterRequestScript();

		if( !getConfig().isSetAfterRequestScript() )
			getConfig().addNewAfterRequestScript();

		getConfig().getAfterRequestScript().setStringValue( script );
		if( afterRequestScriptEnginePool != null )
			afterRequestScriptEnginePool.setScript( script );

		notifyPropertyChanged( "afterRequestScript", oldScript, script );
	}

	public String getAfterRequestScript()
	{
		return getConfig().isSetAfterRequestScript() ? getConfig().getAfterRequestScript().getStringValue() : null;
	}

	public Object runOnRequestScript( WsdlMockRunContext runContext, WsdlMockRequest mockRequest )
			throws Exception
	{
		String script = getOnRequestScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( onRequestScriptEnginePool == null )
		{
			onRequestScriptEnginePool = new ScriptEnginePool( this );
			onRequestScriptEnginePool.setScript( script );
		}

		SoapUIScriptEngine scriptEngine = onRequestScriptEnginePool.getScriptEngine();

		try
		{
			scriptEngine.setVariable( "context", runContext );
			scriptEngine.setVariable( "mockRequest", mockRequest );
			scriptEngine.setVariable( "mockRunner", getMockRunner() );
			scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
			return scriptEngine.run();
		}
		finally
		{
			onRequestScriptEnginePool.returnScriptEngine( scriptEngine );
		}
	}

	public Object runAfterRequestScript( WsdlMockRunContext runContext, MockResult mockResult )
			throws Exception
	{
		String script = getAfterRequestScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( afterRequestScriptEnginePool == null )
		{
			afterRequestScriptEnginePool = new ScriptEnginePool( this );
			afterRequestScriptEnginePool.setScript( script );
		}

		SoapUIScriptEngine scriptEngine = afterRequestScriptEnginePool.getScriptEngine();

		try
		{
			scriptEngine.setVariable( "context", runContext );
			scriptEngine.setVariable( "mockResult", mockResult );
			scriptEngine.setVariable( "mockRunner", getMockRunner());
			scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
			return scriptEngine.run();
		}
		finally
		{
			afterRequestScriptEnginePool.returnScriptEngine( scriptEngine );
		}
	}

	public void setDocroot( String docroot )
	{
		docrootProperty.set( docroot, true );
	}

	public String getDocroot()
	{
		return docrootProperty.get();
	}

	@Override
	public void addExternalDependencies( List<ExternalDependency> dependencies )
	{
		super.addExternalDependencies( dependencies );
		dependencies.add( new MockServiceExternalDependency( docrootProperty ) );
	}

	@Override
	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );
		docrootProperty.resolveFile( context, "Missing MockService docroot" );
	}

	// Implements AbstractWsdlModelItem
	@Override
	public ImageIcon getIcon()
	{
		return iconAnimator.getIcon();
	}

	public abstract MockDispatcher createDispatcher( WsdlMockRunContext mockContext );

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
