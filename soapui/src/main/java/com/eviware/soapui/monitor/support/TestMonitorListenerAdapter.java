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

package com.eviware.soapui.monitor.support;

import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.monitor.TestMonitorListener;
import com.eviware.soapui.security.SecurityTestRunner;

/**
 * Adapter for TestMonitorListener implementations
 * 
 * @author Ole.Matzura
 */

public class TestMonitorListenerAdapter implements TestMonitorListener
{
	public void loadTestStarted( LoadTestRunner runner )
	{
	}

	public void loadTestFinished( LoadTestRunner runner )
	{
	}

	public void securityTestStarted( SecurityTestRunner runner )
	{
	}

	public void securityTestFinished( SecurityTestRunner runner )
	{
	}

	public void testCaseStarted( TestCaseRunner runner )
	{
	}

	public void testCaseFinished( TestCaseRunner runner )
	{
	}

	public void mockServiceStarted( MockRunner runner )
	{
	}

	public void mockServiceStopped( MockRunner runner )
	{
	}
}
