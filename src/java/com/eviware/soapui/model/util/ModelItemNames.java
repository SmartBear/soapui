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
package com.eviware.soapui.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eviware.soapui.model.ModelItem;

/**
 * Utility for handling model item names.
 * 
 * @author Lars Høidahl
 */

public class ModelItemNames<T extends ModelItem>
{
	private List<T> elements;

	public ModelItemNames( List<T> elements )
	{
		this.elements = new ArrayList<T>( elements );
	}

	public ModelItemNames( T[] elements )
	{
		// Create an ArrayList to make sure that elements is modifyable.
		this.elements = new ArrayList<T>( Arrays.asList( elements ) );
	}

	public String[] getNames()
	{
		ArrayList<String> list = getElementNameList();
		return list.toArray( new String[list.size()] );
	}

	private ArrayList<String> getElementNameList()
	{
		ArrayList<String> elementNames = new ArrayList<String>();
		for( T element : elements )
		{
			elementNames.add( element.getName() );
		}
		return elementNames;
	}

	public T getElement( String name )
	{
		int index = getElementNameList().indexOf( name );
		return elements.get( index );
	}

	public void addElement( T element )
	{
		elements.add( element );
	}

	public int getSize()
	{
		return elements.size();
	}

	public String getNameAt( int i )
	{
		return elements.get( i ).getName();
	}
}