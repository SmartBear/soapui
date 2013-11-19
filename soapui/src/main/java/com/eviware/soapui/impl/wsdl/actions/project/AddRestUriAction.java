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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.actions.RestUriDialogHandler;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;

/**
 * Action for adding a new REST URI to a project.
 */

public class AddRestUriAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "AddRestUriAction";

	private static final MessageSupport messages = MessageSupport.getMessages( AddRestUriAction.class );


	private RestUriDialogHandler dialogBuilder = new RestUriDialogHandler();
	private RestServiceBuilder serviceBuilder = new RestServiceBuilder();

	public AddRestUriAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WsdlProject project, Object param )
	{
		XFormDialog dialog = dialogBuilder.buildDialog( messages, null );
		while( dialog.show() )
		{
			try
			{
				String uri = dialogBuilder.getUri();
				if( uri != null )
				{
					serviceBuilder.createRestService( project, uri );
				}
				// If there is no exception or error we break out
				break;

			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex.getMessage() );

				dialogBuilder.resetUriField();
			}
		}
	}


}
