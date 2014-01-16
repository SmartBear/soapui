/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock;

import java.io.File;
import java.io.IOException;
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
import com.eviware.soapui.config.MockOperationDocumentConfig;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.MockServiceExternalDependency;
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
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

/**
 * A MockService for simulation WsdlInterfaces and their operations
 * 
 * @author ole.matzura
 */

public class WsdlMockService extends AbstractMockService<WsdlMockOperation>
{
	private static final String REQUIRE_SOAP_VERSION = WsdlMockService.class.getName() + "@require-soap-version";
	private static final String REQUIRE_SOAP_ACTION = WsdlMockService.class.getName() + "@require-soap-action";

	public final static String START_SCRIPT_PROPERTY = WsdlMockService.class.getName() + "@startScript";
	public final static String STOP_SCRIPT_PROPERTY = WsdlMockService.class.getName() + "@stopScript";
	public static final String INCOMING_WSS = WsdlMockService.class.getName() + "@incoming-wss";
	public static final String OUGOING_WSS = WsdlMockService.class.getName() + "@outgoing-wss";

	private SoapUIScriptEngine startScriptEngine;
	private SoapUIScriptEngine stopScriptEngine;
	private BeanPathPropertySupport docrootProperty;
	private ScriptEnginePool onRequestScriptEnginePool;
	private ScriptEnginePool afterRequestScriptEnginePool;
	private WsdlMockOperation faultMockOperation;
	private String mockServiceEndpoint;

	public WsdlMockService( Project project, MockServiceConfig config )
	{
		super( config, project );

		List<MockOperationConfig> testStepConfigs = config.getMockOperationList();
		for( MockOperationConfig tsc : testStepConfigs )
		{
			WsdlMockOperation testStep = new WsdlMockOperation( this, tsc );
			addMockOperation( testStep );
		}

		if( !getSettings().isSet( REQUIRE_SOAP_ACTION ) )
			setRequireSoapAction( false );

		try
		{
			if( !config.isSetHost() || !StringUtils.hasContent( config.getHost() ) )
				config.setHost( InetAddress.getLocalHost().getHostName() );
		}
		catch( UnknownHostException e )
		{
			SoapUI.logError( e );
		}

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
			faultMockOperation = ( WsdlMockOperation )getMockOperationByName( getConfig().getFaultMockOperation() );
		}
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


