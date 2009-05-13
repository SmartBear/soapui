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

package com.eviware.soapui.model.propertyexpansion;

import java.util.ArrayList;

import com.eviware.soapui.model.ModelItem;

public class PropertyExpansionsResult extends ArrayList<PropertyExpansion>
{
	private final ModelItem modelItem;
	private final Object defaultTarget;

	public PropertyExpansionsResult( ModelItem modelItem )
	{
		this( modelItem, modelItem );
	}

	public PropertyExpansionsResult( ModelItem modelItem, Object defaultTarget )
	{
		this.modelItem = modelItem;
		this.defaultTarget = defaultTarget;
	}

	public boolean extractAndAddAll( Object target, String propertyName )
	{
		return addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, target, propertyName ) );
	}

	public boolean extractAndAddAll( String propertyName )
	{
		return addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, defaultTarget, propertyName ) );
	}

	public PropertyExpansion[] toArray()
	{
		return toArray( new PropertyExpansion[size()] );
	}

	public void addAll( PropertyExpansion[] propertyExpansions )
	{
		if( propertyExpansions == null )
			return;

		for( PropertyExpansion pe : propertyExpansions )
			add( pe );
	}
}