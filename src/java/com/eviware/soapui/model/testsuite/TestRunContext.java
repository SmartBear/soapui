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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

/**
 * Context information for a testcase run session
 */

public interface TestRunContext extends SubmitContext, PropertyExpansionContext
{
	public final static String LOAD_TEST_RUNNER = "LoadTestRunner";
	public static final String THREAD_INDEX = "ThreadIndex";
	public static final String RUN_COUNT = "RunCount";
	public static final String TOTAL_RUN_COUNT = "TotalRunCount";
	public static final String LOAD_TEST_CONTEXT = "LoadTestContext";
	public static final String INTERACTIVE = "Headless";

	public TestStep getCurrentStep();

	public int getCurrentStepIndex();

	public TestRunner getTestRunner();

	public Object getProperty( String testStep, String propertyName );

	public TestCase getTestCase();

	public String expand( String content );
}
