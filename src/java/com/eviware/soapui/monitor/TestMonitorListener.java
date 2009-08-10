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

package com.eviware.soapui.monitor;

import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;

/**
 * Listener for TestMonitor events
 * 
 * @author Ole.Matzura
 */

public interface TestMonitorListener
{
	public void loadTestStarted( LoadTestRunner runner );

	public void loadTestFinished( LoadTestRunner runner );

	public void testCaseStarted( TestCaseRunner runner );

	public void testCaseFinished( TestCaseRunner runner );

	public void mockServiceStarted( MockRunner runner );

	public void mockServiceStopped( MockRunner runner );
}
