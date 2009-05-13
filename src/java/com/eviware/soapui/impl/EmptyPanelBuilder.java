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

package com.eviware.soapui.impl;

import java.awt.Component;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * Empty PanelBuilder implementation for extension.
 * 
 * @author Ole.Matzura
 */

public class EmptyPanelBuilder<T extends ModelItem> implements PanelBuilder<T>
{
	private static final EmptyPanelBuilder<?> instance = new EmptyPanelBuilder<EmptyModelItem>();

	public static EmptyPanelBuilder<?> get()
	{
		return instance;
	}

	public Component buildOverviewPanel( T modelItem )
	{
		String caption = "Properties";
		if( modelItem.getClass().getSimpleName().startsWith( "Wsdl" ) )
		{
			caption = modelItem.getClass().getSimpleName().substring( 4 );

			if( caption.endsWith( "TestStep" ) )
				caption = caption.substring( 0, caption.length() - 8 );

			caption += " Properties";
		}

		return buildDefaultProperties( modelItem, caption );
	}

	protected JPropertiesTable<T> buildDefaultProperties( T modelItem, String caption )
	{
		JPropertiesTable<T> table = new JPropertiesTable<T>( caption, modelItem );

		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );

		table.setPropertyObject( modelItem );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}

	public boolean hasDesktopPanel()
	{
		return false;
	}

	public DesktopPanel buildDesktopPanel( T modelItem )
	{
		return null;
	}
}
