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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.testsuite.CloneTestSuiteAction;
import com.eviware.soapui.support.UISupport;

public class TestSuiteToProjectDropHandler extends AbstractAfterModelItemDropHandler<WsdlTestSuite, WsdlProject>
{
	public TestSuiteToProjectDropHandler()
	{
		super( WsdlTestSuite.class, WsdlProject.class );
	}

	@Override
	boolean canCopyAfter( WsdlTestSuite source, WsdlProject target )
	{
		return true;
	}

	@Override
	boolean canMoveAfter( WsdlTestSuite source, WsdlProject target )
	{
		return source.getProject() != target;
	}

	@Override
	boolean copyAfter( WsdlTestSuite source, WsdlProject target )
	{
		String name = UISupport.prompt( "Specify name for copied TestSuite", "Copy TestSuite", "Copy of "
				+ source.getName() );
		if( name == null )
			return false;

		if( source.getProject() == target )
		{
			return CloneTestSuiteAction.cloneTestSuiteWithinProject( source, name, target );
		}
		else
		{
			return CloneTestSuiteAction.cloneToAnotherProject( source, target.getName(), name, false ) != null;
		}
	}

	@Override
	boolean moveAfter( WsdlTestSuite source, WsdlProject target )
	{
		String name = UISupport.prompt( "Specify name for moved TestSuite", "Move TestSuite", source.getName() );
		if( name == null )
			return false;

		WsdlTestSuite testSuite = CloneTestSuiteAction.cloneToAnotherProject( source, target.getName(), name, true );
		if( testSuite != null )
		{
			source.getProject().removeTestSuite( source );
			return true;
		}

		return false;
	}

	@Override
	String getCopyAfterInfo( WsdlTestSuite source, WsdlProject target )
	{
		return source.getProject() == target ? "Copy TestSuite [" + source.getName() + "] within Project ["
				+ target.getName() + "]" : "Copy TestSuite [" + source.getName() + "] to Project [" + target.getName()
				+ "]";
	}

	@Override
	String getMoveAfterInfo( WsdlTestSuite source, WsdlProject target )
	{
		return "Move TestSuite [" + source.getName() + "] to Project [" + target.getName() + "]";
	}

}
