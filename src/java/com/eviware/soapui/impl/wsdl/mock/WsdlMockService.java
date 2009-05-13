/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockOperationConfig;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

/**
 * A MockService for simulation WsdlInterfaces and their operations
 * 
 * @author ole.matzura
 */

public class WsdlMockService extends AbstractTestPropertyHolderWsdlModelItem<MockServiceConfig> implements MockService
{
	private static final String REQUIRE_SOAP_VERSION = WsdlMockService.class.getName() + "@require-soap-version";
	private static final String REQUIRE_SOAP_ACTION = WsdlMockService.class.getName() + "@require-soap-action";

	public final static String START_SCRIPT_PROPERTY = WsdlMockService.class.getName() + "@startScript";
	public final static String STOP_SCRIPT_PROPERTY = WsdlMockService.class.getName() + "@stopScript";
	public static final String INCOMING_WSS = WsdlMockService.class.getName() + "@incoming-wss";
	public static final String OUGOING_WSS = WsdlMockService.class.getName() + "@outgoing-wss";

	private List<WsdlMockOperation> mockOperations = new ArrayList<WsdlMockOperation>();
	private Set<MockRunListener> mockRunListeners = new HashSet<MockRunListener>();
	private Set<MockServiceListener> mockServiceListeners = new HashSet<MockServiceListener>();
	private MockServiceIconAnimator iconAnimator;
	private WsdlMockRunner mockRunner;
	private SoapUIScriptEngine startScriptEngine;
	private SoapUIScriptEngine stopScriptEngine;
	private BeanPathPropertySupport docrootProperty;
	private SoapUIScriptEngine onRequestScriptEngine;
	private SoapUIScriptEngine afterRequestScriptEngine;
	private WsdlMockOperation faultMockOperation;

	public WsdlMockService( Project project, MockServiceConfig config )
	{
		super( config, project, "/mockService.gif" );

		List<MockOperationConfig> testStepConfigs = config.getMockOperationList();
		for( MockOperationConfig tsc : testStepConfigs )
		{
			WsdlMockOperation testStep = new WsdlMockOperation( this, tsc );
			mockOperations.add( testStep );
		}

		if( !config.isSetPort() || config.getPort() < 1 )
			config.setPort( 8080 );

		if( !config.isSetPath() )
			config.setPath( "/" );

		if( !getSettings().isSet( REQUIRE_SOAP_ACTION ) )
			setRequireSoapAction( true );

		try
		{
			if( !config.isSetHost() || !StringUtils.hasContent( config.getHost() ) )
				config.setHost( InetAddress.getLocalHost().getHostName() );
		}
		catch( UnknownHostException e )
		{
			SoapUI.logError( e );
		}

		iconAnimator = new MockServiceIconAnimator();
		addMockRunListener( iconAnimator );

		for( MockRunListener listener : SoapUI.getListenerRegistry().getListeners( MockRunListener.class ) )
		{
			addMockRunListener( listener );
		}

		if( !getConfig().isSetProperties() )
			getConfig().addNewProperties();

		setPropertiesConfig( getConfig().getProperties() );
		docrootProperty = new BeanPathPropertySupport( this, "docroot" );

		if( getConfig().isSetFaultMockOperation() )
		{
			faultMockOperation = getMockOperationByName( getConfig().getFaultMockOperation() );
		}
	}

	public void addMockRunListener( MockRunListener listener )
	{
		mockRunListeners.add( listener );
	}

	public String getPath()
	{
		return getConfig().getPath();
	}

	public WsdlMockOperation getMockOperationAt( int index )
	{
		return mockOperations.get( index );
	}

	public WsdlMockOperation getMockOperationByName( String name )
	{
		return ( WsdlMockOperation )getWsdlModelItemByName( mockOperations, name );
	}

	public int getMockOperationCount()
	{
		return mockOperations.size();
	}

	public WsdlProject getProject()
	{
		return ( WsdlProject )getParent();
	}

	public int getPort()
	{
		return getConfig().getPort();
	}

