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

package com.eviware.soapui.impl.wsdl.submit.filters;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GroovyScriptStepFactory;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.settings.GlobalPropertySettings;

public class PropertyExpansionTestCase extends TestCase
{
   public void testExpansion() throws Exception
   {
   	WsdlSubmitContext context = new WsdlSubmitContext( null );
   	
   	context.setProperty( "test", "value" );
   	
   	assertEquals( "value", PropertyExpansionUtils.expandProperties( context, "${test}" ));
   	assertEquals( "value", PropertyExpansionUtils.expandProperties( context, "${#test}" ));
   	assertEquals( " value ", PropertyExpansionUtils.expandProperties( context, " ${test} " ));
   	assertEquals( "", PropertyExpansionUtils.expandProperties( context, "${testa}" ));
   	assertEquals( "valuevalue", PropertyExpansionUtils.expandProperties( context, "${test}${test}" ));

   	context.setProperty( "testa", "" );
   	assertEquals( "", PropertyExpansionUtils.expandProperties( context, "${testa}" ));
   }
   
   public void testRecursiveExpansion() throws Exception
   {
   	WsdlSubmitContext context = new WsdlSubmitContext( null );
   	
   	context.setProperty( "test", "value" );
   	context.setProperty( "testexp", "${test}" );
   	
   	assertEquals( "value", PropertyExpansionUtils.expandProperties( context, "${testexp}" ));

   	context.setProperty( "exp", "${exp}" );
   	assertEquals( "${exp}", PropertyExpansionUtils.expandProperties( context, "${exp}" ));
   }
   
   public void testNestedExpansion() throws Exception
   {
   	WsdlSubmitContext context = new WsdlSubmitContext( null );
   	
   	context.setProperty( "test", "value" );
   	context.setProperty( "testexp", "${test}" );
   	context.setProperty( "exp", "exp" );
   	
   	assertEquals( "value", PropertyExpansionUtils.expandProperties( context, "${test${exp}}" ));
   	
   	context.setProperty( "id", "123" );
   	context.setProperty( "testxml", "<test><value id=\"123\">hello</value></test>" );
   	assertEquals( "hello", 
   				PropertyExpansionUtils.expandProperties( context, "${#testxml#//value[@id=${id}]/text()}" ));

   	context.setProperty( "testxpath", "//value[@id=${id}]/text()" );
   	assertEquals( "hello", 
   				PropertyExpansionUtils.expandProperties( context, "${#testxml#${testxpath}}" ));
   }
   
   public void testXPathExpansion() throws Exception 
   {
   	WsdlSubmitContext context = new WsdlSubmitContext( null );
   	
   	context.setProperty( "test", "<test><value>hello</value></test>" );
   	assertEquals( "hello", PropertyExpansionUtils.expandProperties( context, "${#test#//value/text()}" ));
   }
   
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
      
      assertEquals( "123", context.expand( "${#Project#projectId}" ) );
      assertEquals( "234", context.expand( "${#TestSuite#testSuiteId}" ) );
      assertEquals( "345", context.expand( "${#TestCase#testCaseId}" ) );
   	
      SoapUI.getSettings().setBoolean( GlobalPropertySettings.ENABLE_OVERRIDE, true );
      
      assertEquals( "testing", context.expand( "${#TestSuite#testSuiteId}" ) );
   }
}
