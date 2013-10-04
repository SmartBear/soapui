/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */
package com.eviware.soapui.impl;


/**
 * URIParser should parse the URI based on ths standard syntax components referred
 * in {@link http://www.ietf.org/rfc/rfc3986.txt} as [scheme:][//authority][path][?query][#fragment]
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */

public interface URIParser
{

	/**
	 * This method returns the scheme of the URI if there is one, otherwise empty space.
	 *
	 * @return scheme of a the URI
	 */
	public String getScheme();

	/**
	 * This method returns the decoded authority component of the URI.
	 * Usually authority is composed of hostname and port.
	 *
	 * @return decoded authority of the URI or empty space if there is no authority in the URI
	 */
	//public String getAuthority();

	/**
	 * This method returns the decoded path of the URI.
	 *
	 * @return decoded path of the URI or empty space if there is no path in the URI
	 */
	public String getResourcePath();

	/**
	 * This method returns the decoded query of the URI.
	 *
	 * @return decoded query of the URI or empty space if there is no query in the URI
	 */
	public String getQuery();

}
