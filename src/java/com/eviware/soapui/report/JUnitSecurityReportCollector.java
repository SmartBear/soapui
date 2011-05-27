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

import com.eviware.soapui.junit.Mutation;
import com.eviware.soapui.junit.SecurityTest;
import com.eviware.soapui.junit.Testcase;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.SecurityTestRunListener;

/**
 * Collects Security Test results and creates JUnitReports
 * 
 * @author nebojsa.tasic
 */

public class JUnitSecurityReportCollector extends JUnitReportCollector implements SecurityTestRunListener
{

	@Override
	public void afterOriginalStep( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityTestStepResult result )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{

		TestCase testCase = testRunner.getTestCase();
		TestSuite testSuite = testCase.getTestSuite();
		//		if( !reports.containsKey( testSuite.getName() ) )
		{
			JUnitReport report = new JUnitReport();
			report.setTestSuiteName( testSuite.getProject().getName() + "." + testSuite.getName() );
			Testcase secTestCase = report.addTestCase( testCase.getName(), 0 );
			SecurityTest juSecurityTest = report.addSecurityTest( secTestCase, ( ( SecurityTestRunner )testRunner )
					.getSecurityTest().getName() );

			for( TestStep ts : ( ( SecurityTestRunner )testRunner ).getSecurityTest().getSecurityTestStepResultMap()
					.keySet() )
			{
				SecurityTestStepResult secuTestStepResult = ( ( SecurityTestRunner )testRunner ).getSecurityTest()
						.getSecurityTestStepResultMap().get( ts );
				for( SecurityScanResult scanResult : secuTestStepResult.getSecurityScanResultList() )
				{

					com.eviware.soapui.junit.SecurityScan juSecurityScan = report.addSecurityScan( juSecurityTest,
							scanResult.getSecurityScanName() );
					int i = 0;
					for( SecurityScanRequestResult scanRequestResult : scanResult.getSecurityRequestResultList() )
					{
						Mutation mutation = juSecurityScan.addNewMutation();
						mutation.addMessage( scanRequestResult.getChangedParamsInfo( i ) );
						for( String message : scanRequestResult.getMessages() )
						{
							mutation.addMessage( message );
						}
						for( String name : scanRequestResult.getMessageExchange().getProperties().keySet() )
						{
							if( scanRequestResult.getMessageExchange().getProperties().get( name ) != null )
								;
							mutation.addMessage( name + " = "
									+ scanRequestResult.getMessageExchange().getProperties().get( name ) );
						}
						mutation.setRequest( new String( scanRequestResult.getMessageExchange().getRawRequestData() ) );
						mutation.setResponse( new String( scanRequestResult.getMessageExchange().getRawRequestData() ) );
						i++ ;
					}

				}
			}
			reports.put( testSuite.getName(), report );
		}

	}

	@Override
	public void afterSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityScanResult securityScanResult )
	{

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

	}

	@Override
	public void beforeSecurityScan( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityScan securityScan )
	{
	}

	@Override
	public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStepResult testStepResult )
	{
		// TODO Auto-generated method stub

	}
}
