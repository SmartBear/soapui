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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.AsyncResponseStepConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.mock.MockRunnerManager;
import com.eviware.soapui.impl.wsdl.mock.MockRunnerManagerImpl;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.MockRunListenerAdapter;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.RequestAssertedMessageExchange;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;

// TODO Ericsson: Move WsdlAsyncResponseTestStep to pro?
public class WsdlAsyncResponseTestStep extends WsdlTestStepWithProperties implements PropertyChangeListener, Assertable
{
	private static final Logger log = Logger.getLogger( WsdlAsyncResponseTestStep.class );

	public static final String STATUS_PROPERTY = WsdlAsyncResponseTestStep.class.getName() + "@status";
	public static final String TIMEOUT_PROPERTY = WsdlAsyncResponseTestStep.class.getName() + "@timeout";
	public static final String MATCHING_VALUE_PROPERTY = WsdlAsyncResponseTestStep.class.getName() + "@matchingValue";

	private AsyncResponseStepConfig testStepConfig;
	private MockResponseConfig mockResponseConfig;
	private WsdlMockOperation mockOperation;
	private WsdlMockService mockService;
	private WsdlMockResponse mockResponse;

	private AssertionsSupport assertionsSupport;
	private InternalMockRunListener listener;

	private final InternalProjectListener projectListener = new InternalProjectListener();
	private final InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
	private WsdlInterface iface;
	private AssertionStatus oldStatus;

	/**
	 * Constructor
	 * 
	 * @param testCase
	 * @param config
	 * @param forLoadTest
	 */
	public WsdlAsyncResponseTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( config.getConfig() != null )
		{
			testStepConfig = ( AsyncResponseStepConfig )config.getConfig().changeType( AsyncResponseStepConfig.type );
			mockResponseConfig = testStepConfig.getResponse();
		}
		else
		{
			testStepConfig = ( AsyncResponseStepConfig )config.addNewConfig().changeType( AsyncResponseStepConfig.type );
			mockResponseConfig = testStepConfig.addNewResponse();
		}

		assertionsSupport = new AssertionsSupport( this, new AssertableConfig()
		{

			public TestAssertionConfig addNewAssertion()
			{
				return testStepConfig.addNewAssertion();
			}

			public List<TestAssertionConfig> getAssertionList()
			{
				return testStepConfig.getAssertionList();
			}

			public void removeAssertion( int ix )
			{
				testStepConfig.removeAssertion( ix );
			}
		} );

		createMockService();

		if( !forLoadTest )
		{
			setIcon( UISupport.createImageIcon( "/asyncResponseStep.gif" ) );
			if( iface != null )
			{
				iface.getProject().addProjectListener( projectListener );
				iface.addInterfaceListener( interfaceListener );
			}
		}

		addProperty( new TestStepBeanProperty( "Response", false, mockResponse, "responseContent", this ) );
		addProperty( new DefaultTestStepProperty( "Request", true, new DefaultTestStepProperty.PropertyHandlerAdapter()
		{
			public String getValue( DefaultTestStepProperty property )
			{
				WsdlMockResult mockResult = getMockResponse().getMockResult();
				if( mockResult == null )
				{
					return null;
				}
				else
				{
					return mockResult.getMockRequest().getRequestContent();
				}
			}
		}, this ) );

