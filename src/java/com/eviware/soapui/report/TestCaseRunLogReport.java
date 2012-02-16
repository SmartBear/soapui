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
import java.io.IOException;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestCaseRunLogDocumentConfig;
import com.eviware.soapui.config.TestCaseRunLogDocumentConfig.TestCaseRunLog;
import com.eviware.soapui.config.TestCaseRunLogDocumentConfig.TestCaseRunLog.TestCaseRunLogTestStep;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.sun.istack.internal.Nullable;

/**
 * @author Erik R. Yverling
 * 
 *         Creates a report from the test case run log after a test has been
 *         run.
 */
public class TestCaseRunLogReport extends TestRunListenerAdapter
{
	private static final String TEST_CASE_RUN_WAS_TERMINATED_UNEXPECTEDLY_MESSAGE = "TestCase run was terminated unexpectedly";
	private static final String TIMEOUT_STATUS = "TIMEOUT";
	private static final String TIMEOUT_MESSAGE = "The TestStep was interupted due to a timeout";

	private static final String REPORT_FILE_NAME = "test_case_run_log_report.xml";

	private TestCaseRunLogDocumentConfig testCaseRunLogDocumentConfig;
	private TestCaseRunLog testCaseRunLog;
	private final String outputFolder;
	private long startTime;

	private final static Logger log = Logger.getLogger( TestCaseRunLogReport.class );

	private boolean testRunHasFinished = false;

	private TestStepResult currentTestStepResult;
	private TestCaseRunLogTestStep currentTestCaseRunLogTestStep;

	public TestCaseRunLogReport( String outputFolder )
	{
		this.outputFolder = outputFolder;
		testCaseRunLogDocumentConfig = TestCaseRunLogDocumentConfig.Factory.newInstance();
		testCaseRunLog = testCaseRunLogDocumentConfig.addNewTestCaseRunLog();

		initShutDownHook();
	}

	@Override
	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
		currentTestCaseRunLogTestStep = testCaseRunLog.addNewTestCaseRunLogTestStep();
		currentTestStepResult = result;

		currentTestCaseRunLogTestStep.setName( currentTestStepResult.getTestStep().getName() );
		currentTestCaseRunLogTestStep.setTimeTaken( Long.toString( currentTestStepResult.getTimeTaken() ) );
		currentTestCaseRunLogTestStep.setStatus( currentTestStepResult.getStatus().toString() );
		currentTestCaseRunLogTestStep.setMessageArray( currentTestStepResult.getMessages() );
		currentTestCaseRunLogTestStep
				.setTimestamp( SoapUIMetrics.formatTimestamp( currentTestStepResult.getTimeStamp() ) );

		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )runContext
				.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		if( httpMethod != null && currentTestStepResult.getTestStep() instanceof HttpRequestTestStep )
		{
			currentTestCaseRunLogTestStep.setEndpoint( httpMethod.getURI().toString() );

			SoapUIMetrics metrics = httpMethod.getMetrics();
			currentTestCaseRunLogTestStep.setTimestamp( metrics.getFormattedTimeStamp() );
			currentTestCaseRunLogTestStep.setHttpStatus( String.valueOf( metrics.getHttpStatus() ) );
			currentTestCaseRunLogTestStep.setContentLength( String.valueOf( metrics.getContentLength() ) );
			currentTestCaseRunLogTestStep.setReadTime( String.valueOf( metrics.getReadTimer().getDuration() ) );
			currentTestCaseRunLogTestStep.setTotalTime( String.valueOf( metrics.getTotalTimer().getDuration() ) );
			currentTestCaseRunLogTestStep.setDnsTime( String.valueOf( metrics.getDNSTimer().getDuration() ) );
			currentTestCaseRunLogTestStep.setConnectTime( String.valueOf( metrics.getConnectTimer().getDuration() ) );
			currentTestCaseRunLogTestStep.setTimeToFirstByte( String.valueOf( metrics.getTimeToFirstByteTimer()
					.getDuration() ) );
			currentTestCaseRunLogTestStep.setHttpMethod( metrics.getHttpMethod() );
			currentTestCaseRunLogTestStep.setIpAddress( metrics.getIpAddress() );
		}

		Throwable error = result.getError();
		if( error != null )
		{
			currentTestCaseRunLogTestStep.setErrorMessage( error.getMessage() );
		}
	}

	@Override
	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		testCaseRunLog.setTestCase( ( testRunner.getTestCase().getName() ) );
		testCaseRunLog.setTimeTaken( Long.toString( testRunner.getTimeTaken() ) );
		testCaseRunLog.setStatus( testRunner.getStatus().toString() );
		testCaseRunLog.setTimeStamp( SoapUIMetrics.formatTimestamp( startTime ) );

		testRunHasFinished = true;

		saveReportToFile();
	}

	@Override
	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		super.beforeRun( testRunner, runContext );

		startTime = System.currentTimeMillis();
	}

	private void initShutDownHook()
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			public void run()
			{
				if( !testRunHasFinished )
				{
					if( currentTestCaseRunLogTestStep != null )
					{
						log.warn( "Step [" + currentTestStepResult.getTestStep().getName()
								+ "] was interupted due to a timeout" );
						currentTestCaseRunLogTestStep.setName( currentTestStepResult.getTestStep().getName() );
						currentTestCaseRunLogTestStep.setStatus( TIMEOUT_STATUS );
						currentTestCaseRunLogTestStep.setMessageArray( new String[] { TIMEOUT_MESSAGE } );
					}
					log.warn( TEST_CASE_RUN_WAS_TERMINATED_UNEXPECTEDLY_MESSAGE );
					saveReportToFile();
				}
			}
		} );
	}

	private void saveReportToFile()
	{
		final File newFile = new File( outputFolder, REPORT_FILE_NAME );
		try
		{
			testCaseRunLogDocumentConfig.save( newFile );
		}
		catch( IOException e )
		{
			log.error( "Could not write " + REPORT_FILE_NAME + " to disk" );
			SoapUI.logError( e );
		}
	}

}
