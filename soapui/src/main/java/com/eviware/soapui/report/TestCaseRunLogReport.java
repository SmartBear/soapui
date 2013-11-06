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
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

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
	private static final String TIMEOUT_MESSAGE = "The TestStep was interrupted due to a timeout";

	private static final String REPORT_FILE_NAME = "test_case_run_log_report.xml";

	private TestCaseRunLogDocumentConfig testCaseRunLogDocumentConfig;
	private TestCaseRunLog testCaseRunLog;
	private final String outputFolder;
	private long startTime;

	private final static Logger log = Logger.getLogger( TestCaseRunLogReport.class );

	private boolean testRunHasFinished = false;

	private TestStep currentTestStep;
	private TestCaseRunLogTestStep currentTestCaseRunLogTestStepConfig;

	public TestCaseRunLogReport( String outputFolder )
	{
		this.outputFolder = outputFolder;
		testCaseRunLogDocumentConfig = TestCaseRunLogDocumentConfig.Factory.newInstance();
		testCaseRunLog = testCaseRunLogDocumentConfig.addNewTestCaseRunLog();

		initShutDownHook();
	}

	@Override
	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
	{
		currentTestStep = testStep;
		currentTestCaseRunLogTestStepConfig = testCaseRunLog.addNewTestCaseRunLogTestStep();
	}

	@Override
	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
		currentTestCaseRunLogTestStepConfig.setName( result.getTestStep().getName() );
		currentTestCaseRunLogTestStepConfig.setTimeTaken( Long.toString( result.getTimeTaken() ) );
		currentTestCaseRunLogTestStepConfig.setStatus( result.getStatus().toString() );
		currentTestCaseRunLogTestStepConfig.setMessageArray( result.getMessages() );
		currentTestCaseRunLogTestStepConfig.setTimestamp( SoapUIMetrics.formatTimestamp( result.getTimeStamp() ) );

		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )runContext
				.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		if( httpMethod != null && result.getTestStep() instanceof HttpRequestTestStep )
		{
			currentTestCaseRunLogTestStepConfig.setEndpoint( httpMethod.getURI().toString() );

			SoapUIMetrics metrics = httpMethod.getMetrics();
			currentTestCaseRunLogTestStepConfig.setTimestamp( metrics.getFormattedTimeStamp() );
			currentTestCaseRunLogTestStepConfig.setHttpStatus( String.valueOf( metrics.getHttpStatus() ) );
			currentTestCaseRunLogTestStepConfig.setContentLength( String.valueOf( metrics.getContentLength() ) );
			currentTestCaseRunLogTestStepConfig.setReadTime( String.valueOf( metrics.getReadTimer().getDuration() ) );
			currentTestCaseRunLogTestStepConfig.setTotalTime( String.valueOf( metrics.getTotalTimer().getDuration() ) );
			currentTestCaseRunLogTestStepConfig.setDnsTime( String.valueOf( metrics.getDNSTimer().getDuration() ) );
			currentTestCaseRunLogTestStepConfig.setConnectTime( String.valueOf( metrics.getConnectTimer().getDuration() ) );
			currentTestCaseRunLogTestStepConfig.setTimeToFirstByte( String.valueOf( metrics.getTimeToFirstByteTimer()
					.getDuration() ) );
			currentTestCaseRunLogTestStepConfig.setHttpMethod( metrics.getHttpMethod() );
			currentTestCaseRunLogTestStepConfig.setIpAddress( metrics.getIpAddress() );
			//currentTestCaseRunLogTestStepConfig.setPort( metrics.getPort() );
		}

		Throwable error = result.getError();
		if( error != null )
		{
			currentTestCaseRunLogTestStepConfig.setErrorMessage( error.getMessage() );
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
			@Override
			public void run()
			{
				if( !testRunHasFinished )
				{
					if( currentTestCaseRunLogTestStepConfig != null )
					{
						log.warn( "Step [" + currentTestStep.getName() + "] was interrupted due to a timeout" );
						currentTestCaseRunLogTestStepConfig.setName( currentTestStep.getName() );
						currentTestCaseRunLogTestStepConfig.setStatus( TIMEOUT_STATUS );
						currentTestCaseRunLogTestStepConfig.setMessageArray( new String[] { TIMEOUT_MESSAGE } );
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
