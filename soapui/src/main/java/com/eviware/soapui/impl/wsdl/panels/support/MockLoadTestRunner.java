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

package com.eviware.soapui.impl.wsdl.panels.support;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunner;

public class MockLoadTestRunner extends AbstractMockTestRunner<WsdlLoadTest> implements LoadTestRunner
{
	public MockLoadTestRunner( WsdlLoadTest modelItem, Logger logger )
	{
		super( modelItem, logger );
	}

	public WsdlLoadTest getLoadTest()
	{
		return getTestRunnable();
	}

	public float getProgress()
	{
		return 0;
	}

	public int getRunningThreadCount()
	{
		return ( int )getLoadTest().getThreadCount();
	}

	public boolean hasStopped()
	{
		return false;
	}
}
