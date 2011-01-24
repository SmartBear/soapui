/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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

import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerInterface;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * Adapter for SecurityTestStepRunListener implementations
 * 
 * @author dragica.soldo
 */
public class SecurityTestStepRunListenerAdapter implements SecurityTestStepRunListener
{

	@Override
	public void afterSecurityCheck( SecurityTestRunnerInterface testRunner, SecurityTestRunContext runContext,
			SecurityCheckRequestResult securityCheckResult )
	{
	}

	@Override
	public void afterStep( SecurityTestRunnerInterface testRunner, SecurityTestRunContext runContext,
			TestStepResult result )
	{
	}

	@Override
	public void beforeSecurityCheck( SecurityTestRunnerInterface testRunner, SecurityTestRunContext runContext,
			AbstractSecurityCheck securityCheck )
	{
	}

	@Override
	public void beforeStep( SecurityTestRunnerInterface testRunner, SecurityTestRunContext runContext )
	{
	}

}
