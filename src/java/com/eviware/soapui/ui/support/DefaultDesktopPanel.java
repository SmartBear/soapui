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

package com.eviware.soapui.ui.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * Default implementation for simple DesktopPanels
 * 
 * @author Ole.Matzura
 */

public class DefaultDesktopPanel implements DesktopPanel
{
	private PropertyChangeSupport propertyChangeSupport;
	private String title;
	private JComponent component;
	private Set<ModelItem> depends = new HashSet<ModelItem>();
	private ImageIcon icon;
	private final String description;

	public DefaultDesktopPanel( String title, String description, JComponent component )
	{
		this.title = title;
		this.description = description;
		this.component = component;

		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public void loadIcon( String path )
	{
		icon = UISupport.createImageIcon( path );
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setTitle( String title )
	{
		String oldTitle = this.title;
		this.title = title;

		propertyChangeSupport.firePropertyChange( TITLE_PROPERTY, oldTitle, title );
	}

	public ModelItem getModelItem()
	{
		return null;
	}

	public boolean onClose( boolean canCancel )
	{
		return true;
	}

	public JComponent getComponent()
	{
		return component;
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return depends != null && depends.contains( modelItem );
	}

	public void addDependency( ModelItem modelItem )
	{
		depends.add( modelItem );
	}

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

	public Icon getIcon()
	{
		return icon;
	}
}
