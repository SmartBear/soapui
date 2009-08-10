/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.beanutils.PropertyUtils;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;

public class ModelItemPropertyEditorModel<T extends ModelItem> extends AbstractEditorModel implements
		PropertyChangeListener
{
	private T modelItem;
	private String propertyName;

	public ModelItemPropertyEditorModel( T modelItem, String propertyName )
	{
		this.modelItem = modelItem;
		this.propertyName = propertyName;

		modelItem.addPropertyChangeListener( propertyName, this );
	}

	public Settings getSettings()
	{
		return modelItem.getSettings();
	}

	public String getEditorText()
	{
		try
		{
			Object value = PropertyUtils.getSimpleProperty( modelItem, propertyName );
			return value == null ? "" : String.valueOf( value );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return null;
	}

	public void setEditorText( String text )
	{
		try
		{
			PropertyUtils.setSimpleProperty( modelItem, propertyName, text );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void release()
	{
		super.release();
		
		modelItem.removePropertyChangeListener( propertyName, this );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		fireEditorTextChanged( String.valueOf( evt.getOldValue() ), String.valueOf( evt.getNewValue() ) );
	}
}
