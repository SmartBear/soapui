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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleNotContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.http.HttpDownloadAllResourcesAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcStatusAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jdbc.JdbcTimeoutAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jms.JMSStatusAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.jms.JMSTimeoutAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapRequestAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSARequestAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSAResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.WSSStatusAssertion;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.security.assertion.CrossSiteScriptAssertion;
import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.eviware.soapui.security.assertion.SensitiveInfoExposureAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
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

	private TestAssertionRegistry()
	{
		addAssertion( new SoapResponseAssertion.Factory() );
		addAssertion( new SoapRequestAssertion.Factory() );
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
		addAssertion( new JMSStatusAssertion.Factory() );
		addAssertion( new JMSTimeoutAssertion.Factory() );
		addAssertion( new JdbcStatusAssertion.Factory() );
		addAssertion( new JdbcTimeoutAssertion.Factory() );
		addAssertion( new HttpDownloadAllResourcesAssertion.Factory() );

		// security
		addAssertion( new ValidHttpStatusCodesAssertion.Factory() );
		addAssertion( new InvalidHttpStatusCodesAssertion.Factory() );
		addAssertion( new SensitiveInfoExposureAssertion.Factory() );
		addAssertion( new CrossSiteScriptAssertion.Factory() );
		
		// pro placeh0lders
		addAssertion( new ProAssertionPlaceHolderFactory("MessageContentAssertion", "Message Content Assertion"));

		for( TestAssertionFactory factory : SoapUI.getFactoryRegistry().getFactories( TestAssertionFactory.class ) )
		{
			addAssertion( factory );
		}
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

	public Class<? extends WsdlMessageAssertion> getAssertionClassType( String assertionType )
	{
		try
		{
			TestAssertionFactory factory = availableAssertions.get( assertionType );
			if( factory == null )
			{
				log.error( "Missing assertion for type [" + assertionType + "]" );
			}
			else
			{
				return factory.getAssertionClassType();
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return null;
	}

	public Class<? extends WsdlMessageAssertion> getAssertionClassType( TestAssertionConfig config )
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
				return factory.getAssertionClassType();
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

	public AssertionListEntry getAssertionListEntry( String type )
	{
		TestAssertionFactory factory = availableAssertions.get( type );
		if( factory != null )
		{
			return factory.getAssertionListEntry();
		}
		else
		{
			return null;
		}
	}

	public boolean canAssert( String type, Assertable assertable )
	{
		TestAssertionFactory factory = availableAssertions.get( type );
		if( factory != null )
		{
			return factory.canAssert( assertable );
		}
		else
		{
			return false;
		}
	}

	public boolean canAssert( String type, TestModelItem modelItem, String property )
	{
		TestAssertionFactory factory = availableAssertions.get( type );
		if( factory != null )
		{
			return factory.canAssert( modelItem, property );
		}
		else
		{
			return false;
		}
	}

	/**
	 * 
	 * @param assertable
	 * @param categoryAssertionsMap
	 * @return assertion categories mapped with assertions in exact category if @param
	 *         assertable is not null only assertions for specific @param
	 *         assertable will be included if @param assertable is null all
	 *         assertions are included
	 */
	public LinkedHashMap<String, SortedSet<AssertionListEntry>> addCategoriesAssertionsMap( Assertable assertable,
			LinkedHashMap<String, SortedSet<AssertionListEntry>> categoryAssertionsMap )
	{
		for( String category : AssertionCategoryMapping.getAssertionCategories() )
		{
			SortedSet<AssertionListEntry> assertionCategorySet = new TreeSet<AssertionListEntry>();
			categoryAssertionsMap.put( category, assertionCategorySet );
		}

		for( TestAssertionFactory assertion : availableAssertions.values() )
		{
			SortedSet<AssertionListEntry> set;
			if( assertable == null || assertion.canAssert( assertable ) )
			{
				set = categoryAssertionsMap.get( assertion.getCategory() );
				if( set != null )
				{
					AssertionListEntry assertionListEntry = assertion.getAssertionListEntry();
					//					if( assertable == null && disableNonApplicable )
					set.add( assertionListEntry );
					categoryAssertionsMap.put( assertion.getCategory(), set );
				}

			}
		}
		for( String category : AssertionCategoryMapping.getAssertionCategories() )
		{
			if( categoryAssertionsMap.get( category ).isEmpty() )
			{
				categoryAssertionsMap.remove( category );
			}
		}

		return categoryAssertionsMap;
	}

	/**
	 * 
	 * adds all assertions into map, to be disabled later when non applicable
	 */
	public LinkedHashMap<String, SortedSet<AssertionListEntry>> addAllCategoriesMap(
			LinkedHashMap<String, SortedSet<AssertionListEntry>> categoryAssertionsMap )
	{
		for( String category : AssertionCategoryMapping.getAssertionCategories() )
		{
			SortedSet<AssertionListEntry> assertionCategorySet = new TreeSet<AssertionListEntry>();
			categoryAssertionsMap.put( category, assertionCategorySet );
		}

		for( TestAssertionFactory assertion : availableAssertions.values() )
		{
			SortedSet<AssertionListEntry> set;
			set = categoryAssertionsMap.get( assertion.getCategory() );
			if( set != null )
			{
				AssertionListEntry assertionListEntry = assertion.getAssertionListEntry();
				set.add( assertionListEntry );
				categoryAssertionsMap.put( assertion.getCategory(), set );
			}

		}
		for( String category : AssertionCategoryMapping.getAssertionCategories() )
		{
			if( categoryAssertionsMap.get( category ).isEmpty() )
			{
				categoryAssertionsMap.remove( category );
			}
		}

		return categoryAssertionsMap;
	}

	public Map<String, TestAssertionFactory> getAvailableAssertions()
	{
		return availableAssertions;
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

			if( assertion.getClass().equals(
					availableAssertions.get( getAssertionTypeForName( name ) ).getAssertionClassType() ) )
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
