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

import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

/**
 * A SecurityCheck result represents result of one request (modified by a
 * security check and run)
 * 
 * @author dragica.soldo
 */

public class SecurityCheckResult implements SecurityResult
{
	public final static String TYPE = "SecurityCheckResult";
	/**
	 * status is set to SecurityStatus.INITIALIZED but goes to
	 * SecurityStatus.UNKNOWN first time any checkRequestResult is added.
	 * INITIALIZED status is necessary to be able to detect when logging if
	 * SecurityCheck is just started and no status icon should be added, or it
	 * went through execution and gone into any other status, including UNKNOWN
	 * if no assertion is added, when status icon should be added to log
	 */
	private ResultStatus status;
	public SecurityCheck securityCheck;
	private long size;
	private boolean discarded;
	private List<SecurityCheckRequestResult> securityRequestResultList;
	private long timeTaken = 0;
	private long timeStamp;
	public StringBuffer testLog = new StringBuffer();
	private DefaultActionList actionList;
	private boolean hasAddedRequests;
	// along with the status determines if canceled with or without warnings
	private boolean hasRequestsWithWarnings;

	public SecurityCheckResult( SecurityCheck securityCheck )
	{
		this.securityCheck = securityCheck;
		status = ResultStatus.INITIALIZED;
		securityRequestResultList = new ArrayList<SecurityCheckRequestResult>();
		timeStamp = System.currentTimeMillis();
	}

	public List<SecurityCheckRequestResult> getSecurityRequestResultList()
	{
		return securityRequestResultList;
	}

	public ResultStatus getStatus()
	{
		return this.status;
	}

	public void setStatus( ResultStatus status )
	{
		this.status = status;
	}

	public SecurityCheck getSecurityCheck()
	{
		return securityCheck;
	}

	/**
	 * Returns a list of actions that can be applied to this result
	 */

	public ActionList getActions()
	{
		if( actionList == null )
		{
			actionList = new DefaultActionList( getSecurityCheck().getName() );
			actionList.setDefaultAction( new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					// if( getMessages().length > 0 )
					// {
					// StringBuffer buf = new StringBuffer( "<html><body>");
					// if( getError() != null )
					// buf.append( getError().toString() ).append( "<br/>" );
					//
					// for( String s : getMessages() )
					// buf.append( s ).append( "<br/>" );
					//
					// UISupport.showExtendedInfo( "TestStep Result", "Step [" +
					// testStepName + "] ran with status ["
					// + getStatus() + "]", buf.toString(), null );
					// }
					// else if( getError() != null )
					// {
					// UISupport.showExtendedInfo( "TestStep Result", "Step [" +
					// testStepName + "] ran with status ["
					// + getStatus() + "]", getError().toString(), null );
					// }
					// else
					// {
					UISupport.showInfoMessage( "Check [" + getSecurityCheck().getName() + "] ran with status ["
							+ getStatus() + "]", "SecurityCheck Result" );
					// }
				}
			} );
		}

		return actionList;
	}

	public void addSecurityRequestResult( SecurityCheckRequestResult secReqResult )
	{
		if( securityRequestResultList != null )
			securityRequestResultList.add( secReqResult );

		timeTaken += secReqResult.getTimeTaken();

		if( !hasAddedRequests )
		{
			status = ResultStatus.UNKNOWN;
			if( secReqResult.getStatus() == ResultStatus.OK )
			{
				status = ResultStatus.OK;
			}
			else if( secReqResult.getStatus() == ResultStatus.FAILED )
			{
				hasRequestsWithWarnings = true;
				status = ResultStatus.FAILED;
			}
		}
		else if( secReqResult.getStatus() == ResultStatus.FAILED )
		{
			hasRequestsWithWarnings = true;
			status = ResultStatus.FAILED;
		}
		else if( secReqResult.getStatus() == ResultStatus.OK && status != ResultStatus.FAILED )
		{
			status = ResultStatus.OK;
		}

		this.testLog.append( "\nSecurityRequest " ).append( securityRequestResultList.indexOf( secReqResult ) ).append(
				secReqResult.getStatus().toString() ).append( ": took " ).append( secReqResult.getTimeTaken() ).append(
				" ms" );
		for( String s : secReqResult.getMessages() )
			testLog.append( "\n -> " ).append( s );

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

	/**
	 * Raturns Security Test Log
	 */
	public String getSecurityTestLog()
	{
		StringBuffer tl = new StringBuffer().append( "\nSecurityCheck " ).append( " [" ).append(
				securityCheck.getTestStep().getName() ).append( "] " ).append( status.toString() ).append( ": took " )
				.append( timeTaken ).append( " ms" );
		tl.append( testLog );
		return tl.toString();
	}

	@Override
	public String getResultType()
	{
		return TYPE;
	}

	public boolean isCanceled()
	{
		return status == ResultStatus.CANCELED_OK || status == ResultStatus.CANCELED_FAILED;
	}

	public boolean isHasRequestsWithWarnings()
	{
		return hasRequestsWithWarnings;
	}

}
