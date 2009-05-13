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

package com.eviware.soapui.monitor.support;

import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.monitor.TestMonitorListener;

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

	public void testCaseStarted( TestRunner runner )
	{
	}

	public void testCaseFinished( TestRunner runner )
	{
	}

	public void mockServiceStarted( MockRunner runner )
	{
	}

	public void mockServiceStopped( MockRunner runner )
	{
	}
}
