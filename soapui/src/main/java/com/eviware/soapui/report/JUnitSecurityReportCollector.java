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

import java.util.List;

import com.eviware.soapui.junit.Testcase;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.xml.XmlUtils;

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
	}

	@Override
	public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
		TestCase testCase = testRunner.getTestCase();

		SecurityTest securityTest = ( ( SecurityTestRunner )testRunner ).getSecurityTest();

		JUnitReport report = new JUnitReport();
		String reportName = securityTest.getName();
		report.setTestSuiteName( reportName );
		report.setPackage( testCase.getTestSuite().getProject().getName() );
		int errorCount = 0;

		for( TestStep ts : testCase.getTestStepList() )
		{
			SecurityTestStepResult secuTestStepResult = securityTest.getSecurityTestStepResultMap().get( ts );
			if( secuTestStepResult != null )
			{
				for( SecurityScanResult scanResult : secuTestStepResult.getSecurityScanResultList() )
				{
					List<SecurityScanRequestResult> resultList = scanResult.getSecurityRequestResultList();
					Testcase secTestCase = report.addTestCase( ts.getName() + " - " + scanResult.getSecurityScanName(),
							scanResult.getTimeTaken() );

					secTestCase.setPackage( testCase.getTestSuite().getProject().getName() );

					for( int i = 0; i < resultList.size(); i++ )
					{
						SecurityScanRequestResult scanRequestResult = resultList.get( i );
						if( scanRequestResult.getStatus() == ResultStatus.FAILED )
						{
							StringBuffer result = new StringBuffer();
							result.append( "<pre>" )
									.append( XmlUtils.entitize( scanRequestResult.getChangedParamsInfo( i + 1 ) ) )
									.append( "</pre>" );

							for( String message : scanRequestResult.getMessages() )
							{
								result.append( "<pre>" ).append( XmlUtils.entitize( message ) ).append( "</pre>" );
							}

							secTestCase.addNewError().setStringValue( result.toString() );
							errorCount++ ;
						}
					}
				}
			}

			report.setNoofErrorsInTestSuite( errorCount );
			report.setTotalTime( testRunner.getTimeTaken() );

			reports.put( reportName, report );
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
	}

	@Override
	public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
	{
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
	}
}
