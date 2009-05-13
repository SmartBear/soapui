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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlProject
 * 
 * @author Ole.Matzura
 */

public class RenameWorkspaceAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "RenameWorkspaceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( RenameWorkspaceAction.class );

	public RenameWorkspaceAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		String name = UISupport.prompt( messages.get( "Prompt" ), messages.get( "Prompt.Title" ), workspace.getName() );
		if( name == null || name.equals( workspace.getName() ) )
			return;

		workspace.setName( name );
	}
}
