/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
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
