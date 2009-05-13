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

package com.eviware.soapui.impl.wsdl.teststeps;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.mock.MockRunnerManager;
import com.eviware.soapui.impl.wsdl.mock.MockRunnerManagerException;
import com.eviware.soapui.impl.wsdl.mock.MockRunnerManagerImpl;
import com.eviware.soapui.model.support.LoadTestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;

public class LoadTestRunListenerImpl extends LoadTestRunListenerAdapter
{
	private final static Logger log = Logger.getLogger( LoadTestRunListenerImpl.class );

	@Override
	public void loadTestStarted( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		TestCase testCase = loadTestRunner.getLoadTest().getTestCase();

		if( needsMockRunnerManager( testCase ) )
		{
			MockRunnerManager manager = MockRunnerManagerImpl.getInstance( testCase );

			try
			{
				manager.start();
			}
			catch( MockRunnerManagerException e )
			{
				log.error( "Unable to start MockRunnerManager", e );
			}
		}
	}

	@Override
	public void loadTestStopped( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		TestCase testCase = loadTestRunner.getLoadTest().getTestCase();

		MockRunnerManager manager = MockRunnerManagerImpl.getInstance( testCase );

		if( manager != null && manager.isStarted() )
		{
			manager.stop();
		}
	}

	@Override
	public void afterLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		loadTestStopped( loadTestRunner, context );
	}

	private boolean needsMockRunnerManager( TestCase testCase )
	{
		return testCase.getTestStepsOfType( WsdlAsyncResponseTestStep.class ).size() > 0;
	}
}
