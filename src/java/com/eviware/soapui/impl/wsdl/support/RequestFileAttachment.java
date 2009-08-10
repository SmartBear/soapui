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

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlAttachmentContainer;

/**
 * Attachment for a WsdlRequest
 * 
 * @author ole.matzura
 */

public class RequestFileAttachment extends FileAttachment<AbstractHttpRequest<?>>
{
	public RequestFileAttachment( AttachmentConfig config, AbstractHttpRequestInterface<?> request )
	{
		super( ( AbstractHttpRequest<?> )request, config );
	}

	public RequestFileAttachment( File file, boolean cache, AbstractHttpRequest<?> request ) throws IOException
	{
		super( request, file, cache, request.getConfig().addNewAttachment() );
	}

	public AttachmentEncoding getEncoding()
	{
		AbstractHttpRequestInterface<?> request = getModelItem();
		if( request instanceof WsdlAttachmentContainer && ( ( WsdlAttachmentContainer )request ).isEncodeAttachments() )
			return ( ( WsdlAttachmentContainer )request ).getAttachmentEncoding( getPart() );
		else
			return AttachmentEncoding.NONE;
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		if( getModelItem() == null || getPart() == null || getModelItem().getAttachmentPart( getPart() ) == null )
			return AttachmentType.UNKNOWN;
		else
			return getModelItem().getAttachmentPart( getPart() ).getAttachmentType();
	}
}
