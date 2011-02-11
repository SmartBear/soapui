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

import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * Listener for TestRun-related events, schedule events will only be triggered
 * for LoadTest runs.
 * 
 * @author dragica.soldo
 */

public interface SecurityCheckRunListener
{
	public void beforeSecurityCheck( SecurityTestRunner testRunner, SecurityTestRunContext runContext,
			AbstractSecurityCheck securityCheck );

	public void afterSecurityCheck( SecurityTestRunner testRunner, SecurityTestRunContext runContext,
			SecurityCheckResult securityCheckResult );
}
