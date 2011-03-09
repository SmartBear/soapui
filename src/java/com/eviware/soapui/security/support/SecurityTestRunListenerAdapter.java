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

import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;

/**
 * Adapter for SecurityTestRunListener implementations
 * 
 * @author dragica.soldo
 */

public class SecurityTestRunListenerAdapter implements SecurityTestRunListener
{

	@Override
	public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
	}

	@Override
	public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStep testStep )
	{
	}

	@Override
	public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result )
	{
	}

	@Override
	public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext )
	{
	}

	@Override
	public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckResult securityCheckResult )
	{
	}

	@Override
	public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			AbstractSecurityCheck securityCheck )
	{
	}

}
