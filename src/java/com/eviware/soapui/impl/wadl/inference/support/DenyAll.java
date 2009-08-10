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
 * A ConflictHandler that denies any attempt to change the inferred schema.
 * 
 * @author Dain Nilsson
 */
public class DenyAll implements ConflictHandler
{
	/**
	 * Constructs a new DenyAll instance.
	 */
	public DenyAll()
	{

	}

	public boolean callback( Event event, Type type, QName name, String path, String message )
	{
		return false;
	}

}
