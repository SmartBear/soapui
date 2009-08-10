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

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.SchemaSystem;
import com.eviware.soapui.impl.wadl.inference.schema.Type;
import com.eviware.soapui.inferredSchema.TypeConfig;
import com.eviware.soapui.inferredSchema.TypeReferenceConfig;

/**
 * This Type is simply a reference to another, actual Type. It is used when
 * loading previously saved data, since the Type may not yet be loaded.
 * 
 * @author Dain Nilsson
 */
public class TypeReferenceType implements Type
{
	String name;
	String namespace;
	SchemaSystem schemaSystem;

	/**
	 * Constructs a new TypeReferenceType from previously saved data. Should be
	 * called in the Type.Factory.
	 */
	public TypeReferenceType( TypeReferenceConfig xml, Schema schema )
	{
		schemaSystem = schema.getSystem();
		name = xml.getReference().getLocalPart();
		namespace = xml.getReference().getNamespaceURI();
	}

	public TypeConfig save()
	{
		return schemaSystem.getSchemaForNamespace( namespace ).getType( name ).save();
	}

	public String getName()
	{
		return name;
	}

	public Type validate( Context context ) throws XmlException
	{
		return schemaSystem.getSchemaForNamespace( namespace ).getType( name );
	}

	public Schema getSchema()
	{
		return schemaSystem.getSchemaForNamespace( namespace );
	}

	public void setSchema( Schema schema )
	{
		namespace = schema.getNamespace();
	}

}
