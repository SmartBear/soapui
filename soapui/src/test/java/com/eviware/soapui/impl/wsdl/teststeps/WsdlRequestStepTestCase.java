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

package com.eviware.soapui.impl.wsdl.teststeps;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;

public class WsdlRequestStepTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( WsdlRequestStepTestCase.class );
	}

	@Test
	public void executesAndReturnsResult() throws Exception
	{
		File sampleProjectFile = new File( WsdlRequestStepTestCase.class.getResource( "/sample-soapui-project.xml" ).toURI() );
		WsdlProject project = new WsdlProject( sampleProjectFile.getAbsolutePath()  );
		TestSuite testSuite = project.getTestSuiteByName( "Test Suite" );
		com.eviware.soapui.model.testsuite.TestCase testCase = testSuite.getTestCaseByName( "Test Conversions" );

		WsdlTestRequestStep testStep = ( WsdlTestRequestStep )testCase.getTestStepByName( "SEK to USD Test" );

		MockTestRunner testRunner = new MockTestRunner( testStep.getTestCase() );
		MockTestRunContext testRunContext = new MockTestRunContext( testRunner, testStep );

		TestStepResult result = testStep.run( testRunner, testRunContext );

		WsdlTestRequestStepResult wsdlResult = ( WsdlTestRequestStepResult )result;
		assertNotNull( wsdlResult );
	}
}
