/*
 *  soapUI Pro, copyright (C) 2007-2008 eviware software ab 
 */

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.AddMockResponseToTestCaseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.model.tree.nodes.support.WsdlTestStepsModelItem;
import com.eviware.soapui.support.action.SoapUIAction;

public class MockResponseToTestStepsDropHandler extends AbstractAfterModelItemDropHandler<WsdlMockResponse, WsdlTestStepsModelItem>
{
	public MockResponseToTestStepsDropHandler()
	{
		super( WsdlMockResponse.class, WsdlTestStepsModelItem.class );
	}
	
	@Override
	boolean canCopyAfter( WsdlMockResponse source, WsdlTestStepsModelItem target )
	{
		return source.getMockOperation().getMockService().getProject() ==
			target.getTestCase().getTestSuite().getProject();
	}

	@Override
	boolean canMoveAfter( WsdlMockResponse source, WsdlTestStepsModelItem target )
	{
		return canCopyAfter( source, target );
	}

	@Override
	boolean copyAfter( WsdlMockResponse source, WsdlTestStepsModelItem target )
	{
		SoapUIAction<WsdlMockResponse> action = SoapUI.getActionRegistry().getAction( AddMockResponseToTestCaseAction.SOAPUI_ACTION_ID );
		AddMockResponseToTestCaseAction a = (AddMockResponseToTestCaseAction)action;
		
		a.addMockResponseToTestCase( source, target.getTestCase(), 0 );
		return true;
	}

	@Override
	boolean moveAfter( WsdlMockResponse source, WsdlTestStepsModelItem target )
	{
		return copyAfter( source, target );
	}

	@Override
	String getCopyAfterInfo( WsdlMockResponse source, WsdlTestStepsModelItem target )
	{
		return "Add MockResponse TestStep to beginning of TestCase [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlMockResponse source, WsdlTestStepsModelItem target )
	{
		return getCopyAfterInfo( source, target );
	}
}
