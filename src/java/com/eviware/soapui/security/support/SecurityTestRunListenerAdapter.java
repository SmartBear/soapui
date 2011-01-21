/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.support;

import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityTestRunnerInterface;

/**
 * Adapter for SecurityTestRunListener implementations
 * 
 * @author dragica.soldo
 */

public class SecurityTestRunListenerAdapter implements SecurityTestRunListener
{

	public void beforeRun( SecurityTestRunnerInterface testRunner, TestCaseRunContext runContext )
	{
	}

	public void beforeStep( SecurityTestRunnerInterface testRunner, TestCaseRunContext runContext )
	{
	}

	public void afterStep( SecurityTestRunnerInterface testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
	}

	public void afterRun( SecurityTestRunnerInterface testRunner, TestCaseRunContext runContext )
	{
	}

	public void beforeStep( SecurityTestRunnerInterface testRunner, TestCaseRunContext runContext, TestStep testStep )
	{
	}

}
