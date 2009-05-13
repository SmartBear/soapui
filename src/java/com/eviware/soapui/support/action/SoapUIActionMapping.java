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

/**
 * The mapping of a SoapUIAction into a SoapUIActionGroup
 * 
 * @author ole.matzura
 */

public interface SoapUIActionMapping<T extends ModelItem>
{
	public SoapUIAction<T> getAction();

	public String getActionId();

	public String getName();

	public String getDescription();

	public boolean isDefault();

	public boolean isEnabled();

	public String getIconPath();

	public String getKeyStroke();

	public Object getParam();

	public SoapUIActionMapping<T> setName( String name );

	public SoapUIActionMapping<T> setDescription( String description );

	public SoapUIActionMapping<T> setParam( Object param );

	public SoapUIActionMapping<T> setEnabled( boolean enabled );
}