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

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.LoadUISettings;
import com.eviware.soapui.support.components.DirectoryFormComponent;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

public class LoadUIPrefs implements Prefs
{
	public static final String LOADUI_PATH = "loadUI.bat(.sh) folder";
	public static final String LOADUI_CAJO_PORT = "loadUI integration port";
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
			DirectoryFormComponent directoryFormComponent = new DirectoryFormComponent(
					"Folder containing loadUI.bat(.sh) " );
			directoryFormComponent.setInitialFolder( System.getProperty( "soapui.home" ) + File.separator + ".."
					+ File.separator + ".." );
			editorForm.append( LOADUI_PATH, directoryFormComponent );
			editorForm.appendTextField( LOADUI_CAJO_PORT, "Client port for loadUI integration" );
			editorForm.appendTextField( SOAPUI_CAJO_PORT,
					"Server port of SoapUI integration (change requires restart of SoapUI)" );

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
		values.put( LOADUI_CAJO_PORT, settings.getString( LoadUISettings.LOADUI_CAJO_PORT, "1199" ) );
		values.put( SOAPUI_CAJO_PORT, settings.getString( LoadUISettings.SOAPUI_CAJO_PORT, "1198" ) );
		return values;
	}

	public void setFormValues( Settings settings )
	{
		editorForm.setValues( getValues( settings ) );
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
		settings.setString( LoadUISettings.LOADUI_PATH, values.get( LOADUI_PATH ) );
		settings.setString( LoadUISettings.LOADUI_CAJO_PORT, values.get( LOADUI_CAJO_PORT ) );
		settings.setString( LoadUISettings.SOAPUI_CAJO_PORT, values.get( SOAPUI_CAJO_PORT ) );

	}

}
