/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.IOException;

/**
 * Renames a WsdlProject
 *
 * @author Ole.Matzura
 */

public class CloseProjectAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "CloseProjectAction";

	public CloseProjectAction()
	{
		super( "Close Project", "Closes this project" );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( project.isRemote() )
		{
			if( UISupport.confirm( "Close remote project? (changes will be lost)", getName() ) )
			{
				project.getWorkspace().closeProject( project );
			}
		}
		else
		{
			Boolean saveProject = UISupport.confirmOrCancel( "Save project [" + project.getName() + "] before closing?",
					"Close Project" );

			if( saveProject == null )
			{
				return;
			}

			try
			{
				if( saveProject )
				{
					SaveStatus status = project.save();
					if( status == SaveStatus.CANCELLED || status == SaveStatus.FAILED )
					{
						return;
					}
				}
				project.getWorkspace().closeProject( project );
			}
			catch( IOException e )
			{
				UISupport.showErrorMessage( e );
			}
		}
	}
}
