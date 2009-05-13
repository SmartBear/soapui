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

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * Default XmlDocument that works on a standard xml string
 * 
 * @author ole.matzura
 */

public class DefaultXmlDocument extends AbstractXmlDocument
{
	private String xml;
	private SchemaTypeSystem typeSystem;

	public DefaultXmlDocument( String xml )
	{
		this.xml = xml;
	}

	public DefaultXmlDocument()
	{
	}

	public void setTypeSystem( SchemaTypeSystem typeSystem )
	{
		this.typeSystem = typeSystem;
	}

	public SchemaTypeSystem getTypeSystem()
	{
		if( typeSystem != null )
			return typeSystem;

		try
		{
			typeSystem = XmlObject.Factory.parse( xml ).schemaType().getTypeSystem();
			return typeSystem;
		}
		catch( Exception e )
		{
			return XmlBeans.getBuiltinTypeSystem();
		}
	}

	public String getXml()
	{
		return xml;
	}

	public void setXml( String xml )
	{
		String oldXml = this.xml;
		this.xml = xml;

		fireXmlChanged( oldXml, xml );
	}

	public void release()
	{
		typeSystem = null;
	}
}
