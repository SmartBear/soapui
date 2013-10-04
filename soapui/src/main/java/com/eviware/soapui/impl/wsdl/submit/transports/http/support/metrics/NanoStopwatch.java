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

public class NanoStopwatch implements Stopwatch
{

	protected long start;

	protected long stop;

	@Override
	public long getDuration()
	{
		long nanoTime = stop - start;
		// removing time differences by excluding rounding errors
		// so we collect data in milliseconds
		long msTime = nanoTime;/// 1000000;
		return msTime;
	}

	@Override
	public void start()
	{
		start = getCurrentTime();
	}

	@Override
	public void stop()
	{
		stop = getCurrentTime();
	}

	public long getStart()
	{
		return start;
	}

	public long getStop()
	{
		return stop;
	}

	@Override
	public void reset()
	{
		start = 0;
		stop = 0;
	}

	protected long getCurrentTime()
	{
		// return System.nanoTime();
		return System.currentTimeMillis();
	}

	@Override
	public void add( long value )
	{
		stop += value;
	}

	public boolean isStarted()
	{
		return getStart() > 0;
	}

	public boolean isStopped()
	{
		return getStop() > 0;
	}

	public void set( long start, long stop )
	{
		this.start = start;
		this.stop = stop;
	}

}
