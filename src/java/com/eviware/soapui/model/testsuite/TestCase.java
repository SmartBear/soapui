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

import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * A TestCase holding a number of TestSteps
 * 
 * @author Ole.Matzura
 */

public interface TestCase extends TestModelItem, ResultContainer
{
	public final static String STATUS_PROPERTY = TestCase.class.getName() + "@status";
	public final static String DISABLED_PROPERTY = TestCase.class.getName() + "@disabled";

	public TestSuite getTestSuite();

	public TestStep getTestStepAt( int index );

	public int getIndexOfTestStep( TestStep testStep );

	public int getTestStepCount();

	public List<TestStep> getTestStepList();

	public LoadTest getLoadTestAt( int index );

	public LoadTest getLoadTestByName( String loadTestName );

	public int getIndexOfLoadTest( LoadTest loadTest );

	public int getLoadTestCount();

	public List<LoadTest> getLoadTestList();

	public TestRunner run( StringToObjectMap contextProperties, boolean async );

	public void addTestRunListener( TestRunListener listener );

	public void removeTestRunListener( TestRunListener listener );

	public int getTestStepIndexByName( String stepName );

	public <T extends TestStep> T findPreviousStepOfType( TestStep referenceStep, Class<T> stepClass );

	public <T extends TestStep> T findNextStepOfType( TestStep referenceStep, Class<T> stepClass );

	public <T extends TestStep> List<T> getTestStepsOfType( Class<T> stepType );

	public void moveTestStep( int index, int offset );

	public TestStep getTestStepByName( String stepName );

	public boolean isDisabled();

	public String getLabel();
}
