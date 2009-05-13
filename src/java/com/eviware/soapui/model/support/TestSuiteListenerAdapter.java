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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;

/**
 * Adapter for TestSuiteListener implementations
 * 
 * @author Ole.Matzura
 */

public class TestSuiteListenerAdapter implements TestSuiteListener
{
	public void testCaseAdded( TestCase testCase )
	{
	}

	public void testCaseRemoved( TestCase testCase )
	{
	}

	public void testStepAdded( TestStep testStep, int index )
	{
	}

	public void testStepRemoved( TestStep testStep, int index )
	{
	}

	public void loadTestAdded( LoadTest loadTest )
	{
	}

	public void loadTestRemoved( LoadTest loadTest )
	{
	}

	public void testStepMoved( TestStep testStep, int fromIndex, int offset )
	{
	}

	public void testCaseMoved( TestCase testCase, int index, int offset )
	{
	}
}
