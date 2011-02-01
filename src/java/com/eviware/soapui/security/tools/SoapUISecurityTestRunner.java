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

package com.eviware.soapui.security.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectRunListenerAdapter;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.report.JUnitReportCollector;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.tools.AbstractSoapUITestRunner;

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

public class SoapUISecurityTestRunner extends AbstractSoapUITestRunner
{
	public static final String SOAPUI_EXPORT_SEPARATOR = "soapui.export.separator";

	public static final String TITLE = "soapUI " + SoapUI.SOAPUI_VERSION + " Security Test Runner";

	private String testSuite;
	private String testCase;
	private String securityTestName;
	private List<TestAssertion> assertions = new ArrayList<TestAssertion>();
	private Map<TestAssertion, WsdlTestStepResult> assertionResults = new HashMap<TestAssertion, WsdlTestStepResult>();
	// private List<TestCaseRunner> runningTests = new
	// ArrayList<TestCaseRunner>();
	private List<TestCase> failedTests = new ArrayList<TestCase>();

	private int testSuiteCount;
	private int testCaseCount;
	private int testStepCount;
	private int testAssertionCount;

	private boolean printReport;
	private boolean exportAll;
	private boolean ignoreErrors;
	private boolean junitReport;
	private int exportCount;
	private int maxErrors = 5;
	private JUnitReportCollector reportCollector;
	// private WsdlProject project;
	private String projectPassword;
	private boolean saveAfterRun;

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
		String message = "";

		if( cmd.hasOption( "s" ) )
		{
			String testSuite = getCommandLineOptionSubstSpace( cmd, "s" );
			setTestSuite( testSuite );
			message += validateTestSuite();
		}
		if( cmd.hasOption( "c" ) )
		{
			String testCase = getCommandLineOptionSubstSpace( cmd, "c" );
			setTestCase( testCase );
			message += validateTestCase();
		}
		if( cmd.hasOption( "n" ) )
			setSecurityTestName( cmd.getOptionValue( "n" ) );

		if( message.length() > 0 )
		{
			log.error( message );
			return false;
		}

