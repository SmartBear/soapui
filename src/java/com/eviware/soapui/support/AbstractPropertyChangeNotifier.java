/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractPropertyChangeNotifier implements PropertyChangeNotifier
{
	private PropertyChangeSupport propertyChangeSupport;

	protected AbstractPropertyChangeNotifier()
	{
		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		if( propertyChangeSupport != null )
			propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		if( propertyChangeSupport != null )
			propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		if( propertyChangeSupport != null )
			propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		if( propertyChangeSupport != null )
			propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}

	public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
	{
		propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
	}

	public void firePropertyChange( String propertyName, int oldValue, int newValue )
	{
		propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
	}

	public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue )
	{
		propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
	}

	public void firePropertyChange( PropertyChangeEvent evt )
	{
		propertyChangeSupport.firePropertyChange( evt );
	}

	public void fireIndexedPropertyChange( String propertyName, int index, Object oldValue, Object newValue )
	{
		propertyChangeSupport.fireIndexedPropertyChange( propertyName, index, oldValue, newValue );
	}

	public void fireIndexedPropertyChange( String propertyName, int index, int oldValue, int newValue )
	{
		propertyChangeSupport.fireIndexedPropertyChange( propertyName, index, oldValue, newValue );
	}

	public void fireIndexedPropertyChange( String propertyName, int index, boolean oldValue, boolean newValue )
	{
		propertyChangeSupport.fireIndexedPropertyChange( propertyName, index, oldValue, newValue );
	}

	protected PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}
}