	public String getHost()
	{
		return getConfig().getHost();
	}

	public void setHost( String host )
	{
		getConfig().setHost( host );
	}

	public boolean getBindToHostOnly()
	{
		return getConfig().getBindToHostOnly();
	}

	public void setBindToHostOnly( boolean bindToHostOnly )
	{
		getConfig().setBindToHostOnly( bindToHostOnly );
	}

	public void removeMockRunListener( MockRunListener listener )
	{
		mockRunListeners.remove( listener );
	}

	public WsdlMockRunner start( WsdlTestRunContext context ) throws Exception
	{
		String path = getPath();
		if( path == null || path.trim().length() == 0 || path.trim().charAt( 0 ) != '/' )
			throw new Exception( "Invalid path; must start with '/'" );

		mockRunner = new WsdlMockRunner( this, context );
		return mockRunner;
	}

	public WsdlMockRunner getMockRunner()
	{
		return mockRunner;
	}

	public WsdlMockOperation getMockOperation( Operation operation )
	{
		for( int c = 0; c < getMockOperationCount(); c++ )
		{
			WsdlMockOperation mockOperation = mockOperations.get( c );
			if( mockOperation.getOperation() == operation )
				return mockOperation;
		}

		return null;
	}

	public WsdlMockOperation addNewMockOperation( WsdlOperation operation )
	{
		if( getMockOperation( operation ) != null )
			return null;

		MockOperationConfig config = getConfig().addNewMockOperation();
		config.setName( operation.getName() );
		WsdlMockOperation mockOperation = new WsdlMockOperation( this, config, operation );

		mockOperations.add( mockOperation );
		fireMockOperationAdded( mockOperation );

		return mockOperation;
	}

	public void setPort( int port )
	{
		String oldEndpoint = getLocalEndpoint();

		int oldPort = getPort();
		if( port != oldPort )
		{
			getConfig().setPort( port );
			notifyPropertyChanged( PORT_PROPERTY, oldPort, port );

			for( WsdlInterface iface : getMockedInterfaces() )
			{
				if( Arrays.asList( iface.getEndpoints() ).contains( oldEndpoint ) )
					iface.changeEndpoint( oldEndpoint, getLocalEndpoint() );
			}
		}
	}

	public WsdlInterface[] getMockedInterfaces()
	{
		Set<WsdlInterface> result = new HashSet<WsdlInterface>();

		for( WsdlMockOperation mockOperation : mockOperations )
		{
			WsdlOperation operation = mockOperation.getOperation();
			if( operation != null )
				result.add( operation.getInterface() );
		}

		return result.toArray( new WsdlInterface[result.size()] );
	}

	@Override
	public void release()
	{
		super.release();

		for( WsdlMockOperation operation : mockOperations )
			operation.release();

		mockServiceListeners.clear();

		if( startScriptEngine != null )
			startScriptEngine.release();

		if( stopScriptEngine != null )
			stopScriptEngine.release();
	}

	public void setPath( String path )
	{
		String oldEndpoint = getLocalEndpoint();

		String oldPath = getPath();
		if( !path.equals( oldPath ) )
		{
			getConfig().setPath( path );
			notifyPropertyChanged( PATH_PROPERTY, oldPath, path );

			for( WsdlInterface iface : getMockedInterfaces() )
			{
				if( Arrays.asList( iface.getEndpoints() ).contains( oldEndpoint ) )
					iface.changeEndpoint( oldEndpoint, getLocalEndpoint() );
			}
		}
	}

	public MockRunListener[] getMockRunListeners()
	{
		return mockRunListeners.toArray( new MockRunListener[mockRunListeners.size()] );
	}

	public void removeMockOperation( WsdlMockOperation mockOperation )
	{
		int ix = mockOperations.indexOf( mockOperation );
		if( ix == -1 )
			throw new RuntimeException( "Unkonws MockOperation specified to removeMockOperation" );

		mockOperations.remove( ix );
		fireMockOperationRemoved( mockOperation );
		mockOperation.release();
		getConfig().removeMockOperation( ix );
	}

