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

package com.eviware.soapui.model.iface;

import com.eviware.soapui.support.types.StringToStringsMap;

/**
 * Request Response behaviour
 * 
 * @author Ole.Matzura
 */

public interface Response
{
	public Request getRequest();

	public String getContentAsString();

	public long getContentLength();

	public String getRequestContent();

	public String getContentType();

	public long getTimeTaken();

	public Attachment[] getAttachments();

	public Attachment[] getAttachmentsForPart( String partName );

	public StringToStringsMap getRequestHeaders();

	public StringToStringsMap getResponseHeaders();

	public long getTimestamp();

	public byte[] getRawRequestData();

	public byte[] getRawResponseData();

	public String getContentAsXml();

	public String getProperty( String name );

	public void setProperty( String name, String value );

	public String[] getPropertyNames();
}
