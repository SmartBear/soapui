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

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityStatus;
import com.eviware.soapui.support.action.swing.ActionList;

/**
 * Security result of a TestStep represents summary result of all TestStep
 * security checks
 * 
 * @author dragica.soldo
 */

public class SecurityTestStepResult
{
	public SecurityStatus status = SecurityStatus.OK;
	public TestStep testStep;
	private long size;
	// private List<SecurityCheckRequestResult> securityRequestResultList;
	private boolean discarded;
	private long timeTaken = 0;
	private long timeStamp;
	private StringBuffer testLog = new StringBuffer();

	public SecurityTestStepResult( TestStep testStep )
	{
		this.testStep = testStep;
		// securityRequestResultList = new
		// ArrayList<SecurityCheckRequestResult>();
	}

	// public List<SecurityCheckRequestResult> getSecurityRequestResultList()
	// {
	// return securityRequestResultList;
	// }

	public SecurityStatus getStatus()
	{
		return status;
	}

	// public AbstractSecurityCheck getSecurityCheck()
	// {
	// return securityCheck;
	// }

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions()
	{
		return null;
	}

	public void addSecurityRequestResult( SecurityCheckResult securityCheckResult )
	{
		// if( securityRequestResultList != null )
		// securityRequestResultList.add( secReqResult );

		// calulate time taken
		timeTaken += securityCheckResult.getTimeTaken();

		// calculate time stamp (when test is started)
		// if( securityRequestResultList.size() == 1 )
		// timeStamp = securityRequestResultList.get( 0 ).getTimeStamp();
		// else if( timeStamp > secReqResult.getTimeStamp() )
		// timeStamp = securityCheckResult.getTimeStamp();

		// calculate status ( one failed fails whole test )
		if( status == SecurityStatus.OK )
			status = securityCheckResult.getStatus();

		// this.testLog.append( "SecurityRequest " ).append(
		// securityRequestResultList.indexOf( secReqResult ) ).append(
		// secReqResult.getStatus().toString() ).append( ": took " ).append(
		// secReqResult.getTimeTaken() ).append(
		// " ms" );
		// for( String s : secReqResult.getMessages() )
		// testLog.append( "\n -> " ).append( s );
	}

	public long getTimeTaken()
	{
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

	/**
	 * Returns time stamp when test is started.
	 * 
	 * @return
	 */
	public long getTimeStamp()
	{
		return timeStamp;
	}

}
