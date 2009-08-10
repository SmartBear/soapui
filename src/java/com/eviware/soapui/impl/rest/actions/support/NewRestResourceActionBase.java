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
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.resource.NewRestMethodAction;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
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
	private InternalRestParamsTable paramsTable;
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
			// dialog.setBooleanValue(Form.CREATEREQUEST, true);
		}
		else
		{
			dialog.setValue( Form.RESOURCENAME, "" );
			dialog.setValue( Form.RESOURCEPATH, "" );
		}

		params = new XmlBeansRestParamsTestPropertyHolder( null, RestParametersConfig.Factory.newInstance() );

		if( param instanceof URL )
		{
			String path = RestUtils.extractParams( param.toString(), params, false );
			dialog.setValue( Form.RESOURCEPATH, path );

			setNameFromPath( path );

			if( paramsTable != null )
				paramsTable.refresh();
		}

		paramsTable = new InternalRestParamsTable( params, ParamLocation.RESOURCE );
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
			paramsTable.extractParams( resource.getParams(), ParamLocation.RESOURCE );

			// RestMethod method = createRestMethod(resource, dialog);
			// paramsTable.extractParams(method.getParams(), ParamLocation.METHOD);

			// UISupport.select(method);

			// if (dialog.getBooleanValue(Form.CREATEREQUEST)) {
			// createRequest(method);
			// }
			XmlBeansRestParamsTestPropertyHolder methodParams = new XmlBeansRestParamsTestPropertyHolder( null,
					RestParametersConfig.Factory.newInstance() );
			paramsTable.extractParams( methodParams, ParamLocation.METHOD );
			SoapUI.getActionRegistry().getAction( NewRestMethodAction.SOAPUI_ACTION_ID ).perform( resource, methodParams );
		}

		paramsTable.release();
		paramsTable = null;
		params = null;
		dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );
	}

	protected abstract RestResource createRestResource( T service, String path, XFormDialog dialog );

	protected abstract RestMethod createRestMethod( RestResource resource, XFormDialog dialog );

	private void setNameFromPath( String path )
	{
		String[] items = path.split( "/" );

		if( items.length > 0 )
		{
			dialog.setValue( Form.RESOURCENAME, items[items.length - 1] );
		}
	}

	protected void createRequest( RestMethod method )
	{
		// RestRequest request = resource.addNewRequest( dialog.getValue(
		// Form.RESOURCENAME ) );
		RestRequest request = method.addNewRequest( "Request " + ( method.getRequestCount() + 1 ) );
		UISupport.showDesktopPanel( request );
	}

	public enum ParamLocation
	{
		RESOURCE, METHOD
	}

	public static class InternalRestParamsTable extends RestParamsTable
	{
		private ParamLocation defaultLocation;

		public InternalRestParamsTable( RestParamsPropertyHolder params, ParamLocation defaultLocation )
		{
			super( params, false );
			this.defaultLocation = defaultLocation;
		}

		public void extractParams( RestParamsPropertyHolder params, ParamLocation location )
		{
			for( int i = 0; i < paramsTable.getRowCount(); i++ )
			{
				RestParamProperty prop = paramsTableModel.getParameterAt( i );
				if( ( ( InternalRestParamsTableModel )paramsTableModel ).getParamLocationAt( i ) == location )
				{
					params.addParameter( prop );
				}
			}
		}

		protected RestParamsTableModel createTableModel( RestParamsPropertyHolder params )
		{
			return new InternalRestParamsTableModel( params );
		}

		protected void init( RestParamsPropertyHolder params, boolean showInspector )
		{
			super.init( params, showInspector );
			paramsTable.setDefaultEditor( ParamLocation.class, new DefaultCellEditor( new JComboBox( new Object[] {
					ParamLocation.RESOURCE, ParamLocation.METHOD } ) ) );
		}

		public class InternalRestParamsTableModel extends RestParamsTableModel
		{
			private Map<RestParamProperty, ParamLocation> locations = new HashMap<RestParamProperty, ParamLocation>();
			private int columnCount;

			public InternalRestParamsTableModel( RestParamsPropertyHolder params )
			{
				super( params );
				columnCount = super.getColumnCount();
			}

			public int getColumnCount()
			{
				return columnCount + 1;
			}

			public ParamLocation getParamLocationAt( int rowIndex )
			{
				return ( ParamLocation )getValueAt( rowIndex, columnCount );
			}

			public Object getValueAt( int rowIndex, int columnIndex )
			{
				if( columnIndex != columnCount )
					return super.getValueAt( rowIndex, columnIndex );
				RestParamProperty name = params.getPropertyAt( rowIndex );
				if( !locations.containsKey( name ) )
					locations.put( name, defaultLocation );
				return locations.get( name );
			}

			@Override
			public String getColumnName( int column )
			{
				return column != columnCount ? super.getColumnName( column ) : "Location";
			}

			@Override
			public Class<?> getColumnClass( int columnIndex )
			{
				return columnIndex != columnCount ? super.getColumnClass( columnIndex ) : ParamLocation.class;
			}

			@Override
			public void setValueAt( Object value, int rowIndex, int columnIndex )
			{
				if( columnIndex != columnCount )
					super.setValueAt( value, rowIndex, columnIndex );
				else
				{
					RestParamProperty name = params.getPropertyAt( rowIndex );
					locations.put( name, ( ParamLocation )value );
				}
			}

		}

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

		// @AField(description = "Form.CreateRequest.Description", type =
		// AFieldType.BOOLEAN)
		// public final static String CREATEREQUEST = messages
		// .get("Form.CreateRequest.Label");
	}
}