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

package com.eviware.soapui.impl.wadl.inference;

import javax.xml.namespace.QName;

/**
 * Handles schedule conflicts while inferring Xml schema from Xml documents. Has
 * a single callback method.
 * 
 * @author Dain Nilsson
 */
public interface ConflictHandler
{

	/**
	 * Callback method for deciding whether given Xml document is valid or not,
	 * and if so, to expand the schema. The function should return true if the
	 * contents at the cursor is valid in respect to the message provided, false
	 * if not.
	 * 
	 * @param event
	 *           What type of event this is, creation or modification.
	 * @param type
	 *           The type of particle that this is in regards to.
	 * @param name
	 *           The QName for the particle that is being modified.
	 * @param path
	 *           The path to the element that is being changed (or contains the
	 *           attribute/has the type that is beng changed).
	 * @param message
	 *           A short message describing the change.
	 * @return True to accept the schema modification and continue validation,
	 *         false to trigger validation failure.
	 */
	public boolean callback( Event event, Type type, QName name, String path, String message );

	public enum Type
	{
		ELEMENT, ATTRIBUTE, TYPE
	}

	public enum Event
	{
		CREATION, MODIFICATION
	}
}
