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

package com.eviware.soapui.impl.wsdl.actions.support;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Base class for actions that add TestSteps to a TestCase
 * 
 * @author ole.matzura
 */

public abstract class AbstractAddToTestCaseAction<T extends ModelItem> extends AbstractSoapUIAction<T>
{
	public AbstractAddToTestCaseAction( String name, String description )
	{
		super( name, description );
	}

	protected WsdlTestCase getTargetTestCase( WsdlProject project )
	{
		List<WsdlTestCase> testCases = new ArrayList<WsdlTestCase>();
		List<WsdlTestSuite> testSuites = new ArrayList<WsdlTestSuite>();
		List<String> testCaseNames = new ArrayList<String>();
		WsdlTestCase testCase = null;

		if( project.getTestSuiteCount() == 0 )
		{
			return addNewTestSuiteAndTestCase( project );
		}

		for( int c = 0; c < project.getTestSuiteCount(); c++ )
		{
			WsdlTestSuite testSuite = ( WsdlTestSuite )project.getTestSuiteAt( c );
			for( int i = 0; i < testSuite.getTestCaseCount(); i++ )
			{
				testCase = ( WsdlTestCase )testSuite.getTestCaseAt( i );

				testCases.add( testCase );
				testCaseNames.add( ( testCaseNames.size() + 1 ) + ": " + testSuite.getName() + " - " + testCase.getName() );
				testSuites.add( testSuite );
			}

			testCases.add( null );
			testSuites.add( testSuite );
			testCaseNames.add( ( testCaseNames.size() + 1 ) + ": " + testSuite.getName() + " -> Create new TestCase" );
		}

		if( testCases.size() == 0 )
		{
			List<String> testSuiteNames = new ArrayList<String>();

			for( int c = 0; c < project.getTestSuiteCount(); c++ )
			{
				TestSuite testSuite = project.getTestSuiteAt( c );
				testSuiteNames.add( ( testSuiteNames.size() + 1 ) + ": " + testSuite.getName() );
			}

			String selection = ( String )UISupport.prompt( "Select TestSuite to create TestCase in", "Select TestSuite",
					testSuiteNames.toArray() );
			if( selection == null )
				return null;

			WsdlTestSuite testSuite = ( WsdlTestSuite )project.getTestSuiteAt( testSuiteNames.indexOf( selection ) );

			String name = UISupport.prompt( "Enter name for TestCase create", "Create TestCase", "TestCase "
					+ ( testSuite.getTestCaseCount() + 1 ) );
			if( name == null )
				return null;

			return testSuite.addNewTestCase( name );
		}
		else
		{
			testCases.add( null );
			testSuites.add( null );
			testCaseNames.add( ( testCaseNames.size() + 1 ) + ": -> Create new TestSuite" );

			String selection = ( String )UISupport.prompt( "Select TestCase", "Select TestCase", testCaseNames.toArray() );
			if( selection == null )
				return null;

			testCase = testCases.get( testCaseNames.indexOf( selection ) );
			while( testCase != null && SoapUI.getTestMonitor().hasRunningLoadTest( testCase ) )
			{
				UISupport.showErrorMessage( "Can not add to TestCase that is currently LoadTesting" );

				selection = ( String )UISupport.prompt( "Select TestCase", "Select TestCase", testCaseNames.toArray() );
				if( selection == null )
					return null;

				testCase = testCases.get( testCaseNames.indexOf( selection ) );
			}

			// selected create new?
			if( testCase == null )
			{
				WsdlTestSuite testSuite = testSuites.get( testCaseNames.indexOf( selection ) );

				// selected create new testsuite?
				if( testSuite == null )
				{
					return addNewTestSuiteAndTestCase( project );
				}
				else
				{
					String name = UISupport.prompt( "Enter name for TestCase create", "Create TestCase", "TestCase "
							+ ( testSuite.getTestCaseCount() + 1 ) );
					if( name == null )
						return null;

					return testSuite.addNewTestCase( name );
				}
			}
		}

		return testCase;
	}

	protected WsdlTestCase addNewTestSuiteAndTestCase( WsdlProject project )
	{
		String testSuiteName = UISupport.prompt( "Missing TestSuite in project, enter name to create",
				"Create TestSuite", "TestSuite " + ( project.getTestSuiteCount() + 1 ) );
		if( testSuiteName == null )
			return null;

		String testCaseName = UISupport.prompt( "Enter name for TestCase create", "Create TestCase", "TestCase 1" );
		if( testCaseName == null )
			return null;

		WsdlTestSuite testSuite = ( WsdlTestSuite )project.addNewTestSuite( testSuiteName );
		return testSuite.addNewTestCase( testCaseName );
	}
}