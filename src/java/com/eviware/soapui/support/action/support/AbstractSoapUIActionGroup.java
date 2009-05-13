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
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.SoapUIActionMapping;

/**
 * Abstract SoapUIActionGroup for extension
 * 
 * @author ole.matzura
 */

public abstract class AbstractSoapUIActionGroup<T extends ModelItem> implements SoapUIActionGroup<T>
{
	protected final String id;
	protected final String name;

	public AbstractSoapUIActionGroup( String id, String name )
	{
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public SoapUIActionMapping<T> addMapping( String id, SoapUIActionMapping<T> mapping )
	{
		return null;
	}

	public SoapUIActionMapping<T> addMapping( String id, int index, SoapUIActionMapping<T> mapping )
	{
		return null;
	}

	public int getMappingIndex( String positionRef )
	{
		return -1;
	}
}