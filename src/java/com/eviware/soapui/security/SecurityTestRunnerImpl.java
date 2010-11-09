/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
/**
 * 
 * 
 * @author soapUI team
 */

package com.eviware.soapui.security;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTestContext;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestStep;

public class SecurityTestRunnerImpl implements SecurityTestRunner
{

	private SecurityTest securityTest;
	private long startTime = 0;
	private Status status;
	private WsdlLoadTestContext context;

	public SecurityTestRunnerImpl( SecurityTest test )
	{
		this.securityTest = test;
		status = Status.INITIALIZED;
	}

	@Override
	public float getProgress()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	@Override
	public boolean hasStopped()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancel( String reason )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void fail( String reason )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getReason()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestRunContext getRunContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getStartTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	@Override
	public TestRunnable getTestRunnable()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeTaken()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void start( boolean async )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Status waitUntilFinished()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Clones original TestStep for security modification this does not alter the
	 * original test step
	 * 
	 * @param sourceTestStep
	 * @return TestStep
	 */
	private TestStep cloneForSecurityCheck( TestStep sourceTestStep )
	{
		TestStep clonedTestStep = null;
		return clonedTestStep;
	}

}
