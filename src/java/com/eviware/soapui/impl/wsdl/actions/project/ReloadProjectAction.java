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

package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Prompts to reload the specified WsdlProject
 * 
 * @author ole.matzura
 */

public class ReloadProjectAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "ReloadProjectAction";

	public ReloadProjectAction()
	{
		super( "Reload Project", "Reloads this project from file" );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( project.isRemote() )
		{
			String path = UISupport.prompt( "Reload remote project URL", getName(), project.getPath() );
			if( path != null )
			{
				try
				{
					project.reload( path );
				}
				catch( SoapUIException ex )
				{
					UISupport.showErrorMessage( ex );
				}
			}
		}
		else
		{
			File file = UISupport.getFileDialogs().open( this, "Reload Project", ".xml", "soapUI Project Files (*.xml)",
					project.getPath() );
			if( file != null )
			{
				try
				{
					project.reload( file.getAbsolutePath() );
				}
				catch( SoapUIException ex )
				{
					UISupport.showErrorMessage( ex );
				}
			}
		}
	}
}