	public WsdlMockOperation getMockOperation( Operation operation )
	{
		for( int c = 0; c < getMockOperationCount(); c++ )
		{
			WsdlMockOperation mockOperation = getMockOperationAt( c );
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

		addMockOperation( mockOperation );
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

		for( MockOperation mockOperation : getMockOperationList() )
		{
			WsdlOperation operation = ( WsdlOperation )mockOperation.getOperation();
			if( operation != null )
				result.add( operation.getInterface() );
		}

		return result.toArray( new WsdlInterface[result.size()] );
	}

	@Override
	public void release()
	{
		super.release();

		for( MockOperation operation : getMockOperationList() )
		{
			((WsdlMockOperation)operation).release();
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


	protected void fireMockOperationAdded( WsdlMockOperation mockOperation )
	{
		for( MockServiceListener listener : getMockServiceListeners())
		{
			listener.mockOperationAdded( mockOperation );
		}
	}

	protected void fireMockOperationRemoved( WsdlMockOperation mockOperation )
	{
        for( MockServiceListener listener : getMockServiceListeners())
		{
			listener.mockOperationRemoved( mockOperation );
		}
	}

	protected void fireMockResponseAdded( WsdlMockResponse mockResponse )
	{
        for( MockServiceListener listener : getMockServiceListeners())
		{
			listener.mockResponseAdded( mockResponse );
		}
	}

	protected void fireMockResponseRemoved( WsdlMockResponse mockResponse )
	{
        for( MockServiceListener listener : getMockServiceListeners())
		{
			listener.mockResponseRemoved( mockResponse );
		}
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

	@Override
	public WsdlMockRunner start() throws Exception
	{
		return start( null );
	}

	public String getLocalEndpoint()
	{
		String host = getHost();
		if( StringUtils.isNullOrEmpty( host ) )
		{
			host = "127.0.0.1";
		}

		return getProtocol() + host + ":" + getPort() + getPath();
	}

	public String getHost()
	{
		return getConfig().getHost();
	}

	private String getProtocol()
	{
		try
		{
			boolean sslEnabled = SoapUI.getSettings().getBoolean( SSLSettings.ENABLE_MOCK_SSL );
			String protocol = sslEnabled ? "https://" : "http://";
			return protocol;
		}
		catch( Exception e )
		{
			return "http://";
		}
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

	public List<? extends ModelItem> getChildren()
	{
		return mockOperations;
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

		for( MockOperation mockOperation : mockOperations )
		{
			result.add( ( WsdlOperation )mockOperation.getOperation() );
		}


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

	public void replace( WsdlMockOperation mockOperation, MockOperationConfig reloadedMockOperation )
	{
		int ix = mockOperations.indexOf( mockOperation );
		if( ix == -1 )
			throw new RuntimeException( "Unkonws MockOperation specified to removeMockOperation" );

		mockOperations.remove( ix );
		fireMockOperationRemoved( mockOperation );
		mockOperation.release();
		getConfig().removeMockOperation( ix );

		MockOperationConfig newConfig = ( MockOperationConfig )getConfig().insertNewMockOperation( ix )
				.set( reloadedMockOperation ).changeType( MockOperationConfig.type );
		WsdlMockOperation newOperation = new WsdlMockOperation( this, newConfig );
		mockOperations.add( ix, newOperation );
		newOperation.afterLoad();
		fireMockOperationAdded( newOperation );
	}

	public void export( File file )
	{
		try
		{
			this.getConfig().newCursor().save( file );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	public void importMockOperation( File file )
	{
		MockOperationConfig mockOperationNewConfig = null;

		if( !file.exists() )
		{
			UISupport.showErrorMessage( "Error loading mock operation." );
			return;
		}

		try
		{
			mockOperationNewConfig = MockOperationDocumentConfig.Factory.parse( file ).getMockOperation();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		if( mockOperationNewConfig != null )
		{
			MockOperationConfig newConfig = ( MockOperationConfig )getConfig().addNewMockOperation()
					.set( mockOperationNewConfig ).changeType( TestCaseConfig.type );
			WsdlMockOperation newMockOperation = new WsdlMockOperation( this, newConfig );
			ModelSupport.unsetIds( newMockOperation );
			newMockOperation.afterLoad();
			mockOperations.add( newMockOperation );
			fireMockOperationAdded( newMockOperation );

			resolveImportedMockOperation( newMockOperation );

		}
		else
		{
			UISupport.showErrorMessage( "Not valid mock operation xml" );
		}
	}

	private void resolveImportedMockOperation( WsdlMockOperation mockOperation )
	{
		ResolveDialog resolver = new ResolveDialog( "Validate MockOperation", "Checks MockOperation for inconsistencies",
				null );
		resolver.setShowOkMessage( false );
		resolver.resolve( mockOperation );
	}

	public String toString()
	{
		return getName();
	}

	public String getMockServiceEndpoint()
	{
		return mockServiceEndpoint;
	}

	public void setMockServiceEndpoint( String mockServiceEndpoint )
	{
		this.mockServiceEndpoint = mockServiceEndpoint;
	}

	public String getLocalMockServiceEndpoint()
	{
		if( mockServiceEndpoint != null )
			return mockServiceEndpoint + getPath();

		String host = getHost();
		if( StringUtils.isNullOrEmpty( host ) )
			host = "127.0.0.1";

		int port = ( int )( getSettings().getBoolean( SSLSettings.ENABLE_MOCK_SSL ) ? getSettings().getLong(
				SSLSettings.MOCK_PORT, 443 ) : getPort() );

		return getProtocol() + host + ":" + port + getPath();
	}

}
