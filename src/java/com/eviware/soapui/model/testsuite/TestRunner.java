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

import java.util.List;

/**
 * Runs a TestCase
 * 
 * @author Ole.Matzura
 */

public interface TestRunner
{
	/**
	 * Gets the TestCase being run
	 * 
	 * @return the TestCase being run
	 */

	public TestCase getTestCase();

	/**
	 * Gets the accumulated results so far; each TestStep returns a
	 * TestStepResult when running.
	 * 
	 * @return the accumulated results so far
	 */

	public List<TestStepResult> getResults();

	/**
	 * Gets the current status of this TestRunner
	 */

	public Status getStatus();

	public enum Status
	{
		INITIALIZED, RUNNING, CANCELED, FINISHED, FAILED
	};

	/**
	 * Starts running this TestRunners TestCase. If the async flag is set to
	 * true, this method will return directly, otherwise it will block until the
	 * TestCase is finished
	 * 
	 * @param async
	 *           flag controlling if TestCase should be run in seperate or
	 *           callers thread.
	 */

	public void start( boolean async );

	/**
	 * Returns the time taken by this runner since its last start
	 */

	public long getTimeTaken();

	/**
	 * Returns the time this runner was last started
	 */

	public long getStartTime();

	/**
	 * Blocks until this runner is finished, (returns directly if it already has
	 * finished)
	 */

	public Status waitUntilFinished();

	/**
	 * Cancels an ongoing test run with the specified reason
	 */

	public void cancel( String reason );

	/**
	 * Fails an ongoing test run with the specified reason
	 */

	public void fail( String reason );

	/**
	 * Gets the reason why a running test was canceled or failed.
	 */

	public String getReason();

	/**
	 * Transfers execution of this TestRunner to the TestStep with the specified
	 * index in the TestCase
	 */

	public void gotoStep( int index );

	/**
	 * Transfers execution of this TestRunner to the TestStep with the specified
	 * name in the TestCase
	 */

	public void gotoStepByName( String stepName );

	/**
	 * Runs the specified TestStep and returns the result
	 */

	public TestStepResult runTestStepByName( String name );

	/**
	 * Returns the context used by this runner
	 */

	public TestRunContext getRunContext();
}