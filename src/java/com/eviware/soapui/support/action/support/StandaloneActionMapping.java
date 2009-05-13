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
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionMapping;

/**
 * A standalone SoapUIAcionMapping
 * 
 * @author ole.matzura
 */

public class StandaloneActionMapping<T extends ModelItem> implements SoapUIActionMapping<T>
{
	private final SoapUIAction<T> action;
	private String keyStroke;
	private String description;
	private String name;
	private Object param;
	private String iconPath;
	private boolean enabled = true;

	public StandaloneActionMapping( SoapUIAction<T> action, String keyStroke, String iconPath )
	{
		if( action == null )
			throw new IllegalArgumentException( "action can't be null" );
		this.action = action;
		this.keyStroke = keyStroke;
		this.iconPath = iconPath;
	}

	public StandaloneActionMapping( SoapUIAction<T> action, String keyStroke )
	{
		this.action = action;
		this.keyStroke = keyStroke;
	}

	public StandaloneActionMapping( SoapUIAction<T> action )
	{
		this.action = action;
	}

	public SoapUIAction<T> getAction()
	{
		return action;
	}

	public String getActionId()
	{
		return action.getClass().getSimpleName();
	}

	public String getIconPath()
	{
		return iconPath;
	}

	public String getKeyStroke()
	{
		return keyStroke;
	}

	public Object getParam()
	{
		return param;
	}

	public boolean isDefault()
	{
		return false;
	}

	public String getDescription()
	{
		return description == null ? action.getDescription() : description;
	}

	public String getName()
	{
		return name == null ? action.getName() : name;
	}

	public SoapUIActionMapping<T> setDescription( String description )
	{
		this.description = description;
		return this;
	}

	public SoapUIActionMapping<T> setName( String name )
	{
		this.name = name;
		return this;
	}

	public SoapUIActionMapping<T> setParam( Object param )
	{
		this.param = param;
		return this;
	}

	public String getId()
	{
		return null;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public SoapUIActionMapping<T> setEnabled( boolean enabled )
	{
		this.enabled = enabled;
		return this;
	}
}