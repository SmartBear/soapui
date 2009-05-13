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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

public class TestStepToTestStepDropHandler extends AbstractBeforeAfterModelItemDropHandler<WsdlTestStep, WsdlTestStep>
{
	public TestStepToTestStepDropHandler()
	{
		super( WsdlTestStep.class, WsdlTestStep.class );
	}

	boolean copyAfter( WsdlTestStep source, WsdlTestStep target )
	{
		return DragAndDropSupport.copyTestStep( source, target.getTestCase(), target.getTestCase().getIndexOfTestStep(
				target ) + 1 );
	}

	boolean moveAfter( WsdlTestStep source, WsdlTestStep target )
	{
		return DragAndDropSupport.moveTestStep( source, target.getTestCase(), target.getTestCase().getIndexOfTestStep(
				target ) + 1 );
	}

	@Override
	boolean canCopyAfter( WsdlTestStep source, WsdlTestStep target )
	{
		return !SoapUI.getTestMonitor().hasRunningTest( target.getTestCase() );
	}

	@Override
	boolean canMoveAfter( WsdlTestStep source, WsdlTestStep target )
	{
		return source != target;
	}

	@Override
	String getCopyAfterInfo( WsdlTestStep source, WsdlTestStep target )
	{
		return source.getTestCase() == target.getTestCase() ? "Copy TestStep [" + source.getName()
				+ "] within TestCase [" + target.getTestCase().getName() + "]" : "Copy TestStep [" + source.getName()
				+ "] to TestCase [" + target.getTestCase().getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlTestStep source, WsdlTestStep target )
	{
		return source.getTestCase() == target.getTestCase() ? "Move TestStep [" + source.getName()
				+ "] within TestCase [" + target.getTestCase().getName() + "]" : "Move TestStep [" + source.getName()
				+ "] to TestCase [" + target.getTestCase().getName() + "]";
	}

	@Override
	boolean canCopyBefore( WsdlTestStep source, WsdlTestStep target )
	{
		return true;
	}

	@Override
	boolean canMoveBefore( WsdlTestStep source, WsdlTestStep target )
	{
		return source != target;
	}

	@Override
	boolean copyBefore( WsdlTestStep source, WsdlTestStep target )
	{
		return DragAndDropSupport.copyTestStep( source, target.getTestCase(), target.getTestCase().getIndexOfTestStep(
				target ) );
	}

	@Override
	String getCopyBeforeInfo( WsdlTestStep source, WsdlTestStep target )
	{
		return getCopyAfterInfo( source, target );
	}

	@Override
	String getMoveBeforeInfo( WsdlTestStep source, WsdlTestStep target )
	{
		return getMoveAfterInfo( source, target );
	}

	@Override
	boolean moveBefore( WsdlTestStep source, WsdlTestStep target )
	{
		return DragAndDropSupport.moveTestStep( source, target.getTestCase(), target.getTestCase().getIndexOfTestStep(
				target ) );
	}
}
