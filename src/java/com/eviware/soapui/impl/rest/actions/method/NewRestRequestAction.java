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

package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Actions for importing an existing soapUI project file into the current
 * workspace
 * 
 * @author Ole.Matzura
 */

public class NewRestRequestAction extends AbstractSoapUIAction<RestMethod>
{
	public static final String SOAPUI_ACTION_ID = "NewRestRequestAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestRequestAction.class );
	private XFormDialog dialog;

	public NewRestRequestAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}

	public void perform( RestMethod method, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
		}
		else
		{
			dialog.setValue( Form.RESOURCENAME, "" );
		}

		if( dialog.show() )
		{
			RestRequest request = method.addNewRequest( dialog.getValue( Form.RESOURCENAME ) );

			UISupport.select( request );

			if( dialog.getBooleanValue( Form.OPENSREQUEST ) )
			{
				UISupport.showDesktopPanel( request );
			}
		}
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.ResourceName.Description", type = AFieldType.STRING )
		public final static String RESOURCENAME = messages.get( "Form.ResourceName.Label" );

		@AField( description = "Form.OpenRequest.Description", type = AFieldType.BOOLEAN )
		public final static String OPENSREQUEST = messages.get( "Form.OpenRequest.Label" );
	}
}