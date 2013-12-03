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

package com.eviware.soapui.impl.rest.actions.project;

import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.actions.RestUriDialogHandler;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;

/**
 * Actions for importing an existing SoapUI project file into the current
 * workspace
 * 
 * @author Ole.Matzura
 */

public class NewRestServiceAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "NewRestServiceAction";

	public static final MessageSupport messages = MessageSupport.getMessages( NewRestServiceAction.class );


	public NewRestServiceAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WsdlProject project, Object param )
	{
		RestUriDialogHandler dialogBuilder = new RestUriDialogHandler();
		RestServiceBuilder serviceBuilder = new RestServiceBuilder();
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
