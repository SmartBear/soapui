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

import java.io.File;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Actions for importing an existing soapUI project file into the current
 * workspace
 * 
 * @author Ole.Matzura
 */

public class ImportWsdlProjectAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "ImportWsdlProjectAction";
	public static final MessageSupport messages = MessageSupport.getMessages( ImportWsdlProjectAction.class );

	public ImportWsdlProjectAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		File file = null;

		if( param == null )
		{
			file = UISupport.getFileDialogs().openXML( this, messages.get( "prompt.title" ) );
		}
		else
		{
			file = new File( param.toString() );
		}

		if( file == null )
			return;

		String fileName = file.getAbsolutePath();
		if( fileName == null )
			return;

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader( SoapUI.class.getClassLoader() );

		try
		{
			WsdlProject project = ( WsdlProject )workspace.importProject( fileName );
			if( project != null )
				UISupport.select( project );
		}
		catch( Exception ex )
		{
			UISupport.showErrorMessage( ex );
		}
		finally
		{
			Thread.currentThread().setContextClassLoader( contextClassLoader );
		}
	}
}