/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.action.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIMultiAction;

/**
 * Abstract SoapUIAction for extension
 * 
 * @author ole.matzura
 */

public abstract class AbstractSoapUIMultiAction<T extends ModelItem> implements SoapUIMultiAction
{
	private PropertyChangeSupport propertySupport;
	private String name;
	private String description;
	private boolean enabled = true;
	private String id;

	public AbstractSoapUIMultiAction( String id )
	{
		this.id = id;
		propertySupport = new PropertyChangeSupport( this );
	}

	public AbstractSoapUIMultiAction( String name, String description )
	{
		this( null, name, description );
		id = getClass().getSimpleName();
	}

	public AbstractSoapUIMultiAction( String id, String name, String description )
	{
		this.id = id;
		this.name = name;
		this.description = description;

		propertySupport = new PropertyChangeSupport( this );
	}

	public String getId()
	{
		return id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setEnabled( boolean enabled )
	{
		if( enabled == this.enabled )
			return;

		boolean oldEnabled = this.enabled;
		this.enabled = enabled;

		propertySupport.firePropertyChange( ENABLED_PROPERTY, oldEnabled, enabled );
	}

	public boolean isDefault()
	{
		return false;
	}

	public String getName()
	{
		return name;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertySupport.addPropertyChangeListener( propertyName, listener );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertySupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertySupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertySupport.removePropertyChangeListener( propertyName, listener );
	}

	public void perform( ModelItem target, Object param )
	{
	}
}
