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
 * Runner for loadtests
 * 
 * @author Ole.Matzura
 */

public interface LoadTestRunner
{
	/**
	 * Gets the number of threads currently running
	 */

	public int getRunningThreadCount();

	public LoadTest getLoadTest();

	/**
	 * Cancels the loadtest with the specified reason. This should be used for
	 * "normal" cancellations, ie from a ui or some expected signal.
	 * 
	 * @param reason
	 */

	public void cancel( String reason );

	/**
	 * Fails the loadtest with the specified reason. This should be used for
	 * error conditions
	 */

	public void fail( String reason );

	/**
	 * Gets the current status of this runner
	 */

	public Status getStatus();

	public enum Status
	{
		INITIALIZED, RUNNING, CANCELED, FINISHED, FAILED
	}

	/**
	 * Returns the progress of the loadtest as a value between 0 and 1. Progress
	 * is measured depending on the LoadTest limit configuration
	 */

	public float getProgress();

	/**
	 * Gets the reason why a loadtest was cancelled or failed
	 */

	public String getReason();

	/**
	 * Gets the time taken for this loadtest
	 */
	public long getTimeTaken();
}
