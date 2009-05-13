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

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.RemoveInterfaceAction;
import com.eviware.soapui.support.UISupport;

public class InterfaceToProjectDropHandler extends AbstractAfterModelItemDropHandler<AbstractInterface<?>, WsdlProject>
{
	@SuppressWarnings( "unchecked" )
	public InterfaceToProjectDropHandler()
	{
		super( ( Class<AbstractInterface<?>> )( Class )AbstractInterface.class, WsdlProject.class );
	}

	@Override
	boolean canCopyAfter( AbstractInterface<?> source, WsdlProject target )
	{
		return source.getProject() != target && target.isOpen();
	}

	@Override
	boolean canMoveAfter( AbstractInterface<?> source, WsdlProject target )
	{
		return source.getProject() != target && target.isOpen();
	}

	@Override
	boolean copyAfter( AbstractInterface<?> source, WsdlProject target )
	{
		AbstractInterface<?> targetInterface = target.getInterfaceByTechnicalId( source.getTechnicalId() );
		if( targetInterface != null )
		{
			UISupport.showErrorMessage( "Target project already contains this Interface" );
			return false;
		}
		else if( !UISupport.confirm( "Copy Interface [" + source.getName() + "] to Project [" + target.getName() + "]",
				"Copy Interface" ) )
		{
			return false;
		}

		boolean importEndpoints = UISupport.confirm( "Import endpoint defaults also?", "Copy Interface" );
		UISupport.select( target.importInterface( source, importEndpoints, true ) );

		return true;
	}

	@Override
	boolean moveAfter( AbstractInterface<?> source, WsdlProject target )
	{
		AbstractInterface<?> targetInterface = target.getInterfaceByTechnicalId( source.getTechnicalId() );
		if( targetInterface != null )
		{
			UISupport.showErrorMessage( "Target project already contains this Interface" );
			return false;
		}

		if( RemoveInterfaceAction.hasRunningDependingTests( source ) )
		{
			UISupport.showErrorMessage( "Cannot remove Interface due to running depending tests" );
			return false;
		}

		if( RemoveInterfaceAction.hasDependingTests( source ) )
		{
			Boolean retval = UISupport.confirmOrCancel(
					"Interface has depending Test Steps which will be removed. Copy Instead?"
							+ "\r\n(moving will remove dependant Test Steps from source project)", "Move Interface" );

			if( retval == null )
				return false;

			if( retval == true )
			{
				boolean importEndpoints = UISupport.confirm( "Move endpoint defaults also?", "Move Interface" );
				UISupport.select( target.importInterface( source, importEndpoints, true ) );
				return true;
			}
		}
		else if( !UISupport.confirm( "Move Interface [" + source.getName() + "] to Project [" + target.getName() + "]",
				"Move Interface" ) )
		{
			return false;
		}

		boolean importEndpoints = UISupport.confirm( "Move endpoint defaults also?", "Move Interface" );
		UISupport.select( target.importInterface( source, importEndpoints, false ) );
		source.getProject().removeInterface( source );
		return true;
	}

	@Override
	String getCopyAfterInfo( AbstractInterface<?> source, WsdlProject target )
	{
		return "Copy Interface [" + source.getName() + "] to Project [" + target.getName() + "]";
	}

	@Override
	String getMoveAfterInfo( AbstractInterface<?> source, WsdlProject target )
	{
		return "Move Interface [" + source.getName() + "] to Project [" + target.getName() + "]";
	}

}
