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
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToMockServiceAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

public class RequestToMockOperationDropHandler extends
		AbstractAfterModelItemDropHandler<WsdlRequest, WsdlMockOperation>
{
	public RequestToMockOperationDropHandler()
	{
		super( WsdlRequest.class, WsdlMockOperation.class );
	}

	@Override
	boolean canCopyAfter( WsdlRequest source, WsdlMockOperation target )
	{
		return source.getOperation() == target.getOperation();
	}

	@Override
	boolean canMoveAfter( WsdlRequest source, WsdlMockOperation target )
	{
		return source.getOperation() == target.getOperation();
	}

	@Override
	boolean copyAfter( WsdlRequest source, WsdlMockOperation target )
	{
		return addRequestToMockOperation( source, target );
	}

	private boolean addRequestToMockOperation( WsdlRequest request, WsdlMockOperation mockOperation )
	{
		if( !UISupport.confirm( "Add request to MockOperation [" + mockOperation.getName() + "]", "Add Request" ) )
			return false;

		SoapUIAction<WsdlRequest> action = SoapUI.getActionRegistry().getAction(
				AddRequestToMockServiceAction.SOAPUI_ACTION_ID );
		( ( AddRequestToMockServiceAction )action ).perform( request, mockOperation );
		return true;
	}

	@Override
	boolean moveAfter( WsdlRequest source, WsdlMockOperation target )
	{
		return addRequestToMockOperation( source, target );
	}

	@Override
	String getCopyAfterInfo( WsdlRequest source, WsdlMockOperation target )
	{
		return "Add Request [" + source.getName() + "] to MockOperation [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlRequest source, WsdlMockOperation target )
	{
		return getCopyAfterInfo( source, target );
	}
}
