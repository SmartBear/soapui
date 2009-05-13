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
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.actions.operation.AddOperationToMockServiceAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.action.SoapUIAction;

public class OperationToMockServiceDropHandler extends
		AbstractAfterModelItemDropHandler<WsdlOperation, WsdlMockService>
{
	public OperationToMockServiceDropHandler()
	{
		super( WsdlOperation.class, WsdlMockService.class );
	}

	@Override
	boolean canCopyAfter( WsdlOperation source, WsdlMockService target )
	{
		return source.getInterface().getProject() == target.getProject();
	}

	@Override
	boolean canMoveAfter( WsdlOperation source, WsdlMockService target )
	{
		return canCopyAfter( source, target );
	}

	@Override
	boolean copyAfter( WsdlOperation source, WsdlMockService target )
	{
		SoapUIAction<WsdlOperation> action = SoapUI.getActionRegistry().getAction(
				AddOperationToMockServiceAction.SOAPUI_ACTION_ID );
		AddOperationToMockServiceAction a = ( AddOperationToMockServiceAction )action;

		return a.addOperationToMockService( source, target );
	}

	@Override
	boolean moveAfter( WsdlOperation source, WsdlMockService target )
	{
		return copyAfter( source, target );
	}

	@Override
	String getCopyAfterInfo( WsdlOperation source, WsdlMockService target )
	{
		return "Add MockOperation for [" + source.getName() + "] to MockService [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlOperation source, WsdlMockService target )
	{
		return getCopyAfterInfo( source, target );
	}

}
