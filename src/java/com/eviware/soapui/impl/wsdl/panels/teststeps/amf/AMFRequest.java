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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AMFRequestTestStepConfig;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractAnimatableModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;

public class AMFRequest extends AbstractAnimatableModelItem<ModelItemConfig> implements Assertable, TestRequest
{
	private final AMFRequestTestStep testStep;
	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();

	final static Logger logger = Logger.getLogger( AMFRequest.class );

	private AMFResponse response;
	private SoapUIScriptEngine scriptEngine;
	private String endpoint;
	private String amfCall;
	private String groovyScript;
	private HashMap<String, TestProperty> propertyMap;
	private String[] propertyNames;
	private List<Object> arguments = new ArrayList<Object>();

	private boolean forLoadTest;
	private AssertionStatus currentStatus;

	public static final String AMF_RESPONSE_CONTENT = "AMF_RESPONSE_CONTENT";

	private RequestIconAnimator<?> iconAnimator;
	private ImageIcon validRequestIcon;
	private ImageIcon failedRequestIcon;
	private ImageIcon disabledRequestIcon;
	private ImageIcon unknownRequestIcon;

	public AMFRequest( AMFRequestTestStep testStep )
	{
		this.testStep = testStep;
		initIcons();
	}

	public void addSubmitListener( SubmitListener listener )
	{
		submitListeners.add( listener );
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return ModelSupport.dependsOn( testStep, modelItem );
	}

	public Attachment[] getAttachments()
	{
		return null;
	}

	public String getEncoding()
	{
		return null;
	}

	public Operation getOperation()
	{
		return null;
	}

	public String getRequestContent()
	{
		return ( ( AMFRequestTestStepConfig )testStep.getConfig() ).getProperties().toString();
	}

	public MessagePart[] getRequestParts()
	{
		return null;
	}

	public MessagePart[] getResponseParts()
	{
		return null;
	}

	public String getTimeout()
	{
		return null;// testStep.getQueryTimeout();
	}

	public void removeSubmitListener( SubmitListener listener )
	{
		submitListeners.remove( listener );
	}

	public void setEncoding( String string )
	{
	}

	public AMFSubmit submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{

		return new AMFSubmit( this, submitContext, async );
	}

	public List<? extends ModelItem> getChildren()
	{
		return null;
	}

	public String getDescription()
	{
		return testStep.getDescription();
	}

	public String getId()
	{
		return testStep.getId();
	}

	public String getName()
	{
		return testStep.getName();
	}

	public ModelItem getParent()
	{
		return testStep.getParent();
	}

	public Settings getSettings()
	{
		return testStep.getSettings();
	}

	public SubmitListener[] getSubmitListeners()
	{
		return submitListeners.toArray( new SubmitListener[submitListeners.size()] );
	}

	public AMFRequestTestStep getTestStep()
	{
		return testStep;
	}

	public WsdlMessageAssertion importAssertion( WsdlMessageAssertion source, boolean overwrite, boolean createCopy )
	{
		return testStep.importAssertion( source, overwrite, createCopy );
	}

	public TestAssertion addAssertion( String selection )
	{
		return testStep.addAssertion( selection );
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		testStep.addAssertionsListener( listener );
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		return testStep.cloneAssertion( source, name );
	}

	public String getAssertableContent()
	{
		return testStep.getAssertableContent();
	}

	public AssertableType getAssertableType()
	{
		return testStep.getAssertableType();
	}

	public TestAssertion getAssertionAt( int c )
	{
		return testStep.getAssertionAt( c );
	}

	public TestAssertion getAssertionByName( String name )
	{
		return testStep.getAssertionByName( name );
	}

	public int getAssertionCount()
	{
		return testStep.getAssertionCount();
	}

	public List<TestAssertion> getAssertionList()
	{
		return testStep.getAssertionList();
	}

	public AssertionStatus getAssertionStatus()
	{
		currentStatus = AssertionStatus.UNKNOWN;

		if( getResponse() == null )
			return currentStatus;

		int cnt = getAssertionCount();
		if( cnt == 0 )
			return currentStatus;

		boolean hasEnabled = false;

		for( int c = 0; c < cnt; c++ )
		{
			if( !getAssertionAt( c ).isDisabled() )
				hasEnabled = true;

			if( getAssertionAt( c ).getStatus() == AssertionStatus.FAILED )
			{
				currentStatus = AssertionStatus.FAILED;
				break;
			}
		}

		if( currentStatus == AssertionStatus.UNKNOWN && hasEnabled )
			currentStatus = AssertionStatus.VALID;

		return currentStatus;
	}

	public Map<String, TestAssertion> getAssertions()
	{
		return testStep.getAssertions();
	}

	public String getDefaultAssertableContent()
	{
		return testStep.getDefaultAssertableContent();
	}

	public Interface getInterface()
	{
		return testStep.getInterface();
	}

	public ModelItem getModelItem()
	{
		return testStep.getModelItem();
	}

