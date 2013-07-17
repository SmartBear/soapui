/*
 * soapUI, copyright (C) 2004-2013 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */
package com.eviware.soapui.impl;


/**
 * URLParser should parse the URI based on ths standard syntax components referred
 * in http://www.ietf.org/rfc/rfc3986.txt as [scheme:][//authority][path][?query][#fragment]
 * Author: Shadid Chowdhury
 */

public interface URIParser
{
	/**
	 * @return
	 */
	public String getScheme();

	/**
	 * @return
	 */
	public String getAuthority();

	/**
	 * @return
	 */
	public String getPath();

	/**
	 * @return
	 */
	public String getQuery();

	/**
	 * @return
	 */
	public String getFragment();

}
