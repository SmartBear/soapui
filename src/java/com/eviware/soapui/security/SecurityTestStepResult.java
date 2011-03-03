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

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityStatus;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

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
	private List<SecurityCheckResult> securityCheckResultList;
	private boolean discarded;
	private long timeTaken = 0;
	private long timeStamp;
	private StringBuffer testLog = new StringBuffer();
	private TestStepResult originalTestStepResult;
	private DefaultActionList actionList;

	public SecurityTestStepResult( TestStep testStep, TestStepResult originalResult )
	{
		this.testStep = testStep;
		securityCheckResultList = new ArrayList<SecurityCheckResult>();
		timeStamp = System.currentTimeMillis();
		this.testLog.append( "" );
		this.originalTestStepResult = originalResult;
	}

	public List<SecurityCheckResult> getSecurityCheckResultList()
	{
		return securityCheckResultList;
	}

	public SecurityStatus getStatus()
	{
		return status;
	}

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions()
	{
		if( actionList == null )
		{
			actionList = new DefaultActionList( getOriginalTestStepResult().getTestStep().getName() );
		}
		actionList.addActions( getOriginalTestStepResult().getActions() );
		actionList.setDefaultAction( getOriginalTestStepResult().getActions().getDefaultAction() );
		if( !getSecurityCheckResultList().isEmpty() )
		{
			for( SecurityCheckResult checkResult : getSecurityCheckResultList() )
			{
				actionList.addActions( checkResult.getActions() );
			}
		}
		return actionList;
	}

	// TODO
	public void addSecurityCheckResult( SecurityCheckResult securityCheckResult )
	{
		if( securityCheckResultList != null )
			securityCheckResultList.add( securityCheckResult );

		// calculate time taken
		timeTaken += securityCheckResult.getTimeTaken();

		// calculate status ( one failed fails whole test )
		if( status == SecurityStatus.OK )
			status = securityCheckResult.getStatus();

		// TODO check and finish this - seems it's used for reports
		this.testLog.append( "SecurityCheck " ).append( securityCheckResultList.indexOf( securityCheckResult ) ).append(
				securityCheckResult.getStatus().toString() ).append( ": took " )
				.append( securityCheckResult.getTimeTaken() ).append( " ms" );
		this.testLog.append( securityCheckResult.getSecurityTestLog() );
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

	public TestStepResult getOriginalTestStepResult()
	{
		return originalTestStepResult;
	}

	public void setOriginalTestStepResult( TestStepResult originalTestStepResult )
	{
		this.originalTestStepResult = originalTestStepResult;
	}

}
