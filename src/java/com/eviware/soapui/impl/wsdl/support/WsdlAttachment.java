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

package com.eviware.soapui.impl.wsdl.support;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.model.iface.Attachment;

/**
 * WSDL-specific Attachment behaviour
 * 
 * @author ole.matzura
 */

public interface WsdlAttachment extends Attachment
{
	public void updateConfig( AttachmentConfig config );

	public XmlObject getConfig();

	public void setContentID( String contentID );

	public void reload( File file, boolean cache ) throws IOException;

	public void setName( String value );

	public void setUrl( String string );
}