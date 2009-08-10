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
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.mockservice.CloneMockServiceAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

public class MockServiceToProjectDropHandler extends AbstractAfterModelItemDropHandler<WsdlMockService, WsdlProject>
{
	public MockServiceToProjectDropHandler()
	{
		super( WsdlMockService.class, WsdlProject.class );
	}

	@Override
	boolean canCopyAfter( WsdlMockService source, WsdlProject target )
	{
		return true;
	}

	@Override
	boolean canMoveAfter( WsdlMockService source, WsdlProject target )
	{
		return source.getProject() != target;
	}

	@Override
	boolean copyAfter( WsdlMockService source, WsdlProject target )
	{
		SoapUIAction<WsdlMockService> action = SoapUI.getActionRegistry().getAction(
				CloneMockServiceAction.SOAPUI_ACTION_ID );
		CloneMockServiceAction a = ( CloneMockServiceAction )action;

		String name = UISupport.prompt( "Specify name for copied MockService", "Copy MockService", "Copy of "
				+ source.getName() );
		if( name == null )
			return false;

		if( source.getProject() == target )
		{
			a.cloneMockServiceWithinProject( source, name, target, source.getDescription() );
		}
		else
		{
			a.cloneToAnotherProject( source, target.getName(), name, source.getDescription() );
		}

		return true;
	}

	@Override
	boolean moveAfter( WsdlMockService source, WsdlProject target )
	{
		SoapUIAction<WsdlMockService> action = SoapUI.getActionRegistry().getAction(
				CloneMockServiceAction.SOAPUI_ACTION_ID );
		CloneMockServiceAction a = ( CloneMockServiceAction )action;

		String name = UISupport.prompt( "Specify name for moved MockService", "Move MockService", source.getName() );
		if( name == null )
			return false;

		if( a.cloneToAnotherProject( source, target.getName(), name, source.getDescription() ) == null )
			return false;

		source.getProject().removeMockService( source );
		return true;
	}

	@Override
	String getCopyAfterInfo( WsdlMockService source, WsdlProject target )
	{
		return "Copy MockService [" + source.getName() + "] to Project [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( WsdlMockService source, WsdlProject target )
	{
		return "Move MockService [" + source.getName() + "] to Project [" + target.getName() + "]";
	}

}
