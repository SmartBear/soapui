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
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;

/**
 * Listener for SecurityTestRun-related events
 * 
 * @author Dragica
 */

public interface SecurityTestRunListener
{
	public void beforeRun( TestCaseRunner testRunner, SecurityTestRunContext runContext );

	public void afterRun( TestCaseRunner testRunner, SecurityTestRunContext runContext );

	public void beforeStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStep testStep );

	public void afterStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result );

	public void afterOriginalStep( TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result );

	public void beforeSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			AbstractSecurityCheck securityCheck );

	public void afterSecurityCheck( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckResult securityCheckResult );

	public void afterSecurityCheckRequest( TestCaseRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckRequestResult securityCheckReqResult );
}
