/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.actions;

import java.io.File;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.integration.exporter.ProjectExporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ExportProjectAction extends AbstractSoapUIAction<WsdlProject>
{

	public ExportProjectAction()
	{
		super( "Export Project", "Export Project" );
	}

	@Override
	public void perform( WsdlProject project, Object param )
	{
		ProjectExporter exporter = new ProjectExporter( project );

		try
		{
			String path = project.getPath();
			if( path == null )
			{
				project.save();
			}
			else
			{
				File file = UISupport.getFileDialogs().saveAs( this, "Select file to export project", "zip", "zip",
						new File( System.getProperty( "user.home" ) ) );
				if( file == null )
					return;

				String fileName = file.getAbsolutePath();
				if( fileName == null )
					return;

				exporter.exportProject( fileName );
			}
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1, "Failed to export project" );
			UISupport.showErrorMessage( "Failed to export project; " + e1 );
		}

	}

}