		return true;
	}

	private void setSecurityTestName( String securityTestName )
	{
		this.securityTestName = securityTestName;

	}

	private String validateTestCase()
	{

		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew(
				getProjectFile(), getProjectPassword() );

		if( project.getTestSuiteByName( testSuite ) == null )
			return "Test Suite with name:'" + testSuite + "' is missing from project:'" + project.getName() + "' \n";

		if( project.getTestSuiteByName( testSuite ).getTestCaseByName( testCase ) == null )
			return "Test Case with name:'" + testCase + "' is missing from testSuite:'" + testSuite + "' \n";

		return "";
	}

	private String validateTestSuite()
	{
		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew(
				getProjectFile(), getProjectPassword() );

		if( project.getTestSuiteByName( testSuite ) == null )
			return "Test Suite with name:'" + testSuite + "' is missing from project:'" + project.getName() + "' \n";

		return "";

	}

	public void setMaxErrors( int maxErrors )
	{
		this.maxErrors = maxErrors;
	}

	protected int getMaxErrors()
	{
		return maxErrors;
	}

	public void setSaveAfterRun( boolean saveAfterRun )
	{
		this.saveAfterRun = saveAfterRun;
	}

	public void setProjectPassword( String projectPassword )
	{
		this.projectPassword = projectPassword;
	}

	public String getProjectPassword()
	{
		return projectPassword;
	}

	protected SoapUIOptions initCommandLineOptions()
	{
		SoapUIOptions options = new SoapUIOptions( "security test runner" );
		options.addOption( "s", true, "Sets the testsuite" );
		options.addOption( "c", true, "Sets the testcase" );
		options.addOption( "n", true, "Sets the security test name" );

		return options;
	}

	/**
	 * Add console appender to groovy log
	 */

	public void setExportAll( boolean exportAll )
	{
		this.exportAll = exportAll;
	}

	public void setJUnitReport( boolean junitReport )
	{
		this.junitReport = junitReport;
		if( junitReport )
			reportCollector = createJUnitReportCollector();
	}

	protected JUnitReportCollector createJUnitReportCollector()
	{
		return new JUnitReportCollector( maxErrors );
	}

	public SoapUISecurityTestRunner()
	{
		super( SoapUISecurityTestRunner.TITLE );
	}

	public SoapUISecurityTestRunner( String title )
	{
		super( title );
	}

	/**
	 * Controls if a short test summary should be printed after the test runs
	 * 
	 * @param printReport
	 *           a flag controlling if a summary should be printed
	 */

	public void setPrintReport( boolean printReport )
	{
		this.printReport = printReport;
	}

	public void setIgnoreError( boolean ignoreErrors )
	{
		this.ignoreErrors = ignoreErrors;
	}

	public boolean runRunner() throws Exception
	{
		initGroovyLog();

		assertions.clear();

		String projectFile = getProjectFile();

		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( projectFile,
				getProjectPassword() );

		if( project.isDisabled() )
			throw new Exception( "Failed to load soapUI project file [" + projectFile + "]" );

		initProject( project );
		ensureOutputFolder( project );

		log.info( "Running soapUI tests in project [" + project.getName() + "]" );

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
			for( TestCase testCase : testCasesToRun )
				runTestCase( ( WsdlTestCase )testCase );
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

		if( printReport )
		{
			printReport( timeTaken );
		}

		exportReports( project );

		if( saveAfterRun && !project.isRemote() )
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

		if( ( assertions.size() > 0 || failedTests.size() > 0 ) && !ignoreErrors )
		{
			throwFailureException();
		}

		return true;
	}

	protected void runProject( WsdlProject project )
	{
		// add listener for counting..
		InternalProjectRunListener projectRunListener = new InternalProjectRunListener();
		project.addProjectRunListener( projectRunListener );

		try
		{
			log.info( ( "Running Project [" + project.getName() + "], runType = " + project.getRunType() ) );
			for(TestSuite testSuite: project.getTestSuiteList()){
				runSuite( ( WsdlTestSuite )testSuite );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			project.removeProjectRunListener( projectRunListener );
		}
	}

	protected void initProject( WsdlProject project ) throws Exception
	{
		initProjectProperties( project );
	}

	protected void exportReports( WsdlProject project ) throws Exception
	{
		if( junitReport )
		{
			exportJUnitReports( reportCollector, getAbsoluteOutputFolder( project ), project );
		}
	}

	protected void addListeners( TestCase tc )
	{
		tc.addTestRunListener( this );
		if( junitReport )
			tc.addTestRunListener( reportCollector );
	}

	protected void throwFailureException() throws Exception
	{
		StringBuffer buf = new StringBuffer();

		for( int c = 0; c < assertions.size(); c++ )
		{
			TestAssertion assertion = assertions.get( c );
			Assertable assertable = assertion.getAssertable();
			if( assertable instanceof WsdlTestStep )
				failedTests.remove( ( ( WsdlTestStep )assertable ).getTestCase() );

			buf.append( assertion.getName() + " in [" + assertable.getModelItem().getName() + "] failed;\n" );
			buf.append( Arrays.toString( assertion.getErrors() ) + "\n" );

			WsdlTestStepResult result = assertionResults.get( assertion );
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter( stringWriter );
			result.writeTo( writer );
			buf.append( stringWriter.toString() );
		}

		while( !failedTests.isEmpty() )
		{
			buf.append( "TestCase [" + failedTests.remove( 0 ).getName() + "] failed without assertions\n" );
		}

		throw new Exception( buf.toString() );
	}

	public void exportJUnitReports( JUnitReportCollector collector, String folder, WsdlProject project )
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
		System.out.println( "Total TestSuites: " + testSuiteCount );
		System.out.println( "Total TestCases: " + testCaseCount + " (" + failedTests.size() + " failed)" );
		System.out.println( "Total TestSteps: " + testStepCount );
		System.out.println( "Total Request Assertions: " + testAssertionCount );
		System.out.println( "Total Failed Assertions: " + assertions.size() );
		System.out.println( "Total Exported Results: " + exportCount );
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
			log.info( ( "Running TestSuite [" + suite.getName() + "], runType = " + suite.getRunType() ) );
			for( TestCase testCase : suite.getTestCaseList() )
			{
				runTestCase( ( WsdlTestCase )testCase );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			testSuiteCount++ ;
		}

		
	}

	/**
	 * Runs the specified TestCase
	 * 
	 * @param testCase
	 *           the testcase to run
	 * @param context
	 */

	protected void runTestCase( WsdlTestCase testCase )
	{
		try
		{
			log.info( "Running TestCase [" + testCase.getName() + "]" );
			for( SecurityTest securityTest : testCase.getSecurityTestList() )
			{
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
		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( securityTest );
		testRunner.start( false );
//		log.info( "\n" + securityTest.getSecurityTestLog().getMessages() );
//		log.info( "SecurityTest [" + securityTest.getName() + "] finished  in " + ( testRunner.getTimeTaken() )
//				+ "ms" );
	}

	/**
	 * Sets the testcase to run
	 * 
	 * @param testCase
	 *           the testcase to run
	 */

	public void setTestCase( String testCase )
	{
		this.testCase = testCase;
	}

	/**
	 * Sets the TestSuite to run. If not set all TestSuites in the specified
	 * project file are run
	 * 
	 * @param testSuite
	 *           the testSuite to run.
	 */

	public void setTestSuite( String testSuite )
	{
		this.testSuite = testSuite;
	}

	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		log.info( "Running soapUI testcase [" + testRunner.getTestCase().getName() + "]" );
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep currentStep )
	{
		super.beforeStep( testRunner, runContext, currentStep );

		if( currentStep != null )
			log.info( "running step [" + currentStep.getName() + "]" );
	}

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
		super.afterStep( testRunner, runContext, result );
		TestStep currentStep = runContext.getCurrentStep();

		if( currentStep instanceof Assertable )
		{
			Assertable requestStep = ( Assertable )currentStep;
			for( int c = 0; c < requestStep.getAssertionCount(); c++ )
			{
				TestAssertion assertion = requestStep.getAssertionAt( c );
				log.info( "Assertion [" + assertion.getName() + "] has status " + assertion.getStatus() );
				if( assertion.getStatus() == AssertionStatus.FAILED )
				{
					for( AssertionError error : assertion.getErrors() )
						log.error( "ASSERTION FAILED -> " + error.getMessage() );

					assertions.add( assertion );
					assertionResults.put( assertion, ( WsdlTestStepResult )result );
				}

				testAssertionCount++ ;
			}
		}

		String countPropertyName = currentStep.getName() + " run count";
		Long count = ( Long )runContext.getProperty( countPropertyName );
		if( count == null )
		{
			count = new Long( 0 );
		}

		runContext.setProperty( countPropertyName, new Long( count.longValue() + 1 ) );

		if( result.getStatus() == TestStepStatus.FAILED || exportAll )
		{
			try
			{
				String exportSeparator = System.getProperty( SOAPUI_EXPORT_SEPARATOR, "-" );

				TestCase tc = currentStep.getTestCase();
				String nameBase = StringUtils.createFileName( tc.getTestSuite().getName(), '_' ) + exportSeparator
						+ StringUtils.createFileName( tc.getName(), '_' ) + exportSeparator
						+ StringUtils.createFileName( currentStep.getName(), '_' ) + "-" + count.longValue() + "-"
						+ result.getStatus();

				WsdlTestCaseRunner callingTestCaseRunner = ( WsdlTestCaseRunner )runContext
						.getProperty( "#CallingTestCaseRunner#" );

				if( callingTestCaseRunner != null )
				{
					WsdlTestCase ctc = callingTestCaseRunner.getTestCase();
					WsdlRunTestCaseTestStep runTestCaseTestStep = ( WsdlRunTestCaseTestStep )runContext
							.getProperty( "#CallingRunTestCaseStep#" );

					nameBase = StringUtils.createFileName( ctc.getTestSuite().getName(), '_' ) + exportSeparator
							+ StringUtils.createFileName( ctc.getName(), '_' ) + exportSeparator
							+ StringUtils.createFileName( runTestCaseTestStep.getName(), '_' ) + exportSeparator
							+ StringUtils.createFileName( tc.getTestSuite().getName(), '_' ) + exportSeparator
							+ StringUtils.createFileName( tc.getName(), '_' ) + exportSeparator
							+ StringUtils.createFileName( currentStep.getName(), '_' ) + "-" + count.longValue() + "-"
							+ result.getStatus();
				}

				String absoluteOutputFolder = getAbsoluteOutputFolder( ModelSupport.getModelItemProject( tc ) );
				String fileName = absoluteOutputFolder + File.separator + nameBase + ".txt";

				if( result.getStatus() == TestStepStatus.FAILED )
					log.error( currentStep.getName() + " failed, exporting to [" + fileName + "]" );

				new File( fileName ).getParentFile().mkdirs();

				PrintWriter writer = new PrintWriter( fileName );
				result.writeTo( writer );
				writer.close();

				// write attachments
				if( result instanceof MessageExchange )
				{
					Attachment[] attachments = ( ( MessageExchange )result ).getResponseAttachments();
					if( attachments != null && attachments.length > 0 )
					{
						for( int c = 0; c < attachments.length; c++ )
						{
							fileName = nameBase + "-attachment-" + ( c + 1 ) + ".";

							Attachment attachment = attachments[c];
							String contentType = attachment.getContentType();
							if( !"application/octet-stream".equals( contentType ) && contentType != null
									&& contentType.indexOf( '/' ) != -1 )
							{
								fileName += contentType.substring( contentType.lastIndexOf( '/' ) + 1 );
							}
							else
							{
								fileName += "dat";
							}

							fileName = absoluteOutputFolder + File.separator + fileName;

							FileOutputStream outFile = new FileOutputStream( fileName );
							Tools.writeAll( outFile, attachment.getInputStream() );
							outFile.close();
						}
					}
				}

				exportCount++ ;
			}
			catch( Exception e )
			{
				log.error( "Error saving failed result: " + e, e );
			}
		}

		testStepCount++ ;
	}

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		log.info( "Finished running soapUI testcase [" + testRunner.getTestCase().getName() + "], time taken: "
				+ testRunner.getTimeTaken() + "ms, status: " + testRunner.getStatus() );

		if( testRunner.getStatus() == Status.FAILED )
		{
			failedTests.add( testRunner.getTestCase() );
		}

		testCaseCount++ ;
	}

	private class InternalProjectRunListener extends ProjectRunListenerAdapter
	{
		public void afterTestSuite( ProjectRunner projectRunner, ProjectRunContext runContext, TestSuiteRunner testRunner )
		{
			testSuiteCount++ ;
		}
	}
}
