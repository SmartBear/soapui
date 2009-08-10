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

package com.eviware.soapui.model.tree.nodes.support;

import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;

/**
 * Empty ModelItem used by intermediary TreeNodes
 * 
 * @author ole.matzura
 */

public class EmptyModelItem extends AbstractModelItem
{
	private String name;
	private ImageIcon icon;

	public EmptyModelItem( String name, ImageIcon icon )
	{
		this.name = name;
		this.icon = icon;
	}

	public void setName( String name )
	{
		String oldName = this.name;
		this.name = name;

		notifyPropertyChanged( ModelItem.NAME_PROPERTY, oldName, name );
	}

	public String getName()
	{
		return name;
	}

	public ImageIcon getIcon()
	{
		return icon;
	}

	public String getDescription()
	{
		return name;
	}

	public Settings getSettings()
	{
		return SoapUI.getSettings();
	}

	public void release()
	{
	}

	public String getId()
	{
		return String.valueOf( hashCode() );
	}

	@SuppressWarnings( "unchecked" )
	public List<? extends ModelItem> getChildren()
	{
		return Collections.EMPTY_LIST;
	}

	public ModelItem getParent()
	{
		return null;
	}
}