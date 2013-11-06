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

package com.eviware.soapui.impl.wadl.inference.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.inferredSchema.SchemaConfig;
import com.eviware.soapui.inferredSchema.SchemaSetConfig;

/**
 * Represents a set of namespaces with inferred schemas.
 * 
 * @author Dain Nilsson
 */
public class SchemaSystem
{
	Map<String, Schema> schemas;

	/**
	 * Constructs a new SchemaSystem object.
	 */
	public SchemaSystem()
	{
		schemas = new LinkedHashMap<String, Schema>();
	}

	/**
	 * Constructs a SchemaSystem object using previously saved data.
	 * 
	 * @param xml
	 *           The XmlObject to which data has previously been saved.
	 */
	public SchemaSystem( SchemaSetConfig xml )
	{
		schemas = new LinkedHashMap<String, Schema>();
		for( SchemaConfig item : xml.getSchemaList() )
			schemas.put( item.getNamespace(), new Schema( item, this ) );
	}

	/**
	 * Saves the SchemaSystem to an XmlObject.
	 * 
	 * @param xml
	 *           A blank XmlObject to save to.
	 */
	public void save( SchemaSetConfig xml )
	{
		for( Schema item : schemas.values() )
			item.save( xml.addNewSchema() );
	}

	/**
	 * Create a blank new Schema under this SchemaSystem for a given namespace.
	 * 
	 * @param namespace
	 *           The namespace for which to create a Schema.
	 * @return The newly created Schema.
	 */
	public Schema newSchema( String namespace )
	{
		Schema schema = new Schema( namespace, this );
		schemas.put( namespace, schema );
		return schema;
	}

	/**
	 * Returns the matching Schema for the given namespace.
	 * 
	 * @param namespace
	 *           A namespace that already exists within the SchemaSystem.
	 * @return Returns the Schema corresponding to the given namespace if one
	 *         exists. Otherwise returns null.
	 */
	public Schema getSchemaForNamespace( String namespace )
	{
		return schemas.get( namespace );
	}

	/**
	 * Get an existing Type by its QName.
	 * 
	 * @param qname
	 *           A QName containing the namespace URI of the schema in which the
	 *           Type exists, and also the name of the type.
	 * @return Returns the Type, if one is found. Otherwise returns null.
	 */
	public Type getType( QName qname )
	{
		return getSchemaForNamespace( qname.getNamespaceURI() ).getType( qname.getLocalPart() );
	}

	/**
	 * Validate an XmlObject against the contained inferred schemas. Upon
	 * validation errors, the ConflictHandler is used to determine if a schema
	 * should be adjusted, or if validation should fail.
	 * 
	 * @param xmlo
	 *           An XmlObject containing the document to be validated.
	 * @param handler
	 *           A ConflictHandler to use on validation errors.
	 * @throws XmlException
	 *            On unresolvable validation error.
	 */
	public void validate( XmlObject xmlo, ConflictHandler handler ) throws XmlException
	{
		XmlCursor cursor = xmlo.newCursor();
		cursor.toFirstChild();
		Schema s = getSchemaForNamespace( cursor.getName().getNamespaceURI() );
		boolean created = false;
		if( s == null )
		{
			s = newSchema( cursor.getName().getNamespaceURI() );
			created = true;
		}
		Context context = new Context( this, handler, cursor );
		try
		{
			s.validate( context );
		}
		catch( XmlException e )
		{
			if( created )
				schemas.remove( s.getNamespace() );
			throw e;
		}
	}

	/**
	 * Get a list of contained namespaces.
	 * 
	 * @return Returns the contained namespaces, as a Set.
	 */
	public Set<String> getNamespaces()
	{
		return schemas.keySet();
	}

	public void deleteNamespace( String ns )
	{
		schemas.remove( ns );
	}

}
