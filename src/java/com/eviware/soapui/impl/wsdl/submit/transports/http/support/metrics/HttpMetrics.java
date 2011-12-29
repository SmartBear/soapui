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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics;

import java.util.Date;

public class HttpMetrics
{
	// format: 2011-12-21 23:14:48 (SimpleDateFormat)
	private Date timestamp = null;
	private int httpStatus = -1;
	private int contentLength = -1;

	private long timeDNS = -1;
	private long timeConnect = -1;
	private long timeToFirstByte = -1;
	private long timeRead = -1;
	private long timeTotal = -1;

	public void reset()
	{
		timestamp = null;
		httpStatus = -1;
		contentLength = -1;

		timeDNS = -1;
		timeConnect = -1;
		timeToFirstByte = -1;
		timeRead = -1;
		timeTotal = -1;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( Date timestamp )
	{
		this.timestamp = timestamp;
	}

	public int getHttpStatus()
	{
		return httpStatus;
	}

	public void setHttpStatus( int httpStatus )
	{
		this.httpStatus = httpStatus;
	}

	public int getContentLength()
	{
		return contentLength;
	}

	public void setContentLength( int contentLength )
	{
		this.contentLength = contentLength;
	}

	public long getTimeDNS()
	{
		return timeDNS;
	}

	public void setTimeDNS( long timeDNS )
	{
		this.timeDNS = timeDNS;
	}

	public long getTimeConnect()
	{
		return timeConnect;
	}

	public void setTimeConnect( long timeConnect )
	{
		this.timeConnect = timeConnect;
	}

	public long getTimeToFirstByte()
	{
		return timeToFirstByte;
	}

	public void setTimeToFirstByte( long timeToFirstByte )
	{
		this.timeToFirstByte = timeToFirstByte;
	}

	public long getTimeRead()
	{
		return timeRead;
	}

	public void setTimeRead( long timeRead )
	{
		this.timeRead = timeRead;
	}

	public long getTimeTotal()
	{
		return timeTotal;
	}

	public void setTimeTotal( long timeTotal )
	{
		this.timeTotal = timeTotal;
	}
}
