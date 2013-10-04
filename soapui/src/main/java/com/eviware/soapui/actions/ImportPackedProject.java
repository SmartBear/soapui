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
import java.util.List;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportWsdlProjectAction;
import com.eviware.soapui.integration.exporter.ProjectExporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ImportPackedProject extends AbstractSoapUIAction<WorkspaceImpl>
{
	public ImportPackedProject()
	{
		super( "Import Packed Project", "Import Packed Project" );
	}

	@Override
	public void perform( WorkspaceImpl workspace, Object param )
	{

		try
		{

			File target = UISupport.getFileDialogs().open( this, "Select file to unpack project", "zip", "zip",
					System.getProperty( "user.home" ) );
			if( target == null )
				return;

			String fileName = target.getAbsolutePath();
			if( fileName == null )
				return;

			File dest = UISupport.getFileDialogs().saveAsDirectory( this, "Select where to unpack it",
					new File( System.getProperty( "user.home" ) ) );

			if( dest.getAbsoluteFile() == null )
				return;
			ProjectExporter.unpackageAll( fileName, dest.getAbsolutePath() );
			List<String> contents = ProjectExporter.getZipContents( fileName );

			for( String fName : dest.list() )
				if( contents.contains( fName ) && fName.endsWith( "-soapui-project.xml" ) )
				{
					new ImportWsdlProjectAction().perform( workspace, new File( dest, fName ).getAbsoluteFile() );
					break;
				}

		}
		catch( Exception e1 )
		{
			UISupport.showErrorMessage( "Failed to export project; " + e1 );
		}

	}

}
