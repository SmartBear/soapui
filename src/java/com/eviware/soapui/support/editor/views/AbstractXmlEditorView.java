/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.EditorLocationListener;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlLocation;

/**
 * Abstract base-class to be extended by XmlViews
 * 
 * @author ole.matzura
 */

public abstract class AbstractXmlEditorView implements XmlEditorView, PropertyChangeListener
{
	private String title;
	private boolean isActive;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );
	private XmlDocument xmlDocument;
	private boolean xmlChanged;
	private Set<EditorLocationListener<XmlDocument>> listeners = new HashSet<EditorLocationListener<XmlDocument>>();
	private XmlEditor editor;
	
	public AbstractXmlEditorView(String title, XmlEditor xmlEditor)
	{
		super();
		this.title = title;
		editor = xmlEditor;
		xmlChanged = false;
	}

	protected PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}
	
	public boolean activate(EditorLocation<XmlDocument> location)
	{
		isActive = true;
		update();
		
		return true;
	}

	public void update()
	{
		if( xmlChanged  )
		{
			setXml( xmlDocument == null ? null : xmlDocument.getXml() );
			xmlChanged = false;
		}
	}

	public boolean isXmlChanged()
	{
		return xmlChanged;
	}

	public boolean deactivate()
	{
		isActive = false;
		xmlChanged = false;
		
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
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
	   propertyChangeSupport.addPropertyChangeListener(propertyName, listener);	
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}
	
	public XmlDocument getDocument()
	{
		return xmlDocument;
	}

	public void setDocument(XmlDocument xmlDocument)
	{
		if( this.xmlDocument != null )
		{
			this.xmlDocument.removePropertyChangeListener( XmlDocument.XML_PROPERTY, this );
		}
		
		this.xmlDocument = xmlDocument;
		
		if( xmlDocument != null )
		{
			this.xmlDocument.addPropertyChangeListener( XmlDocument.XML_PROPERTY, this );
			setXml( xmlDocument.getXml() );
		}
		else
		{
			setXml( null );
		}
		
		xmlChanged = false;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if( isActive() )
			setXml( (String) evt.getNewValue() );
		else
			xmlChanged = true;
	}
	
	public abstract void setXml( String xml );

	public void release()
	{
		if( this.xmlDocument != null )
		{
			this.xmlDocument.removePropertyChangeListener( XmlDocument.XML_PROPERTY, this );
			this.xmlDocument = null;
		}		
	}

	public void addLocationListener(EditorLocationListener<XmlDocument> listener)
	{
		listeners.add( listener );
	}

	public void removeLocationListener(EditorLocationListener<XmlDocument> listener)
	{
		listeners.remove( listener );
	}
	
	public void fireLocationChanged( EditorLocation<XmlDocument> location )
	{
		for( EditorLocationListener<XmlDocument> listener : listeners )
			listener.locationChanged( location );
	}
	
	public XmlLocation getEditorLocation()
	{
		return null;
	}
	
	public String getXml()
	{
		return xmlDocument == null ? null : xmlDocument.getXml();
	}

	public void setLocation(EditorLocation<XmlDocument> location)
	{
	}
	
	public void locationChanged(EditorLocation<XmlDocument> location)
	{
	}

	public void syncUpdates()
	{
		if( !isActive() && xmlChanged )
		{
			setXml( xmlDocument == null ? null : xmlDocument.getXml() );
			xmlChanged = false;
		}
	}
	
	public XmlEditor getEditor()
	{
		return editor;
	}

	public void requestFocus()
	{
	}
}
