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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.DefinitionContext;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wadl.support.WadlValidator;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.RestMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

/**
 * Asserts that a request or response message complies with its related WSDL
 * definition / XML Schema
 * 
 * @author Ole.Matzura
 */

public class SchemaComplianceAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion
{
	public static final String ID = "Schema Compliance";
	public static final String LABEL = "Schema Compliance";

	private String definition;
	private DefinitionContext<?> definitionContext;
	private String wsdlContextDef;
	private static Map<String, WsdlContext> wsdlContextMap = new HashMap<String, WsdlContext>();
	private static final String SCHEMA_COMPLIANCE_HAS_CLEARED_CACHE_FLAG = SchemaComplianceAssertion.class.getName()
			+ "@SchemaComplianceHasClearedCacheFlag";

	public SchemaComplianceAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, true, false, true );

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		definition = reader.readString( "definition", null );
	}

	@Override
	public void prepare( TestCaseRunner testRunner, TestCaseRunContext testRunContext ) throws Exception
	{
		super.prepare( testRunner, testRunContext );

		definitionContext = null;
		wsdlContextDef = null;

		// get correct context for checking if cache has been cleared for this run
		PropertyExpansionContext context = testRunContext.hasProperty( TestCaseRunContext.LOAD_TEST_CONTEXT ) ? ( PropertyExpansionContext )testRunContext
				.getProperty( TestCaseRunContext.LOAD_TEST_CONTEXT )
				: testRunContext;

		synchronized( context )
		{
			if( !context.hasProperty( SCHEMA_COMPLIANCE_HAS_CLEARED_CACHE_FLAG ) )
			{
				wsdlContextMap.clear();
				context.setProperty( SCHEMA_COMPLIANCE_HAS_CLEARED_CACHE_FLAG, "yep!" );
			}
		}
	}

	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		if( messageExchange instanceof WsdlMessageExchange )
		{
			return assertWsdlResponse( ( WsdlMessageExchange )messageExchange, context );
		}
		else if( messageExchange instanceof RestMessageExchange )
		{
			return assertWadlResponse( ( RestMessageExchange )messageExchange, context );
		}

		throw new AssertionException( new AssertionError( "Unknown MessageExchange type" ) );
	}

	private String assertWadlResponse( RestMessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		WadlDefinitionContext wadlContext = null;
		try
		{
			definitionContext = getWadlContext( messageExchange, context );
		}
		catch( Exception e1 )
		{
			throw new AssertionException( new AssertionError( e1.getMessage() ) );
		}

		WadlValidator validator = new WadlValidator( wadlContext );

		try
		{
			AssertionError[] errors = validator.assertResponse( messageExchange );
			if( errors.length > 0 )
				throw new AssertionException( errors );
		}
		catch( AssertionException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new AssertionException( new AssertionError( e.getMessage() ) );
		}

		return "Schema compliance OK";
	}

	private String assertWsdlResponse( WsdlMessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		WsdlContext wsdlContext = null;
		try
		{
			wsdlContext = ( WsdlContext )getWsdlContext( messageExchange, context );
		}
		catch( Exception e1 )
		{
			throw new AssertionException( new AssertionError( e1.getMessage() ) );
		}

		WsdlValidator validator = new WsdlValidator( wsdlContext );

		try
		{
			AssertionError[] errors = validator.assertResponse( messageExchange, false );
			if( errors.length > 0 )
				throw new AssertionException( errors );
		}
		catch( AssertionException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new AssertionException( new AssertionError( e.getMessage() ) );
		}

		return "Schema compliance OK";
	}

	private DefinitionContext<?> getWsdlContext( WsdlMessageExchange messageExchange, SubmitContext context )
			throws Exception
	{
		WsdlOperation operation = messageExchange.getOperation();
		WsdlInterface iface = operation.getInterface();
		if( StringUtils.isNullOrEmpty( definition ) || definition.equals( iface.getDefinition() ) )
		{
			definitionContext = ( iface ).getWsdlContext();
			( ( WsdlContext )definitionContext ).loadIfNecessary();
		}
		else
		{
			String def = PathUtils.expandPath( definition, iface, context );
			if( definitionContext == null || !def.equals( wsdlContextDef ) )
			{
				definitionContext = getContext( def, iface.getSoapVersion() );
				// ( (WsdlContext) definitionContext ).load();
				( ( WsdlContext )definitionContext ).setInterface( iface );
				wsdlContextDef = def;
			}
		}

		return definitionContext;
	}

	private synchronized WsdlContext getContext( String wsdlLocation, SoapVersion soapVersion ) throws Exception
	{
		if( wsdlContextMap.containsKey( wsdlLocation ) )
		{
			return wsdlContextMap.get( wsdlLocation );
		}
		else
		{
			WsdlContext newWsdlContext = new WsdlContext( wsdlLocation, soapVersion );
			newWsdlContext.load();
			wsdlContextMap.put( wsdlLocation, newWsdlContext );
			return newWsdlContext;
		}
	}

	private DefinitionContext<?> getWadlContext( RestMessageExchange messageExchange, SubmitContext context )
			throws Exception
	{
		RestResource operation = messageExchange.getResource();
		RestService service = operation.getService();
		if( StringUtils.isNullOrEmpty( definition )
				|| definition.equals( PathUtils.expandPath( service.getDefinition(), service, context ) ) )
		{
			definitionContext = service.getWadlContext();
			( ( WadlDefinitionContext )definitionContext ).loadIfNecessary();
		}
		else
		{
			String def = PathUtils.expandPath( definition, service, context );
			if( definitionContext == null || !def.equals( wsdlContextDef ) )
			{
				definitionContext = new WadlDefinitionContext( def );
				( ( WadlDefinitionContext )definitionContext ).load();
				( ( WadlDefinitionContext )definitionContext ).setInterface( service );
				wsdlContextDef = def;
			}
		}

		return definitionContext;
	}

	public boolean configure()
	{
		String value = definition;

		AbstractInterface<?> iface = ( AbstractInterface<?> )getAssertable().getInterface();
		String orgDef = iface == null ? null : iface.getDefinition();

		if( StringUtils.isNullOrEmpty( value ) )
		{
			value = orgDef;
		}

		value = UISupport.prompt( "Specify definition url to validate by", "Configure SchemaCompliance Assertion", value );

		if( value == null )
			return false;

		if( StringUtils.isNullOrEmpty( value ) || value.equals( orgDef ) )
			definition = "";
		else
			definition = value;

		setConfiguration( createConfiguration() );
		return true;
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		return builder.add( "definition", definition ).finish();
	}

	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		WsdlContext wsdlContext = null;
		try
		{
			wsdlContext = ( WsdlContext )getWsdlContext( ( WsdlMessageExchange )messageExchange, context );
		}
		catch( Exception e1 )
		{
			throw new AssertionException( new AssertionError( e1.getMessage() ) );
		}
		WsdlValidator validator = new WsdlValidator( wsdlContext );

		try
		{
			AssertionError[] errors = validator.assertRequest( ( WsdlMessageExchange )messageExchange, false );
			if( errors.length > 0 )
				throw new AssertionException( errors );
		}
		catch( AssertionException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new AssertionException( new AssertionError( e.getMessage() ) );
		}

		return "Schema compliance OK";
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( SchemaComplianceAssertion.ID, SchemaComplianceAssertion.LABEL, SchemaComplianceAssertion.class );
		}

		@Override
		public boolean canAssert( Assertable assertable )
		{
			return super.canAssert( assertable ) && assertable.getInterface() instanceof AbstractInterface
					&& ( ( AbstractInterface<?> )assertable.getInterface() ).getDefinitionContext().hasSchemaTypes();
		}
	}
}
