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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

/**
 * Security result of a TestStep represents summary result of all TestStep
 * security checks
 * 
 * @author dragica.soldo
 */

public class SecurityTestStepResult implements SecurityResult
{
	private ResultStatus status = ResultStatus.UNKNOWN;
	public static final String TYPE = "SecurityTestStepResult";
	private TestStep testStep;
	private long size;
	private List<SecurityCheckResult> securityCheckResultList;
	private boolean discarded;
	private long timeTaken = 0;
	private long timeStamp;
	private StringBuffer testLog = new StringBuffer();
	private TestStepResult originalTestStepResult;
	private DefaultActionList actionList;
	private boolean hasAddedRequests;
	private boolean addedAction;

	public SecurityTestStepResult( TestStep testStep, TestStepResult originalResult )
	{
		this.testStep = testStep;
		securityCheckResultList = new ArrayList<SecurityCheckResult>();
		timeStamp = System.currentTimeMillis();
		this.originalTestStepResult = originalResult;
	}

	public List<SecurityCheckResult> getSecurityCheckResultList()
	{
		return securityCheckResultList;
	}

	public ResultStatus getStatus()
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
		// if( !addedAction )
		// {
		// actionList.addActions( getOriginalTestStepResult().getActions() );
		// addedAction = true;
		// }
		actionList.setDefaultAction( getOriginalTestStepResult().getActions().getDefaultAction() );
		// if( !getSecurityCheckResultList().isEmpty() )
		// {
		// for( SecurityCheckResult checkResult : getSecurityCheckResultList() )
		// {
		// actionList.addActions( checkResult.getActions() );
		// actionList.setDefaultAction(
		// checkResult.getActions().getDefaultAction() );
		// }
		// }
		return actionList;
	}

	public void addSecurityCheckResult( SecurityCheckResult securityCheckResult )
	{
		if( securityCheckResultList != null )
			securityCheckResultList.add( securityCheckResult );

		timeTaken += securityCheckResult.getTimeTaken();

		if( !hasAddedRequests )
		{
			status = securityCheckResult.getStatus();
		}
		else if( (securityCheckResult.getStatus() == ResultStatus.OK
				|| securityCheckResult.getStatus() == ResultStatus.CANCELED_OK ) && status != ResultStatus.FAILED )
		{
			status = securityCheckResult.getStatus();
		}
		else {
			status = securityCheckResult.getStatus();
		}

		// if( !hasAddedRequests && securityCheckResult.getStatus() ==
		// ResultStatus.OK )
		// {
		// status = ResultStatus.OK;
		// }
		// else if( securityCheckResult.getStatus() == ResultStatus.FAILED )
		// {
		// status = ResultStatus.FAILED;
		// }
		// else if( securityCheckResult.getStatus() ==
		// ResultStatus.CANCELED_FAILED )
		// {
		// status = ResultStatus.CANCELED_FAILED;
		//
		// }
		// else
		// {
		// status = ResultStatus.CANCELED_OK;
		// }
		// TODO check and finish this - seems it's used for reports
		// this.testLog.append( "SecurityCheck " ).append(
		// securityCheckResultList.indexOf( securityCheckResult ) ).append(
		// securityCheckResult.getStatus().toString() ).append( ": took " )
		// .append( securityCheckResult.getTimeTaken() ).append( " ms" );
		this.testLog.append( securityCheckResult.getSecurityTestLog() );

		hasAddedRequests = true;
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

	public TestStep getTestStep()
	{
		return testStep;
	}

	/**
	 * Raturns Security Test Log
	 */
	public String getSecurityTestLog()
	{
		StringBuffer tl = new StringBuffer().append( "TestStep " ).append( " [" ).append( testStep.getName() ).append(
				"] " ).append( getOriginalTestStepResult().getStatus().toString() ).append( ": took " ).append(
				getOriginalTestStepResult().getTimeTaken() ).append( " ms" );
		tl.append( testLog );
		return tl.toString();
	}

	@Override
	public String getResultType()
	{
		return TYPE;
	}
}
