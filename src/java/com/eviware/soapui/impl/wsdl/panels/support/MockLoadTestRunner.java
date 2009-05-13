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

package com.eviware.soapui.impl.wsdl.panels.support;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunner;

public class MockLoadTestRunner implements LoadTestRunner
{
	private long startTime;
	private String reason;
	private final WsdlLoadTest loadTest;
	private final Logger logger;
	private Status status = Status.RUNNING;

	public MockLoadTestRunner( WsdlLoadTest modelItem, Logger logger )
	{
		loadTest = modelItem;
		this.logger = logger == null ? SoapUI.ensureGroovyLog() : logger;
		startTime = System.currentTimeMillis();
	}

	public void cancel( String reason )
	{
		this.reason = reason;
		status = Status.CANCELED;
		logger.info( "Canceled with reason [" + reason + "]" );
	}

	public void fail( String reason )
	{
		this.reason = reason;
		status = Status.CANCELED;
		logger.info( "Failed with reason [" + reason + "]" );
	}

	public LoadTest getLoadTest()
	{
		return loadTest;
	}

	public float getProgress()
	{
		return 0;
	}

	public String getReason()
	{
		return reason;
	}

	public int getRunningThreadCount()
	{
		return ( int )loadTest.getThreadCount();
	}

	public Status getStatus()
	{
		return status;
	}

	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
	}
}
