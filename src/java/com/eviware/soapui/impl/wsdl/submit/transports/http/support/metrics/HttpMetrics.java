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

public class HttpMetrics
{

	private long timestamp = -1;
	private int httpStatus = -1;
	private long contentLength = -1;

	private Timer timeToFirstByteTimer;
	private Timer readTimer;
	private Timer totalTimer;

	private static final Timer DNSTimer = new NanoSecondTimer();
	private static final Timer connectTimer = new NanoSecondTimer();

	private final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	public HttpMetrics()
	{
		timeToFirstByteTimer = new NanoSecondTimer();
		readTimer = new NanoSecondTimer();
		totalTimer = new NanoSecondTimer();
	}

	public synchronized void reset()
	{
		DNSTimer.reset();
		connectTimer.reset();
	}

	public static synchronized Timer getDNSTimer()
	{
		return DNSTimer;
	}

	public static synchronized Timer getConnectTimer()
	{
		return connectTimer;
	}

	public Timer getTimeToFirstByteTimer()
	{
		return timeToFirstByteTimer;
	}

	public Timer getReadTimer()
	{
		return readTimer;
	}

	public Timer getTotalTimer()
	{
		return totalTimer;
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

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "timestamp:" ).append( dateFormat.format( getTimestamp() ) ).append( ";status:" )
				.append( getHttpStatus() ).append( ";length:" ).append( getContentLength() ).append( ";DNS time:" )
				.append( getDNSTimer().getDuration() ).append( " ms;connect time:" )
				.append( getConnectTimer().getDuration() ).append( " ms;time to first byte:" )
				.append( getTimeToFirstByteTimer().getDuration() ).append( " ms;read time:" )
				.append( getReadTimer().getDuration() ).append( " ms;total time:" ).append( getTotalTimer().getDuration() );
		return sb.toString();
	}
}
