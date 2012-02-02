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

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;

import com.eviware.soapui.config.TestCaseRunLogDocumentConfig;
import com.eviware.soapui.config.TestCaseRunLogDocumentConfig.TestCaseRunLog;
import com.eviware.soapui.config.TestCaseRunLogDocumentConfig.TestCaseRunLog.TestCaseRunLogTestStep;
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

		HttpRequestBase httpMethod = ( HttpRequestBase )runContext.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
		if( httpMethod != null )
		{
			testCaseRunLogTestStep.setEndpoint( httpMethod.getURI().toString() );

			Object metricsObj = httpMethod.getParams().getParameter( ExtendedHttpMethod.HTTP_METRICS );
			if( metricsObj instanceof SoapUIMetrics )
			{
				SoapUIMetrics httpMetrics = ( SoapUIMetrics )metricsObj;
				testCaseRunLogTestStep.setTimestamp( httpMetrics.getFormattedTimeStamp() );
				testCaseRunLogTestStep.setHttpStatus( String.valueOf( httpMetrics.getHttpStatus() ) );
				testCaseRunLogTestStep.setContentLength( String.valueOf( httpMetrics.getContentLength() ) );
				testCaseRunLogTestStep.setReadTime( String.valueOf( httpMetrics.getReadTimer().getDuration() ) );
				testCaseRunLogTestStep.setTotalTime( String.valueOf( httpMetrics.getTotalTimer().getDuration() ) );
				testCaseRunLogTestStep.setDnsTime( String.valueOf( httpMetrics.getDNSTimer().getDuration() ) );
				testCaseRunLogTestStep.setConnectTime( String.valueOf( httpMetrics.getConnectTimer().getDuration() ) );
				testCaseRunLogTestStep.setTimeToFirstByte( String.valueOf( httpMetrics.getTimeToFirstByteTimer()
						.getDuration() ) );
			}
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
}
