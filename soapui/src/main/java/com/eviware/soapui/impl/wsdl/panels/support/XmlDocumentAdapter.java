/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
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
