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
package com.eviware.soapui.impl.wsdl.panels.project;

import java.util.Date;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.testcase.JTestRunLog;
import com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLogTestRunListener;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class TestRunLogTestSuiteRunListener extends TestRunLogTestRunListener implements TestSuiteRunListener
{
	public TestRunLogTestSuiteRunListener( JTestRunLog runLog, boolean clearOnRun )
	{
		super( runLog, clearOnRun );
	}

	public void beforeRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestSuite() ) )
			return;

		if( clearOnRun )
			runLog.clear();

		String testSuiteName = testRunner.getTestRunnable().getName();
		runLog.addBoldText( "TestSuite [" + testSuiteName + "] started at " + dateFormat.format( new Date() ) );
		runLog.setStepIndex( 0 );
	}

	public void afterRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestSuite() ) )
			return;

		WsdlTestSuiteRunner wsdlRunner = ( WsdlTestSuiteRunner )testRunner;

		String testSuiteName = testRunner.getTestRunnable().getName();
		if( testRunner.getStatus() == TestCaseRunner.Status.CANCELED )
			runLog.addText( "TestSuite [" + testSuiteName + "] canceled [" + testRunner.getReason() + "], time taken = "
					+ wsdlRunner.getTimeTaken() );

		else if( testRunner.getStatus() == TestCaseRunner.Status.FAILED )
		{
			String msg = wsdlRunner.getReason();
			if( wsdlRunner.getError() != null )
			{
				if( msg != null )
					msg += ":";

				msg += wsdlRunner.getError();
			}

			runLog.addText( "TestSuite [" + testSuiteName + "] failed [" + msg + "], time taken = "
					+ wsdlRunner.getTimeTaken() );
		}
		else
			runLog.addText( "TestSuite [" + testSuiteName + "] finished with status [" + testRunner.getStatus()
					+ "], time taken = " + wsdlRunner.getTimeTaken() );
	}

	public void beforeTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCase testCase )
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestSuite() ) )
			return;

		testCase.addTestRunListener( this );
	}

	public void afterTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCaseRunner testCaseRunner )
	{
		testCaseRunner.getTestCase().removeTestRunListener( this );
	}
}