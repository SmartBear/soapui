/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.model.iface.MessagePart;

/**
 * Descriptor for a Message/SOAP Header
 * 
 * @author ole.matzura
 */

public class WsdlHeaderPart extends MessagePart.HeaderPart
{
	private String name;
	private SchemaType schemaType;
	private QName partElementName;
	private final SchemaGlobalElement partElement;

	public WsdlHeaderPart( String name, SchemaType schemaType, QName partElementName, SchemaGlobalElement partElement )
	{
		super();

		this.name = name;
		this.schemaType = schemaType;
		this.partElementName = partElementName;
		this.partElement = partElement;
	}

	public SchemaType getSchemaType()
	{
		return schemaType;
	}

	public String getDescription()
	{
		return name + " of type [" + schemaType.getName() + "]";
	}

	public String getName()
	{
		return name;
	}

	public QName getPartElementName()
	{
		return partElementName;
	}

	@Override
	public SchemaGlobalElement getPartElement()
	{
		return partElement;
	}
}
