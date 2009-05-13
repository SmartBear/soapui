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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionMapping;

/**
 * Default implementation for a SoapUIActionMapping
 * 
 * @author ole.matzura
 */

public class DefaultActionMapping<T extends ModelItem> implements SoapUIActionMapping<T>
{
	private String actionId;
	private String keyStroke;
	private String iconPath;
	private boolean isDefault;
	private Object param;
	private String description;
	private String name;
	private boolean enabled = true;

	public DefaultActionMapping( String actionId, String keyStroke, String iconPath, boolean isDefault, Object param )
	{
		super();
		this.actionId = actionId;
		this.keyStroke = keyStroke;
		this.iconPath = iconPath;
		this.isDefault = isDefault;
		this.param = param;
	}

	@SuppressWarnings( "unchecked" )
	public SoapUIAction<T> getAction()
	{
		return SoapUI.getActionRegistry().getAction( actionId );
	}

	public boolean isDefault()
	{
		return isDefault;
	}

	public String getIconPath()
	{
		return iconPath;
	}

	public String getKeyStroke()
	{
		return keyStroke;
	}

	public String getActionId()
	{
		return actionId;
	}

	public Object getParam()
	{
		return param;
	}

	public String getDescription()
	{
		return description == null ? getAction().getDescription() : description;
	}

	public String getName()
	{
		return name == null ? getAction().getName() : name;
	}

	public void setActionId( String actionId )
	{
		this.actionId = actionId;
	}

	public SoapUIActionMapping<T> setDescription( String description )
	{
		this.description = description;
		return this;
	}

	public void setIconPath( String iconPath )
	{
		this.iconPath = iconPath;
	}

	public void setDefault( boolean isDefault )
	{
		this.isDefault = isDefault;
	}

	public void setKeyStroke( String keyStroke )
	{
		this.keyStroke = keyStroke;
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