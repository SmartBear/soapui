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

import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * Adapter for TestRunListener implementations
 * 
 * @author Ole.Matzura
 */

public class TestRunListenerAdapter implements TestRunListener
{

	public void beforeRun( TestRunner testRunner, TestRunContext runContext )
	{
	}

	public void beforeStep( TestRunner testRunner, TestRunContext runContext )
	{
	}

	public void afterStep( TestRunner testRunner, TestRunContext runContext, TestStepResult result )
	{
	}

	public void afterRun( TestRunner testRunner, TestRunContext runContext )
	{
	}
}
