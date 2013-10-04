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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

public class TestSuiteMetrics
{
	final private TestSuite testSuite;

	public TestSuiteMetrics( TestSuite testSuite )
	{
		this.testSuite = testSuite;
	}

	public int getTestCaseCount()
	{
		return testSuite.getTestCaseCount();
	}

	public int getTestStepCount()
	{
		int result = 0;

		for( TestCase testCase : testSuite.getTestCaseList() )
		{
			result += testCase.getTestStepCount();
		}

		return result;
	}

	public int getAssertionCount()
	{
		int result = 0;

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

		return result;
	}

	public int getLoadTestCount()
	{
		int result = 0;
		for( TestCase testCase : testSuite.getTestCaseList() )
		{
			result += testCase.getLoadTestCount();
		}
		return result;
	}

}
