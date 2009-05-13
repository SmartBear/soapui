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

package com.eviware.soapui.impl.wsdl.mock;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * MockRunContext available during dispatching of a WsdlMockRequest
 * 
 * @author ole.matzura
 */

public class WsdlMockRunContext implements MockRunContext, Map<String, Object>, TestRunContext
{
	private DefaultPropertyExpansionContext properties;
	private final WsdlMockService mockService;
	private final WsdlTestRunContext context;
	private WsdlMockResponse mockResponse;

	public WsdlMockRunContext( WsdlMockService mockService, WsdlTestRunContext context )
	{
		this.mockService = mockService;
		this.context = context;

		properties = context == null ? new DefaultPropertyExpansionContext( mockService ) : context.getProperties();
	}

	public WsdlMockService getMockService()
	{
		return mockService;
	}

	public Object getProperty( String name )
	{
		return get( name );
	}

	public boolean hasProperty( String name )
	{
		return properties.containsKey( name );
	}

	public Object removeProperty( String name )
	{
		return properties.remove( name );
	}

	public void setProperty( String name, Object value )
	{
		if( context != null )
		{
			int ix = name.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
			if( ix > 0 )
			{
				String teststepname = name.substring( 0, ix );
				TestStep refTestStep = context.getTestCase().getTestStepByName( teststepname );
				if( refTestStep != null )
				{
					TestProperty property = refTestStep.getProperty( name.substring( ix + 1 ) );
					if( property != null && !property.isReadOnly() )
					{
						property.setValue( value.toString() );
						return;
					}
				}
			}
		}

		properties.put( name, value );
	}

	public StringToStringMap toStringToStringMap()
	{
		StringToStringMap result = new StringToStringMap();

		for( String key : properties.keySet() )
		{
			Object value = properties.get( key );
			if( value != null )
				result.put( key, value.toString() );
		}

		return result;
	}

	public void clear()
	{
		properties.clear();
	}

	public Object clone()
	{
		return properties.clone();
	}

	public boolean containsKey( Object arg0 )
	{
		return properties.containsKey( arg0 );
	}

	public boolean containsValue( Object arg0 )
	{
		return properties.containsValue( arg0 );
	}

	public Set<Entry<String, Object>> entrySet()
	{
		return properties.entrySet();
	}

	public boolean equals( Object arg0 )
	{
		return properties.equals( arg0 );
	}

	public Object get( Object arg0 )
	{
		if( "mockService".equals( arg0 ) )
			return getMockService();

		if( "mockResponse".equals( arg0 ) )
			return getMockResponse();

		if( "modelItem".equals( arg0 ) )
			return getModelItem();

		if( "currentStep".equals( arg0 ) )
			return getCurrentStep();

		if( "currentStepIndex".equals( arg0 ) )
			return getCurrentStepIndex();

		if( "settings".equals( arg0 ) )
			return getSettings();

		if( "testCase".equals( arg0 ) )
			return getTestCase();

		if( "testRunner".equals( arg0 ) )
			return getTestRunner();

		return properties.get( arg0 );
	}

	public int hashCode()
	{
		return properties.hashCode();
	}

	public boolean isEmpty()
	{
		return properties.isEmpty();
	}

	public Set<String> keySet()
	{
		return properties.keySet();
	}

	public Object put( String arg0, Object arg1 )
	{
		return properties.put( arg0, arg1 );
	}

	public void putAll( Map<? extends String, ? extends Object> arg0 )
	{
		properties.putAll( arg0 );
	}

	public Object remove( Object arg0 )
	{
		return properties.remove( arg0 );
	}

	public int size()
	{
		return properties.size();
	}

	public String toString()
	{
		return properties.toString();
	}

	public Collection<Object> values()
	{
		return properties.values();
	}

	public TestStep getCurrentStep()
	{
		return context == null ? null : context.getCurrentStep();
	}

	public int getCurrentStepIndex()
	{
		return context == null ? -1 : context.getCurrentStepIndex();
	}

	public Object getProperty( String testStep, String propertyName )
	{
		return context == null ? null : context.getProperty( testStep, propertyName );
	}

	public TestRunner getTestRunner()
	{
		return context == null ? null : context.getTestRunner();
	}

	public TestCase getTestCase()
	{
		return context == null ? null : context.getTestCase();
	}

	public Settings getSettings()
	{
		return context == null ? mockService.getSettings() : context.getTestCase().getSettings();
	}

	public void setMockResponse( WsdlMockResponse mockResponse )
	{
		this.mockResponse = mockResponse;
	}

	public WsdlMockResponse getMockResponse()
	{
		return mockResponse;
	}

	public ModelItem getModelItem()
	{
		return mockService;
	}

	public String expand( String content )
	{
		return PropertyExpansionUtils.expandProperties( this, content );
	}

	public String[] getPropertyNames()
	{
		return properties.keySet().toArray( new String[properties.size()] );
	}

	public StringToObjectMap getProperties()
	{
		return properties;
	}

	public MockRunner getMockRunner()
	{
		return mockService.getMockRunner();
	}
}