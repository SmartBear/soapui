/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.result;

import com.eviware.soapui.support.action.swing.ActionList;

/**
 * Interface for all result classes used in Security testing
 * 
 * @author dragica.soldo
 * 
 */
public interface SecurityResult
{
	/**
	 * 
	 * INITIALIZED - just started, for distinguishing if icon should be added in the security log
	 * UNKNOWN - when no assertions are added
	 * OK - finished with no errors/warnings
	 * FAILED 
	 * CANCELED
	 */
	public enum SecurityStatus
	{
		INITIALIZED, UNKNOWN, OK, FAILED, CANCELED
	}

	/**
	 * Gets type of specific result, i.e. SecurityTestStep, SecurityCheck or
	 * SecurityCheckRequest used in displaying result details from SecurityLog
	 * 
	 * @return
	 */
	public String getResultType();

	public SecurityStatus getStatus();

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions();
}
