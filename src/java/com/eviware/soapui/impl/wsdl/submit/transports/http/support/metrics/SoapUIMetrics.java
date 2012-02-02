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

import java.text.SimpleDateFormat;

public class SoapUIMetrics
{
	private long timestamp = -1;
	private int httpStatus = -1;
	private long contentLength = -1;

	private Stopwatch readTimer;
	private Stopwatch totalTimer;
	private Stopwatch DNSTimer;
	private Stopwatch connectTimer;
	private Stopwatch timeToFirstByteTimer;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	public SoapUIMetrics()
	{
		readTimer = new NanoStopwatch();
		totalTimer = new NanoStopwatch();
		DNSTimer = new NanoStopwatch();
		connectTimer = new NanoStopwatch();
		timeToFirstByteTimer = new NanoStopwatch();
	}

	public void reset()
	{
		readTimer.reset();
		totalTimer.reset();
		DNSTimer.reset();
		connectTimer.reset();
		timeToFirstByteTimer.reset();
	}

	public synchronized static String formatTimestamp( long timestamp )
	{
		return dateFormat.format( timestamp );
	}

	public Stopwatch getDNSTimer()
	{
		return DNSTimer;
	}

	public Stopwatch getTimeToFirstByteTimer()
	{
		return timeToFirstByteTimer;
	}

	public Stopwatch getConnectTimer()
	{
		return connectTimer;
	}

	public Stopwatch getReadTimer()
	{
		return readTimer;
	}

	public Stopwatch getTotalTimer()
	{
		return totalTimer;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public String getFormattedTimeStamp()
	{
		return dateFormat.format( getTimestamp() );
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

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "timestamp:" ).append( getFormattedTimeStamp() ).append( ";status:" ).append( getHttpStatus() )
				.append( ";length:" ).append( getContentLength() ).append( ";DNS time:" )
				.append( getDNSTimer().getDuration() ).append( " ms;connect time:" )
				.append( getConnectTimer().getDuration() ).append( " ms;time to first byte:" )
				.append( getTimeToFirstByteTimer().getDuration() ).append( " ms;read time:" )
				.append( getReadTimer().getDuration() ).append( " ms;total time:" ).append( getTotalTimer().getDuration() );
		return sb.toString();
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o )
		{
			return true;
		}
		if( !( o instanceof SoapUIMetrics ) )
		{
			return false;
		}
		SoapUIMetrics that = ( SoapUIMetrics )o;

		return this.toString().equals( that.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

}
