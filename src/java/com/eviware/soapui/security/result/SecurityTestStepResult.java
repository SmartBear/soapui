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

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;
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
	private ResultStatus executionProgressStatus = ResultStatus.UNKNOWN;;
	private ResultStatus logIconStatus = ResultStatus.UNKNOWN;;

	public SecurityTestStepResult( TestStep testStep, TestStepResult originalResult )
	{
		this.testStep = testStep;
		executionProgressStatus = ResultStatus.INITIALIZED;
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

	public void setStatus( ResultStatus status )
	{
		this.status = status;
	}

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions()
	{
		if( actionList == null )
		{
			actionList = new DefaultActionList( getTestStep().getName() );
			actionList.setDefaultAction( new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					UISupport.showInfoMessage( "Step [" + getTestStep().getName() + "] ran with security status ["
							+ getExecutionProgressStatus() + "]", "TestStep Result" );
				}
			} );
		}

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
		else if( status != ResultStatus.FAILED )
		{
			status = securityCheckResult.getStatus();
		}

		securityCheckResult.detectMissingItems();
		if( securityCheckResult.getExecutionProgressStatus().equals( ResultStatus.CANCELED ) )
		{
			executionProgressStatus = securityCheckResult.getExecutionProgressStatus();
		}
		else if( securityCheckResult.getExecutionProgressStatus().equals( ResultStatus.MISSING_PARAMETERS )
				&& executionProgressStatus != ResultStatus.CANCELED )
		{
			executionProgressStatus = ResultStatus.MISSING_PARAMETERS;
		}
		else if( securityCheckResult.getExecutionProgressStatus().equals( ResultStatus.MISSING_ASSERTIONS )
				&& executionProgressStatus != ResultStatus.CANCELED
				&& executionProgressStatus != ResultStatus.MISSING_PARAMETERS )
		{
			executionProgressStatus = ResultStatus.MISSING_ASSERTIONS;
		}
		else if( securityCheckResult.getExecutionProgressStatus().equals( ResultStatus.FAILED )
				&& executionProgressStatus != ResultStatus.CANCELED
				&& executionProgressStatus != ResultStatus.MISSING_PARAMETERS
				&& executionProgressStatus != ResultStatus.MISSING_ASSERTIONS )
		{
			executionProgressStatus = ResultStatus.FAILED;
		}
		else if( securityCheckResult.getExecutionProgressStatus().equals( ResultStatus.OK )
				&& executionProgressStatus != ResultStatus.CANCELED
				&& executionProgressStatus != ResultStatus.MISSING_PARAMETERS
				&& executionProgressStatus != ResultStatus.MISSING_ASSERTIONS
				&& executionProgressStatus != ResultStatus.FAILED )
		{
			executionProgressStatus = ResultStatus.OK;
		}

		if( securityCheckResult.getLogIconStatus().equals( ResultStatus.FAILED ) )
		{
			logIconStatus = securityCheckResult.getLogIconStatus();
		}
		else if( ( securityCheckResult.getLogIconStatus().equals( ResultStatus.MISSING_ASSERTIONS ) || securityCheckResult
				.getLogIconStatus().equals( ResultStatus.MISSING_PARAMETERS ) )
				&& logIconStatus != ResultStatus.FAILED )
		{
			logIconStatus = securityCheckResult.getLogIconStatus();
		}
		else if( securityCheckResult.getLogIconStatus().equals( ResultStatus.OK ) && logIconStatus != ResultStatus.FAILED
				&& logIconStatus != ResultStatus.MISSING_ASSERTIONS && logIconStatus != ResultStatus.MISSING_PARAMETERS )
		{
			logIconStatus = ResultStatus.OK;
		}

		// TODO check and finish this - seems it's used for reports
		// this.testLog.append( "SecurityScan " ).append(
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
		StringBuffer tl = new StringBuffer().append( "Step " ).append( " [" ).append( testStep.getName() ).append( "] " )
				.append( getExecutionProgressStatus().toString() ).append( ": took " ).append(
						getOriginalTestStepResult().getTimeTaken() ).append( " ms" );
		tl.append( testLog );
		return tl.toString();
	}

	@Override
	public String getResultType()
	{
		return TYPE;
	}

	@Override
	public ResultStatus getExecutionProgressStatus()
	{
		return executionProgressStatus;
	}

	public void setExecutionProgressStatus( ResultStatus status )
	{
		executionProgressStatus = status;
	}

	@Override
	public ResultStatus getLogIconStatus()
	{
		return logIconStatus;
	}
}
