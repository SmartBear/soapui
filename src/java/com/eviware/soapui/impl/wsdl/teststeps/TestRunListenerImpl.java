/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.impl.wsdl.mock.MockRunnerManager;
import com.eviware.soapui.impl.wsdl.mock.MockRunnerManagerException;
import com.eviware.soapui.impl.wsdl.mock.MockRunnerManagerImpl;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import org.apache.log4j.Logger;

import java.util.List;

public class TestRunListenerImpl extends TestRunListenerAdapter
{
	private final static Logger log = 
		Logger.getLogger(TestRunListenerImpl.class);
	
	public void beforeRun(TestRunner testRunner, TestRunContext runContext)
	{
		LoadTestRunner loadTestRunner = 
			(LoadTestRunner) runContext.getProperty("LoadTestRunner");

		if (loadTestRunner == null)
		{
			TestCase testCase = testRunner.getTestCase();

			if (needsMockRunnerManager(testCase))
			{
				createMockServices(testCase);
				
				MockRunnerManager manager = MockRunnerManagerImpl.getInstance(
						testCase);
				
				try
				{
					manager.start();
				}
				catch (MockRunnerManagerException e)
				{
					log.error("Unable to start MockRunnerManager", e);
				}
			}
		}
	}

	public void afterRun(TestRunner testRunner, TestRunContext runContext)
	{
		LoadTestRunner loadTestRunner = 
			(LoadTestRunner) runContext.getProperty("LoadTestRunner");

		if (loadTestRunner == null)
		{
			TestCase testCase = testRunner.getTestCase();

			MockRunnerManager manager = MockRunnerManagerImpl.getInstance(
					testCase);

			if (manager != null)
			{
				manager.stop();
			}
		}
	}
	
	private boolean needsMockRunnerManager(TestCase testCase)
	{
		return testCase.getTestStepsOfType(
				WsdlAsyncResponseTestStep.class).size() > 0;
	}

	private void createMockServices(TestCase testCase)
	{
		List<WsdlAsyncResponseTestStep> teststeps = 
			testCase.getTestStepsOfType(WsdlAsyncResponseTestStep.class);
		
		for (WsdlAsyncResponseTestStep teststep : teststeps)
		{
			teststep.createMockService();
		}
	}
}
