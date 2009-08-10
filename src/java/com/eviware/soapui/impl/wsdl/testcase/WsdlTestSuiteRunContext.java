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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * TestRunContext for WsdlTestCase runners
 * 
 * @author Ole.Matzura
 */

public class WsdlTestSuiteRunContext extends AbstractSubmitContext<WsdlTestSuite> implements TestSuiteRunContext
{
	private final WsdlTestSuiteRunner testRunner;
	private TestSuite testSuite;

	public WsdlTestSuiteRunContext( TestSuiteRunner testRunner, StringToObjectMap properties )
	{
		super( ( WsdlTestSuite )testRunner.getTestSuite(), properties );
		this.testRunner = ( WsdlTestSuiteRunner )testRunner;
	}

	public TestSuiteRunner getTestRunner()
	{
		return testRunner;
	}

	public TestSuite getTestSuite()
	{
		return testRunner.getTestSuite();
	}

	@Override
	public Object get( Object key )
	{
		if( "currentTestCase".equals( key ) )
			return getCurrentTestCase();

		if( "currentTestCaseIndex".equals( key ) )
			return getCurrentTestCaseIndex();

		if( "settings".equals( key ) )
			return getSettings();

		if( "testSuite".equals( key ) )
			return getTestSuite();

		if( "testRunner".equals( key ) )
			return getTestRunner();

		return super.get( key );
	}

	@Override
	public Object put( String key, Object value )
	{
		Object oldValue = get( key );
		setProperty( key, value );
		return oldValue;
	}

	public void reset()
	{
		resetProperties();
	}

	public String expand( String content )
	{
		return PropertyExpander.expandProperties( this, content );
	}

	public Settings getSettings()
	{
		return testSuite == null ? SoapUI.getSettings() : testSuite.getSettings();
	}

	public TestCase getCurrentTestCase()
	{
		return testRunner.getCurrentTestCase();
	}

	public int getCurrentTestCaseIndex()
	{
		return testRunner.getCurrentTestCaseIndex();
	}

	public TestSuiteRunner getTestSuiteRunner()
	{
		return testRunner;
	}

	public Object getProperty( String name )
	{
		return super.get( name );
	}
}
