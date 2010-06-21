/*
 *  soapUI, copyright (C) 2007-2009 eviware software ab 
 */
package com.eviware.soapui.model.support;

import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

public class TestSuiteMetrics
{
	final private TestSuite testSuite;

	public TestSuiteMetrics(TestSuite testSuite)
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

		for (TestCase testCase : testSuite.getTestCaseList())
		{
			result += testCase.getTestStepCount();
		}

		return result;
	}

	public int getAssertionCount()
	{
		int result = 0;

		for (TestCase testCase : testSuite.getTestCaseList())
		{
			for (TestStep testStep : testCase.getTestStepList())
			{
				if (testStep instanceof Assertable)
				{
					result += ((Assertable) testStep).getAssertionCount();
				}
			}
		}

		return result;
	}

	public int getLoadTestCount()
	{
		int result = 0;
		for (TestCase testCase : testSuite.getTestCaseList())
		{
			result += testCase.getLoadTestCount();
		}
		return result;
	}

}
