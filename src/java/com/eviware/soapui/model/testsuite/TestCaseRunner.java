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

import java.util.List;

/**
 * Runs a TestCase
 * 
 * @author Ole.Matzura
 */

public interface TestCaseRunner extends TestRunner
{
	/**
	 * Gets the TestCase being run
	 * 
	 * @return the TestCase being run
	 */

	public TestCase getTestCase();

	/**
	 * Gets the accumulated results so far; each TestStep returns a
	 * TestStepResult when running.
	 * 
	 * @return the accumulated results so far
	 */

	public List<TestStepResult> getResults();

	/**
	 * Transfers execution of this TestRunner to the TestStep with the specified
	 * index in the TestCase
	 */

	public void gotoStep( int index );

	/**
	 * Transfers execution of this TestRunner to the TestStep with the specified
	 * name in the TestCase
	 */

	public void gotoStepByName( String stepName );

	/**
	 * Runs the specified TestStep and returns the result
	 */

	public TestStepResult runTestStepByName( String name );

	/**
	 * Returns the context used by this runner
	 */

	public TestCaseRunContext getRunContext();
}