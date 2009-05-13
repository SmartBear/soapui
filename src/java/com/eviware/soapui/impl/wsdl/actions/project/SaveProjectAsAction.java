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
import java.io.IOException;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Prompts to save a WsdlProject to a new file
 * 
 * @author Ole.Matzura
 */

public class SaveProjectAsAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "SaveProjectAsAction";

	public SaveProjectAsAction()
	{
		super( "Save Project As", "Saves this project to a new file" );
	}

	public void perform( WsdlProject project, Object param )
	{
		try
		{
			String path = project.getPath();
			if( path == null )
			{
				project.save();
			}
			else
			{
				File file = UISupport.getFileDialogs().saveAs( this, "Select soapui project file", "xml", "XML",
						new File( path ) );
				if( file == null )
					return;

				String fileName = file.getAbsolutePath();
				if( fileName == null )
					return;

				if( project.saveAs( fileName ) )
				{
					project.getWorkspace().save( true );
				}
			}
		}
		catch( IOException e1 )
		{
			UISupport.showErrorMessage( "Failed to save project; " + e1 );
		}
	}
}
