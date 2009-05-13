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

package com.eviware.soapui.model.testsuite;

/**
 * Listener for LoadTest run events
 * 
 * @author Ole.Matzura
 */

public interface LoadTestRunListener
{
	/**
	 * Called before a load-test is about to be run
	 * 
	 * @param loadTestRunner
	 * @param context
	 */

	public void beforeLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context );

	/**
	 * Called after all initial loadtest threads have been started
	 * 
	 * @param loadTestRunner
	 * @param context
	 */

	public void loadTestStarted( LoadTestRunner loadTestRunner, LoadTestRunContext context );

	/**
	 * Called before the execution of a testcase
	 * 
	 * @param loadTestRunner
	 * @param context
	 * @param testRunner
	 * @param runContext
	 */

	public void beforeTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext );

	/**
	 * Called before the execution of a teststep
	 * 
	 * @param loadTestRunner
	 * @param context
	 * @param testRunner
	 * @param runContext
	 * @param testStep
	 */

	public void beforeTestStep( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext, TestStep testStep );

	/**
	 * Called after the execution of a teststep
	 * 
	 * @param loadTestRunner
	 * @param context
	 * @param testRunner
	 * @param runContext
	 * @param testStepResult
	 */

	public void afterTestStep( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext, TestStepResult testStepResult );

	/**
	 * Called after the execution of a testcase
	 * 
	 * @param loadTestRunner
	 * @param context
	 * @param testRunner
	 * @param runContext
	 */

	public void afterTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext );

	/**
	 * Called when a loadtest has been stopped for some reason
	 * 
	 * @param loadTestRunner
	 * @param context
	 */

	public void loadTestStopped( LoadTestRunner loadTestRunner, LoadTestRunContext context );

	/**
	 * Called after the execution of a loadtest when all threads have terminated
	 * 
	 * @param loadTestRunner
	 * @param context
	 */
	public void afterLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context );
}
