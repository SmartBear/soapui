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

package com.eviware.soapui.impl.wsdl;

import java.io.File;
import java.io.IOException;

import com.eviware.soapui.model.iface.Attachment;

/**
 * Behaviour for ModelItems that contain attachments (Requests and
 * MockResponses)
 * 
 * @author ole.matzura
 */

public interface MutableAttachmentContainer extends AttachmentContainer
{
	public Attachment attachFile( File file, boolean cache ) throws IOException;

	public void removeAttachment( Attachment attachment );
}