		addProperty( new TestStepBeanProperty( "Matching Value", false, this, "matchingValue", this ) );
	}

	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );

		testStepConfig = ( AsyncResponseStepConfig )config.getConfig().changeType( AsyncResponseStepConfig.type );
		mockResponseConfig = this.testStepConfig.getResponse();
		mockResponse.setConfig( mockResponseConfig );

		assertionsSupport.refresh();
	}

	public boolean cancel()
	{
		if( listener != null )
		{
			listener.cancel();
		}

		return true;
	}

	public void createMockService()
	{
		WsdlProject project = getTestCase().getTestSuite().getProject();
		MockRunnerManager manager = MockRunnerManagerImpl.getInstance( getTestCase() );

		mockService = manager.getMockService( getPort(), getPath() );

		iface = ( WsdlInterface )project.getInterfaceByName( testStepConfig.getInterface() );
		iface.addInterfaceListener( interfaceListener );

		WsdlOperation operation = iface.getOperationByName( testStepConfig.getOperation() );
		mockOperation = mockService.getMockOperation( operation );
		if( mockOperation == null )
			mockOperation = mockService.addNewMockOperation( operation );
		// mockOperation.setDispatchStyle( DispatchStyleConfig.QUERY_MATCH );
		mockResponse = mockOperation.addNewMockResponse( mockResponseConfig );
		mockResponse.addPropertyChangeListener( this );

		listener = new InternalMockRunListener();
		mockService.addMockRunListener( listener );
	}

	public TestStepResult run( TestRunner testRunner, TestRunContext context )
	{
		WsdlMockResponse oldResponse = getMockResponse();
		mockResponse = null; // mockOperation.addNewMockResponse(
									// getRequestQuery(), getMatchingValue() );
		mockResponse.setConfig( oldResponse.getConfig() );

		WsdlSingleMessageExchangeTestStepResult result = new WsdlSingleMessageExchangeTestStepResult( this );

		result.startTimer();
		long timeout = getTimeout();

		synchronized( listener )
		{
			long start = System.currentTimeMillis();
			do
			{
				try
				{
					listener.wait( timeout );
				}
				catch( InterruptedException e )
				{
					// Do nothing
				}

				timeout -= ( System.currentTimeMillis() - start );
			}
			while( timeout > 0 );
		}

		if( listener.getResult() != null )
		{
			AssertedWsdlMockResultMessageExchange messageExchange = new AssertedWsdlMockResultMessageExchange( listener
					.getResult() );

			result.setMessageExchange( messageExchange );

			context.setProperty( AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, messageExchange );
			updateAssertionStatus( listener.getResult(), ( ( context ) ) );
		}

		result.stopTimer();

		if( listener.getResult() == null )
		{
			if( listener.isCanceled() )
			{
				result.setStatus( TestStepStatus.CANCELED );
			}
			else
			{
				result.setStatus( TestStepStatus.FAILED );
				result.addMessage( ( new StringBuilder() ).append( "Timeout occured after " ).append( timeout ).append(
						" milliseconds" ).toString() );
			}
		}
		else if( getAssertionStatus() == AssertionStatus.FAILED )
		{
			result.setStatus( TestStepStatus.FAILED );
			if( getAssertionCount() == 0 )
			{
				result.addMessage( "Invalid/empty request" );
			}
			else
			{
				for( int i = 0; i < getAssertionCount(); i++ )
				{
					AssertionError assertionErrors[] = getAssertionAt( i ).getErrors();
					if( assertionErrors != null )
					{
						for( int j = 0; j < assertionErrors.length; j++ )
						{
							AssertionError assertionError = assertionErrors[j];
							result.addMessage( assertionError.getMessage() );
						}
					}
				}
			}
		}
		else
		{
			result.setStatus( TestStepStatus.OK );
		}

		return result;
	}

	private void updateAssertionStatus( WsdlMockResult mockResult, SubmitContext submitContext )
	{
		if( oldStatus == null )
		{
			oldStatus = getAssertionStatus();
		}

		for( int i = 0; i < getAssertionCount(); i++ )
		{
			WsdlMessageAssertion assertion = getAssertionAt( i );
			if( !assertion.isDisabled() )
				assertion.assertRequest( new WsdlMockResultMessageExchange( mockResult, getMockResponse() ), submitContext );
		}

		AssertionStatus newAssertionStatus = getAssertionStatus();
		if( newAssertionStatus != oldStatus )
		{
			notifyPropertyChanged( STATUS_PROPERTY, oldStatus, newAssertionStatus );
			oldStatus = newAssertionStatus;
		}
	}

	public void finish( TestRunner testrunner, TestRunContext testruncontext )
	{
	}

	public WsdlMockResponse getMockResponse()
	{
		return mockResponse;
	}

	public void setPort( int port )
	{
		mockService.setPort( port );
		testStepConfig.setPort( port );
	}

	public int getPort()
	{
		return testStepConfig.getPort();
	}

	public void setPath( String path )
	{
		mockService.setPath( path );
		testStepConfig.setPath( path );
	}

	public String getPath()
	{
		return testStepConfig.getPath();
	}

	public void setRequestQuery( String query )
	{
		testStepConfig.setRequestQuery( query );
	}

	public String getRequestQuery()
	{
		return testStepConfig.getRequestQuery();
	}

	public void setMatchingValue( String value )
	{
		// Make sure matching value is never null.
		if( value == null )
		{
			value = "";
		}

		String oldValue = testStepConfig.getMatchingValue();

		if( !value.equals( oldValue ) )
		{
			testStepConfig.setMatchingValue( value );
			notifyPropertyChanged( MATCHING_VALUE_PROPERTY, oldValue, value );
		}
	}

	public String getMatchingValue()
	{
		return testStepConfig.getMatchingValue();
	}

	public long getContentLength()
	{
		if( mockResponse == null )
		{
			return 0;
		}
		else
		{
			return mockResponse.getContentLength();
		}
	}

	public String getEncoding()
	{
		return mockResponse.getEncoding();
	}

	public void setEncoding( String encoding )
	{
		mockResponse.setEncoding( encoding );
	}

	public boolean isMtomEnabled()
	{
		return mockResponse.isMtomEnabled();
	}

	public void setMtomEnabled( boolean mtomEnabled )
	{
		mockResponse.setMtomEnabled( mtomEnabled );
	}

	public String getOutgoingWss()
	{
		return mockResponse.getOutgoingWss();
	}

	public void setOutgoingWss( String outgoingWss )
	{
		mockResponse.setOutgoingWss( outgoingWss );
	}

	public boolean isForceMtom()
	{
		return mockResponse.isForceMtom();
	}

	public void setForceMtom( boolean forceMtom )
	{
		mockResponse.setForceMtom( forceMtom );
	}

	public boolean isInlineFilesEnabled()
	{
		return mockResponse.isInlineFilesEnabled();
	}

	public void setInlineFilesEnabled( boolean inlineFilesEnabled )
	{
		mockResponse.setInlineFilesEnabled( inlineFilesEnabled );
	}

	public boolean isMultipartEnabled()
	{
		return mockResponse.isMultipartEnabled();
	}

	public void setMultipartEnabled( boolean multipartEnabled )
	{
		mockResponse.setMultipartEnabled( multipartEnabled );
	}

	public long getResponseDelay()
	{
		return mockResponse.getResponseDelay();
	}

	public void setResponseDelay( long delay )
	{
		mockResponse.setResponseDelay( delay );
	}

	public String getResponseHttpStatus()
	{
		return mockResponse.getResponseHttpStatus();
	}

	public void setResponseHttpStatus( String status )
	{
		mockResponse.setResponseHttpStatus( status );
	}

	public boolean isEncodeAttachments()
	{
		return mockResponse.isEncodeAttachments();
	}

	public boolean isRemoveEmptyContent()
	{
		return mockResponse.isRemoveEmptyContent();
	}

	public boolean isStripWhitespaces()
	{
		return mockResponse.isStripWhitespaces();
	}

	public void setEncodeAttachments( boolean encodeAttachments )
	{
		mockResponse.setEncodeAttachments( encodeAttachments );
	}

	public void setRemoveEmptyContent( boolean removeEmptyContent )
	{
		mockResponse.setRemoveEmptyContent( removeEmptyContent );
	}

	public void setStripWhitespaces( boolean stripWhitespaces )
	{
		mockResponse.setStripWhitespaces( stripWhitespaces );
	}

	public void propertyChange( PropertyChangeEvent event )
	{
		if( event.getSource() == mockResponse )
		{
			mockResponseConfig.set( mockResponse.getConfig() );

			notifyPropertyChanged( event.getPropertyName(), event.getOldValue(), event.getNewValue() );
		}
		/*
		 * else if (event.getSource() == this) { if
		 * (event.getPropertyName().equals(MATCHING_VALUE_PROPERTY)) {
		 * testStepConfig.setMatchingValue((String) event.getNewValue());
		 * notifyPropertyChanged(event.getPropertyName(), event.getOldValue(),
		 * event.getNewValue()); } }
		 */
	}

	public WsdlMessageAssertion addAssertion( String assertion )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		TestAssertionConfig assertionConfig = testStepConfig.addNewAssertion();
		assertionConfig.setType( TestAssertionRegistry.getInstance().getAssertionTypeForName( assertion ) );

		WsdlMessageAssertion messageAssertion = assertionsSupport.addWsdlAssertion( assertionConfig );

		assertionsSupport.fireAssertionAdded( messageAssertion );

		if( getMockResponse().getMockResult() != null )
		{
			messageAssertion.assertRequest( new WsdlMockResultMessageExchange( getMockResponse().getMockResult(),
					getMockResponse() ), new WsdlSubmitContext( this ) );
			notifier.notifyChange();
		}

		return messageAssertion;
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		assertionsSupport.addAssertionsListener( listener );
	}

	public WsdlMessageAssertion getAssertionAt( int i )
	{
		return assertionsSupport.getAssertionAt( i );
	}

	public int getAssertionCount()
	{
		return assertionsSupport.getAssertionCount();
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		assertionsSupport.removeAssertionsListener( listener );
	}

	public AssertionStatus getAssertionStatus()
	{
		AssertionStatus currentStatus = AssertionStatus.UNKNOWN;
		int i = getAssertionCount();
		if( i == 0 )
		{
			return currentStatus;
		}

		int j = 0;
		do
		{
			if( j >= i )
			{
				break;
			}

			WsdlMessageAssertion assertion = getAssertionAt( j );
			if( !assertion.isDisabled() && assertion.getStatus() == AssertionStatus.FAILED )
			{
				currentStatus = AssertionStatus.FAILED;
				break;
			}

			j++ ;
		}
		while( true );

		if( currentStatus == AssertionStatus.UNKNOWN )
		{
			currentStatus = AssertionStatus.VALID;
		}

		return currentStatus;
	}

	public void removeAssertion( TestAssertion assertion )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		assertionsSupport.removeAssertion( ( WsdlMessageAssertion )assertion );

		notifier.notifyChange();
	}

	public String getAssertableContent()
	{
		WsdlMockResult result = getMockResponse().getMockResult();

		if( result == null )
		{
			return null;
		}
		else
		{
			return result.getMockRequest().getRequestContent();
		}
	}

	public TestStep getTestStep()
	{
		return this;
	}

	public void setName( String name )
	{
		super.setName( name );

		if( mockService != null )
		{
			mockService.setName( getName() );
		}
	}

	public WsdlInterface getInterface()
	{
		return getOperation().getInterface();
	}

	public WsdlOperation getOperation()
	{
		return getMockResponse().getMockOperation().getOperation();
	}

	public void setInterface( String interfaceName )
	{
		WsdlInterface wsdlInterface = ( WsdlInterface )getTestCase().getTestSuite().getProject().getInterfaceByName(
				interfaceName );

		if( wsdlInterface != null )
		{
			testStepConfig.setInterface( wsdlInterface.getName() );
			WsdlOperation wsdloperation = wsdlInterface.getOperationAt( 0 );
			testStepConfig.setOperation( wsdloperation.getName() );
			mockOperation.setOperation( wsdloperation );
		}
	}

	public void setOperation( String operationName )
	{
		WsdlOperation wsdlOperation = getInterface().getOperationByName( operationName );
		if( wsdlOperation != null )
		{
			testStepConfig.setOperation( operationName );
			mockOperation.setOperation( wsdlOperation );
		}
	}

	public void release()
	{
		super.release();

		assertionsSupport.release();

		if( mockResponse != null )
		{
			mockResponse.removePropertyChangeListener( this );
		}

		if( mockService != null )
		{
			mockService.release();
		}

		if( iface != null )
		{
			iface.getProject().removeProjectListener( projectListener );
			iface.removeInterfaceListener( interfaceListener );
		}
	}

	public TestAssertionRegistry.AssertableType getAssertableType()
	{
		return TestAssertionRegistry.AssertableType.REQUEST;
	}

	public Collection<Interface> getRequiredInterfaces()
	{
		ArrayList<Interface> interfaces = new ArrayList<Interface>();
		interfaces.add( getInterface() );

		return interfaces;
	}

	public String getDefaultSourcePropertyName()
	{
		return "Response";
	}

	public String getDefaultTargetPropertyName()
	{
		return "Resource";
	}

	public void beforeSave()
	{
		super.beforeSave();

		if( mockResponse != null )
		{
			mockResponse.beforeSave();
			mockResponseConfig.set( mockResponse.getConfig() );
		}
	}

	public long getTimeout()
	{
		return testStepConfig.getTimeout();
	}

	public void setTimeout( long newTimeout )
	{
		long oldTimeout = getTimeout();
		testStepConfig.setTimeout( newTimeout );
		notifyPropertyChanged( TIMEOUT_PROPERTY, Long.valueOf( oldTimeout ), Long.valueOf( newTimeout ) );
	}

	public boolean dependsOn( AbstractWsdlModelItem<?> modelItem )
	{
		return modelItem == getOperation().getInterface();
	}

	public WsdlMessageAssertion cloneAssertion( TestAssertion testAssertion, String s )
	{
		TestAssertionConfig assertionConfig = testStepConfig.addNewAssertion();
		assertionConfig.set( ( ( WsdlMessageAssertion )testAssertion ).getConfig() );
		assertionConfig.setName( s );
		WsdlMessageAssertion messageAssertion = assertionsSupport.addWsdlAssertion( assertionConfig );

		assertionsSupport.fireAssertionAdded( messageAssertion );
		return messageAssertion;
	}

	public List<TestAssertion> getAssertionList()
	{
		return new ArrayList<TestAssertion>( assertionsSupport.getAssertionList() );
	}

	public List<WsdlMessageAssertion> getChildren()
	{
		return assertionsSupport.getAssertionList();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		ArrayList<PropertyExpansion> expansions = new ArrayList<PropertyExpansion>();
		expansions.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, mockResponse, "responseContent" ) );

		StringToStringMap headers = mockResponse.getResponseHeaders();
		String s;
		for( Iterator<?> iterator = headers.keySet().iterator(); iterator.hasNext(); PropertyExpansionUtils
				.extractPropertyExpansions( this, new ResponseHeaderHolder( headers, s ), "value" ) )
			s = ( String )iterator.next();

		return ( PropertyExpansion[] )expansions.toArray( new PropertyExpansion[expansions.size()] );
	}

	public WsdlMessageAssertion getAssertionByName( String s )
	{
		return assertionsSupport.getAssertionByName( s );
	}

	public Map<String, TestAssertion> getAssertions()
	{
		HashMap<String, TestAssertion> hashmap = new HashMap<String, TestAssertion>();
		TestAssertion testassertion;
		for( Iterator<?> iterator = getAssertionList().iterator(); iterator.hasNext(); hashmap.put( testassertion
				.getName(), testassertion ) )
			testassertion = ( TestAssertion )iterator.next();

		return hashmap;
	}

	public String toString()
	{
		return WsdlAsyncResponseTestStep.class.getName() + " [port= " + this.getPort() + ", path= " + this.getPath()
				+ ", query=" + this.getRequestQuery() + ", value=" + this.getMatchingValue() + ", interface="
				+ this.getInterface().getName() + ", operation=" + this.getOperation().getName() + "]";
	}

	public String getDefaultAssertableContent()
	{
		return getOperation().createResponse( true );
	}

	private class AssertedWsdlMockResultMessageExchange extends WsdlMockResultMessageExchange implements
			RequestAssertedMessageExchange, AssertedXPathsContainer
	{
		private List<AssertedXPath> list;

		public AssertedWsdlMockResultMessageExchange( WsdlMockResult result )
		{
			super( result, result.getMockResponse() );
		}

		public AssertedXPath[] getAssertedXPathsForRequest()
		{
			return( list == null ? new AssertedXPath[0] : list.toArray( new AssertedXPath[list.size()] ) );
		}

		public void addAssertedXPath( AssertedXPath assertedXPath )
		{
			if( list == null )
			{
				list = new ArrayList<AssertedXPath>();
			}

			list.add( assertedXPath );
		}
	}

	private class ResponseHeaderHolder
	{
		private final StringToStringMap headers;
		private final String header;

		public String getValue()
		{
			return headers.get( header );
		}

		public void setValue( String value )
		{
			headers.put( header, value );
			getMockResponse().setResponseHeaders( headers );
		}

		public ResponseHeaderHolder( StringToStringMap headers, String header )
		{
			this.headers = headers;
			this.header = header;
		}
	}

	private class InternalInterfaceListener extends InterfaceListenerAdapter
	{
		public void operationRemoved( Operation operation )
		{
			if( operation == getOperation() )
			{
				log.debug( "Removing test step due to removed operation" );
				getTestCase().removeTestStep( WsdlAsyncResponseTestStep.this );
			}
		}

		public void operationUpdated( Operation operation )
		{
			if( operation == getOperation() )
			{
				setOperation( operation.getName() );
			}
		}
	}

	private class InternalProjectListener extends ProjectListenerAdapter
	{
		public void interfaceRemoved( Interface iface )
		{
			if( getOperation() != null && getOperation().getInterface().equals( iface ) )
			{
				log.debug( "Removing test step due to removed interface" );
				getTestCase().removeTestStep( WsdlAsyncResponseTestStep.this );
			}
		}
	}

	private class PropertyChangeNotifier
	{
		private AssertionStatus status;
		private ImageIcon icon;

		public PropertyChangeNotifier()
		{
			status = getAssertionStatus();
			icon = getIcon();
		}

		public void notifyChange()
		{
			AssertionStatus newStatus = getAssertionStatus();
			ImageIcon newIcon = getIcon();

			if( this.status != newStatus )
			{
				notifyPropertyChanged( WsdlAsyncResponseTestStep.STATUS_PROPERTY, this.status, newStatus );
			}

			if( this.icon != newIcon )
			{
				notifyPropertyChanged( ModelItem.ICON_PROPERTY, this.icon, newIcon );
			}
		}
	}

	private class InternalMockRunListener extends MockRunListenerAdapter
	{
		private WsdlMockResult result;
		private boolean canceled;

		public void onMockResult( MockResult mockResult )
		{
			result = ( WsdlMockResult )mockResult;

			// Wake up our thread if our MockResponse was used.
			if( result == null || ( result != null && result.getMockResponse() == mockResponse ) )
			{
				synchronized( this )
				{
					notifyAll();
				}
			}
		}

		public void cancel()
		{
			canceled = true;
			listener.onMockResult( null );
		}

		public WsdlMockResult getResult()
		{
			return result;
		}

		public boolean isCanceled()
		{
			return canceled;
		}
	}

}
