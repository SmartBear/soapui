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
package com.eviware.soapui.impl.wsdl.panels.testcase;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;

public class TestRunLogTestRunListener extends TestRunListenerAdapter
{
	protected SimpleDateFormat dateFormat;
	protected final JTestRunLog runLog;
	protected final boolean clearOnRun;

	public TestRunLogTestRunListener( JTestRunLog runLog, boolean clearOnRun )
	{
		this.runLog = runLog;
		this.clearOnRun = clearOnRun;
		dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
	}

	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestCase() ) )
			return;

		if( clearOnRun )
			runLog.clear();

		String testCaseName = testRunner.getTestCase().getName();
		runLog.addBoldText( "TestCase [" + testCaseName + "] started at " + dateFormat.format( new Date() ) );
		runLog.setStepIndex( 0 );
	}

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestCase() ) )
			return;

		WsdlTestCaseRunner wsdlRunner = ( WsdlTestCaseRunner )testRunner;

		String testCaseName = testRunner.getTestCase().getName();
		if( testRunner.getStatus() == TestCaseRunner.Status.CANCELED )
			runLog.addText( "TestCase [" + testCaseName + "] canceled [" + testRunner.getReason() + "], time taken = "
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

			runLog.addText( "TestCase [" + testCaseName + "] failed [" + msg + "], time taken = "
					+ wsdlRunner.getTimeTaken() );
		}
		else
			runLog.addText( "TestCase [" + testCaseName + "] finished with status [" + testRunner.getStatus()
					+ "], time taken = " + wsdlRunner.getTimeTaken() );
	}

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult stepResult )
	{
		if( SoapUI.getTestMonitor().hasRunningLoadTest( testRunner.getTestCase() ) )
			return;

		runLog.addTestStepResult( stepResult );
	}
}