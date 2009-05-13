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

package com.eviware.soapui.support.editor.xml;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.support.editor.EditorLocation;

/**
 * Location in a XmlDocument
 * 
 * @author ole.matzura
 */

public class XmlLocation implements EditorLocation<XmlDocument>
{
	private final int line;
	private final int column;
	private XmlObject xmlObject;
	private final SchemaType schemaType;
	private String documentation;

	public XmlLocation( int line, int column )
	{
		this( line, column, null, null, null );
	}

	public XmlLocation( int line, int column, XmlObject xmlObject, SchemaType schemaType, String documentation )
	{
		this.line = line;
		this.column = column;
		this.xmlObject = xmlObject;
		this.schemaType = schemaType;
		this.documentation = documentation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.panels.request.components.editor.EditorLocation
	 * #getColumn()
	 */
	public int getColumn()
	{
		return column;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.panels.request.components.editor.EditorLocation
	 * #getLine()
	 */
	public int getLine()
	{
		return line;
	}

	public SchemaType getSchemaType()
	{
		return schemaType;
	}

	public XmlObject getXmlObject()
	{
		return xmlObject;
	}

	public String getDocumentation()
	{
		return documentation;
	}

	public void setDocumentation( String documentation )
	{
		this.documentation = documentation;
	}

}
