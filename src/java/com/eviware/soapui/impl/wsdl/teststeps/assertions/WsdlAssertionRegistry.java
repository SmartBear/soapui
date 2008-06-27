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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RequestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Registry for WsdlAssertions
 * 
 * @author Ole.Matzura
 */

public class WsdlAssertionRegistry
{
   private static WsdlAssertionRegistry instance;
	private Map<String,Class<? extends WsdlMessageAssertion> > availableAssertions = new HashMap<String,Class<? extends WsdlMessageAssertion> >();
	private StringToStringMap assertionLabels = new StringToStringMap();
	private final static Logger log = Logger.getLogger( WsdlAssertionRegistry.class );
	
	public WsdlAssertionRegistry()
	{
		addAssertion( SoapResponseAssertion.ID, "SOAP Response", SoapResponseAssertion.class );
		addAssertion( SchemaComplianceAssertion.ID, "Schema Compliance", SchemaComplianceAssertion.class );
		addAssertion( SimpleContainsAssertion.ID, "Contains", SimpleContainsAssertion.class );
		addAssertion( SimpleNotContainsAssertion.ID, "Not Contains", SimpleNotContainsAssertion.class );
		addAssertion( XPathContainsAssertion.ID, XPathContainsAssertion.LABEL, XPathContainsAssertion.class );
		addAssertion( NotSoapFaultAssertion.ID, NotSoapFaultAssertion.LABEL, NotSoapFaultAssertion.class );
		addAssertion( SoapFaultAssertion.ID, "SOAP Fault", SoapFaultAssertion.class );
		addAssertion( ResponseSLAAssertion.ID, "Response SLA", ResponseSLAAssertion.class );
		addAssertion( GroovyScriptAssertion.ID, GroovyScriptAssertion.LABEL,	GroovyScriptAssertion.class );
		addAssertion( XQueryContainsAssertion.ID, XQueryContainsAssertion.LABEL, XQueryContainsAssertion.class );
		addAssertion( WSSStatusAssertion.ID, "WS-Security Status", WSSStatusAssertion.class );
	}
	
	public void addAssertion( String id, String label, Class<? extends WsdlMessageAssertion> assertionClass )
	{
		availableAssertions.put(  id, assertionClass );
		assertionLabels.put( label, id );
	}
	
	public static synchronized WsdlAssertionRegistry getInstance()
	{
		if( instance == null )
			instance = new WsdlAssertionRegistry();
		
		return instance;
	}

	public WsdlMessageAssertion buildAssertion(RequestAssertionConfig config, Assertable assertable)
	{
	   try
		{
			String type = config.getType();
			Class<? extends WsdlMessageAssertion> clazz = availableAssertions.get(type);
			if( clazz == null )
			{
				log.error( "Missing assertion for type [" + type + "]" );
			}
			else
			{
				Constructor<? extends WsdlMessageAssertion> ctor = clazz
					.getConstructor(new Class[] { RequestAssertionConfig.class,
							Assertable.class });
				
				return (WsdlMessageAssertion) ctor.newInstance(config, assertable);
			}
		}
		catch (Exception e)
		{
			SoapUI.logError( e );
		}
		
		return null;
	}
	
	public boolean canBuildAssertion( RequestAssertionConfig config )
	{
		return availableAssertions.get(config.getType()) != null;
	}
	
	public String getAssertionTypeForName( String name )
	{
		return assertionLabels.get( name );
	}
	
	public enum AssertableType { REQUEST, RESPONSE, BOTH }
	
	public String[] getAvailableAssertionNames( AssertableType type )
	{
		List<String> result = new ArrayList<String>();
		
		for( String assertion : assertionLabels.keySet() )
		{
			switch( type )
			{
				case BOTH : 
				{
				   result.add( assertion );
				   break;
				}
				case REQUEST :
				{
					String assertionId = assertionLabels.get( assertion );
					if( Arrays.asList( availableAssertions.get( assertionId ).getInterfaces() ).contains( RequestAssertion.class )) 
					{
						result.add(  assertion );
					}
					break;
				}
				
				case RESPONSE :
				{
					String assertionId = assertionLabels.get( assertion );
					if( Arrays.asList( availableAssertions.get( assertionId ).getInterfaces() ).contains( ResponseAssertion.class )) 
					{
						result.add(  assertion );
					}
					break;
				}
			}
		}
		
      return result.toArray( new String[result.size()] );
	}

	public String getAssertionNameForType( String type )
	{
		for( String assertion : assertionLabels.keySet() )
		{
			if( assertionLabels.get( assertion ).equals( type  ))
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
			
			if( assertion.getClass().equals( availableAssertions.get( getAssertionTypeForName( name ))))
			{
				return false;
			}
		}
		
		return true;
	}

	public boolean canAddAssertion( WsdlMessageAssertion assertion, Assertable assertable )
	{
		if( assertion.isAllowMultiple())
			return true;
		
		for( int c = 0; c < assertable.getAssertionCount(); c++ )
		{
			if( assertion.getClass().equals( assertable.getAssertionAt( c ).getClass()))
			{
				return false;
			}
		}
		
		return true;
	}
}
