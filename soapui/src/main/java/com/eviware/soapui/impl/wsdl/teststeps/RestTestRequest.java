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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

public class RestTestRequest extends RestRequest implements RestTestRequestInterface
{
	private ImageIcon validRequestIcon;
	private ImageIcon failedRequestIcon;
	private ImageIcon disabledRequestIcon;
	private ImageIcon unknownRequestIcon;

	private AssertionStatus currentStatus;
	private RestTestRequestStep testStep;

	private AssertionsSupport assertionsSupport;
	private RestResponseMessageExchange messageExchange;
	private final boolean forLoadTest;
	private PropertyChangeNotifier notifier;

	public RestTestRequest( RestMethod method, RestRequestConfig callConfig, RestTestRequestStep testStep,
			boolean forLoadTest )
	{
		super( method, callConfig, forLoadTest );
		this.forLoadTest = forLoadTest;

		setSettings( new XmlBeansSettingsImpl( this, testStep.getSettings(), callConfig.getSettings() ) );

		this.testStep = testStep;

		initAssertions();

		if( !forLoadTest )
			initIcons();
	}

	public ModelItem getParent()
	{
		return getTestStep();
	}

	public WsdlTestCase getTestCase()
	{
		return testStep.getTestCase();
	}

	protected void initIcons()
	{
		validRequestIcon = UISupport.createImageIcon( "/valid_rest_request.gif" );
		failedRequestIcon = UISupport.createImageIcon( "/invalid_rest_request.gif" );
		unknownRequestIcon = UISupport.createImageIcon( "/unknown_rest_request.gif" );
		disabledRequestIcon = UISupport.createImageIcon( "/disabled_rest_request.gif" );

		// setIconAnimator(new RequestIconAnimator<RestTestRequest>(this,
		// "/rest_request.gif", "/exec_rest_request", 4, "gif"));
		setIconAnimator( new TestRequestIconAnimator( this ) );
	}

	private void initAssertions()
	{
		assertionsSupport = new AssertionsSupport( testStep, new AssertableConfig()
		{
			public TestAssertionConfig addNewAssertion()
			{
				return getConfig().addNewAssertion();
			}

			public List<TestAssertionConfig> getAssertionList()
			{
				return getConfig().getAssertionList();
			}

			public void removeAssertion( int ix )
			{
				getConfig().removeAssertion( ix );
			}

			public TestAssertionConfig insertAssertion( TestAssertionConfig source, int ix )
			{
				TestAssertionConfig conf = getConfig().insertNewAssertion( ix );
				conf.set( source );
				return conf;
			}

		} );
	}

	public int getAssertionCount()
	{
		return assertionsSupport.getAssertionCount();
	}

	public WsdlMessageAssertion getAssertionAt( int c )
	{
		return assertionsSupport.getAssertionAt( c );
	}

	public void setResponse( HttpResponse response, SubmitContext context )
	{
		super.setResponse( response, context );
		assertResponse( context );
	}

	public void assertResponse( SubmitContext context )
	{
		if( notifier == null )
			notifier = new PropertyChangeNotifier();

		messageExchange = getResponse() == null ? null : new RestResponseMessageExchange( this );

		if( messageExchange != null )
		{
			// assert!
			for( WsdlMessageAssertion assertion : assertionsSupport.getAssertionList() )
			{
				assertion.assertResponse( messageExchange, context );
			}
		}

		notifier.notifyChange();
	}

	private class PropertyChangeNotifier
	{
		private AssertionStatus oldStatus;
		private ImageIcon oldIcon;

		public PropertyChangeNotifier()
		{
			oldStatus = getAssertionStatus();
			oldIcon = getIcon();
		}

		public void notifyChange()
		{
			AssertionStatus newStatus = getAssertionStatus();
			ImageIcon newIcon = getIcon();

			if( oldStatus != newStatus )
				notifyPropertyChanged( STATUS_PROPERTY, oldStatus, newStatus );

			if( oldIcon != newIcon )
				notifyPropertyChanged( ICON_PROPERTY, oldIcon, getIcon() );

			oldStatus = newStatus;
			oldIcon = newIcon;
		}
	}

	public WsdlMessageAssertion addAssertion( String assertionLabel )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		try
		{
			WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion( assertionLabel );
			if( assertion == null )
				return null;

			if( getResponse() != null )
			{
				assertion.assertResponse( new RestResponseMessageExchange( this ), new WsdlTestRunContext( testStep ) );
				notifier.notifyChange();
			}

			return assertion;
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	public void removeAssertion( TestAssertion assertion )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		try
		{
			assertionsSupport.removeAssertion( ( WsdlMessageAssertion )assertion );

		}
		finally
		{
			( ( WsdlMessageAssertion )assertion ).release();
			notifier.notifyChange();
		}
	}