	public TestAssertion moveAssertion( int ix, int offset )
	{
		return testStep.moveAssertion( ix, offset );
	}

	public void removeAssertion( TestAssertion assertion )
	{
		testStep.removeAssertion( assertion );
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		testStep.removeAssertionsListener( listener );
	}

	public void setResponse( AMFResponse response )
	{
		this.response = response;
	}

	public AMFResponse getResponse()
	{
		return response;
	}

	public void initIcons()
	{
		if( validRequestIcon == null )
			validRequestIcon = UISupport.createImageIcon( "/valid_amf_request.gif" );

		if( failedRequestIcon == null )
			failedRequestIcon = UISupport.createImageIcon( "/invalid_amf_request.gif" );

		if( unknownRequestIcon == null )
			unknownRequestIcon = UISupport.createImageIcon( "/unknown_amf_request.gif" );

		if( disabledRequestIcon == null )
			disabledRequestIcon = UISupport.createImageIcon( "/disabled_amf_request.gif" );

		setIconAnimator( new RequestIconAnimator<AMFRequest>( this, "/amf_request.gif", "/exec_amf_request", 3, "gif" ) );
	}

	protected RequestIconAnimator<?> initIconAnimator()
	{
		return new RequestIconAnimator<AMFRequest>( this, "/amf_request.gif", "/exec_amf_request", 3, "gif" );
	}

	public static class RequestIconAnimator<T extends AMFRequest> extends ModelItemIconAnimator<T> implements
			SubmitListener
	{
		public RequestIconAnimator( T modelItem, String baseIcon, String animIconRoot, int iconCount, String iconExtension )
		{
			super( modelItem, baseIcon, animIconRoot, iconCount, iconExtension );
		}

		public boolean beforeSubmit( Submit submit, SubmitContext context )
		{
			if( isEnabled() && submit.getRequest() == getTarget() )
				start();
			return true;
		}

		public void afterSubmit( Submit submit, SubmitContext context )
		{
			if( submit.getRequest() == getTarget() )
				stop();
		}
	}

	public RequestIconAnimator<?> getIconAnimator()
	{
		return iconAnimator;
	}

	public void setIconAnimator( RequestIconAnimator<?> iconAnimator )
	{
		if( this.iconAnimator != null )
			removeSubmitListener( this.iconAnimator );

		this.iconAnimator = iconAnimator;
		addSubmitListener( this.iconAnimator );
	}

	public ImageIcon getIcon()
	{
		if( forLoadTest || UISupport.isHeadless() )
			return null;

		TestMonitor testMonitor = SoapUI.getTestMonitor();
		if( testMonitor != null && testMonitor.hasRunningLoadTest( getTestStep().getTestCase() ) )
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

	@Override
	public void setIcon( ImageIcon icon )
	{
		getTestStep().setIcon( icon );
	}

	public String getEndpoint()
	{
		return endpoint;
	}

	public void setEndpoint( String endpoint )
	{
		this.endpoint = endpoint;
	}

	public String getAmfCall()
	{
		return amfCall;
	}

	public void setAmfCall( String amfCall )
	{
		this.amfCall = amfCall;
	}

	public String getGroovyScript()
	{
		return groovyScript;
	}

	public void setGroovyScript( String groovyScript )
	{
		this.groovyScript = groovyScript;
	}

	public HashMap<String, TestProperty> getPropertyMap()
	{
		return propertyMap;
	}

	public void setPropertyMap( HashMap<String, TestProperty> map )
	{
		this.propertyMap = map;
	}

	public void setArguments( List<Object> arguments )
	{
		this.arguments = arguments;
	}

	public void clearArguments()
	{
		this.arguments.clear();
	}

	public List<Object> getArguments()
	{
		return arguments;
	}

	public List<Object> addArgument( Object obj )
	{
		arguments.add( obj );
		return arguments;
	}

	public Object[] argumentsToArray()
	{
		return arguments.toArray();
	}

	public void extractProperties( SubmitContext context )
	{
		HashMap<String, Object> property = new HashMap<String, Object>();
		try
		{
			scriptEngine.setScript( groovyScript );
			scriptEngine.setVariable( "property", property );
			scriptEngine.setVariable( "log", SoapUI.log );
			scriptEngine.setVariable( "context", context );

			scriptEngine.run();

			for( String name : propertyNames )
			{
				TestProperty propertyValue = propertyMap.get( name );
				if( property.containsKey( name ) )
				{
					addArgument( property.get( name ) );
				}
				else
				{
					addArgument( PropertyExpander.expandProperties( context,propertyValue.getValue() ) );
				}
			}
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	public void setPropertyNames( String[] propertyNames )
	{
		this.propertyNames = propertyNames;
	}

	public String[] getPropertyNames()
	{
		return propertyNames;
	}

	public void setScriptEngine( SoapUIScriptEngine scriptEngine )
	{
		this.scriptEngine = scriptEngine;
	}

	public SoapUIScriptEngine getScriptEngine()
	{
		return scriptEngine;
	}
}
