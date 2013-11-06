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
package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.URIParser;

/**
 * RestURIParser should parse the URI based on ths standard syntax components referred
 * in {@link http://www.ietf.org/rfc/rfc3986.txt} as [scheme:][//authority][path][?query][#fragment] for HTTP/HTTPS scheme
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */
public interface RestURIParser extends URIParser
{

	/**
	 * This method returns the decoded endpoint of the URI.
	 * Endpoint is composed of [HTTP/HTTPS] followed by hostname and port.
	 *
	 * @return decoded endpoint of the URI or empty space if there is no endpoint in the URI
	 */
	public String getEndpoint();

	/**
	 * This method returns the resource name.
	 * Resource name is taken from the path, usually the last part of the path.
	 *
	 * @return decoded resourceName of the URI or empty space if there is no path in the URI
	 */
	public String getResourceName();

}
