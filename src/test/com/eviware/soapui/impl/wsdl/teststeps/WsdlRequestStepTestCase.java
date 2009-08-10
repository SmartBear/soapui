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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.io.File;

import junit.framework.TestCase;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;

public class WsdlRequestStepTestCase extends TestCase
{
	public void testAssert() throws Exception
	{
		WsdlProject project = new WsdlProject("src" + File.separatorChar + "test-resources" + File.separatorChar + "sample-soapui-project.xml");
      TestSuite testSuite = project.getTestSuiteByName("Test Suite");
      com.eviware.soapui.model.testsuite.TestCase testCase = 
         testSuite.getTestCaseByName("Test Conversions");
     
      WsdlTestRequestStep testStep = ( WsdlTestRequestStep ) testCase.getTestStepByName("SEK to USD Test");
     
      MockTestRunner testRunner = new MockTestRunner( (WsdlTestCase) testStep.getTestCase() ); 
      MockTestRunContext testRunContext = new MockTestRunContext( testRunner, (WsdlTestStep) testStep ); 
     
      TestStepResult result = testStep.run( testRunner, testRunContext ); 

      WsdlTestRequestStepResult wsdlResult = (WsdlTestRequestStepResult)result;
      assertNotNull( wsdlResult );
     // assertEquals(TestStepResult.TestStepStatus.OK, wsdlResult.getStatus());
	}
}
