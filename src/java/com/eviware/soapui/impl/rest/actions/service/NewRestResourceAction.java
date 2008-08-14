/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.actions.service;

import java.net.MalformedURLException;
import java.net.URL;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestRequest.RequestMethod;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.validators.RequiredValidator;

/**
 * Actions for importing an existing soapUI project file into the current workspace
 * 
 * @author Ole.Matzura
 */

public class NewRestResourceAction extends AbstractSoapUIAction<RestService>
{
	public static final String SOAPUI_ACTION_ID = "NewRestResourceAction"; 
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestResourceAction.class );
	private XFormDialog dialog;
	
	public NewRestResourceAction()
   {
      super( messages.get( "title"), messages.get( "description") ); 
   }

	public void perform( RestService service, Object param )
	{
		if( dialog == null )
   	{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.getFormField(Form.RESOURCENAME ).addFormFieldValidator(new RequiredValidator());
   	}
   	else 
   	{
   		dialog.setValue( Form.RESOURCENAME, "" ); 
   		dialog.setValue( Form.RESOURCEPATH, "" ); 
   	}
   	
   	if( dialog.show() )
   	{
   		String path = dialog.getValue(Form.RESOURCEPATH);

   		try
			{
				URL url = new URL( path );
				path = url.getPath();
			}
			catch (MalformedURLException e)
			{
			}
   		
			RestResource resource = service.addNewResource( dialog.getValue(Form.RESOURCENAME), path );
			UISupport.select(resource);
			
			if( dialog.getBooleanValue(Form.EXTRACTPARAMS))
			{
				extractParams( resource, path );
			}
			
			if( dialog.getBooleanValue(Form.CREATEREQUEST))
			{
				createRequest( resource );
			}
   	}
   }
	
   protected void createRequest(RestResource resource)
	{
   	RestRequest request = resource.addNewRequest( dialog.getValue(Form.RESOURCENAME));
		request.setMethod( RequestMethod.GET );
		UISupport.showDesktopPanel( request );
	}

	protected void extractParams(RestResource resource, String path)
	{
		
	}

	@AForm( name="Form.Title", description = "Form.Description", helpUrl=HelpUrls.NEWRESTSERVICE_HELP_URL, icon=UISupport.TOOL_ICON_PATH)
	public interface Form 
	{
		@AField( description = "Form.ServiceName.Description", type = AFieldType.STRING ) 
		public final static String RESOURCENAME = messages.get("Form.ResourceName.Label"); 
		
		@AField(description = "Form.ServiceUrl.Description", type = AFieldType.STRING ) 
		public final static String RESOURCEPATH = messages.get("Form.ResourcePath.Label"); 
		
		@AField(description = "Form.ExtractParams.Description", type = AFieldType.BOOLEAN ) 
		public final static String EXTRACTPARAMS = messages.get("Form.ExtractParams.Label"); 
		
		@AField(description = "Form.CreateRequest.Description", type = AFieldType.BOOLEAN ) 
		public final static String CREATEREQUEST = messages.get("Form.CreateRequest.Label"); 
	}
}