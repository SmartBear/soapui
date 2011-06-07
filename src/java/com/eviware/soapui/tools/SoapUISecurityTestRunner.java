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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.report.JUnitSecurityReportCollector;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.support.SecurityTestRunListenerAdapter;
import com.eviware.soapui.support.StringUtils;

/**
 * Standalone security test-runner used from maven-plugin, can also be used from
 * command-line (see xdocs) or directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other
 * desired properties before calling run
 * </p>
 * 
 * @author nebojsa.tasic
 */

public class SoapUISecurityTestRunner extends SoapUITestCaseRunner
{
	public static final String SOAPUI_EXPORT_SEPARATOR = "soapui.export.separator";

	public static final String TITLE = "soapUI " + SoapUI.SOAPUI_VERSION + " Security Test Runner";
	private String securityTestName;
	private int securityTestCount;
	private int securityScanCount;
	private int securityScanRequestCount;
	private int securityScanAlertCount;
	private JUnitSecurityReportCollector reportCollector = new JUnitSecurityReportCollector();

	/**
	 * Runs the tests in the specified soapUI project file, see soapUI xdocs for
	 * details.
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main( String[] args ) throws Exception
	{
		System.exit( new SoapUISecurityTestRunner().runFromCommandLine( args ) );
	}

	protected boolean processCommandLine( CommandLine cmd )
	{
		if( cmd.hasOption( "n" ) )
			setSecurityTestName( cmd.getOptionValue( "n" ) );

		return super.processCommandLine( cmd );
	}

	public void setSecurityTestName( String securityTestName )
	{
		this.securityTestName = securityTestName;
	}

	protected SoapUIOptions initCommandLineOptions()
	{
		SoapUIOptions options = super.initCommandLineOptions();
		options.addOption( "n", true, "Sets the security test name" );

		return options;
	}

	public SoapUISecurityTestRunner()
	{
		super( SoapUISecurityTestRunner.TITLE );
	}

	public SoapUISecurityTestRunner( String title )
	{
		super( title );
	}

	public boolean runRunner() throws Exception
	{
		initGroovyLog();

		String projectFile = getProjectFile();

		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( projectFile,
				getProjectPassword() );

		if( project.isDisabled() )
			throw new Exception( "Failed to load soapUI project file [" + projectFile + "]" );

		initProject( project );
		ensureOutputFolder( project );

		log.info( "Running soapUI tests in project [" + project.getName() + "]" );

		String testSuite = getTestSuite();
		String testCase = getTestCase();

		long startTime = System.nanoTime();

		List<TestCase> testCasesToRun = new ArrayList<TestCase>();

		// start by listening to all testcases.. (since one testcase can call
		// another)
		for( int c = 0; c < project.getTestSuiteCount(); c++ )
		{
			TestSuite suite = project.getTestSuiteAt( c );
			for( int i = 0; i < suite.getTestCaseCount(); i++ )
			{
				TestCase tc = suite.getTestCaseAt( i );
				if( ( testSuite == null || suite.getName().equals( suite.getName() ) ) && testCase != null
						&& tc.getName().equals( testCase ) )
					testCasesToRun.add( tc );

				addListeners( tc );
			}
		}

		// decide what to run
		if( testCasesToRun.size() > 0 )
		{
			for( TestCase tc : testCasesToRun )
			{
				runTestCase( ( WsdlTestCase )tc );
			}
		}
		else if( testSuite != null )
		{
			WsdlTestSuite ts = project.getTestSuiteByName( testSuite );
			if( ts == null )
				throw new Exception( "TestSuite with name [" + testSuite + "] not found in project" );
			else
				runSuite( ts );
		}
		else
		{
			runProject( project );
		}

		long timeTaken = ( System.nanoTime() - startTime ) / 1000000;

		if( isPrintReport() )
		{
			printReport( timeTaken );
		}

		exportReports( project );

		if( isSaveAfterRun() && !project.isRemote() )
		{
			try
			{
				project.save();
			}
			catch( Throwable t )
			{
				log.error( "Failed to save project", t );
			}
		}

		if( securityScanAlertCount > 0 && !isIgnoreErrors() )
		{
			throw new Exception( "SecurityTest execution failed with " + securityScanAlertCount + " alert"
					+ ( securityScanAlertCount > 1 ? "s" : "" ) );
		}

		return true;
	}

	protected void runProject( WsdlProject project )
	{
		try
		{
			log.info( ( "Running Project [" + project.getName() + "], runType = " + project.getRunType() ) );
			for( TestSuite testSuite : project.getTestSuiteList() )
			{
				runSuite( ( WsdlTestSuite )testSuite );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected void initProject( WsdlProject project ) throws Exception
	{
		initProjectProperties( project );
	}

	protected void exportReports( WsdlProject project ) throws Exception
	{
		if( isJUnitReport() )
		{
			exportJUnitReports( reportCollector, getAbsoluteOutputFolder( project ), project );
		}
	}

	public void exportJUnitReports( JUnitSecurityReportCollector collector, String folder, WsdlProject project )
			throws Exception
	{
		collector.saveReports( folder == null ? "" : folder );
	}

	public void printReport( long timeTaken )
	{
		System.out.println();
		System.out.println( "SoapUI " + SoapUI.SOAPUI_VERSION + " Security TestCaseRunner Summary" );
		System.out.println( "-----------------------------" );
		System.out.println( "Time Taken: " + timeTaken + "ms" );
		System.out.println( "Total SecurityTests: " + securityTestCount );
		System.out.println( "Total SecurityScans: " + securityScanCount );
		System.out.println( "Total SecurityScan Requests: " + securityScanRequestCount );
		System.out.println( "Total Failed SecurityScan Requests: " + securityScanAlertCount );
	}

	/**
	 * Run tests in the specified TestSuite
	 * 
	 * @param suite
	 *           the TestSuite to run
	 */

