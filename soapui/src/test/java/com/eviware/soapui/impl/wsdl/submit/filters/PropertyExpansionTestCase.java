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

package com.eviware.soapui.impl.wsdl.submit.filters;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GroovyScriptStepFactory;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.settings.GlobalPropertySettings;

public class PropertyExpansionTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( PropertyExpansionTestCase.class );
	}

	@Test
	public void testExpansion() throws Exception
	{
		WsdlSubmitContext context = new WsdlSubmitContext( null );

		context.setProperty( "test", "value" );

		assertEquals( "${test}", PropertyExpander.expandProperties( context, "$${test}" ) );
		assertEquals( "value${test}", PropertyExpander.expandProperties( context, "${test}$${test}" ) );
		assertEquals( "${value", PropertyExpander.expandProperties( context, "$${${test}" ) );
		assertEquals( "value", PropertyExpander.expandProperties( context, "${test}" ) );
		assertEquals( "value", PropertyExpander.expandProperties( context, "${#test}" ) );
		assertEquals( " value ", PropertyExpander.expandProperties( context, " ${test} " ) );
		assertEquals( "", PropertyExpander.expandProperties( context, "${testa}" ) );
		assertEquals( "valuevalue", PropertyExpander.expandProperties( context, "${test}${test}" ) );

		context.setProperty( "testa", "" );
		assertEquals( "", PropertyExpander.expandProperties( context, "${testa}" ) );
	}

	@Test
	public void testRecursiveExpansion() throws Exception
	{
		WsdlSubmitContext context = new WsdlSubmitContext( null );

		context.setProperty( "test", "value" );
		context.setProperty( "testexp", "${test}" );

		assertEquals( "value", PropertyExpander.expandProperties( context, "${testexp}" ) );

		context.setProperty( "exp", "${exp}" );
		assertEquals( "${exp}", PropertyExpander.expandProperties( context, "${exp}" ) );
	}

	@Test
	public void testNestedExpansion() throws Exception
	{
		WsdlSubmitContext context = new WsdlSubmitContext( null );

		context.setProperty( "test", "value" );
		context.setProperty( "testexp", "${test}" );
		context.setProperty( "exp", "exp" );

		assertEquals( "value", PropertyExpander.expandProperties( context, "${test${exp}}" ) );

		context.setProperty( "id", "123" );
		context.setProperty( "testxml", "<test><value id=\"123\">hello</value></test>" );
		assertEquals( "hello", PropertyExpander.expandProperties( context, "${#testxml#//value[@id=${id}]/text()}" ) );

		context.setProperty( "testxpath", "//value[@id=${id}]/text()" );
		assertEquals( "hello", PropertyExpander.expandProperties( context, "${#testxml#${testxpath}}" ) );
	}

	@Test
	public void testXPathExpansion() throws Exception
	{
		WsdlSubmitContext context = new WsdlSubmitContext( null );

		context.setProperty( "test", "<test><value>hello</value></test>" );
		assertEquals( "hello", PropertyExpander.expandProperties( context, "${#test#//value/text()}" ) );
	}

	@Test
	public void testScopedPropertyExpansion() throws Exception
	{
		WsdlProject project = new WsdlProject();
		project.addProperty( "projectId" ).setValue( "123" );
		WsdlTestSuite testSuite = project.addNewTestSuite( "TestSuite" );
		testSuite.addProperty( "testSuiteId" ).setValue( "234" );
		WsdlTestCase testCase = testSuite.addNewTestCase( "TestCase" );
		testCase.addProperty( "testCaseId" ).setValue( "345" );

		WsdlTestStep testStep = testCase.addTestStep( GroovyScriptStepFactory.GROOVY_TYPE, "GroovyScript" );

		MockTestRunner mockTestRunner = new MockTestRunner( testCase, Logger.getLogger( "testing" ) );
		MockTestRunContext context = new MockTestRunContext( mockTestRunner, testStep );

		PropertyExpansionUtils.getGlobalProperties().setPropertyValue( "testSuiteId", "testing" );
		SoapUI.getSettings().setBoolean( GlobalPropertySettings.ENABLE_OVERRIDE, false );

		assertEquals( "123", context.expand( "${#Project#projectId}" ) );
		assertEquals( "234", context.expand( "${#TestSuite#testSuiteId}" ) );
		assertEquals( "345", context.expand( "${#TestCase#testCaseId}" ) );

		SoapUI.getSettings().setBoolean( GlobalPropertySettings.ENABLE_OVERRIDE, true );

		assertEquals( "testing", context.expand( "${#TestSuite#testSuiteId}" ) );
	}
}
