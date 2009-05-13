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

package com.eviware.soapui.support.dnd.handlers;

import java.util.HashSet;
import java.util.Set;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;

public class TestCaseToTestSuiteDropHandler extends AbstractAfterModelItemDropHandler<WsdlTestCase, WsdlTestSuite>
{
	public TestCaseToTestSuiteDropHandler()
	{
		super( WsdlTestCase.class, WsdlTestSuite.class );
	}

	@Override
	boolean canCopyAfter( WsdlTestCase source, WsdlTestSuite target )
	{
		return true;
	}

	@Override
	boolean canMoveAfter( WsdlTestCase source, WsdlTestSuite target )
	{
		return true;
	}

	@Override
	boolean copyAfter( WsdlTestCase source, WsdlTestSuite target )
	{
		WsdlTestCase testCase = copyTestCase( source, target, 0 );
		if( testCase != null )
			UISupport.select( testCase );

		return testCase != null;
	}

	public static WsdlTestCase copyTestCase( WsdlTestCase testCase, WsdlTestSuite target, int position )
	{
		String name = UISupport.prompt( "Specify name of copied TestCase", "Copy TestCase", "Copy of "
				+ testCase.getName() );
		if( name == null )
			return null;

		if( testCase.getTestSuite() == target )
		{
			return target.importTestCase( testCase, name, position, true, true );
		}
		else if( testCase.getTestSuite().getProject() == target.getProject() )
		{
			return target.importTestCase( testCase, name, position, true, true );
		}
		else
		{
			Set<Interface> requiredInterfaces = new HashSet<Interface>();

			// get required interfaces
			for( int y = 0; y < testCase.getTestStepCount(); y++ )
			{
				WsdlTestStep testStep = testCase.getTestStepAt( y );
				requiredInterfaces.addAll( testStep.getRequiredInterfaces() );
			}

			if( DragAndDropSupport.importRequiredInterfaces( target.getProject(), requiredInterfaces, "Copy TestCase" ) )
			{
				return target.importTestCase( testCase, name, position, true, true );
			}
		}

		return null;
	}

	@Override
	boolean moveAfter( WsdlTestCase source, WsdlTestSuite target )
	{
		WsdlTestCase testCase = moveTestCase( source, target, 0 );
		if( testCase != null )
			UISupport.select( testCase );

		return testCase != null;
	}

	public static WsdlTestCase moveTestCase( WsdlTestCase testCase, WsdlTestSuite target, int position )
	{
		if( testCase.getTestSuite() == target )
		{
			int ix = target.getIndexOfTestCase( testCase );

			if( position == -1 )
			{
				target.moveTestCase( ix, target.getTestCaseCount() - ix );
			}
			else if( ix >= 0 && position != ix )
			{
				int offset = position - ix;
				if( offset > 0 )
					offset-- ;
				target.moveTestCase( ix, offset );
			}
		}
		else if( testCase.getTestSuite().getProject() == target.getProject() )
		{
			if( UISupport.confirm( "Move TestCase [" + testCase.getName() + "] to TestSuite [" + target.getName() + "]",
					"Move TestCase" ) )
			{
				WsdlTestCase importedTestCase = target.importTestCase( testCase, testCase.getName(), position, true, false );
				if( importedTestCase != null )
				{
					testCase.getTestSuite().removeTestCase( testCase );
					return importedTestCase;
				}
			}
		}
		else if( UISupport.confirm( "Move TestCase [" + testCase.getName() + "] to TestSuite [" + target.getName() + "]",
				"Move TestCase" ) )
		{
			Set<Interface> requiredInterfaces = new HashSet<Interface>();

			// get required interfaces
			for( int y = 0; y < testCase.getTestStepCount(); y++ )
			{
				WsdlTestStep testStep = testCase.getTestStepAt( y );
				requiredInterfaces.addAll( testStep.getRequiredInterfaces() );
			}

			if( DragAndDropSupport.importRequiredInterfaces( target.getProject(), requiredInterfaces, "Move TestCase" ) )
			{
				WsdlTestCase importedTestCase = target.importTestCase( testCase, testCase.getName(), position, true, false );
				if( importedTestCase != null )
				{
					testCase.getTestSuite().removeTestCase( testCase );
					return importedTestCase;
				}
			}
		}

		return null;
	}

	@Override
	String getCopyAfterInfo( WsdlTestCase source, WsdlTestSuite target )
	{
		return "Copy TestCase [" + source.getName() + "] to TestSuite [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlTestCase source, WsdlTestSuite target )
	{
		return "Move TestCase [" + source.getName() + "] to TestSuite [" + target.getName() + "]";
	}

}
