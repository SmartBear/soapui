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

import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * Adapter for LoadTestRunListener implementations
 * 
 * @author Ole.Matzura
 */

public class LoadTestRunListenerAdapter implements LoadTestRunListener
{
	public void beforeLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void beforeTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext )
	{
	}

	public void beforeTestStep( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext, TestStep testStep )
	{
	}

	public void afterTestStep( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext, TestStepResult testStepResult )
	{
	}

	public void afterTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext )
	{
	}

	public void afterLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void loadTestStarted( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void loadTestStopped( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

}
