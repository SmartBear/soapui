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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.project.Project;

/**
 * A TestSuite holding a number of TestCases
 * 
 * @author Ole.Matzura
 */

public interface TestSuite extends TestModelItem, ResultContainer
{
	public final static String RUNTYPE_PROPERTY = ModelItem.class.getName() + "@runtype";
	public final static String DISABLED_PROPERTY = TestSuite.class.getName() + "@disabled";

	public Project getProject();

	public int getTestCaseCount();

	public TestCase getTestCaseAt( int index );

	public TestCase getTestCaseByName( String testCaseName );

	public List<TestCase> getTestCaseList();

	public void addTestSuiteListener( TestSuiteListener listener );

	public void removeTestSuiteListener( TestSuiteListener listener );

	public enum TestSuiteRunType
	{
		PARALLEL, SEQUENTIAL
	};

	public TestSuiteRunType getRunType();

	public int getIndexOfTestCase( TestCase testCase );

	public boolean isDisabled();

	public String getLabel();
}
