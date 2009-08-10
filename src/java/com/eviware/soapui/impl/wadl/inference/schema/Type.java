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

package com.eviware.soapui.impl.wadl.inference.schema;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.schema.types.CustomType;
import com.eviware.soapui.impl.wadl.inference.schema.types.EmptyType;
import com.eviware.soapui.impl.wadl.inference.schema.types.SimpleType;
import com.eviware.soapui.impl.wadl.inference.schema.types.TypeReferenceType;
import com.eviware.soapui.inferredSchema.CustomTypeConfig;
import com.eviware.soapui.inferredSchema.EmptyTypeConfig;
import com.eviware.soapui.inferredSchema.SimpleTypeConfig;
import com.eviware.soapui.inferredSchema.TypeConfig;
import com.eviware.soapui.inferredSchema.TypeReferenceConfig;

/**
 * An instance of an XML Schema type.
 * 
 * @author Dain Nilsson
 */
public interface Type
{

	/**
	 * Return the name for the type, not including namespace prefix.
	 * 
	 * @return The name of the type.
	 */
	public String getName();

	/**
	 * Getter for the schema in which the element/attribute with this type lives.
	 * 
	 * @return The Schema for the type.
	 */
	public Schema getSchema();

	/**
	 * Validate an element/attribute with this type.
	 * 
	 * @param context
	 *           A Context object holding the current
	 * @return Returns a Type that is valid for the element/attribute, quite
	 *         possibly this Type instance itself.
	 * @throws XmlException
	 */
	public Type validate( Context context ) throws XmlException;

	public String toString();

	/**
	 * Setter for the schema in which this type lives.
	 * 
	 * @param schema
	 */
	public void setSchema( Schema schema );

	/**
	 * Serialize instance to XmlObject.
	 * 
	 * @return Returns an XmlObject storing the variables of this instance.
	 */
	public TypeConfig save();

	/**
	 * A static factory class for creating new instances.
	 * 
	 * @author Dain Nilsson
	 */
	public class Factory
	{

		/**
		 * Creates a new empty Type object.
		 * 
		 * @param schema
		 *           The Schema in which to place the type.
		 * @return The newly created Type.
		 */
		public static Type newType( Schema schema )
		{
			return new EmptyType( schema );
		}

		/**
		 * Parses the given XmlObject into a Type instance.
		 * 
		 * @param xml
		 *           The XmlObject storing the saved type.
		 * @param schema
		 *           The schema in which to place the type.
		 * @return The loaded type.
		 */
		public static Type parse( TypeConfig xml, Schema schema )
		{
			if( xml instanceof TypeReferenceConfig )
				return new TypeReferenceType( ( TypeReferenceConfig )xml, schema );
			if( xml instanceof SimpleTypeConfig )
				return new SimpleType( ( SimpleTypeConfig )xml, schema );
			if( xml instanceof EmptyTypeConfig )
				return new EmptyType( ( EmptyTypeConfig )xml, schema );
			if( xml instanceof CustomTypeConfig )
				return new CustomType( ( CustomTypeConfig )xml, schema );
			return null;
		}
	}
}
