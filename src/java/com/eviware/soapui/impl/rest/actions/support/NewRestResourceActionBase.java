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

package com.eviware.soapui.impl.rest.actions.support;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpRequest.RequestMethod;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.validators.RequiredValidator;

/**
 * Actions for importing an existing soapUI project file into the current
 * workspace
 * 
 * @author Ole.Matzura
 */

public abstract class NewRestResourceActionBase<T extends ModelItem> extends AbstractSoapUIAction<T>
{
	private XFormDialog dialog;
	private XmlBeansRestParamsTestPropertyHolder params;
	private RestParamsTable paramsTable;
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestResourceActionBase.class );

	public NewRestResourceActionBase( String title, String description )
	{
		super( title, description );
	}

	public void perform( T service, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.getFormField( Form.RESOURCENAME ).addFormFieldValidator( new RequiredValidator() );
			dialog.getFormField( Form.EXTRACTPARAMS ).setProperty( "action", new ExtractParamsAction() );
			dialog.setBooleanValue( Form.CREATEREQUEST, true );
		}
		else
		{
			dialog.setValue( Form.RESOURCENAME, "" );
			dialog.setValue( Form.RESOURCEPATH, "" );
		}

		params = new XmlBeansRestParamsTestPropertyHolder( service, RestParametersConfig.Factory.newInstance() );

		if( param instanceof URL )
		{
			String path = RestUtils.extractParams( param.toString(), params, false );
			dialog.setValue( Form.RESOURCEPATH, path );

			setNameFromPath( path );

			if( paramsTable != null )
				paramsTable.refresh();
		}

		paramsTable = new RestParamsTable( params, false );
		dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );

		if( dialog.show() )
		{
			String path = dialog.getValue( Form.RESOURCEPATH );

			try
			{
				URL url = new URL( path );
				path = url.getPath();
			}
			catch( MalformedURLException e )
			{
			}

			RestResource resource = createRestResource( service, path, dialog );

			resource.getParams().addParameters( params );

			UISupport.select( resource );

			if( dialog.getBooleanValue( Form.CREATEREQUEST ) )
			{
				createRequest( resource );
			}
		}

		paramsTable.release();
		paramsTable = null;
		params = null;
		dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );
	}

	protected abstract RestResource createRestResource( T service, String path, XFormDialog dialog );

	private void setNameFromPath( String path )
	{
		String[] items = path.split( "/" );

		if( items.length > 0 )
		{
			dialog.setValue( Form.RESOURCENAME, items[items.length - 1] );
		}
	}

	protected void createRequest( RestResource resource )
	{
		RestRequest request = resource.addNewRequest( dialog.getValue( Form.RESOURCENAME ) );
		request.setMethod( RequestMethod.GET );
		UISupport.showDesktopPanel( request );
	}

	private class ExtractParamsAction extends AbstractAction
	{
		public ExtractParamsAction()
		{
			super( "Extract Params" );
		}

		public void actionPerformed( ActionEvent e )
		{
			try
			{
				String path = RestUtils.extractParams( dialog.getValue( Form.RESOURCEPATH ), params, false );
				dialog.setValue( Form.RESOURCEPATH, path );

				if( StringUtils.isNullOrEmpty( dialog.getValue( Form.RESOURCENAME ) ) )
					setNameFromPath( path );

				paramsTable.refresh();
			}
			catch( Exception e1 )
			{
				UISupport.showInfoMessage( "No parameters to extract!" );
			}
		}
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.ServiceName.Description", type = AFieldType.STRING )
		public final static String RESOURCENAME = messages.get( "Form.ResourceName.Label" );

		@AField( description = "Form.ServiceUrl.Description", type = AFieldType.STRING )
		public final static String RESOURCEPATH = messages.get( "Form.ResourcePath.Label" );

		@AField( description = "Form.ExtractParams.Description", type = AFieldType.ACTION )
		public final static String EXTRACTPARAMS = messages.get( "Form.ExtractParams.Label" );

		@AField( description = "Form.ParamsTable.Description", type = AFieldType.COMPONENT )
		public final static String PARAMSTABLE = messages.get( "Form.ParamsTable.Label" );

		@AField( description = "Form.CreateRequest.Description", type = AFieldType.BOOLEAN )
		public final static String CREATEREQUEST = messages.get( "Form.CreateRequest.Label" );
	}
}