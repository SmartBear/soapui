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

import com.eviware.soapui.model.TestModelItem;

/**
 * A TestStep in a TestCase
 * 
 * @author Ole.Matzura
 */

public interface TestStep extends TestModelItem, ResultContainer
{
	public final static String DISABLED_PROPERTY = TestStep.class.getName() + "@disabled";

	public TestCase getTestCase();

	public void prepare( TestRunner testRunner, TestRunContext testRunContext ) throws Exception;

	public void finish( TestRunner testRunner, TestRunContext testRunContext );

	public boolean cancel();

	public TestStepResult run( TestRunner testRunner, TestRunContext testRunContext );

	public boolean isDisabled();

	public String getLabel();
}
