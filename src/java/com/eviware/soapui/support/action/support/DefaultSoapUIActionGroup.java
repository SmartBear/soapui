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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.types.StringList;

/**
 * Default SoapUIActionGroup implementation
 * 
 * @author ole.matzura
 */

public class DefaultSoapUIActionGroup<T extends ModelItem> extends AbstractSoapUIActionGroup<T>
{
	private SoapUIActionMappingList<T> mappings = new SoapUIActionMappingList<T>();
	private StringList ids = new StringList();

	public DefaultSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	public SoapUIActionMappingList<T> getActionMappings( T modelItem )
	{
		return mappings;
	}

	@Override
	public SoapUIActionMapping<T> addMapping( String id, int index, SoapUIActionMapping<T> mapping )
	{
		if( index == -1 || index >= mappings.size() )
			return addMapping( id, mapping );

		mappings.add( index, mapping );
		ids.add( index, id );
		return mapping;
	}

	@Override
	public SoapUIActionMapping<T> addMapping( String id, SoapUIActionMapping<T> mapping )
	{
		mappings.add( mapping );
		ids.add( id );
		return mapping;
	}

	@Override
	public int getMappingIndex( String positionRef )
	{
		return ids.indexOf( positionRef );
	}
}