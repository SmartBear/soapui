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

package com.eviware.soapui.tools;

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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;
import com.eviware.soapui.report.JUnitReportCollector;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;

/**
 * Standalone test-runner used from maven-plugin, can also be used from
 * command-line (see xdocs) or directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other
 * desired properties before calling run
 * </p>
 * 
 * @author Ole.Matzura
 */

public class SoapUITestCaseRunner extends AbstractSoapUITestRunner
{
	public static final String TITLE = "soapUI " + SoapUI.SOAPUI_VERSION + " TestCase Runner";

	private String testSuite;
	private String testCase;
	private List<TestAssertion> assertions = new ArrayList<TestAssertion>();
	private Map<TestAssertion, WsdlTestStepResult> assertionResults = new HashMap<TestAssertion, WsdlTestStepResult>();
	private List<TestRunner> runningTests = new ArrayList<TestRunner>();
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
	private JUnitReportCollector reportCollector;
	private WsdlProject project;

	private String projectPassword;

	private boolean saveAfterRun;

	public SoapUITestCaseRunner()
	{
		super( SoapUITestCaseRunner.TITLE );
	}

	public SoapUITestCaseRunner( String title )
	{
		super( title );
	}

	/**
	 * Runs the tests in the specified soapUI project file, see soapUI xdocs for
	 * details.
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main( String[] args ) throws Exception
	{
		System.exit( new SoapUITestCaseRunner().runFromCommandLine( args ));
	}

	protected boolean processCommandLine( CommandLine cmd )
	{
		if( cmd.hasOption( "e" ) )
			setEndpoint( cmd.getOptionValue( "e" ) );

		if( cmd.hasOption( "s" ) )
			setTestSuite( getCommandLineOptionSubstSpace( cmd, "s" ) );

		if( cmd.hasOption( "c" ) )
			setTestCase( getCommandLineOptionSubstSpace( cmd, "c" ) );

		if( cmd.hasOption( "u" ) )
			setUsername( cmd.getOptionValue( "u" ) );

		if( cmd.hasOption( "p" ) )
			setPassword( cmd.getOptionValue( "p" ) );

		if( cmd.hasOption( "w" ) )
			setWssPasswordType( cmd.getOptionValue( "w" ) );

		if( cmd.hasOption( "d" ) )
			setDomain( cmd.getOptionValue( "d" ) );

		if( cmd.hasOption( "h" ) )
			setHost( cmd.getOptionValue( "h" ) );

		if( cmd.hasOption( "f" ) )
			setOutputFolder( getCommandLineOptionSubstSpace( cmd, "f" ) );

		if( cmd.hasOption( "t" ) )
			setSettingsFile( getCommandLineOptionSubstSpace( cmd, "t" ) );

		if( cmd.hasOption( "x" ) )
		{
			setProjectPassword( cmd.getOptionValue( "x" ) );
		}

		if( cmd.hasOption( "v" ) )
		{
			setSoapUISettingsPassword( cmd.getOptionValue( "v" ) );
		}

		if( cmd.hasOption( "D" ) )
		{
			setSystemProperties( cmd.getOptionValues( "D" ) );
		}

		if( cmd.hasOption( "G" ) )
		{
			setGlobalProperties( cmd.getOptionValues( "G" ) );
		}

		if( cmd.hasOption( "P" ) )
		{
			setProjectProperties( cmd.getOptionValues( "P" ) );
		}

		setIgnoreError( cmd.hasOption( "I" ) );
		setEnableUI( cmd.hasOption( "i" ) );
		setPrintReport( cmd.hasOption( "r" ) );
		setExportAll( cmd.hasOption( "a" ) );
		setJUnitReport( cmd.hasOption( "j" ) );
		setSaveAfterRun( cmd.hasOption( "S" ) );

		return true;
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
		SoapUIOptions options = new SoapUIOptions( "testrunner" );
		options.addOption( "e", true, "Sets the endpoint" );
		options.addOption( "s", true, "Sets the testsuite" );
		options.addOption( "c", true, "Sets the testcase" );
		options.addOption( "u", true, "Sets the username" );
		options.addOption( "p", true, "Sets the password" );
		options.addOption( "w", true, "Sets the WSS password type, either 'Text' or 'Digest'" );
		options.addOption( "i", false, "Enables Swing UI for scripts" );
		options.addOption( "d", true, "Sets the domain" );
		options.addOption( "h", true, "Sets the host" );
		options.addOption( "r", false, "Prints a small summary report" );
		options.addOption( "f", true, "Sets the output folder to export results to" );
		options.addOption( "j", false, "Sets the output to include JUnit XML reports" );
		options.addOption( "a", false, "Turns on exporting of all results" );
		options.addOption( "t", true, "Sets the soapui-settings.xml file to use" );
		options.addOption( "x", true, "Sets project password for decryption if project is encrypted" );
		options.addOption( "v", true, "Sets password for soapui-settings.xml file" );
		options.addOption( "D", true, "Sets system property with name=value" );
		options.addOption( "G", true, "Sets global property with name=value" );
		options.addOption( "P", true, "Sets or overrides project property with name=value" );
		options.addOption( "I", false, "Do not stop if error occurs, ignore them" );
		options.addOption( "S", false, "Saves the project after running the tests" );

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
			reportCollector = new JUnitReportCollector();
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

		// project = new WsdlProject(projectFile, getProjectPassword());
		project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( projectFile,
				getProjectPassword() );

		if( project.isDisabled() )
			throw new Exception( "Failed to load soapUI project file [" + projectFile + "]" );

		initProject();
		ensureOutputFolder( project );

		log.info( "Running soapUI tests in project [" + project.getName() + "]" );

		long startTime = System.nanoTime();

		// start by listening to all testcases.. (since one testcase can call
		// another)
		for( int c = 0; c < project.getTestSuiteCount(); c++ )
		{
			TestSuite suite = project.getTestSuiteAt( c );
			for( int i = 0; i < suite.getTestCaseCount(); i++ )
			{
				TestCase tc = suite.getTestCaseAt( i );
				addListeners( tc );
			}
		}

		// now run tests..
		for( int c = 0; c < project.getTestSuiteCount(); c++ )
		{
			WsdlTestSuite ts = project.getTestSuiteAt( c );
			if( !ts.isDisabled() && ( testSuite == null || ts.getName().equalsIgnoreCase( testSuite ) ) )
			{
				runSuite( ts );
				testSuiteCount++ ;

				// wait for tests to finish if running in parallell mode
				while( !runningTests.isEmpty() )
				{
					StringBuffer buf = new StringBuffer();
					TestRunner[] runners = runningTests.toArray( new TestRunner[runningTests.size()] );

					for( int i = 0; i < runners.length; i++ )
					{
						TestRunner runner = runners[i];
						if( runner.getStatus() != TestRunner.Status.RUNNING )
						{
							runningTests.remove( runner );
							continue;
						}

						buf.append( "\r\n- " );
						buf.append( runner.getTestCase().getTestSuite().getName() ).append( ':' );
						buf.append( runner.getTestCase().getName() ).append( ':' );
						buf.append( runner.getStatus() ).append( ':' );

						TestStep currentStep = runner.getRunContext().getCurrentStep();
						if( currentStep != null )
						{
							buf.append( currentStep.getName() );
						}
						else
						{
							buf.append( "currentStep is null" );
						}

						if( System.getProperty( "soapui.dumpstacktrace", "false" ).equals( "true" ) )
						{
							Thread thread = ( ( WsdlTestCaseRunner )runner ).getThread();
							if( thread != null )
							{
								StackTraceElement[] trace = thread.getStackTrace();
								for( int y = 0; y < trace.length; y++ )
									buf.append( "\tat " + trace[y] );
							}
						}
					}

					if( runningTests.size() > 0 )
					{
						log.info( "Waiting for " + runners.length + " tests to finish: " + buf.toString() );
						Thread.sleep( 5000 );
					}
				}
			}
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

	protected void initProject() throws Exception
	{
		initProjectProperties( project );
	}

	protected void exportReports( WsdlProject project ) throws Exception
	{
		if( junitReport )
		{
			exportJUnitReports( reportCollector, getAbsoluteOutputFolder( project ) );
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

	public void exportJUnitReports( JUnitReportCollector collector, String folder ) throws Exception
	{
		collector.saveReports( folder == null ? "" : folder );
	}

	public void printReport( long timeTaken )
	{
		System.out.println();
		System.out.println( "SoapUI " + SoapUI.SOAPUI_VERSION + " TestCaseRunner Summary" );
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

	public void runSuite( WsdlTestSuite suite )
	{
		log.info( ( "Running soapUI suite [" + suite.getName() + "], runType = " + suite.getRunType() ) );
		PropertyExpansionContext context = new DefaultPropertyExpansionContext( suite );
		long start = System.currentTimeMillis();

		try
		{
			suite.runSetupScript( context );
			for( int c = 0; c < suite.getTestCaseCount(); c++ )
			{
				WsdlTestCase tc = suite.getTestCaseAt( c );

				String name = tc.getName();
				if( testCase == null || name.equalsIgnoreCase( testCase ) )
				{
					if( !tc.isDisabled() )
					{
						runTestCase( tc, context );
					}
					else
					{
						log.info( "Skipping disabled testcase [" + name + "]" );
					}
				}
				else
				{
					log.info( "Skipping testcase [" + name + "], filter is [" + testCase + "]" );
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				suite.runTearDownScript( context );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		log.info( "soapUI suite [" + suite.getName() + "] finished in " + ( System.currentTimeMillis() - start ) + "ms" );
	}

	/**
	 * Runs the specified TestCase
	 * 
	 * @param testCase
	 *           the testcase to run
	 * @param context
	 */

