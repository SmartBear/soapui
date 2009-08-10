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
package com.eviware.soapui.model.support;

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

public class ProjectMetrics
{
	private final Project project;

	public ProjectMetrics( Project project )
	{
		this.project = project;
	}

	public int getTestCaseCount()
	{
		int result = 0;

		for( TestSuite testSuite : project.getTestSuiteList() )
			result += testSuite.getTestCaseCount();

		return result;
	}

	public int getTestStepCount()
	{
		int result = 0;

		for( TestSuite testSuite : project.getTestSuiteList() )
		{
			for( TestCase testCase : testSuite.getTestCaseList() )
			{
				result += testCase.getTestStepCount();
			}
		}

		return result;
	}

	public int getAssertionCount()
	{
		int result = 0;

		for( TestSuite testSuite : project.getTestSuiteList() )
		{
			for( TestCase testCase : testSuite.getTestCaseList() )
			{
				for( TestStep testStep : testCase.getTestStepList() )
				{
					if( testStep instanceof Assertable )
					{
						result += ( ( Assertable )testStep ).getAssertionCount();
					}
				}
			}
		}

		return result;
	}

	public int getLoadTestCount()
	{
		int result = 0;

		for( TestSuite testSuite : project.getTestSuiteList() )
		{
			for (TestCase testCase : testSuite.getTestCaseList())
			{
				result += testCase.getLoadTestCount();
			}
		}

		return result;
	}

	public int getMockOperationCount()
	{
		int result = 0;

		for( MockService mockService : project.getMockServiceList() )
			result += mockService.getMockOperationCount();

		return result;
	}

	public int getMockResponseCount()
	{
		int result = 0;

		for( MockService mockService : project.getMockServiceList() )
		{
			for( MockOperation mockOperation : mockService.getMockOperationList() )
			{
				result += mockOperation.getMockResponseCount();
			}
		}

		return result;
	}
}
