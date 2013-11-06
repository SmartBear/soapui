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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.Setting;
import com.eviware.soapui.support.components.DirectoryFormComponent;
import com.eviware.soapui.support.components.FileFormComponent;
import com.eviware.soapui.support.components.FileListFormComponent;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.components.StringListFormComponent;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Support class for annotation-based settings
 * 
 * @author ole.matzura
 */

public class AnnotatedSettingsPrefs implements Prefs
{
	private SimpleForm simpleForm;
	private Class<?> settingsClass;
	private final String title;

	public AnnotatedSettingsPrefs( Class<?> settingsClass, String title )
	{
		this.settingsClass = settingsClass;
		this.title = title;
	}

	public SimpleForm getForm()
	{
		if( simpleForm == null )
		{
			simpleForm = new SimpleForm();
			simpleForm.addSpace( 5 );

			buildForm( simpleForm );

			simpleForm.addSpace( 5 );
		}

		return simpleForm;
	}

	public List<Setting> getSettings()
	{
		ArrayList<Setting> settings = new ArrayList<Setting>();
		for( Field field : settingsClass.getFields() )
		{
			Setting annotation = field.getAnnotation( Setting.class );
			if( annotation != null )
			{
				settings.add( annotation );
			}
		}
		return settings;
	}

	private void buildForm( SimpleForm form )
	{
		List<Setting> settings = getSettings();
		for( Setting annotation : settings )
		{
			switch( annotation.type() )
			{
			case BOOLEAN :
			{
				form.appendCheckBox( annotation.name(), annotation.description(), false );
				break;
			}
			case FILE :
			{
				form.append( annotation.name(), new FileFormComponent( annotation.description() ) );
				break;
			}
			case FILELIST :
			{
				form.append( annotation.name(), new FileListFormComponent( annotation.description() ) );
				break;
			}
			case STRINGLIST :
			{
				form.append( annotation.name(), new StringListFormComponent( annotation.description() ) );
				break;
			}
			case FOLDER :
			{
				form.append( annotation.name(), new DirectoryFormComponent( annotation.description() ) );
				break;
			}
			case ENUMERATION :
			{
				form.appendComboBox( annotation.name(), annotation.values(), annotation.description() );
				break;
			}
			case PASSWORD :
			{
				form.appendPasswordField( annotation.name(), annotation.description() );
				break;
			}
			default :
			{
				form.appendTextField( annotation.name(), annotation.description() );
				break;
			}
			}
		}
	}

	public StringToStringMap getValues( Settings settings )
	{
		StringToStringMap result = new StringToStringMap();

		for( Field field : settingsClass.getFields() )
		{
			Setting annotation = field.getAnnotation( Setting.class );
			if( annotation != null )
			{
				try
				{
					result.put( annotation.name(),
							settings.getString( field.get( null ).toString(), annotation.defaultValue() ) );
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}

		return result;
	}

	public void setFormValues( Settings settings )
	{
		getForm().setValues( getValues( settings ) );
	}

	public void getFormValues( Settings settings )
	{
		StringToStringMap values = new StringToStringMap();
		getForm().getValues( values );
		storeValues( values, settings );
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
		for( Field field : settingsClass.getFields() )
		{
			Setting annotation = field.getAnnotation( Setting.class );
			if( annotation != null )
			{
				try
				{
					settings.setString( field.get( null ).toString(), values.get( annotation.name() ) );
				}
				catch( IllegalArgumentException e )
				{
					SoapUI.logError( e );
				}
				catch( IllegalAccessException e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}

	public String getTitle()
	{
		return title;
	}

}
