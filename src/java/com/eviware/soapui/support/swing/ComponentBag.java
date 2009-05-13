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

package com.eviware.soapui.support.swing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;

/**
 * Utility for working with collections of components
 * 
 * @author Ole.Matzura
 */

public class ComponentBag
{
	private Map<String, JComponent> components = new HashMap<String, JComponent>();

	public ComponentBag()
	{
	}

	public void add( JComponent component )
	{
		components.put( String.valueOf( component.hashCode() ), component );
	}

	public void add( String name, JComponent component )
	{
		components.put( name, component );
	}

	public JComponent get( String name )
	{
		return components.get( name );
	}

	public void setEnabled( boolean enabled )
	{
		Iterator<JComponent> iterator = components.values().iterator();
		while( iterator.hasNext() )
		{
			iterator.next().setEnabled( enabled );
		}
	}

	public void setEnabled( boolean enabled, String name )
	{
		if( components.containsKey( name ) )
			components.get( name ).setEnabled( enabled );
	}

	public void setEnabled( boolean enabled, String[] names )
	{
		for( int c = 0; c < names.length; c++ )
			setEnabled( enabled, names[c] );
	}
}
