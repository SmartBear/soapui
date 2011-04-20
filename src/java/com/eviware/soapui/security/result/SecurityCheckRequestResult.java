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

import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

/**
 * A SecurityCheck result represents result of one request (modified by a
 * security check and run)
 * 
 * @author dragica.soldo
 */

public class SecurityCheckRequestResult implements SecurityResult
{
	private static final String[] EMPTY_MESSAGES = new String[0];
	public final static String TYPE = "SecurityCheckRequestResult";
	private ResultStatus status = ResultStatus.UNKNOWN;
	private SecurityCheck securityCheck;
	private List<String> messages = new ArrayList<String>();
	private long timeTaken;
	private long startTime;
	private long timeStamp;
	private long size;
	private boolean discarded;
	private MessageExchange messageExchange;
	public StringBuffer testLog = new StringBuffer();
	private DefaultActionList actionList;
	private boolean addedAction;
	private ResultStatus executionProgressStatus;

	public SecurityCheckRequestResult( SecurityCheck securityCheck )
	{
		this.securityCheck = securityCheck;
		executionProgressStatus = ResultStatus.INITIALIZED;
		timeStamp = System.currentTimeMillis();
	}

	public ResultStatus getStatus()
	{
		return status;
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
		}
		if( !addedAction )
		{
			actionList
					.addAction( new ShowMessageExchangeAction( this.getMessageExchange(), "SecurityCheckRequest" ), true );
			addedAction = true;
		}
		return actionList;
	}

	public String[] getMessages()
	{
		return messages == null ? EMPTY_MESSAGES : messages.toArray( new String[messages.size()] );
	}

	public void addMessage( String message )
	{
		if( messages != null )
			messages.add( message );
	}

	// public Throwable getError();

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public long getTimeStamp()
	{
		return timeStamp;
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

	public MessageExchange getMessageExchange()
	{
		return messageExchange;
	}

	// TODO not sure if this should exist, it should be set when result is
	// created
	// but for now for first step refactoring it's added this way
	public void setMessageExchange( MessageExchange messageExchange )
	{
		this.messageExchange = messageExchange;
	}

	public void setTimeTaken( long timeTaken )
	{
		this.timeTaken = timeTaken;
	}

	public void startTimer()
	{
		startTime = System.nanoTime();
	}

	public void stopTimer()
	{
		timeTaken = ( ( System.nanoTime() - startTime ) / 1000000 );
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

}