	public TestAssertion moveAssertion( int ix, int offset )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();
		WsdlMessageAssertion assertion = getAssertionAt( ix );
		try
		{
			return assertionsSupport.moveAssertion( ix, offset );
		}
		finally
		{
			( ( WsdlMessageAssertion )assertion ).release();
			notifier.notifyChange();
		}
	}

	public AssertionStatus getAssertionStatus()
	{
		currentStatus = AssertionStatus.UNKNOWN;

		if( messageExchange != null )
		{
			if( !messageExchange.hasResponse() && getOperation() != null && getOperation().isBidirectional() )
			{
				currentStatus = AssertionStatus.FAILED;
			}
		}
		else
			return currentStatus;

		int cnt = getAssertionCount();
		if( cnt == 0 )
			return currentStatus;

		for( int c = 0; c < cnt; c++ )
		{
			if( getAssertionAt( c ).getStatus() == AssertionStatus.FAILED )
			{
				currentStatus = AssertionStatus.FAILED;
				break;
			}
		}

		if( currentStatus == AssertionStatus.UNKNOWN )
			currentStatus = AssertionStatus.VALID;

		return currentStatus;
	}

	@Override
	public ImageIcon getIcon()
	{
		if( forLoadTest || getIconAnimator() == null)
			return null;

		TestMonitor testMonitor = SoapUI.getTestMonitor();
		if( testMonitor != null
				&& ( testMonitor.hasRunningLoadTest( getTestStep().getTestCase() ) || testMonitor
						.hasRunningSecurityTest( getTestStep().getTestCase() ) ) )
			return disabledRequestIcon;

		ImageIcon icon = getIconAnimator().getIcon();
		if( icon == getIconAnimator().getBaseIcon() )
		{
			AssertionStatus status = getAssertionStatus();
			if( status == AssertionStatus.VALID )
				return validRequestIcon;
			else if( status == AssertionStatus.FAILED )
				return failedRequestIcon;
			else if( status == AssertionStatus.UNKNOWN )
				return unknownRequestIcon;
		}

		return icon;
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		assertionsSupport.addAssertionsListener( listener );
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		assertionsSupport.removeAssertionsListener( listener );
	}

	/**
	 * Called when a testrequest is moved in a testcase
	 */

	public void updateConfig( RestRequestConfig request )
	{
		super.updateConfig( request );

		assertionsSupport.refresh();
	}

	@Override
	public void release()
	{
		super.release();
		assertionsSupport.release();

		if( getRestMethod() != null )
		{
			getRestMethod().getResource().removePropertyChangeListener( this );
		}

		messageExchange = null;
	}

	public String getAssertableContent()
	{
		return getResponseContentAsXml();
	}

	public RestTestRequestStep getTestStep()
	{
		return testStep;
	}

	public RestService getInterface()
	{
		return getOperation() == null ? null : getOperation().getInterface();
	}

	@Override
	public RestResource getOperation()
	{
		return testStep instanceof RestTestRequestStepInterface ? ( ( RestTestRequestStepInterface )testStep )
				.getResource() : null;
	}

	protected static class TestRequestIconAnimator extends RequestIconAnimator<RestTestRequest>
	{
		public TestRequestIconAnimator( RestTestRequestInterface modelItem )
		{
			super( ( RestTestRequest )modelItem, "/rest_request.gif", "/exec_rest_request.gif", 4 );
		}

		@Override
		public boolean beforeSubmit( Submit submit, SubmitContext context )
		{
			if( SoapUI.getTestMonitor() != null
					&& ( SoapUI.getTestMonitor().hasRunningLoadTest( getTarget().getTestCase() ) || SoapUI.getTestMonitor()
							.hasRunningSecurityTest( getTarget().getTestCase() ) ) )
				return true;

			return super.beforeSubmit( submit, context );
		}

		@Override
		public void afterSubmit( Submit submit, SubmitContext context )
		{
			if( submit.getRequest() == getTarget() )
				stop();
		}
	}

	public AssertableType getAssertableType()
	{
		return AssertableType.RESPONSE;
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		return assertionsSupport.cloneAssertion( source, name );
	}

	public WsdlMessageAssertion importAssertion( WsdlMessageAssertion source, boolean overwrite, boolean createCopy,
			String newName )
	{
		return assertionsSupport.importAssertion( source, overwrite, createCopy, newName );
	}

	public List<TestAssertion> getAssertionList()
	{
		return new ArrayList<TestAssertion>( assertionsSupport.getAssertionList() );
	}

	public WsdlMessageAssertion getAssertionByName( String name )
	{
		return assertionsSupport.getAssertionByName( name );
	}

	public ModelItem getModelItem()
	{
		return testStep;
	}

	public Map<String, TestAssertion> getAssertions()
	{
		return assertionsSupport.getAssertions();
	}

	public String getDefaultAssertableContent()
	{
		return "";
	}

	public String getResponseContentAsString()
	{
		return getResponse() == null ? null : getResponse().getContentAsString();
	}

	public void setPath( String fullPath )
	{
		super.setPath( fullPath );

		if( getOperation() == null )
		{
			setEndpoint( fullPath );
		}
	}

	public void setRestMethod( RestMethod restMethod )
	{
		RestMethod old = this.getRestMethod();

		if( old != null )
		{
			old.getResource().removePropertyChangeListener( this );
		}

		super.setRestMethod( restMethod );

		restMethod.getResource().addPropertyChangeListener( this );
		notifyPropertyChanged( "restMethod", old, restMethod );
	}

	public RestResource getResource()
	{
		return getRestMethod().getResource();
	}

	public String getRestMethodName()
	{
		return getRestMethod().getName();
	}

	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );
		assertionsSupport.resolve( context );
	}

	public String getServiceName()
	{
		return testStep instanceof RestTestRequestStepInterface ? ( ( RestTestRequestStepInterface )testStep )
				.getService() : null;
	}

	public boolean isDiscardResponse()
	{
		return getSettings().getBoolean( "discardResponse" );
	}

	public void setDiscardResponse( boolean discardResponse )
	{
		getSettings().setBoolean( "discardResponse", discardResponse );
	}

}
