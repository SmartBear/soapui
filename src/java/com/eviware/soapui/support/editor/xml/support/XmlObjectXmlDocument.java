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

import com.eviware.soapui.SoapUI;

/**
 * Default XmlDocument that works on an existing XmlObject
 * 
 * @author ole.matzura
 */

public class XmlObjectXmlDocument extends AbstractXmlDocument
{
	private XmlObject xmlObject;

	public XmlObjectXmlDocument( XmlObject xmlObject )
	{
		this.xmlObject = xmlObject;
	}

	public SchemaTypeSystem getTypeSystem()
	{
		return xmlObject == null ? XmlBeans.getBuiltinTypeSystem() : xmlObject.schemaType().getTypeSystem();
	}

	public String getXml()
	{
		return xmlObject.toString();
	}

	public void setXml( String xml )
	{
		try
		{
			String old = getXml();
			xmlObject = XmlObject.Factory.parse( xml );
			fireXmlChanged( old, getXml() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public void release()
	{
		xmlObject = null;
	}
}
