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

package com.eviware.soapui.support.action;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * A group of actions for a ModelItem
 * 
 * @author ole.matzura
 */

public interface SoapUIActionGroup<T extends ModelItem>
{
	public String getId();

	public String getName();

	public SoapUIActionMappingList<T> getActionMappings( T modelItem );

	public SoapUIActionMapping<? extends ModelItem> addMapping( String id, SoapUIActionMapping<T> mapping );

	public SoapUIActionMapping<? extends ModelItem> addMapping( String id, int index, SoapUIActionMapping<T> mapping );

	public int getMappingIndex( String positionRef );
}