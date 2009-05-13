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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.RequestStepConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepProperty;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringToStringMap;

public class HttpTestRequestStep extends WsdlTestStepWithProperties implements PropertyChangeListener,
		PropertyExpansionContainer, Assertable, HttpRequestTestStep<RestTestRequest>
{
	@SuppressWarnings( "unused" )
	private final static Logger log = Logger.getLogger( HttpTestRequestStep.class );
	private RestRequestStepConfig requestStepConfig;
	private RestTestRequest testRequest;
	private WsdlSubmit<RestRequest> submit;

	public HttpTestRequestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( getConfig().getConfig() != null )
		{
			requestStepConfig = ( RestRequestStepConfig )getConfig().getConfig().changeType( RestRequestStepConfig.type );

			testRequest = new RestTestRequest( null, requestStepConfig.getRestRequest(), this, forLoadTest );
			testRequest.addPropertyChangeListener( this );
			testRequest.addTestPropertyListener( new InternalTestPropertyListener() );

			if( config.isSetName() )
				testRequest.setName( config.getName() );
			else
				config.setName( testRequest.getName() );

			if( testRequest.getPath() != null )
				testRequest.setEndpoint( testRequest.getPath() );
		}
		else
		{
			requestStepConfig = ( RestRequestStepConfig )getConfig().addNewConfig()
					.changeType( RestRequestStepConfig.type );
		}

		for( TestProperty property : testRequest.getProperties().values() )
		{
			addProperty( new RestTestStepProperty( ( XmlBeansRestParamsTestPropertyHolder.RestParamProperty )property ) );
		}

		// init default properties
		addProperty( new TestStepBeanProperty( "Endpoint", false, testRequest, "endpoint", this ) );
		addProperty( new TestStepBeanProperty( "Username", false, testRequest, "username", this ) );
		addProperty( new TestStepBeanProperty( "Password", false, testRequest, "password", this ) );
		addProperty( new TestStepBeanProperty( "Domain", false, testRequest, "domain", this ) );
		addProperty( new TestStepBeanProperty( "Path", false, testRequest, "path", this ) );

		// init properties
		addProperty( new TestStepBeanProperty( "Request", false, testRequest, "requestContent", this )
		{
			@Override
			public String getDefaultValue()
			{
				return createDefaultRequestContent();
			}
		} );

		addProperty( new TestStepBeanProperty( "ResponseAsXml", true, testRequest, "responseContentAsXml", this )
		{
			@Override
			public String getDefaultValue()
			{
				return createDefaultResponseXmlContent();
			}
		} );

		addProperty( new TestStepBeanProperty( "Response", true, testRequest, "responseContentAsString", this )
		{
			@Override
			public String getDefaultValue()
			{
				return createDefaultRawResponseContent();
			}
		} );
	}

	protected String createDefaultRawResponseContent()
	{
		return "";
	}

	protected String createDefaultResponseXmlContent()
	{
		return "";
	}

	protected String createDefaultRequestContent()
	{
		return "";
	}

	public RestRequestStepConfig getRequestStepConfig()
	{
		return requestStepConfig;
	}

	@Override
	public WsdlTestStep clone( WsdlTestCase targetTestCase, String name )
	{
		beforeSave();

		TestStepConfig config = ( TestStepConfig )getConfig().copy();
		RequestStepConfig stepConfig = ( RequestStepConfig )config.getConfig().changeType( RequestStepConfig.type );

		while( stepConfig.getRequest().sizeOfAttachmentArray() > 0 )
			stepConfig.getRequest().removeAttachment( 0 );

		config.setName( name );
		stepConfig.getRequest().setName( name );

		WsdlTestRequestStep result = ( WsdlTestRequestStep )targetTestCase.addTestStep( config );
		testRequest.copyAttachmentsTo( result.getTestRequest() );

		return result;
	}

	@Override
	public void release()
	{
		super.release();

		// could be null if initialization failed..
		if( testRequest != null )
		{
			testRequest.removePropertyChangeListener( this );
			testRequest.release();
		}
	}

	@Override
	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );

		requestStepConfig = ( RestRequestStepConfig )config.getConfig().changeType( RestRequestStepConfig.type );
		testRequest.updateConfig( requestStepConfig.getRestRequest() );
	}

	@Override
	public ImageIcon getIcon()
	{
		return testRequest == null ? null : testRequest.getIcon();
	}

	public RestTestRequest getTestRequest()
	{
		return testRequest;
	}

	@Override
	public void setName( String name )
	{
		super.setName( name );
		testRequest.setName( name );
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		if( arg0.getPropertyName().equals( TestAssertion.CONFIGURATION_PROPERTY )
				|| arg0.getPropertyName().equals( TestAssertion.DISABLED_PROPERTY ) )
		{
			if( getTestRequest().getResponse() != null )
			{
				getTestRequest().assertResponse( new WsdlTestRunContext( this ) );
			}
		}
		else
		{
			if( arg0.getSource() == testRequest && arg0.getPropertyName().equals( WsdlTestRequest.NAME_PROPERTY ) )
			{
				if( !super.getName().equals( ( String )arg0.getNewValue() ) )
					super.setName( ( String )arg0.getNewValue() );
			}

			notifyPropertyChanged( arg0.getPropertyName(), arg0.getOldValue(), arg0.getNewValue() );
		}
	}

	public TestStepResult run( TestRunner runner, TestRunContext runContext )
	{
		RestRequestStepResult testStepResult = new RestRequestStepResult( this );

		try
		{
			submit = testRequest.submit( runContext, false );
			HttpResponse response = ( HttpResponse )submit.getResponse();

			if( submit.getStatus() != Submit.Status.CANCELED )
			{
				if( submit.getStatus() == Submit.Status.ERROR )
				{
					testStepResult.setStatus( TestStepStatus.FAILED );
					testStepResult.addMessage( submit.getError().toString() );

					testRequest.setResponse( null, runContext );
				}
				else if( response == null )
				{
					testStepResult.setStatus( TestStepStatus.FAILED );
					testStepResult.addMessage( "Request is missing response" );

					testRequest.setResponse( null, runContext );
				}
				else
				{
					runContext.setProperty( AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY, testStepResult );
					testRequest.setResponse( response, runContext );

					testStepResult.setTimeTaken( response.getTimeTaken() );
					testStepResult.setSize( response.getContentLength() );
					testStepResult.setResponse( response );

					switch( testRequest.getAssertionStatus() )
					{
					case FAILED :
						testStepResult.setStatus( TestStepStatus.FAILED );
						break;
					case VALID :
						testStepResult.setStatus( TestStepStatus.OK );
						break;
					case UNKNOWN :
						testStepResult.setStatus( TestStepStatus.UNKNOWN );
						break;
					}
				}
			}
			else
			{
				testStepResult.setStatus( TestStepStatus.CANCELED );
				testStepResult.addMessage( "Request was canceled" );
			}

			if( response != null )
			{
				testStepResult.addProperty( "URL", response.getURL() == null ? "<missing>" : response.getURL().toString() );
				testStepResult.addProperty( "Method", String.valueOf( response.getMethod() ) );
				testStepResult.addProperty( "StatusCode", String.valueOf( response.getStatusCode() ) );
				testStepResult.addProperty( "HTTP Version", response.getHttpVersion() );
				testStepResult.setRequestContent( response.getRequestContent() );
			}
			else
			{
				testStepResult.addMessage( "Missing Response" );
				testStepResult.setRequestContent( testRequest.getRequestContent() );
			}
		}
		catch( SubmitException e )
		{
			testStepResult.setStatus( TestStepStatus.FAILED );
			testStepResult.addMessage( "SubmitException: " + e );
		}
		finally
		{
			submit = null;
		}

		testStepResult.setDomain( PropertyExpansionUtils.expandProperties( runContext, testRequest.getDomain() ) );
		testStepResult.setUsername( PropertyExpansionUtils.expandProperties( runContext, testRequest.getUsername() ) );
		testStepResult.setEndpoint( PropertyExpansionUtils.expandProperties( runContext, testRequest.getEndpoint() ) );
		testStepResult.setPassword( PropertyExpansionUtils.expandProperties( runContext, testRequest.getPassword() ) );
		testStepResult.setEncoding( testRequest.getEncoding() );

		if( testStepResult.getStatus() != TestStepStatus.CANCELED )
		{
			AssertionStatus assertionStatus = testRequest.getAssertionStatus();
			switch( assertionStatus )
			{
			case FAILED :
			{
				testStepResult.setStatus( TestStepStatus.FAILED );
				if( getAssertionCount() == 0 )
				{
					testStepResult.addMessage( "Invalid/empty response" );
				}
				else
					for( int c = 0; c < getAssertionCount(); c++ )
					{
						AssertionError[] errors = getAssertionAt( c ).getErrors();
						if( errors != null )
						{
							for( AssertionError error : errors )
							{
								testStepResult.addMessage( error.getMessage() );
							}
						}
					}

				break;
			}
			}
		}

		return testStepResult;
	}

	public WsdlMessageAssertion getAssertionAt( int index )
	{
		return testRequest.getAssertionAt( index );
	}

	public int getAssertionCount()
	{
		return testRequest == null ? 0 : testRequest.getAssertionCount();
	}

	@Override
	public boolean cancel()
	{
		if( submit == null )
			return false;

		submit.cancel();

		return true;
	}

	@Override
	public boolean dependsOn( AbstractWsdlModelItem<?> modelItem )
	{
		return false;
	}

	@Override
	public void beforeSave()
	{
		super.beforeSave();

		if( testRequest != null )
			testRequest.beforeSave();
	}

	@Override
	public String getDescription()
	{
		return testRequest == null ? "<missing>" : testRequest.getDescription();
	}

	@Override
	public void setDescription( String description )
	{
		if( testRequest != null )
			testRequest.setDescription( description );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List<? extends ModelItem> getChildren()
	{
		return testRequest == null ? Collections.EMPTY_LIST : testRequest.getAssertionList();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		if( testRequest == null )
			return new PropertyExpansion[0];

		PropertyExpansionsResult result = new PropertyExpansionsResult( this, testRequest );

		result.extractAndAddAll( "requestContent" );
		result.extractAndAddAll( "path" );
		result.extractAndAddAll( "username" );
		result.extractAndAddAll( "password" );
		result.extractAndAddAll( "domain" );

		StringToStringMap requestHeaders = testRequest.getRequestHeaders();
		for( String key : requestHeaders.keySet() )
		{
			result.extractAndAddAll( new RequestHeaderHolder( requestHeaders, key ), "value" );
		}

		for( String key : testRequest.getParams().getPropertyNames() )
		{
			result.extractAndAddAll( new RequestParamHolder( key ), "value" );
		}

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public RestTestRequest getHttpRequest()
	{
		return testRequest;
	}

	public class RequestHeaderHolder
	{
		private final StringToStringMap valueMap;
		private final String key;

		public RequestHeaderHolder( StringToStringMap valueMap, String key )
		{
			this.valueMap = valueMap;
			this.key = key;
		}

		public String getValue()
		{
			return valueMap.get( key );
		}

		public void setValue( String value )
		{
			valueMap.put( key, value );
			testRequest.setRequestHeaders( valueMap );
		}
	}

	public class RequestParamHolder
	{
		private final String name;

		public RequestParamHolder( String name )
		{
			this.name = name;
		}

		public String getValue()
		{
			return testRequest.getParams().getPropertyValue( name );
		}

		public void setValue( String value )
		{
			testRequest.setPropertyValue( name, value );
		}
	}

	public TestAssertion addAssertion( String type )
	{
		WsdlMessageAssertion result = testRequest.addAssertion( type );
		return result;
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		testRequest.addAssertionsListener( listener );
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		return testRequest.cloneAssertion( source, name );
	}

	public String getAssertableContent()
	{
		return testRequest.getAssertableContent();
	}

	public AssertableType getAssertableType()
	{
		return testRequest.getAssertableType();
	}

	public TestAssertion getAssertionByName( String name )
	{
		return testRequest.getAssertionByName( name );
	}

	public List<TestAssertion> getAssertionList()
	{
		return testRequest.getAssertionList();
	}

	public AssertionStatus getAssertionStatus()
	{
		return testRequest.getAssertionStatus();
	}

	public Interface getInterface()
	{
		return null;
	}

	public TestStep getTestStep()
	{
		return this;
	}

	public void removeAssertion( TestAssertion assertion )
	{
		testRequest.removeAssertion( assertion );
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		testRequest.removeAssertionsListener( listener );
	}

	public Map<String, TestAssertion> getAssertions()
	{
		return testRequest.getAssertions();
	}

	@Override
	public void prepare( TestRunner testRunner, TestRunContext testRunContext ) throws Exception
	{
		super.prepare( testRunner, testRunContext );

		for( TestAssertion assertion : testRequest.getAssertionList() )
		{
			assertion.prepare( testRunner, testRunContext );
		}
	}

	public String getDefaultSourcePropertyName()
	{
		return "ResponseAsXml";
	}

	public String getDefaultTargetPropertyName()
	{
		return "Request";
	}

	public String getDefaultAssertableContent()
	{
		return testRequest.getDefaultAssertableContent();
	}

	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		testRequest.resolve( context );
	}

	private class InternalTestPropertyListener extends TestPropertyListenerAdapter
	{
		@Override
		public void propertyAdded( String name )
		{
			addProperty( new RestTestStepProperty( getTestRequest().getProperty( name ) ), true );
		}

		@Override
		public void propertyRemoved( String name )
		{
			HttpTestRequestStep.this.deleteProperty( name, true );
		}

		@Override
		public void propertyRenamed( String oldName, String newName )
		{
			HttpTestRequestStep.this.propertyRenamed( oldName );
		}

		@Override
		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			HttpTestRequestStep.this.firePropertyValueChanged( name, oldValue, newValue );
		}

		@Override
		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
			HttpTestRequestStep.this.firePropertyMoved( name, oldIndex, newIndex );
		}
	}

	private class RestTestStepProperty implements TestStepProperty
	{
		private XmlBeansRestParamsTestPropertyHolder.RestParamProperty property;

		public RestTestStepProperty( XmlBeansRestParamsTestPropertyHolder.RestParamProperty property )
		{
			this.property = property;
		}

		public TestStep getTestStep()
		{
			return HttpTestRequestStep.this;
		}

		public String getName()
		{
			return property.getName();
		}

		public String getDescription()
		{
			return property.getDescription();
		}

		public String getValue()
		{
			return property.getValue();
		}

		public String getDefaultValue()
		{
			return property.getDefaultValue();
		}

		public void setValue( String value )
		{
			property.setValue( value );
		}

		public boolean isReadOnly()
		{
			return false;
		}

		public QName getType()
		{
			return property.getType();
		}

		public ModelItem getModelItem()
		{
			return getTestRequest();
		}
	}
}