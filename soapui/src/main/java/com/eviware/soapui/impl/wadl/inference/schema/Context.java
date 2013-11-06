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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlCursor;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;

/**
 * An object that holds information about a validation in-progress, such as the
 * cursor of the content to be validated., etc.
 * 
 * @author Dain Nilsson
 */
public class Context
{
	private ConflictHandler handler;
	private XmlCursor cursor;
	private SchemaSystem system;
	private List<String> path;
	private List<List<String>> stack;
	private Map<String, String> attributes;

	/**
	 * Creates a new Context object.
	 * 
	 * @param system
	 *           The SchemaSystem holding the namespaces to be used for
	 *           validation.
	 * @param handler
	 *           The ConflictHandler to use whenever a validation error occurs to
	 *           decide upon which action to take.
	 * @param cursor
	 *           An XmlCursor pointing to the beginning of the XML content to
	 *           validate.
	 */
	public Context( SchemaSystem system, ConflictHandler handler, XmlCursor cursor )
	{
		this.system = system;
		this.handler = handler;
		this.cursor = cursor;
		path = new ArrayList<String>();
		stack = new ArrayList<List<String>>();
		attributes = new HashMap<String, String>();
	}

	/**
	 * Getter for the contained ConflictHandler.
	 * 
	 * @return Returns the ConflictHandler used for validation.
	 */
	public ConflictHandler getHandler()
	{
		return handler;
	}

	/**
	 * Get a name to use for a Complex Type at the current path. Names are
	 * derived from paths, using the locality from the Settings class.
	 * 
	 * @return A name to be used for the Complex Type at the current location.
	 */
	public String getName()
	{
		String path = getPath().replace( "/", "_" );
		int parts = Settings.locality;
		int i = path.length();
		while( parts > 0 && i > 0 )
		{
			i-- ;
			if( path.charAt( i ) == '_' )
				parts-- ;
		}
		if( parts > 0 )
			return path;
		return path.substring( i + 1 );
	}

	/**
	 * Get a stored attribute.
	 * 
	 * @param key
	 *           The key of the attribute to get.
	 * @return Returns the value of the attribute, if it exists. An empty string
	 *         is returned if not.
	 */
	public String getAttribute( String key )
	{
		if( attributes.containsKey( key ) )
			return attributes.get( key );
		return "";
	}

	/**
	 * Store an attribute.
	 * 
	 * @param key
	 *           The name of the attribute to store.
	 * @param value
	 *           The value to store.
	 */
	public void putAttribute( String key, String value )
	{
		attributes.put( key, value );
	}

	/**
	 * Delete a stored attribute.
	 * 
	 * @param key
	 *           The name of the attribute to delete.
	 */
	public void clearAttribute( String key )
	{
		attributes.remove( key );
	}

	/**
	 * Get the path currently at.
	 * 
	 * @return Returns the current path, elements are separated by slash.
	 */
	public String getPath()
	{
		StringBuilder s = new StringBuilder();
		for( String item : path )
			s.append( "/" + item );
		return s.toString();
	}

	/**
	 * Push the current path to an internal stack, and start with an empty path.
	 */
	public void pushPath()
	{
		stack.add( path );
		path = new ArrayList<String>();
	}

	/**
	 * Pop a previously pushed path from the internal stack, overwriting whatever
	 * is currently in the path.
	 */
	public void popPath()
	{
		int last = stack.size() - 1;
		if( last >= 0 )
		{
			path = stack.get( last );
			stack.remove( last );
		}
	}

	/**
	 * Append an element to the end of the current path.
	 * 
	 * @param item
	 *           The name of the element to trascend into.
	 */
	public void cd( String item )
	{
		path.add( item );
	}

	/**
	 * Move up one level, removing the last element from the path.
	 */
	public void up()
	{
		if( path.size() > 0 )
			path.remove( path.size() - 1 );
	}

	/**
	 * Get the internal cursor pointing to the current position of the XML
	 * content to be validated.
	 * 
	 * @return Returns an XmlCursor.
	 */
	public XmlCursor getCursor()
	{
		return cursor;
	}

	/**
	 * Get the SchemaSystem currently used for validation.
	 * 
	 * @return Returns a SchemaSystem.
	 */
	public SchemaSystem getSchemaSystem()
	{
		return system;
	}
}
