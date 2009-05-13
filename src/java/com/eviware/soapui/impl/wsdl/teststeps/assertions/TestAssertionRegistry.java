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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleNotContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSARequestAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSAResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSSStatusAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Registry for WsdlAssertions
 * 
 * @author Ole.Matzura
 */

public class TestAssertionRegistry
{
	private static TestAssertionRegistry instance;
	private Map<String, TestAssertionFactory> availableAssertions = new HashMap<String, TestAssertionFactory>();
	private StringToStringMap assertionLabels = new StringToStringMap();
	private final static Logger log = Logger.getLogger( TestAssertionRegistry.class );

	public TestAssertionRegistry()
	{
		addAssertion( new SoapResponseAssertion.Factory() );
		addAssertion( new SchemaComplianceAssertion.Factory() );
		addAssertion( new SimpleContainsAssertion.Factory() );
		addAssertion( new SimpleNotContainsAssertion.Factory() );
		addAssertion( new XPathContainsAssertion.Factory() );
		addAssertion( new NotSoapFaultAssertion.Factory() );
		addAssertion( new SoapFaultAssertion.Factory() );
		addAssertion( new ResponseSLAAssertion.Factory() );
		addAssertion( new GroovyScriptAssertion.Factory() );
		addAssertion( new XQueryContainsAssertion.Factory() );
		addAssertion( new WSSStatusAssertion.Factory() );
		addAssertion( new WSAResponseAssertion.Factory() );
		addAssertion( new WSARequestAssertion.Factory() );
	}

	public void addAssertion( TestAssertionFactory factory )
	{
		availableAssertions.put( factory.getAssertionId(), factory );
		assertionLabels.put( factory.getAssertionLabel(), factory.getAssertionId() );
	}

	public static synchronized TestAssertionRegistry getInstance()
	{
		if( instance == null )
			instance = new TestAssertionRegistry();

		return instance;
	}

	public WsdlMessageAssertion buildAssertion( TestAssertionConfig config, Assertable assertable )
	{
		try
		{
			String type = config.getType();
			TestAssertionFactory factory = availableAssertions.get( type );
			if( factory == null )
			{
				log.error( "Missing assertion for type [" + type + "]" );
			}
			else
			{
				return ( WsdlMessageAssertion )factory.buildAssertion( config, assertable );
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return null;
	}

	public boolean canBuildAssertion( TestAssertionConfig config )
	{
		return availableAssertions.containsKey( config.getType() );
	}

	public String getAssertionTypeForName( String name )
	{
		return assertionLabels.get( name );
	}

	public enum AssertableType
	{
		REQUEST, RESPONSE, BOTH
	}

	public String[] getAvailableAssertionNames( Assertable assertable )
	{
		List<String> result = new ArrayList<String>();

		for( TestAssertionFactory assertion : availableAssertions.values() )
		{
			if( assertion.canAssert( assertable ) )
				result.add( assertion.getAssertionLabel() );
		}

		return result.toArray( new String[result.size()] );
	}

	public String getAssertionNameForType( String type )
	{
		for( String assertion : assertionLabels.keySet() )
		{
			if( assertionLabels.get( assertion ).equals( type ) )
				return assertion;
		}

		return null;
	}

	public boolean canAddMultipleAssertions( String name, Assertable assertable )
	{
		for( int c = 0; c < assertable.getAssertionCount(); c++ )
		{
			TestAssertion assertion = assertable.getAssertionAt( c );
			if( assertion.isAllowMultiple() )
				continue;

			if( assertion.getClass().equals( availableAssertions.get( getAssertionTypeForName( name ) ) ) )
			{
				return false;
			}
		}

		return true;
	}

	public boolean canAddAssertion( WsdlMessageAssertion assertion, Assertable assertable )
	{
		if( assertion.isAllowMultiple() )
			return true;

		for( int c = 0; c < assertable.getAssertionCount(); c++ )
		{
			if( assertion.getClass().equals( assertable.getAssertionAt( c ).getClass() ) )
			{
				return false;
			}
		}

		return true;
	}
}
