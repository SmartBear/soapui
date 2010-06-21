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
package com.eviware.soapui.actions;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.LoadUISettings;
import com.eviware.soapui.support.components.DirectoryFormComponent;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

public class LoadUIPrefs implements Prefs
{
	public static final String LOADUI_PATH = "loadUI.bat folder";
//	public static final String LOADUI_CAJO_SERVER = "LoadUI Cajo server name";
	public static final String LOADUI_CAJO_PORT = "LoadUI integration port";
//	public static final String LOADUI_CAJO_ITEM_NAME = "LoadUI Cajo Item name";
	public static final String SOAPUI_CAJO_PORT = "SoapUI integration port";

	private final String title;
	private SimpleForm editorForm;

	public LoadUIPrefs( String title )
	{
		this.title = title;
	}

	public SimpleForm getForm()
	{
		if( editorForm == null )
		{
			editorForm = new SimpleForm();
			editorForm.addSpace( 5 );
			editorForm.append( LOADUI_PATH, new DirectoryFormComponent( "Folder contains loadUI.bat " ) );
//			editorForm.appendTextField( LOADUI_CAJO_SERVER, "Server name of LoadUI machine." );
			editorForm.appendTextField( LOADUI_CAJO_PORT, "Client port for LoadUI" );
			editorForm.appendTextField( SOAPUI_CAJO_PORT, "Server port of SoapUI  (change requires restart of SoapUI)" );
//			editorForm.appendTextField( LOADUI_CAJO_ITEM_NAME, "Item name for Cajo server" );

		}
		return editorForm;
	}

	public void getFormValues( Settings settings )
	{
		StringToStringMap values = new StringToStringMap();
		editorForm.getValues( values );
		storeValues( values, settings );
	}

	public String getTitle()
	{
		return title;
	}

	public StringToStringMap getValues( Settings settings )
	{
		StringToStringMap values = new StringToStringMap();
		values.put( LOADUI_PATH, settings.getString( LoadUISettings.LOADUI_PATH, "" ) );
//		values.put( LOADUI_CAJO_SERVER, settings.getString( LoadUISettings.LOADUI_CAJO_SERVER, "localhost" ) );
		values.put( LOADUI_CAJO_PORT, settings.getString( LoadUISettings.LOADUI_CAJO_PORT, "1199" ) );
		values.put( SOAPUI_CAJO_PORT, settings.getString( LoadUISettings.SOAPUI_CAJO_PORT, "1198" ) );
//		values.put( LOADUI_CAJO_ITEM_NAME, settings.getString( LoadUISettings.LOADUI_CAJO_ITEM_NAME, "loaduiIntegration" ) );

		return values;
	}

	public void setFormValues( Settings settings )
	{
		editorForm.setValues( getValues( settings ) );
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
		settings.setString( LoadUISettings.LOADUI_PATH, values.get( LOADUI_PATH ) );
//		settings.setString( LoadUISettings.LOADUI_CAJO_SERVER, values.get( LOADUI_CAJO_SERVER ) );
		settings.setString( LoadUISettings.LOADUI_CAJO_PORT, values.get( LOADUI_CAJO_PORT ) );
		settings.setString( LoadUISettings.SOAPUI_CAJO_PORT, values.get( SOAPUI_CAJO_PORT ) );
//		settings.setString( LoadUISettings.LOADUI_CAJO_ITEM_NAME, values.get( LOADUI_CAJO_ITEM_NAME ) );

		//CajoServer.getInstance().restart();
		
	}

}
