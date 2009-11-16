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

package com.eviware.soapui.support.editor.xml.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.support.editor.xml.XmlDocument;

/**
 * Abstract base-class for XmlDocument implementations
 * 
 * @author ole.matzura
 */

public abstract class AbstractXmlDocument implements XmlDocument
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

	protected void fireXmlChanged( String oldValue, String newValue )
	{
		propertyChangeSupport.firePropertyChange( XML_PROPERTY, oldValue, newValue );
	}

	public void release()
	{
	}

	public SchemaTypeSystem getTypeSystem()
	{
		return XmlBeans.getBuiltinTypeSystem();
	}
}
