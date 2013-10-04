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
	 * INITIALIZED - just started, for distinguishing if icon should be added in
	 * the security log UNKNOWN - when no assertions are added OK - finished with
	 * no errors/warnings FAILED CANCELED note:
	 * MISSING_ASSERTIONS,MISSING_PARAMETERS - are used only for indicating
	 * progress execution for security log entry icons
	 */
	public enum ResultStatus
	{
		INITIALIZED, UNKNOWN, OK, FAILED, CANCELED, MISSING_ASSERTIONS, MISSING_PARAMETERS, SKIPPED
	}

	/**
	 * Gets type of specific result, i.e. SecurityTestStep, SecurityCheck or
	 * SecurityCheckRequest used in displaying result details from SecurityLog
	 * 
	 * @return
	 */
	public String getResultType();

	/**
	 * Gets execution progress status used for indicating icon color in the
	 * SecurityLog introduced in general in case of missing assertions and
	 * missing parameters to match status in progress bars and yet not to need
	 * resultStatus changed
	 * 
	 * @return
	 */
	public ResultStatus getExecutionProgressStatus();

	public ResultStatus getLogIconStatus();

	public ResultStatus getStatus();

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions();
}
