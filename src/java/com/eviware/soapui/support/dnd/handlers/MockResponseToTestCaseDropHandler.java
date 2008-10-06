/*
 *  soapUI Pro, copyright (C) 2007-2008 eviware software ab 
 */

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.AddMockResponseToTestCaseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.action.SoapUIAction;

public class MockResponseToTestCaseDropHandler extends AbstractAfterModelItemDropHandler<WsdlMockResponse, WsdlTestCase>
{
	public MockResponseToTestCaseDropHandler()
	{
		super( WsdlMockResponse.class, WsdlTestCase.class );
	}
	
	@Override
	boolean canCopyAfter( WsdlMockResponse source, WsdlTestCase target )
	{
		return source.getMockOperation().getMockService().getProject() ==
			target.getTestSuite().getProject();
	}

	@Override
	boolean canMoveAfter( WsdlMockResponse source, WsdlTestCase target )
	{
		return canCopyAfter( source, target );
	}

	@Override
	boolean copyAfter( WsdlMockResponse source, WsdlTestCase target )
	{
		SoapUIAction<WsdlMockResponse> action = SoapUI.getActionRegistry().getAction( AddMockResponseToTestCaseAction.SOAPUI_ACTION_ID );
		AddMockResponseToTestCaseAction a = (AddMockResponseToTestCaseAction)action;
		
		a.addMockResponseToTestCase( source, target, -1 );
		return true;
	}

	@Override
	boolean moveAfter( WsdlMockResponse source, WsdlTestCase target )
	{
		return copyAfter( source, target );
	}

	@Override
	String getCopyAfterInfo( WsdlMockResponse source, WsdlTestCase target )
	{
		return "Add MockResponse TestStep to TestCase [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlMockResponse source, WsdlTestCase target )
	{
		return getCopyAfterInfo( source, target );
	}
}