	private void runTestCase( TestCase testCase, PropertyExpansionContext context )
	{
		TestRunner testRunner = testCase.run( context.getProperties(),
				testCase.getTestSuite().getRunType() == TestSuiteRunType.PARALLEL );
		if( testRunner.getStatus() == TestRunner.Status.RUNNING )
			runningTests.add( testRunner );
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

	public void beforeRun( TestRunner testRunner, TestRunContext runContext )
	{
		log.info( "Running soapUI testcase [" + testRunner.getTestCase().getName() + "]" );
	}

	public void beforeStep( TestRunner testRunner, TestRunContext runContext )
	{
		super.beforeStep( testRunner, runContext );

		TestStep currentStep = runContext.getCurrentStep();
		if( currentStep != null )
			log.info( "running step [" + currentStep.getName() + "]" );
	}

	public void afterStep( TestRunner testRunner, TestRunContext runContext, TestStepResult result )
	{
		super.afterStep( testRunner, runContext, result );
		TestStep currentStep = result.getTestStep();

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
				TestCase tc = currentStep.getTestCase();
				String nameBase = StringUtils.createFileName( tc.getTestSuite().getName(), '_' ) + "-"
						+ StringUtils.createFileName( tc.getName(), '_' ) + "-"
						+ StringUtils.createFileName( currentStep.getName(), '_' ) + "-" + count.longValue() + "-"
						+ result.getStatus();

				String absoluteOutputFolder = getAbsoluteOutputFolder( project );
				String fileName = absoluteOutputFolder + File.separator + nameBase + ".txt";

				if( result.getStatus() == TestStepStatus.FAILED )
					log.error( currentStep.getName() + " failed, exporting to [" + fileName + "]" );

				PrintWriter writer = new PrintWriter( fileName );
				result.writeTo( writer );
				writer.close();

				// write attachments
				if( result instanceof WsdlTestRequestStepResult )
				{
					Attachment[] attachments = ( ( WsdlTestRequestStepResult )result ).getResponseAttachments();
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

	public void afterRun( TestRunner testRunner, TestRunContext runContext )
	{
		log.info( "Finished running soapUI testcase [" + testRunner.getTestCase().getName() + "], time taken: "
				+ testRunner.getTimeTaken() + "ms, status: " + testRunner.getStatus() );

		if( testRunner.getStatus() == Status.FAILED )
		{
			failedTests.add( testRunner.getTestCase() );
		}

		runningTests.remove( testRunner );

		testCaseCount++ ;
	}

	protected WsdlProject getProject()
	{
		return project;
	}

}