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

package com.eviware.soapui.impl.wadl.inference.support;

import javax.xml.namespace.QName;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;

/**
 * ConflictHandler that allows any changes that need to be made to the schema in
 * order to validate against given XML document.
 * 
 * @author Dain Nilsson
 */
public class AllowAll implements ConflictHandler
{
	/**
	 * Constructs a new AllowAll instance.
	 */
	public AllowAll()
	{

	}

	public boolean callback( Event event, Type type, QName name, String path, String message )
	{
		StringBuilder s = new StringBuilder( message ).append( "\n" );
		if( event == Event.CREATION )
			s.append( "Create " );
		else if( event == Event.MODIFICATION )
			s.append( "Modify " );
		if( type == Type.ELEMENT )
			s.append( "element '" );
		else if( type == Type.ATTRIBUTE )
			s.append( "attribute '" );
		else if( type == Type.TYPE )
			s.append( "type '" );
		s.append( name.getLocalPart() ).append( "' in namespace '" ).append( name.getNamespaceURI() ).append( "'" );
		s.append( " at path " ).append( path ).append( "?" );
		System.out.println( s.toString() );
		return true;
	}

}
