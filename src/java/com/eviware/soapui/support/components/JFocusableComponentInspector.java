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

package com.eviware.soapui.support.components;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class JFocusableComponentInspector<T extends JComponent> implements Inspector
{
	private final T component;
	private String title;
	private String description;
	private boolean enabled;
	private PropertyChangeSupport propertyChangeSupport;
	private ImageIcon imageIcon;
	private String id;
	private final JComponent target;

	public JFocusableComponentInspector( T component, JComponent target, String title, String description,
			boolean enabled )
	{
		this.component = component;
		this.target = target;
		this.title = title;
		this.id = title;
		this.description = description;
		this.enabled = enabled;
	}

	public void activate()
	{
		target.requestFocusInWindow();
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		if( propertyChangeSupport == null )
			propertyChangeSupport = new PropertyChangeSupport( this );

		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public T getComponent()
	{
		return component;
	}

	public String getDescription()
	{
		return description;
	}

	public String getInspectorId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void release()
	{
	}

	public void setDescription( String description )
	{
		String old = this.description;
		this.description = description;

		if( propertyChangeSupport != null )
			propertyChangeSupport.firePropertyChange( Inspector.DESCRIPTION_PROPERTY, old, description );
	}

	public void setEnabled( boolean enabled )
	{
		if( enabled == this.enabled )
			return;

		this.enabled = enabled;
		if( propertyChangeSupport != null )
			propertyChangeSupport.firePropertyChange( Inspector.ENABLED_PROPERTY, !enabled, enabled );
	}

	public void setTitle( String title )
	{
		String old = this.title;
		this.title = title;

		if( propertyChangeSupport != null )
			propertyChangeSupport.firePropertyChange( Inspector.TITLE_PROPERTY, old, title );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		if( propertyChangeSupport != null )
			propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public ImageIcon getIcon()
	{
		return imageIcon;
	}

	public void setIcon( ImageIcon imageIcon )
	{
		ImageIcon old = this.imageIcon;

		this.imageIcon = imageIcon;
		if( propertyChangeSupport != null )
			propertyChangeSupport.firePropertyChange( Inspector.ICON_PROPERTY, old, imageIcon );
	}

	public void deactivate()
	{
	}
}
