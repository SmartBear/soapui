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
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestStepResult;

/**
 * Listener for TestRun-related events, schedule events will only be triggered
 * for LoadTest runs.
 * 
 * @author Dragica
 */

public interface SecurityTestRunListener
{
	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext );

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext );

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep );

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, SecurityTestStepResult result );

}
