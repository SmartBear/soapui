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

package com.eviware.soapui.security;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.support.action.swing.ActionList;

/**
 * A SecurityCheck result represents result of one request (modified by a
 * security check and run)
 * 
 * @author dragica.soldo
 */

public class SecurityCheckResult
{
	public enum SecurityCheckStatus
	{
		OK, FAILED
	}

	private static final String[] EMPTY_MESSAGES = new String[0];
	public SecurityCheckStatus status;
	public AbstractSecurityCheck securityCheck;
	private long size;
	private boolean discarded;
	private List<SecurityCheckRequestResult> requestResultList;

	public SecurityCheckResult( AbstractSecurityCheck securityCheck )
	{
		this.securityCheck = securityCheck;
		requestResultList = new ArrayList<SecurityCheckRequestResult>();
	}

	public SecurityCheckStatus getStatus()
	{
		return status;
	}

	public void setStatus( SecurityCheckStatus status )
	{
		this.status = status;
	}

	public AbstractSecurityCheck getSecurityCheck()
	{
		return securityCheck;
	}

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions()
	{
		return null;
	}

	public void addSecurityRequestResult( SecurityCheckRequestResult secReqResult )
	{
		if( requestResultList != null )
			requestResultList.add( secReqResult );
	}

	// public Throwable getError();

	public long getTimeTaken()
	{
		long timeTaken = 0;
		for( SecurityCheckRequestResult requestResult : requestResultList )
		{
			timeTaken += requestResult.getTimeTaken();
		}
		return timeTaken;
	}

	/**
	 * Used for calculating the output
	 * 
	 * @return the number of bytes in this result
	 */

	public long getSize()
	{
		return size;
	}

	/**
	 * Writes this result to the specified writer, used for logging.
	 */

	public void writeTo( PrintWriter writer )
	{

	}

	/**
	 * Can discard any result data that may be taking up memory. Timing-values
	 * must not be discarded.
	 */

	public void discard()
	{

	}

	public boolean isDiscarded()
	{
		return discarded;
	}

}
