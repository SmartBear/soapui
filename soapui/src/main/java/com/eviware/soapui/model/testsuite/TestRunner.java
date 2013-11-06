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

package com.eviware.soapui.model.testsuite;

public interface TestRunner
{

	/**
	 * Gets the current status of this TestRunner
	 */

	public Status getStatus();

	public enum Status
	{
		INITIALIZED, RUNNING, CANCELED, FINISHED, FAILED, WARNING
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

	public TestRunContext getRunContext();

	public TestRunnable getTestRunnable();

	public boolean isRunning();
}
