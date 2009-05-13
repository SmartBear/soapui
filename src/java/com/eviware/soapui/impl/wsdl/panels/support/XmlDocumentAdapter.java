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

package com.eviware.soapui.impl.wsdl.panels.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.xmlbeans.SchemaTypeSystem;

import com.eviware.soapui.support.editor.xml.XmlDocument;

/**
 * Adapter for XmlDocument implementations/sources
 * 
 * @author ole.matzura
 */

public class XmlDocumentAdapter implements XmlDocument
{
	private String xml;
	private SchemaTypeSystem typeSystem;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

	public SchemaTypeSystem getTypeSystem()
	{
		return typeSystem;
	}

	public void setTypeSystem( SchemaTypeSystem typeSystem )
	{
		this.typeSystem = typeSystem;
	}

	public String getXml()
	{
		return xml;
	}

	public boolean hasTypeSystem()
	{
		return typeSystem != null;
	}

	public void setXml( String xml )
	{
		String oldXml = this.xml;
		this.xml = xml;

		propertyChangeSupport.firePropertyChange( XML_PROPERTY, oldXml, xml );
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

	public void release()
	{
		typeSystem = null;
	}
}
