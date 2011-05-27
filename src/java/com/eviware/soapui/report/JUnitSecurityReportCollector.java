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

package com.eviware.soapui.report;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunListener;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Collects Security Test results and creates JUnitReports
 * 
 * @author nebojsa.tasic
 */

public class JUnitSecurityReportCollector extends JUnitReportCollector implements SecurityTestRunListener
{
	HashMap<String, JUnitReport> reports;
	HashMap<TestCase, String> failures;
	HashMap<TestCase, Integer> errorCount;
	private int maxErrors = 0;

	public JUnitSecurityReportCollector()
	{
		this( 0 );
	}

	public JUnitSecurityReportCollector( int maxErrors )
	{
		this.maxErrors = maxErrors;
		reports = new HashMap<String, JUnitReport>();
		errorCount = new HashMap<TestCase, Integer>();
		failures = new HashMap<TestCase, String>();
	}

	public List<String> saveReports( String path ) throws Exception
	{

		File file = new File( path );
		if( !file.exists() || !file.isDirectory() )
			file.mkdirs();

		List<String> result = new ArrayList<String>();

		Iterator<String> keyset = reports.keySet().iterator();
		while( keyset.hasNext() )
		{
			String name = keyset.next();
			JUnitReport report = reports.get( name );
			String fileName = path + File.separatorChar + "TEST-" + StringUtils.createFileName( name, '_' ) + ".xml";
			saveReport( report, fileName );
			result.add( fileName );
		}

		return result;
	}

	public HashMap<String, JUnitReport> getReports()
	{
		return reports;
	}

	public void saveReport( JUnitReport report, String filename ) throws Exception
	{
		report.save( new File( filename ) );
	}

	public String getReport()
	{
		Set<String> keys = reports.keySet();
		if( keys.size() > 0 )
		{
			String key = ( String )keys.toArray()[0];
			return reports.get( key ).toString();
		}
		return "No reports..:";
	}

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		TestCase testCase = testRunner.getTestCase();
		JUnitReport report = reports.get( testCase.getTestSuite().getName() );

		if( Status.INITIALIZED != testRunner.getStatus() && Status.RUNNING != testRunner.getStatus() )
		{
			if( Status.CANCELED == testRunner.getStatus() )
			{
				report.addTestCaseWithFailure( testCase.getName(), testRunner.getTimeTaken(), testRunner.getReason(), "" );
			}
			if( Status.FAILED == testRunner.getStatus() )
			{
				String msg = "";
				if( failures.containsKey( testCase ) )
				{
					msg = failures.get( testCase ).toString();
				}
				report.addTestCaseWithFailure( testCase.getName(), testRunner.getTimeTaken(), testRunner.getReason(), msg );
			}
			if( Status.FINISHED == testRunner.getStatus() )
			{
				report.addTestCase( testCase.getName(), testRunner.getTimeTaken() );
			}

		}
	}

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
		TestStep currentStep = result.getTestStep();
		TestCase testCase = currentStep.getTestCase();

		if( result.getStatus() == TestStepStatus.FAILED )
		{
			if( maxErrors > 0 )
			{
				Integer errors = errorCount.get( testCase );
				if( errors == null )
					errors = 0;

				if( errors >= maxErrors )
					return;

				errorCount.put( testCase, errors + 1 );
			}

			StringBuffer buf = new StringBuffer();
			if( failures.containsKey( testCase ) )
			{
				buf.append( failures.get( testCase ) );
			}

			buf.append( "<h3><b>" ).append( XmlUtils.entitize( result.getTestStep().getName() ) )
					.append( " Failed</b></h3><pre>" );
			for( String message : result.getMessages() )
			{
				if( message.toLowerCase().startsWith( "url:" ) )
				{
					String url = XmlUtils.entitize( message.substring( 4 ).trim() );
					buf.append( "URL: <a target=\"new\" href=\"" ).append( url ).append( "\">" ).append( url )
							.append( "</a>" );
				}
				else
				{
					buf.append( message );
				}

				buf.append( "\r\n" );
			}

			// use string value since constant is defined in pro.. duh..
			if( testRunner.getTestCase().getSettings().getBoolean( "Complete Error Logs" ) )
			{
				StringWriter stringWriter = new StringWriter();
				PrintWriter writer = new PrintWriter( stringWriter );
				result.writeTo( writer );

				buf.append( XmlUtils.entitize( stringWriter.toString() ) );
			}

			buf.append( "</pre><hr/>" );

			failures.put( testCase, buf.toString() );
		}
	}

	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		TestCase testCase = testRunner.getTestCase();
		TestSuite testSuite = testCase.getTestSuite();
		if( !reports.containsKey( testSuite.getName() ) )
		{
			JUnitReport report = new JUnitReport();
			report.setTestSuiteName( testSuite.getProject().getName() + "." + testSuite.getName() );
			reports.put( testSuite.getName(), report );
		}
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
	{
	}

	public void reset()
	{
		reports.clear();
		failures.clear();
		errorCount.clear();
	}

	public void afterRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
	{
	}

	public void afterTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCaseRunner testCaseRunner )
	{
		testCaseRunner.getTestCase().removeTestRunListener( this );
	}

	public void beforeRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
	{
	}

	public void beforeTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCase testCase )
	{
		testCase.addTestRunListener( this );
	}

	public void afterRun( ProjectRunner testScenarioRunner, ProjectRunContext runContext )
	{
	}

	public void afterTestSuite( ProjectRunner testScenarioRunner, ProjectRunContext runContext,
			TestSuiteRunner testRunner )
	{
		testRunner.getTestSuite().removeTestSuiteRunListener( this );
	}

	public void beforeRun( ProjectRunner testScenarioRunner, ProjectRunContext runContext )
	{
	}

	public void beforeTestSuite( ProjectRunner testScenarioRunner, ProjectRunContext runContext, TestSuite testSuite )
	{
		testSuite.addTestSuiteRunListener( this );
	}

	/**
	 * Use this factory method to allow usage of an external reportCollecto; it
	 * checks for a soapui.junit.reportCollector system property that should
	 * specify a class derived from this JUnitReportCollector which will be used
	 * instead
	 * 
	 * @param maxErrors
	 */

	public static JUnitSecurityReportCollector createNew( int maxErrors )
	{
		String className = System.getProperty( "soapui.junit.reportCollector", null );
		if( StringUtils.hasContent( className ) )
		{
			try
			{
				return ( JUnitSecurityReportCollector )Class.forName( className ).getConstructor( Integer.class )
						.newInstance( maxErrors );
			}
			catch( Exception e )
			{
				System.err.println( "Failed to create JUnitReportCollector class [" + className + "]; " + e.toString() );
			}
		}

		return new JUnitSecurityReportCollector( maxErrors );
	}

	@Override
	public void afterOriginalStep( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityTestStepResult result )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityScanResult securityScanResult )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterSecurityScanRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityScanRequestResult securityScanReqResult )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityScan securityScan )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStepResult testStepResult )
	{
		// TODO Auto-generated method stub
		
	}
}
