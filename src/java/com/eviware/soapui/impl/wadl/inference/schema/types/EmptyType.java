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

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.impl.wadl.inference.schema.content.EmptyContent;
import com.eviware.soapui.impl.wadl.inference.support.TypeInferrer;
import com.eviware.soapui.inferredSchema.EmptyTypeConfig;

/**
 * EmptyRtpe corresponds to an instance of a type with no attributes, nor any
 * content.
 * 
 * @author Dain Nilsson
 */
public class EmptyType implements Type
{
	private Schema schema;
	private EmptyContent empty;
	private boolean completed = false;

	public EmptyType( Schema schema )
	{
		this.schema = schema;
		empty = new EmptyContent( schema, false );
	}

	public EmptyType( EmptyTypeConfig xml, Schema schema )
	{
		this.schema = schema;
		empty = new EmptyContent( schema, xml.getCompleted() );
		completed = xml.getCompleted();
	}

	public EmptyTypeConfig save()
	{
		EmptyTypeConfig xml = EmptyTypeConfig.Factory.newInstance();
		xml.setCompleted( completed );
		return xml;
	}

	public String getName()
	{
		return "empty_element";
	}

	public Schema getSchema()
	{
		return schema;
	}

	public void setSchema( Schema schema )
	{
		this.schema = schema;
	}

	public Type validate( Context context ) throws XmlException
	{
		XmlCursor cursor = context.getCursor();
		if( !cursor.isAttr() && ( cursor.toFirstAttribute() || cursor.toFirstChild() ) )
		{
			// Element has attributes or children, must be complexType
			ComplexType newType = new ComplexType( schema, context.getName(), completed );
			newType.setContent( empty );
			return newType;
		}
		cursor.toFirstContentToken();
		if( empty.validate( context ) != empty )
		{
			// Element has simple content, must be simpleType
			String value = cursor.getTextValue();
			XmlAnySimpleType simpleType;
			if( completed )
				simpleType = TypeInferrer.getBlankType();
			else
				simpleType = TypeInferrer.inferSimpleType( value );
			// return
			// context.getSchemaSystem().getType(simpleType.schemaType().getName());
			return new SimpleType( schema, simpleType, completed );
		}
		completed = true;
		return this;
	}

	public String toString()
	{
		String xsdns = schema.getPrefixForNamespace( Settings.xsdns );
		StringBuilder s = new StringBuilder( "<" + xsdns + ":complexType name=\"" + getName() + "\"/>" );
		return s.toString();
	}

}
