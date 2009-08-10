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

/**
 * Static class containing package global variables.
 * 
 * @author Dain Nilsson
 */
public class Settings
{

	/**
	 * Locality used for iLOCAL algorithm when inferring complex types.
	 */
	public static final int locality = 2;

	/**
	 * The namespace for XML Schema.
	 */
	public static final String xsdns = "http://www.w3.org/2001/XMLSchema";

	/**
	 * The namespace for XML Schema-instance.
	 */
	public static final String xsins = "http://www.w3.org/2001/XMLSchema-instance";
}
