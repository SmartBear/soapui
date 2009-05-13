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

package com.eviware.soapui.support.editor.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorDocument;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.EditorLocationListener;
import com.eviware.soapui.support.editor.EditorView;

/**
 * Abstract base-class to be extended by XmlViews
 * 
 * @author ole.matzura
 */

public abstract class AbstractEditorView<T extends EditorDocument> implements EditorView<T>
{
	private String title;
	private boolean isActive;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );
	private T xmlDocument;
	private Set<EditorLocationListener<T>> listeners = new HashSet<EditorLocationListener<T>>();
	private Editor<T> editor;
	private JComponent component;
	private final String viewId;

	public AbstractEditorView( String title, Editor<T> editor, String viewId )
	{
		super();
		this.title = title;
		this.editor = editor;
		this.viewId = viewId;
	}

	protected PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}

	public JComponent getComponent()
	{
		if( component == null )
			component = buildUI();

		return component;
	}

	public String getViewId()
	{
		return viewId;
	}

	public void requestFocus()
	{
		if( component != null )
			component.requestFocusInWindow();
	}

	public abstract JComponent buildUI();

	public boolean activate( EditorLocation<T> location )
	{
		isActive = true;
		return true;
	}

	public boolean deactivate()
	{
		isActive = false;
		return true;
	}

	public boolean isActive()
	{
		return isActive;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		String oldTitle = this.title;
		this.title = title;

		propertyChangeSupport.firePropertyChange( TITLE_PROPERTY, oldTitle, title );
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

	public T getDocument()
	{
		return xmlDocument;
	}

	public void setDocument( T xmlDocument )
	{
		this.xmlDocument = xmlDocument;
	}

	public void release()
	{
		if( this.xmlDocument != null )
		{
			this.xmlDocument = null;
		}
	}

	public void addLocationListener( EditorLocationListener<T> listener )
	{
		listeners.add( listener );
	}

	public void removeLocationListener( EditorLocationListener<T> listener )
	{
		listeners.remove( listener );
	}

	public void fireLocationChanged( EditorLocation<T> location )
	{
		for( EditorLocationListener<T> listener : listeners )
			listener.locationChanged( location );
	}

	public EditorLocation<T> getEditorLocation()
	{
		return null;
	}

	public void setLocation( EditorLocation<T> location )
	{
	}

	public void locationChanged( EditorLocation<T> location )
	{
	}

	public Editor<T> getEditor()
	{
		return editor;
	}

	public void setEditable( boolean enabled )
	{
	}
}
