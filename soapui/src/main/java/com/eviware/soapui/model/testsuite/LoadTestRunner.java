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

/**
 * Runner for loadtests
 * 
 * @author Ole.Matzura
 */

public interface LoadTestRunner extends TestRunner
{
	/**
	 * Gets the number of threads currently running
	 */

	public int getRunningThreadCount();

	public LoadTest getLoadTest();

	/**
	 * Returns the progress of the loadtest as a value between 0 and 1. Progress
	 * is measured depending on the LoadTest limit configuration
	 */

	public float getProgress();

	/**
	 * Confusing but unfortunately necessary; isStopped will return false until
	 * the loadtest has called all handlers, etc.. the status will be set to
	 * FINISHED before that.
	 * 
	 * @return
	 */

	public boolean hasStopped();
}
