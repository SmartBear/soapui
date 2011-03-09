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
	private SecurityStatus status = SecurityStatus.OK;
	public SecurityCheck securityCheck;
	private long size;
	private boolean discarded;
	private List<SecurityCheckRequestResult> securityRequestResultList;
	private long timeTaken = 0;
	private long timeStamp;
	public StringBuffer testLog = new StringBuffer();
	private DefaultActionList actionList;

	public SecurityCheckResult( SecurityCheck securityCheck )
	{
		this.securityCheck = securityCheck;
		securityRequestResultList = new ArrayList<SecurityCheckRequestResult>();
		timeStamp = System.currentTimeMillis();
	}

	public List<SecurityCheckRequestResult> getSecurityRequestResultList()
	{
		return securityRequestResultList;
	}

	public SecurityStatus getStatus()
	{
		return status;
	}

	public void setStatus( SecurityStatus status )
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
//						if( getMessages().length > 0 )
//						{
//							StringBuffer buf = new StringBuffer( "<html><body>");
//							if( getError() != null )
//								buf.append( getError().toString() ).append( "<br/>" );
//
//							for( String s : getMessages() )
//								buf.append( s ).append( "<br/>" );
//
//							UISupport.showExtendedInfo( "TestStep Result", "Step [" + testStepName + "] ran with status ["
//									+ getStatus() + "]", buf.toString(), null );
//						}
//						else if( getError() != null )
//						{
//							UISupport.showExtendedInfo( "TestStep Result", "Step [" + testStepName + "] ran with status ["
//									+ getStatus() + "]", getError().toString(), null );
//						}
//						else
//						{
							UISupport.showInfoMessage( "Check [" + getSecurityCheck().getName() + "] ran with status [" + getStatus() + "]",
									"SecurityCheck Result" );
//						}
					}
				} );
			}

			return actionList;
		}
		
	public void addSecurityRequestResult( SecurityCheckRequestResult secReqResult )
	{
		if( securityRequestResultList != null )
			securityRequestResultList.add( secReqResult );

		// calulate time taken
		timeTaken += secReqResult.getTimeTaken();

		// calculate status ( one failed fails whole test )
		if( status == SecurityStatus.OK )
			status = secReqResult.getStatus();

		this.testLog.append( "\nSecurityRequest " ).append( securityRequestResultList.indexOf( secReqResult ) ).append(
				secReqResult.getStatus().toString() ).append( ": took " ).append( secReqResult.getTimeTaken() ).append(
				" ms" );
		for( String s : secReqResult.getMessages() )
			testLog.append( "\n -> " ).append( s );
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

}
