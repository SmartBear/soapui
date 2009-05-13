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

import java.io.PrintWriter;

import com.eviware.soapui.support.action.swing.ActionList;

/**
 * A TestStep result
 * 
 * @author Ole.Matzura
 */

public interface TestStepResult
{
	public enum TestStepStatus
	{
		UNKNOWN, OK, FAILED, CANCELED
	}

	public TestStepStatus getStatus();

	public TestStep getTestStep();

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions();

	public String[] getMessages();

	public Throwable getError();

	public long getTimeTaken();

	public long getTimeStamp();

	/**
	 * Used for calculating throughput
	 * 
	 * @return the number of bytes in this result
	 */

	public long getSize();

	/**
	 * Writes this result to the specified writer, used for logging.
	 */

	public void writeTo( PrintWriter writer );

	/**
	 * Can discard any result data that may be taking up memory. Timing-values
	 * must not be discarded.
	 */

	public void discard();

	public boolean isDiscarded();
}
