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
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner;

/**
 * Dummy TestRunner used when executing TestSteps one by one
 * 
 * @author ole.matzura
 */

public abstract class AbstractMockTestRunner<T extends TestRunnable> implements TestRunner
{
	private long startTime;
	private String reason;
	private final T modelItem;
	private final Logger logger;
	private Status status = Status.RUNNING;
	private TestRunContext context;

	public AbstractMockTestRunner( T modelItem, Logger logger )
	{
		this.modelItem = modelItem;
		this.logger = logger == null ? SoapUI.ensureGroovyLog() : logger;
		startTime = System.currentTimeMillis();
	}
	
	public void setRunContext( TestRunContext context )
	{
		this.context = context;
	}

	public TestRunContext getRunContext()
	{
		return context;
	}

	public Logger getLog()
	{
		return logger;
	}

	public T getTestRunnable()
	{
		return modelItem;
	}

	public Status getStatus()
	{
		return status;
	}

	public void start( boolean async )
	{
		logger.info( "Started with async [" + async + "]" );
		startTime = System.currentTimeMillis();
	}
	
	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
	}

	public Status waitUntilFinished()
	{
		status = Status.FINISHED;
		return status;
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
		status = Status.FAILED;
		logger.error( "Failed with reason [" + reason + "]" );
	}

	public long getStartTime()
	{
		return startTime;
	}

	public String getReason()
	{
		return reason;
	}
}