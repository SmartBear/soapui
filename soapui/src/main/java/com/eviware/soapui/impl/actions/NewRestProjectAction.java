/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Action class to create new REST project.
 *
 * @author Shadid Chowdhury
 */

public class NewRestProjectAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "NewRestProjectAction";

	private static final String DEFAULT_PROJECT_NAME = "REST Project";
	private static final MessageSupport messages = MessageSupport.getMessages( NewRestProjectAction.class );


	private RestUriDialogHandler dialogBuilder = new RestUriDialogHandler();
	private XFormDialog dialog;
	private RestServiceBuilder serviceBuilder = new RestServiceBuilder();


	public NewRestProjectAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}


	public void perform( WorkspaceImpl workspace, Object param )
	{
		dialog = dialogBuilder.buildDialog( messages, new AbstractAction( "Import WADL..." )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				dialog.setVisible( false );
				SoapUI.getActionRegistry().getAction( NewWadlProjectAction.SOAPUI_ACTION_ID ).perform( SoapUI.getWorkspace(), null );
			}
		} );
		while( dialog.show() )
		{
			WsdlProject project = null;
			try
			{
				String uri = dialogBuilder.getUri();
				if( uri != null )
				{
					project = workspace.createProject( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ), null );
					serviceBuilder.createRestService( project, uri );
				}
				// If there is no exception or error we break out
				break;

			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex.getMessage() );
				if( project != null )
				{
					workspace.removeProject( project );
				}
				dialogBuilder.resetUriField();
			}
		}
	}


}