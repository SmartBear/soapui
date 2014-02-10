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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepWithProperties;
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
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public class AMFRequest extends AbstractAnimatableModelItem<ModelItemConfig> implements Assertable, TestRequest
{
	public static final String AMF_SCRIPT_HEADERS = "AMF_SCRIPT_HEADERS";
	public static final String AMF_SCRIPT_PARAMETERS = "AMF_SCRIPT_PARAMETERS";
	public static final String AMF_SCRIPT_ERROR = "AMF_SCRIPT_ERROR";
	public static final String AMF_RESPONSE_CONTENT = "AMF_RESPONSE_CONTENT";
	public static final String AMF_REQUEST = "AMF_REQUEST";
	public static final String RAW_AMF_REQUEST = "RAW_AMF_REQUEST";
	public static final String AMF_RESPONSE_PROPERTY = "response";

	private final AMFRequestTestStep testStep;
	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
	private AMFResponse response;
	private SoapUIScriptEngine scriptEngine;
	private String endpoint;
	private String amfCall;
	private String script;
	private HashMap<String, TestProperty> propertyMap;
	private String[] propertyNames;
	private List<Object> arguments = new ArrayList<Object>();
	private StringToStringsMap httpHeaders;
	private StringToObjectMap amfHeaders;
	private StringToStringMap amfHeadersString;

	private boolean forLoadTest;
	private AssertionStatus currentStatus;

	// icon related
	private RequestIconAnimator<?> iconAnimator;
	private ImageIcon validRequestIcon;
	private ImageIcon failedRequestIcon;
	private ImageIcon disabledRequestIcon;
	private ImageIcon unknownRequestIcon;

	public AMFRequest( AMFRequestTestStep testStep, boolean forLoadTest )
	{
		this.testStep = testStep;

		if( !forLoadTest )
			initIcons();
	}

	public AMFSubmit submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{

		return new AMFSubmit( this, submitContext, async );
	}

	public boolean executeAmfScript( SubmitContext context )
	{
		boolean scriptOK = true;
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		HashMap<String, Object> amfHeadersTemp = new HashMap<String, Object>();
		try
		{
			scriptEngine.setScript( script );
			scriptEngine.setVariable( "parameters", parameters );
			scriptEngine.setVariable( "amfHeaders", amfHeadersTemp );
			scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
			scriptEngine.setVariable( "context", context );

			scriptEngine.run();

			context.setProperty( AMF_SCRIPT_PARAMETERS, parameters );
			context.setProperty( AMF_SCRIPT_HEADERS, amfHeadersTemp );

			for( String name : propertyNames )
			{
				if( name.equals( WsdlTestStepWithProperties.RESPONSE_AS_XML ) )
					continue; // skip ResponseAsXML

				TestProperty propertyValue = propertyMap.get( name );
				if( parameters.containsKey( name ) )
				{
					addArgument( parameters.get( name ) );
				}
				else
				{
					addArgument( PropertyExpander.expandProperties( context, propertyValue.getValue() ) );
				}
			}

			StringToObjectMap stringToObjectMap = new StringToObjectMap();
			for( String key : getAmfHeadersString().getKeys() )
			{
				if( amfHeadersTemp.containsKey( key ) )
				{
					stringToObjectMap.put( key, amfHeadersTemp.get( key ) );
				}
				else
				{
					stringToObjectMap.put( key,
							PropertyExpander.expandProperties( context, getAmfHeadersString().get( key ) ) );
				}
			}
			setAmfHeaders( stringToObjectMap );

		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
			scriptOK = false;
			context.setProperty( AMF_SCRIPT_ERROR, e );
		}
		finally
		{
			scriptEngine.clearVariables();
		}
		return scriptOK;
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
		AMFResponse old = this.response;
		this.response = response;
		notifyPropertyChanged( AMF_RESPONSE_PROPERTY, old, response );
	}

	public AMFResponse getResponse()
	{
		return response;
	}

	public String getResponseContent()
	{
		if( response != null )
		{
			return response.getResponseContentXML();
		}
		else
		{
			return "";
		}
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

		setIconAnimator( new RequestIconAnimator<AMFRequest>( this, "/amf_request.gif", "/exec_amf_request.gif", 3 ) );
	}

	protected RequestIconAnimator<?> initIconAnimator()
	{
		return new RequestIconAnimator<AMFRequest>( this, "/amf_request.gif", "/exec_amf_request.gif", 3 );
	}

	public static class RequestIconAnimator<T extends AMFRequest> extends ModelItemIconAnimator<T> implements
			SubmitListener
	{
		public RequestIconAnimator( T modelItem, String baseIcon, String baseAnimateIcon, int iconCount )
		{
			super( modelItem, baseIcon, baseAnimateIcon, iconCount );
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
		if( forLoadTest || getIconAnimator() == null )
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

	@Override
	public void setIcon( ImageIcon icon )
	{
		getTestStep().setIcon( icon );
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

	public void setScript( String script )
	{
		this.script = script;
	}

	public String getScript()
	{
		return script;
	}

	public void setAmfCall( String amfCall )
	{
		this.amfCall = amfCall;
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
		return requestAsXML();
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

	public WsdlMessageAssertion importAssertion( WsdlMessageAssertion source, boolean overwrite, boolean createCopy,
			String newName )
	{
		return testStep.importAssertion( source, overwrite, createCopy, newName );
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

	public String requestAsXML()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "<AMFRequest>\n" );
		sb.append( " <endpoint>" + getEndpoint() + "</endpoint>\n" );
		sb.append( " <amfcall>" + getAmfCall() + "</amfcall>\n" );

		if( getPropertyNames() != null )
		{
			sb.append( " <parameters>\n" );
			for( String name : getPropertyNames() )
			{
				if( name.equals( WsdlTestStepWithProperties.RESPONSE_AS_XML ) )
					continue;
				sb.append( "  <parameter>\n" );
				sb.append( "   <name>" + name + "</name>\n" );
				sb.append( "   <value>" + getPropertyMap().get( name ).getValue() + "</value>\n" );
				sb.append( "  </parameter>\n" );
			}
			sb.append( " </parameters>\n" );
		}

		sb.append( " <script>" + getScript() + "</script>\n" );
		sb.append( "</AMFRequest>" );
		return sb.toString();
	}

	public void setHttpHeaders( StringToStringsMap httpHeaders )
	{
		this.httpHeaders = httpHeaders;
	}

	public StringToStringsMap getHttpHeaders()
	{
		return httpHeaders;
	}

	public void setAmfHeaders( StringToObjectMap amfHeaders )
	{
		this.amfHeaders = amfHeaders;
	}

	public StringToObjectMap getAmfHeaders()
	{
		return amfHeaders;
	}

	public void setAmfHeadersString( StringToStringMap amfHeadersString )
	{
		this.amfHeadersString = amfHeadersString;
	}

	public StringToStringMap getAmfHeadersString()
	{
		return amfHeadersString;
	}

	public String getPassword()
	{
		return null;
	}

	public String getUsername()
	{
		return null;
	}

	@Override
	public String getAuthType()
	{
		return null;
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
