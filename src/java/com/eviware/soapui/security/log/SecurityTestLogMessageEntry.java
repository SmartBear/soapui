/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.ImageIcon;

import com.eviware.soapui.support.action.swing.ActionList;

/**
 * A simple message SecurityTest Log entry
 * 
 * @author soapUI team
 */

public class SecurityTestLogMessageEntry implements SecurityTestLogEntry
{
	private String message;
	private long timestamp;
	private ImageIcon icon;
	private boolean discarded;

	public SecurityTestLogMessageEntry( String message )
	{
		this.message = message;
		timestamp = System.currentTimeMillis();

//		icon = UISupport.createImageIcon( "/securitytest_log_message.gif" );
	}

	public String getMessage()
	{
		return message;
	}

	public long getTimeStamp()
	{
		return timestamp;
	}

	public String getTargetStepName()
	{
		return null;
	}

	public ImageIcon getIcon()
	{
		return icon;
	}

	public String getType()
	{
		return "Message";
	}

	public boolean isError()
	{
		return false;
	}

	public ActionList getActions()
	{
		return null;
	}

	public void exportToFile( String fileName ) throws IOException
	{
		PrintWriter writer = new PrintWriter( fileName );
		writer.write( new Date( timestamp ).toString() );
		writer.write( ":" );
		writer.write( message );
		writer.close();
	}

	public void discard()
	{
		discarded = true;
	}

	public boolean isDiscarded()
	{
		return discarded;
	}
}
