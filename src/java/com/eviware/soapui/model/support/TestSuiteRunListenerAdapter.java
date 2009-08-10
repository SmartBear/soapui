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

import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class TestSuiteRunListenerAdapter implements TestSuiteRunListener
{

	public void afterRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
	{
	}

	public void afterTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCaseRunner testCaseRunner )
	{
	}

	public void beforeRun( TestSuiteRunner testRunner, TestSuiteRunContext runContext )
	{
	}

	public void beforeTestCase( TestSuiteRunner testRunner, TestSuiteRunContext runContext, TestCase testCase )
	{
	}

}
