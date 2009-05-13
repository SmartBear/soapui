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

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.tree.nodes.support.WsdlTestStepsModelItem;

public class TestStepToTestStepsDropHandler extends
		AbstractAfterModelItemDropHandler<WsdlTestStep, WsdlTestStepsModelItem>
{
	public TestStepToTestStepsDropHandler()
	{
		super( WsdlTestStep.class, WsdlTestStepsModelItem.class );
	}

	boolean copyAfter( WsdlTestStep source, WsdlTestStepsModelItem target )
	{
		return DragAndDropSupport.copyTestStep( source, target.getTestCase(), 0 );
	}

	boolean moveAfter( WsdlTestStep source, WsdlTestStepsModelItem target )
	{
		return DragAndDropSupport.moveTestStep( source, target.getTestCase(), 0 );
	}

	@Override
	boolean canCopyAfter( WsdlTestStep source, WsdlTestStepsModelItem target )
	{
		return true;
	}

	@Override
	boolean canMoveAfter( WsdlTestStep source, WsdlTestStepsModelItem target )
	{
		return true;
	}

	@Override
	String getCopyAfterInfo( WsdlTestStep source, WsdlTestStepsModelItem target )
	{
		return source.getTestCase() == target.getTestCase() ? "Copy TestStep [" + source.getName()
				+ "] within TestCase [" + target.getTestCase().getName() + "]" : "Copy TestStep [" + source.getName()
				+ "] to TestCase [" + target.getTestCase().getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlTestStep source, WsdlTestStepsModelItem target )
	{
		return source.getTestCase() == target.getTestCase() ? "Move TestStep [" + source.getName()
				+ "] within TestCase [" + target.getTestCase().getName() + "]" : "Move TestStep [" + source.getName()
				+ "] to TestCase [" + target.getTestCase().getName() + "]";
	}
}
