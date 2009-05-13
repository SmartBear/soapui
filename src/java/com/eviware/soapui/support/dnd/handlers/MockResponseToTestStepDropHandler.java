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
import com.eviware.soapui.impl.wsdl.actions.mockresponse.AddMockResponseToTestCaseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.action.SoapUIAction;

public class MockResponseToTestStepDropHandler extends
		AbstractAfterModelItemDropHandler<WsdlMockResponse, WsdlTestStep>
{
	public MockResponseToTestStepDropHandler()
	{
		super( WsdlMockResponse.class, WsdlTestStep.class );
	}

	@Override
	boolean canCopyAfter( WsdlMockResponse source, WsdlTestStep target )
	{
		return source.getMockOperation().getMockService().getProject() == target.getTestCase().getTestSuite()
				.getProject();
	}

	@Override
	boolean canMoveAfter( WsdlMockResponse source, WsdlTestStep target )
	{
		return canCopyAfter( source, target );
	}

	@Override
	boolean copyAfter( WsdlMockResponse source, WsdlTestStep target )
	{
		SoapUIAction<WsdlMockResponse> action = SoapUI.getActionRegistry().getAction(
				AddMockResponseToTestCaseAction.SOAPUI_ACTION_ID );
		AddMockResponseToTestCaseAction a = ( AddMockResponseToTestCaseAction )action;

		a.addMockResponseToTestCase( source, target.getTestCase(), target.getTestCase().getIndexOfTestStep( target ) + 1 );
		return true;
	}

	@Override
	boolean moveAfter( WsdlMockResponse source, WsdlTestStep target )
	{
		return copyAfter( source, target );
	}

	@Override
	String getCopyAfterInfo( WsdlMockResponse source, WsdlTestStep target )
	{
		return "Insert MockResponse TestStep in TestCase [" + target.getTestCase().getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlMockResponse source, WsdlTestStep target )
	{
		return getCopyAfterInfo( source, target );
	}
}
