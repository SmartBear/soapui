/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.RequestAssertionConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.WsdlAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.WsdlAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.UISupport;

/**
 * WsdlRequest extension that adds WsdlAssertions
 * 
 * @author Ole.Matzura
 */

public class WsdlTestRequest extends WsdlRequest implements Assertable
{
	public static final String RESPONSE_PROPERTY = WsdlTestRequest.class.getName() + "@response";
	public static final String STATUS_PROPERTY = WsdlTestRequest.class.getName() + "@status";

	private static ImageIcon validRequestIcon;
	private static ImageIcon failedRequestIcon;
	private static ImageIcon disabledRequestIcon;
	private static ImageIcon unknownRequestIcon;

	private AssertionStatus currentStatus;
	private final WsdlTestRequestStep testStep;

	private AssertionsSupport assertionsSupport;
	private WsdlResponseMessageExchange messageExchange;
	private final boolean forLoadTest;
	private PropertyChangeNotifier notifier;

	public WsdlTestRequest( WsdlOperation operation, WsdlRequestConfig callConfig, WsdlTestRequestStep testStep,
				boolean forLoadTest )
	{
		super( operation, callConfig, forLoadTest );
		this.forLoadTest = forLoadTest;

		setSettings( new XmlBeansSettingsImpl( this, testStep.getSettings(), callConfig.getSettings() ) );

		this.testStep = testStep;

		initAssertions();
		initIcons();
	}

	public WsdlTestCase getTestCase()
	{
		return testStep.getTestCase();
	}

	protected void initIcons()
	{
		if( validRequestIcon == null )
			validRequestIcon = UISupport.createImageIcon( "/valid_request.gif" );

		if( failedRequestIcon == null )
			failedRequestIcon = UISupport.createImageIcon( "/invalid_request.gif" );

		if( unknownRequestIcon == null )
			unknownRequestIcon = UISupport.createImageIcon( "/unknown_request.gif" );

		if( disabledRequestIcon == null )
			disabledRequestIcon = UISupport.createImageIcon( "/disabled_request.gif" );
	}

	@Override
	protected RequestIconAnimator<?> initIconAnimator()
	{
		return new TestRequestIconAnimator( this );
	}

	private void initAssertions()
	{
		assertionsSupport = new AssertionsSupport( testStep, getConfig().getAssertionList() );
	}

	public int getAssertionCount()
	{
		return assertionsSupport.getAssertionCount();
	}

	public WsdlMessageAssertion getAssertionAt( int c )
	{
		return assertionsSupport.getAssertionAt( c );
	}

	public void setResponse( WsdlResponse response, SubmitContext context )
	{
		WsdlResponse oldResponse = getResponse();
		super.setResponse( response, context );

		if( response != oldResponse )
			assertResponse( context );
	}

	public void assertResponse( SubmitContext context )
	{
		if( notifier == null )
			notifier = new PropertyChangeNotifier();
		
		messageExchange = new WsdlResponseMessageExchange( this );

		// assert!
		for( Iterator<WsdlMessageAssertion> iter = assertionsSupport.iterator(); iter.hasNext(); )
		{
			WsdlMessageAssertion assertion = iter.next();
			assertion.assertResponse( messageExchange, context );
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
			RequestAssertionConfig assertionConfig = getConfig().addNewAssertion();
			assertionConfig.setType( WsdlAssertionRegistry.getInstance().getAssertionTypeForName( assertionLabel ) );

			WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion( assertionConfig );
			if( assertion == null )
				return null;
			
			assertionsSupport.fireAssertionAdded( assertion );

			if( getResponse() != null )
			{
				assertion.assertResponse( new WsdlResponseMessageExchange( this ), new WsdlTestRunContext( testStep ) );
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
			int ix = assertionsSupport.removeAssertion( ( WsdlMessageAssertion ) assertion );
			getConfig().removeAssertion( ix );
		}
		finally
		{
			((WsdlMessageAssertion)assertion).release();
			notifier.notifyChange();
		}
	}

	public AssertionStatus getAssertionStatus()
	{
		currentStatus = AssertionStatus.UNKNOWN;

		if( messageExchange != null )
		{
			if( !messageExchange.hasResponse() && 
					getOperation().isBidirectional() )
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
		if( forLoadTest )
			return null;

		TestMonitor testMonitor = SoapUI.getTestMonitor();
		if( testMonitor != null && testMonitor.hasRunningLoadTest( testStep.getTestCase() ) )
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

	@Override
	public void updateConfig( WsdlRequestConfig request )
	{
		super.updateConfig( request );

		assertionsSupport.updateConfig( getConfig().getAssertionList() );

		List<AttachmentConfig> attachmentConfigs = getConfig().getAttachmentList();
		for( int i = 0; i < attachmentConfigs.size(); i++ )
		{
			AttachmentConfig config = attachmentConfigs.get( i );
			getAttachmentsList().get( i ).updateConfig( config );
		}
	}

	@Override
	public void release()
	{
		super.release();
		assertionsSupport.release();
	}

	public String getAssertableContent()
	{
		return getResponse() == null ? null : getResponse().getContentAsString();
	}

	public WsdlTestRequestStep getTestStep()
	{
		return testStep;
	}

	public WsdlInterface getInterface()
	{
		return getOperation().getInterface();
	}

	protected static class TestRequestIconAnimator extends RequestIconAnimator<WsdlTestRequest>
	{
		public TestRequestIconAnimator( WsdlTestRequest modelItem)
		{
			super( modelItem, "/request.gif", "/exec_request", 4, "gif" );
		}

		@Override
		public boolean beforeSubmit( Submit submit, SubmitContext context )
		{
			if( SoapUI.getTestMonitor() != null && SoapUI.getTestMonitor().hasRunningLoadTest( getTarget().getTestCase() ) )
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

	public String getInterfaceName()
	{
		return testStep.getInterfaceName();
	}

	public String getOperationName()
	{
		return testStep.getOperationName();
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		RequestAssertionConfig conf = getConfig().addNewAssertion();
		conf.set( ((WsdlMessageAssertion)source).getConfig() );
		conf.setName( name );

		WsdlMessageAssertion result = assertionsSupport.addWsdlAssertion( conf );
		assertionsSupport.fireAssertionAdded( result );
		return result;
	}

	public WsdlMessageAssertion importAssertion( WsdlMessageAssertion source, boolean overwrite, boolean createCopy )
	{
		RequestAssertionConfig conf = getConfig().addNewAssertion();
		conf.set( source.getConfig() );
		if( createCopy && conf.isSetId() )
			conf.unsetId();

		if( !source.isAllowMultiple() )
		{
			List<WsdlMessageAssertion> existing = assertionsSupport.getAssertionsOfType( source.getClass() );
			if( !existing.isEmpty() && !overwrite )
				return null;

			while( !existing.isEmpty() )
			{
				removeAssertion( existing.remove( 0 ) );
			}
		}

		WsdlMessageAssertion result = assertionsSupport.addWsdlAssertion( conf );
		assertionsSupport.fireAssertionAdded( result );
		return result;
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
	
	public Map<String,TestAssertion> getAssertions()
	{
		Map<String,TestAssertion> result = new HashMap<String, TestAssertion>();
		
		for( TestAssertion assertion : getAssertionList() )
			result.put( assertion.getName(), assertion );
		
		return result;
	}

	public String getDefaultAssertableContent()
	{
		return getOperation().createResponse( true );
	}
}
