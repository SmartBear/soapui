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

package com.eviware.soapui.impl.wadl.inference.schema.types;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.CustomTypeConfig;

/**
 * CustomType corresponds to any custom type given as a user-defined xsd type
 * definition.
 * 
 * @author Dain Nilsson
 */
public class CustomType implements Type
{
	private String xsd;
	private String name;
	private Schema schema;

	public CustomType( String name, String xsd )
	{
		this.name = name;
		this.xsd = xsd;
	}

	public CustomType( CustomTypeConfig xml, Schema schema )
	{
		this.schema = schema;
		name = xml.getName();
		xsd = xml.getXsd();
	}

	public CustomTypeConfig save()
	{
		CustomTypeConfig xml = CustomTypeConfig.Factory.newInstance();
		xml.setName( name );
		xml.setXsd( xsd );
		return xml;
	}

	public Type validate( Context context ) throws XmlException
	{
		String name = context.getCursor().getName().getLocalPart();
		SchemaTypeSystem sts = XmlBeans.compileXsd( new XmlObject[] { XmlObject.Factory
				.parse( "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"><element name=\"" + name + "\">" + xsd
						+ "</element></schema>" ) }, XmlBeans.getBuiltinTypeSystem(), null );
		SchemaTypeLoader stl = XmlBeans.typeLoaderUnion( new SchemaTypeLoader[] { sts, XmlBeans.getBuiltinTypeSystem() } );
		if( !stl.parse( context.getCursor().xmlText(), null, null ).validate() )
			throw new XmlException( "Element '" + name + "' does not validate for custom type!" );
		return this;
	}

	@Override
	public String toString()
	{
		return xsd;
	}

	public String getName()
	{
		return name;
	}

	public Schema getSchema()
	{
		return schema;
	}

	public void setSchema( Schema schema )
	{
		this.schema = schema;
	}

}
