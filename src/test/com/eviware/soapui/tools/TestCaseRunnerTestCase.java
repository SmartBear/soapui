/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.tools;

import java.io.File;

import junit.framework.TestCase;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.Tools;

public class TestCaseRunnerTestCase extends TestCase
{
	public void testReplaceHost() throws Exception
	{
		assertEquals( "http://test2:8080/test", Tools.replaceHost( "http://test:8080/test", "test2" ) );

		assertEquals( "http://test2/test", Tools.replaceHost( "http://test/test", "test2" ) );

		assertEquals( "http://test2:8080", Tools.replaceHost( "http://test:8080", "test2" ) );

		assertEquals( "http://test2", Tools.replaceHost( "http://test", "test2" ) );

		assertEquals( "http://test2:8081", Tools.replaceHost( "http://test:8080", "test2:8081" ) );

		assertEquals( "http://test2:8081/test", Tools.replaceHost( "http://test:8080/test", "test2:8081" ) );
	}

	public void testInvalidTestCaseName() throws Exception
	{
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		runner.setProjectFile( new File( "src/test-resources/sample-soapui-project.xml" ).toURI().toString() );
		runner.setTestCase( "tjoho" );

		boolean failed = false;
		try
		{
			runner.run();
			failed = true;
		}
		catch( Exception e )
		{
			assertEquals( e.getMessage(), "TestCase with name [tjoho] is missing in Project [Sample Project]" );
		}

		assertFalse( failed );
	}

	public void testPropertyExpansionInOutputFolder() throws Exception
	{
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		runner.setOutputFolder( "/dev/${#Project#Env}" );

		assertEquals( "/dev/", runner.getAbsoluteOutputFolder( null ) );
		WsdlProject project = new WsdlProject( "src/test-resources/sample-soapui-project.xml" );
		project.setPropertyValue( "Env", "test" );
		assertEquals( "/dev/test", runner.getAbsoluteOutputFolder( project ) );
	}

	public void testInvalidTestCaseWithValidTestSuiteName() throws Exception
	{
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		runner.setProjectFile( new File( "src/test-resources/sample-soapui-project.xml" ).toURI().toString() );
		runner.setTestCase( "tjoho" );
		runner.setTestSuite( "Test Suite" );

		boolean failed = false;
		try
		{
			runner.run();
			failed = true;
		}
		catch( Exception e )
		{
			assertEquals( e.getMessage(),
					"TestCase with name [tjoho] in TestSuite [Test Suite] is missing in Project [Sample Project]" );
		}

		assertFalse( failed );
	}

	public void testInvalidTestSuiteName() throws Exception
	{
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		runner.setProjectFile( new File( "src/test-resources/sample-soapui-project.xml" ).toURI().toString() );
		runner.setTestSuite( "tjoho" );

		boolean failed = false;
		try
		{
			runner.run();
			failed = true;
		}
		catch( Exception e )
		{
			assertEquals( e.getMessage(), "TestSuite with name [tjoho] is missing in Project [Sample Project]" );
		}

		assertFalse( failed );
	}

	public void testTestCaseRunner() throws Exception
	{
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		runner.setProjectFile( new File( "src/test-resources/sample-soapui-project.xml" ).toURI().toString() );
		// assertTrue( runner.run() );
	}

	public void testValidTestSuiteAndTestCaseName() throws Exception
	{
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		runner.setProjectFile( new File( "src/test-resources/sample-soapui-project.xml" ).toURI().toString() );
		runner.setTestSuite( "Test Suite" );
		runner.setTestCase( "Test Conversions" );

		// assertTrue( runner.run() );
	}
}
