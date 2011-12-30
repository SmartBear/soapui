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

public class HttpMetrics
{
	// format: 2011-12-21 23:14:48 (SimpleDateFormat)
	private long timestamp = -1;
	private int httpStatus = -1;
	private long contentLength = -1;

	private long timeDNS = -1;
	private long timeConnect = -1;
	private long timeToFirstByte = -1;
	private long timeRead = -1;
	private long timeTotal = -1;

	private long startTime = -1;

	private final static int TIME_DNS = 0;
	private final static int TIME_CONNECT = 1;
	private final static int TIME_TO_FIRST_BYTE = 2;
	private final static int TIME_READ = 3;
	private final static int TIME_TOTAL = 4;

	public void reset()
	{
		timestamp = -1;
		httpStatus = -1;
		contentLength = -1;

		timeDNS = -1;
		timeConnect = -1;
		timeToFirstByte = -1;
		timeRead = -1;
		timeTotal = -1;

		startTime = -1;
	}

	public void init()
	{
		startTime = System.nanoTime();
	}

	public void recordTime( int code )
	{
		long recordedValue = System.nanoTime() - startTime;
		switch( code )
		{
		case TIME_DNS :
			timeDNS = recordedValue;
			break;
		case TIME_CONNECT :
			timeConnect = recordedValue;
			break;
		case TIME_TO_FIRST_BYTE :
			timeToFirstByte = recordedValue;
			break;
		case TIME_READ :
			timeRead = recordedValue;
			break;
		case TIME_TOTAL :
			timeTotal = recordedValue;
			break;
		default :
			throw new IllegalArgumentException( "Illegal value for recorded time" );
		}
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( long timestamp )
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

	public long getContentLength()
	{
		return contentLength;
	}

	public void setContentLength( long contentLength )
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
