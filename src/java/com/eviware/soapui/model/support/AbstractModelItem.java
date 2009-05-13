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

package com.eviware.soapui.model.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

import com.eviware.soapui.model.ModelItem;

/**
 * Base-class for ModelItem implementations
 * 
 * @author Ole.Matzura
 */

public abstract class AbstractModelItem implements ModelItem
{
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}

	public void notifyPropertyChanged( String name, Object oldValue, Object newValue )
	{
		propertyChangeSupport.firePropertyChange( name, oldValue, newValue );
	}

	public void notifyPropertyChanged( String name, int oldValue, int newValue )
	{
		propertyChangeSupport.firePropertyChange( name, oldValue, newValue );
	}

	public void notifyPropertyChanged( String name, boolean oldValue, boolean newValue )
	{
		propertyChangeSupport.firePropertyChange( name, oldValue, newValue );
	}

	public void fireIndexedPropertyChange( String propertyName, int index, boolean oldValue, boolean newValue )
	{
		propertyChangeSupport.fireIndexedPropertyChange( propertyName, index, oldValue, newValue );
	}

	public void fireIndexedPropertyChange( String propertyName, int index, int oldValue, int newValue )
	{
		propertyChangeSupport.fireIndexedPropertyChange( propertyName, index, oldValue, newValue );
	}

	public void fireIndexedPropertyChange( String propertyName, int index, Object oldValue, Object newValue )
	{
		propertyChangeSupport.fireIndexedPropertyChange( propertyName, index, oldValue, newValue );
	}

	@SuppressWarnings( "unchecked" )
	public List<? extends ModelItem> getChildren()
	{
		return Collections.EMPTY_LIST;
	}

}
