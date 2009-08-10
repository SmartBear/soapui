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

package com.eviware.soapui.impl.wsdl.support;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * WSDL TestCase Runner - runs all steps in a testcase and collects performance
 * data
 * 
 * @author Ole.Matzura
 */

public abstract class AbstractTestRunner<T extends TestRunnable, T2 extends TestRunContext> implements Runnable,
		TestRunner
{
	private final T testRunnable;
	private Status status;
	private Throwable error;
	private T2 runContext;
	private long startTime;
	private String reason;
	private volatile Future<?> future;
	private int id;
	private final static ExecutorService threadPool = Executors.newCachedThreadPool();

	private final static Logger log = Logger.getLogger( AbstractTestRunner.class );

	private static int idCounter = 0;

	private Timer timeoutTimer;
	private TimeoutTimerTask timeoutTimerTask;
	private Thread thread;

	public AbstractTestRunner( T modelItem, StringToObjectMap properties )
	{
		this.testRunnable = modelItem;
		status = Status.INITIALIZED;
		id = ++idCounter;

		runContext = createContext( properties );
	}

	public abstract T2 createContext( StringToObjectMap properties );

	public T2 getRunContext()
	{
		return runContext;
	}

	public void start( boolean async )
	{
		status = Status.RUNNING;
		if( async )
			future = threadPool.submit( this );
		else
			run();
	}

	public void cancel( String reason )
	{
		if( status == Status.CANCELED || status == Status.FINISHED || status == Status.FAILED || runContext == null )
			return;
		onCancel( reason );
		status = Status.CANCELED;
		this.reason = reason;
	}

	protected void onCancel( String reason2 )
	{
	}

	public void fail( String reason )
	{
		if( status == Status.CANCELED || status == Status.FAILED || runContext == null )
			return;
		onFail( reason );
		status = Status.FAILED;
		this.reason = reason;
	}

	protected void onFail( String reason )
	{
	}

	public Status getStatus()
	{
		return status;
	}

	public int getId()
	{
		return id;
	}

	public Thread getThread()
	{
		return thread;
	}

	public void run()
	{
		if( future != null )
		{
			thread = Thread.currentThread();
			thread.setName( "TestRunner Thread for " + testRunnable.getName() );
		}

		try
		{
			status = Status.RUNNING;
			startTime = System.currentTimeMillis();

			internalRun( runContext );
		}
		catch( Throwable t )
		{
			log.error( "Exception during Test Execution", t );

			if( t instanceof OutOfMemoryError && UISupport.confirm( "Exit now without saving?", "Out of Memory Error" ) )
			{
				System.exit( 0 );
			}

			status = Status.FAILED;
			error = t;
			reason = t.toString();
		}
		finally
		{
			if( timeoutTimer != null )
			{
				timeoutTimer.cancel();
			}

			if( status == Status.RUNNING )
			{
				status = Status.FINISHED;
			}

			internalFinally( runContext );
		}
	}

	public boolean isRunning()
	{
		return getStatus() == Status.RUNNING;
	}

	public boolean isCanceled()
	{
		return getStatus() == Status.CANCELED;
	}

	public boolean isFailed()
	{
		return getStatus() == Status.FAILED;
	}

	protected void setStatus( Status status )
	{
		this.status = status;
	}

	protected void setError( Throwable error )
	{
		this.error = error;
	}

	protected abstract void internalRun( T2 runContext2 ) throws Exception;

	protected abstract void internalFinally( T2 runContext2 );

	protected void startTimeoutTimer( long timeout )
	{
		timeoutTimer = new Timer();
		timeoutTimerTask = new TimeoutTimerTask();
		timeoutTimer.schedule( timeoutTimerTask, timeout );
	}

	public T getTestRunnable()
	{
		return testRunnable;
	}

	public synchronized Status waitUntilFinished()
	{
		if( future != null )
		{
			if( !future.isDone() )
			{
				try
				{
					future.get();
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
		else
			throw new RuntimeException( "cannot wait on null future" );

		return getStatus();
	}

	public long getTimeTaken()
	{
		return System.currentTimeMillis() - startTime;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public Throwable getError()
	{
		return error;
	}

	public String getReason()
	{
		return reason == null ? error == null ? null : error.toString() : reason;
	}

	private final class TimeoutTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			fail( "TestCase timed out" );
		}
	}

	
}