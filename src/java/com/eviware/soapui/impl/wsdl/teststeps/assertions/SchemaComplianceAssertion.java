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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.RequestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

/**
 * Asserts that a request or response message complies with its related 
 * WSDL definition / XML Schema
 * 
 * @author Ole.Matzura
 */

public class SchemaComplianceAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion
{
	public static final String ID = "Schema Compliance";
	private String definition;
	private WsdlContext wsdlContext;
	private String wsdlContextDef;
	
   public SchemaComplianceAssertion(RequestAssertionConfig assertionConfig, Assertable assertable)
   {
      super(assertionConfig, assertable,false, true, false, true);
      
      XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
      definition = reader.readString( "definition", null );
   }
   
	@Override
	public void prepare( TestRunner testRunner, TestRunContext testRunContext ) throws Exception
	{
		super.prepare( testRunner, testRunContext );
		
		wsdlContext = null;
		wsdlContextDef = null;
	}

	protected String internalAssertResponse(WsdlMessageExchange messageExchange, SubmitContext context) throws AssertionException
	{
		WsdlContext wsdlContext = null;
		try
		{
			wsdlContext = getWsdlContext( messageExchange, context );
		}
		catch( Exception e1 )
		{
			throw new AssertionException( new AssertionError( e1.getMessage()));
		}
		
		WsdlValidator validator = new WsdlValidator( wsdlContext );
		
		try
		{
			AssertionError[] errors = validator.assertResponse( messageExchange, false );
			if (errors.length > 0)
				throw new AssertionException(errors);
		}
		catch( AssertionException e )
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionException( new AssertionError( e.getMessage() ));
		}
		
		return "Schema compliance OK";
	}

	private WsdlContext getWsdlContext( WsdlMessageExchange messageExchange, SubmitContext context ) throws Exception
	{
		WsdlOperation operation = messageExchange.getOperation();
		WsdlInterface iface = (WsdlInterface)operation.getInterface();
		if( definition == null || definition.trim().length() == 0 || definition.equals( 
				PathUtils.expandPath( iface.getDefinition(), iface, context )))
		{
			wsdlContext = (iface).getWsdlContext();
			wsdlContext.loadIfNecessary();
		}
		else
		{
			String def = PropertyExpansionUtils.expandProperties( context, definition );
			if( wsdlContext == null || !def.equals( wsdlContextDef ))
			{
				wsdlContext = new WsdlContext( def, iface.getSoapVersion() );
				wsdlContext.load();
				wsdlContext.setInterface( iface );
				wsdlContextDef = def;
			}
		}

		return wsdlContext;
	}
	
   public boolean configure()
   {
   	String value = definition;
   	
   	WsdlInterface iface = ( WsdlInterface ) getAssertable().getInterface();
   	String orgDef = iface == null ? null : PathUtils.expandPath(iface.getDefinition(), iface);
   		
   	if( value == null || value.trim().length() == 0 )
   	{
			value = orgDef;
   	}
   	
      value = UISupport.prompt( "Specify definition url to validate by", "Configure SchemaCompliance Assertion", value );
      
      if( value == null ) return false;
      
      if( value.trim().length() == 0 || value.equals( orgDef ))
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

	protected String internalAssertRequest( WsdlMessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		WsdlContext wsdlContext = null;
		try
		{
			wsdlContext = getWsdlContext( messageExchange, context );
		}
		catch( Exception e1 )
		{
			throw new AssertionException( new AssertionError( e1.getMessage() ));
		}
		WsdlValidator validator = new WsdlValidator( wsdlContext );
		
		try
		{
			AssertionError[] errors = validator.assertRequest( messageExchange, false );
			if (errors.length > 0)
				throw new AssertionException(errors);
		}
		catch( AssertionException e )
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new AssertionException( new AssertionError( e.getMessage() ));
		}
		
		return "Schema compliance OK";
	}
}