	protected void runSuite( WsdlTestSuite suite )
	{
		try
		{
			for( TestCase testCase : suite.getTestCaseList() )
			{
				runTestCase( ( WsdlTestCase )testCase );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

	}

	/**
	 * Runs the SecurityTests in the specified TestCase
	 * 
	 * @param testCase
	 *           the testcase to run
	 * @param context
	 */

	protected void runTestCase( WsdlTestCase testCase )
	{
		try
		{
			for( SecurityTest securityTest : testCase.getSecurityTestList() )
			{
				if( StringUtils.isNullOrEmpty( securityTestName ) || securityTest.getName().equals( securityTestName ) )
					runSecurityTest( securityTest );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param securityTest
	 */
	protected void runSecurityTest( SecurityTest securityTest )
	{
		securityTest.addSecurityTestRunListener( new SecurityTestRunListenerAdapter()
		{
			private int requestIndex = 0;

			@Override
			public void afterSecurityScanRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
					SecurityScanRequestResult securityCheckReqResult )
			{
				securityScanRequestCount++ ;
				if( securityCheckReqResult.getStatus() == ResultStatus.FAILED )
					securityScanAlertCount++ ;

				log.info( securityCheckReqResult.getSecurityScan().getName() + " - "
						+ securityCheckReqResult.getChangedParamsInfo( ++requestIndex ) );
			}

			@Override
			public void afterSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
					SecurityScanResult securityCheckResult )
			{
				securityScanCount++ ;
			}

			@Override
			public void beforeSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
					SecurityScan securityCheck )
			{
				requestIndex = 0;
			}

		} );

		if( isJUnitReport() )
			securityTest.addSecurityTestRunListener( reportCollector );

		log.info( "Running SecurityTest [" + securityTest.getName() + "] in TestCase ["
				+ securityTest.getTestCase().getName() + "] in TestSuite ["
				+ securityTest.getTestCase().getTestSuite().getName() + "]" );

		SecurityTestRunner runner = securityTest.run( null, false );
		// log.info( "\n" + securityTest.getSecurityTestLog().getMessages() );
		log.info( "SecurityTest [" + securityTest.getName() + "] finished with status [" + runner.getStatus() + "] in "
				+ ( runner.getTimeTaken() ) + "ms" );

		if( isJUnitReport() )
			securityTest.removeSecurityTestRunListener( reportCollector );
	}

	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep currentStep )
	{
	}

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
	}

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
	}

}
