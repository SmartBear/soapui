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

/**
 * @author Erik R. Yverling
 * 
 *         Creates a report from the test case run log after a test has been
 *         run.
 */
public class TestCaseRunLogReport extends TestRunListenerAdapter
{
	private static final String REPORT_FILE_NAME = "test_case_run_log_report.xml";
	private TestCaseRunLogDocumentConfig testCaseRunLogDocumentConfig;
	private Logger log = Logger.getLogger( TestCaseRunLogReport.class );
	private TestCaseRunLog testCaseRunLog;
	private final String outputFolder;
	private long startTime;

	public TestCaseRunLogReport( String outputFolder )
	{
		this.outputFolder = outputFolder;
		testCaseRunLogDocumentConfig = TestCaseRunLogDocumentConfig.Factory.newInstance();
		testCaseRunLog = testCaseRunLogDocumentConfig.addNewTestCaseRunLog();
	}

	@Override
	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
		TestCaseRunLogTestStep testCaseRunLogTestStep = testCaseRunLog.addNewTestCaseRunLogTestStep();
		testCaseRunLogTestStep.setName( result.getTestStep().getName() );
		testCaseRunLogTestStep.setTimeTaken( Long.toString( result.getTimeTaken() ) );
		testCaseRunLogTestStep.setStatus( result.getStatus().toString() );
		testCaseRunLogTestStep.setMessageArray( result.getMessages() );
		testCaseRunLogTestStep.setTimestamp( SoapUIMetrics.formatTimestamp( result.getTimeStamp() ) );

		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )runContext
				.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		if( httpMethod != null && result.getTestStep() instanceof HttpRequestTestStep )
		{
			testCaseRunLogTestStep.setEndpoint( httpMethod.getURI().toString() );

			SoapUIMetrics metrics = ( SoapUIMetrics )httpMethod.getMetrics();
			testCaseRunLogTestStep.setTimestamp( metrics.getFormattedTimeStamp() );
			testCaseRunLogTestStep.setHttpStatus( String.valueOf( metrics.getHttpStatus() ) );
			testCaseRunLogTestStep.setContentLength( String.valueOf( metrics.getContentLength() ) );
			testCaseRunLogTestStep.setReadTime( String.valueOf( metrics.getReadTimer().getDuration() ) );
			testCaseRunLogTestStep.setTotalTime( String.valueOf( metrics.getTotalTimer().getDuration() ) );
			testCaseRunLogTestStep.setDnsTime( String.valueOf( metrics.getDNSTimer().getDuration() ) );
			testCaseRunLogTestStep.setConnectTime( String.valueOf( metrics.getConnectTimer().getDuration() ) );
			testCaseRunLogTestStep.setTimeToFirstByte( String.valueOf( metrics.getTimeToFirstByteTimer().getDuration() ) );
		}

		Throwable error = result.getError();
		if( error != null )
		{
			testCaseRunLogTestStep.setErrorMessage( error.getMessage() );
		}
	}

	@Override
	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		testCaseRunLog.setTestCase( ( testRunner.getTestCase().getName() ) );
		testCaseRunLog.setTimeTaken( Long.toString( testRunner.getTimeTaken() ) );
		testCaseRunLog.setStatus( testRunner.getStatus().toString() );
		testCaseRunLog.setTimeStamp( SoapUIMetrics.formatTimestamp( startTime ) );

		final File newFile = new File( outputFolder, REPORT_FILE_NAME );

		try
		{
			testCaseRunLogDocumentConfig.save( newFile );
		}
		catch( IOException e )
		{
			log.error( "Could not write " + REPORT_FILE_NAME + " to disk " );
		}
	}

	@Override
	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		super.beforeRun( testRunner, runContext );

		startTime = System.currentTimeMillis();
	}
}