	public void addMockServiceListener( MockServiceListener listener )
	{
		mockServiceListeners.add( listener );
	}

	public void removeMockServiceListener( MockServiceListener listener )
	{
		mockServiceListeners.remove( listener );
	}

	protected void fireMockOperationAdded( WsdlMockOperation mockOperation )
	{
		MockServiceListener[] listeners = mockServiceListeners.toArray( new MockServiceListener[mockServiceListeners
				.size()] );
		for( MockServiceListener listener : listeners )
		{
			listener.mockOperationAdded( mockOperation );
		}
	}

	protected void fireMockOperationRemoved( WsdlMockOperation mockOperation )
	{
		MockServiceListener[] listeners = mockServiceListeners.toArray( new MockServiceListener[mockServiceListeners
				.size()] );
		for( MockServiceListener listener : listeners )
		{
			listener.mockOperationRemoved( mockOperation );
		}
	}

	protected void fireMockResponseAdded( WsdlMockResponse mockResponse )
	{
		MockServiceListener[] listeners = mockServiceListeners.toArray( new MockServiceListener[mockServiceListeners
				.size()] );
		for( MockServiceListener listener : listeners )
		{
			listener.mockResponseAdded( mockResponse );
		}
	}

	protected void fireMockResponseRemoved( WsdlMockResponse mockResponse )
	{
		MockServiceListener[] listeners = mockServiceListeners.toArray( new MockServiceListener[mockServiceListeners
				.size()] );
		for( MockServiceListener listener : listeners )
		{
			listener.mockResponseRemoved( mockResponse );
		}
	}

	@Override
	public ImageIcon getIcon()
	{
		return iconAnimator.getIcon();
	}

	public WsdlMockOperation getFaultMockOperation()
	{
		return faultMockOperation;
	}

	public void setFaultMockOperation( WsdlMockOperation mockOperation )
	{
		faultMockOperation = mockOperation;
		if( faultMockOperation == null )
		{
			if( getConfig().isSetFaultMockOperation() )
			{
				getConfig().unsetFaultMockOperation();
			}
		}
		else
		{
			getConfig().setFaultMockOperation( faultMockOperation.getName() );
		}
	}

	private class MockServiceIconAnimator extends ModelItemIconAnimator<WsdlMockService> implements MockRunListener
	{
		public MockServiceIconAnimator()
		{
			super( WsdlMockService.this, "/mockService.gif", "/mockService", 4, "gif" );
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
			WsdlMockService.this.mockRunner = null;
		}
	}

	public String getLocalEndpoint()
	{
		String host = getHost();
		if( StringUtils.isNullOrEmpty( host ) )
			host = "127.0.0.1";

		return "http://" + host + ":" + getPort() + getPath();
	}

	public boolean isRequireSoapVersion()
	{
		return getSettings().getBoolean( REQUIRE_SOAP_VERSION );
	}

	public void setRequireSoapVersion( boolean requireSoapVersion )
	{
		getSettings().setBoolean( REQUIRE_SOAP_VERSION, requireSoapVersion );
	}

	public boolean isRequireSoapAction()
	{
		return getSettings().getBoolean( REQUIRE_SOAP_ACTION );
	}

	public void setRequireSoapAction( boolean requireSoapAction )
	{
		getSettings().setBoolean( REQUIRE_SOAP_ACTION, requireSoapAction );
	}

	public WsdlMockRunner start() throws Exception
	{
		return start( null );
	}

	public boolean hasMockOperation( Operation operation )
	{
		return getMockOperation( operation ) != null;
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
			startScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
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
			stopScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
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

		if( onRequestScriptEngine != null )
			onRequestScriptEngine.setScript( script );

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
		if( afterRequestScriptEngine != null )
			afterRequestScriptEngine.setScript( script );

		notifyPropertyChanged( "afterRequestScript", oldScript, script );
	}

	public String getAfterRequestScript()
	{
		return getConfig().isSetAfterRequestScript() ? getConfig().getAfterRequestScript().getStringValue() : null;
	}

