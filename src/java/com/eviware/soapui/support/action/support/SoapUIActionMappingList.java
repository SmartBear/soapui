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

package com.eviware.soapui.support.action.support;

import java.util.ArrayList;
import java.util.Collection;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIActionMapping;

/**
 * A list of SoapUIActionMappings
 * 
 * @author ole.matzura
 */

public class SoapUIActionMappingList<T extends ModelItem> extends ArrayList<SoapUIActionMapping<T>>
{
	public SoapUIActionMappingList()
	{
		super();
	}

	public SoapUIActionMappingList( Collection<? extends SoapUIActionMapping<T>> arg0 )
	{
		super( arg0 );
	}

	public int getMappingIndex( String id )
	{
		for( int c = 0; c < size(); c++ )
		{
			if( get( c ).getActionId().equals( id ) )
				return c;
		}

		return -1;
	}

	public SoapUIActionMapping<T> getMapping( String id )
	{
		for( SoapUIActionMapping<T> mapping : this )
		{
			if( mapping.getActionId().equals( id ) )
				return mapping;
		}

		return null;
	}
}
