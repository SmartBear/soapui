/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.eviware.soapui.SoapUI;

/**
 * Log4j appender thats appends to SoapUI log panel
 * 
 * @author Ole.Matzura
 */

public class SoapUIAppender extends AppenderSkeleton
{
	public SoapUIAppender()
	{
	}

	protected void append( LoggingEvent event )
	{
		SoapUI.log( event );
	}

	public void close()
	{
	}

	public boolean requiresLayout()
	{
		return false;
	}

}