	public Object runOnRequestScript( WsdlMockRunContext runContext, WsdlMockRunner runner, WsdlMockRequest mockRequest )
			throws Exception
	{
		String script = getOnRequestScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( onRequestScriptEngine == null )
		{
			onRequestScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
			onRequestScriptEngine.setScript( script );
		}

		onRequestScriptEngine.setVariable( "context", runContext );
		onRequestScriptEngine.setVariable( "mockRequest", mockRequest );
		onRequestScriptEngine.setVariable( "mockRunner", runner );
		onRequestScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return onRequestScriptEngine.run();
	}

	public Object runAfterRequestScript( WsdlMockRunContext runContext, WsdlMockRunner runner, MockResult mockResult )
			throws Exception
	{
		String script = getAfterRequestScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( afterRequestScriptEngine == null )
		{
			afterRequestScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
			afterRequestScriptEngine.setScript( script );
		}

		afterRequestScriptEngine.setVariable( "context", runContext );
		afterRequestScriptEngine.setVariable( "mockResult", mockResult );
		afterRequestScriptEngine.setVariable( "mockRunner", runner );
		afterRequestScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return afterRequestScriptEngine.run();
	}

	public List<? extends ModelItem> getChildren()
	{
		return mockOperations;
	}

	public List<MockOperation> getMockOperationList()
	{
		return Collections.unmodifiableList( new ArrayList<MockOperation>( mockOperations ) );
	}

	public String getIncomingWss()
	{
		return getConfig().getIncomingWss();
	}

	public void setIncomingWss( String incomingWss )
	{
		String old = getIncomingWss();
		getConfig().setIncomingWss( incomingWss );
		notifyPropertyChanged( INCOMING_WSS, old, incomingWss );
	}

	public String getOutgoingWss()
	{
		return getConfig().getOutgoingWss();
	}

	public void setOutgoingWss( String outgoingWss )
	{
		String old = getOutgoingWss();
		getConfig().setOutgoingWss( outgoingWss );
		notifyPropertyChanged( OUGOING_WSS, old, outgoingWss );
	}

	public boolean isDispatchResponseMessages()
	{
		return getConfig().getDispatchResponseMessages();
	}

	public void setDispatchResponseMessages( boolean dispatchResponseMessages )
	{
		boolean old = isDispatchResponseMessages();
		getConfig().setDispatchResponseMessages( dispatchResponseMessages );
		notifyPropertyChanged( "dispatchResponseMessages", old, dispatchResponseMessages );
	}

	public List<WsdlOperation> getMockedOperations()
	{
		List<WsdlOperation> result = new ArrayList<WsdlOperation>();

		for( WsdlMockOperation mockOperation : mockOperations )
			result.add( mockOperation.getOperation() );

		return result;
	}

	public void setDocroot( String docroot )
	{
		docrootProperty.set( docroot, true );
	}

	public String getDocroot()
	{
		return docrootProperty.get();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void resolve( ResolveContext context )
	{
		super.resolve( context );
		docrootProperty.resolveFile( context, "Missing MockService docroot" );
	}

	public void replace( WsdlMockOperation mockOperation, MockOperationConfig reloadedMockOperation )
	{
		int ix = mockOperations.indexOf( mockOperation );
		if( ix == -1 )
			throw new RuntimeException( "Unkonws MockOperation specified to removeMockOperation" );

		mockOperations.remove( ix );
		fireMockOperationRemoved( mockOperation );
		mockOperation.release();
		getConfig().removeMockOperation( ix );

		MockOperationConfig newConfig = ( MockOperationConfig )getConfig().insertNewMockOperation( ix ).set(
				reloadedMockOperation ).changeType( MockOperationConfig.type );
		WsdlMockOperation newOperation = new WsdlMockOperation( this, newConfig );
		mockOperations.add( ix, newOperation );
		newOperation.afterLoad();
		fireMockOperationAdded( newOperation );
	}

}
