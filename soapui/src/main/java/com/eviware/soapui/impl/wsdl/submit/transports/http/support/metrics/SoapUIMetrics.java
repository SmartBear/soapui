/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics;

import java.util.Date;

import org.apache.http.impl.HttpConnectionMetricsImpl;
import org.apache.http.io.HttpTransportMetrics;

import com.eviware.soapui.support.DateUtil;

public class SoapUIMetrics extends HttpConnectionMetricsImpl
{
	private long timestamp = -1;
	private int httpStatus = -1;
	private long contentLength = -1;

	private String httpMethod = "";
	private String ipAddress = "";
	private int port = -1;

	private final Stopwatch readTimer;
	private final Stopwatch totalTimer;
	private final Stopwatch DNSTimer;
	private final Stopwatch connectTimer;
	private final Stopwatch timeToFirstByteTimer;

	private boolean done = false;

	public SoapUIMetrics( final HttpTransportMetrics inTransportMetric, final HttpTransportMetrics outTransportMetric )
	{
		super( inTransportMetric, outTransportMetric );
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

		httpStatus = -1;
		contentLength = -1;

		done = true;
	}

	public boolean isDone()
	{
		return done;
	}

	public static String formatTimestamp( long timestamp )
	{
		return DateUtil.formatFull( new Date(timestamp) );
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
		return DateUtil.formatFull( new Date(getTimestamp()) );
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

	public String getHttpMethod()
	{
		return httpMethod;
	}

	public void setHttpMethod( String httpMethod )
	{
		this.httpMethod = httpMethod;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress( String ipAddress )
	{
		this.ipAddress = ipAddress;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort( int port, int defaultPort )
	{
		if( port != -1 )
		{
			this.port = port;
		}
		else
		{
			this.port = defaultPort;
		}
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
