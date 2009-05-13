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

package com.eviware.soapui.model.iface;

import java.io.InputStream;

/**
 * Attachment for Requests and their responses
 * 
 * @author Ole.Matzura
 */

public interface Attachment
{
	public String getName();

	public String getContentType();

	public void setContentType( String contentType );

	public long getSize();

	public String getPart();

	public void setPart( String part );

	public InputStream getInputStream() throws Exception;

	public String getUrl();

	public boolean isCached();

	public AttachmentType getAttachmentType();

	public enum AttachmentType
	{
		MIME, XOP, CONTENT, SWAREF, UNKNOWN
	}

	public String getContentID();

	public enum AttachmentEncoding
	{
		BASE64, HEX, NONE
	}

	public AttachmentEncoding getEncoding();

	public String getContentEncoding();
}
