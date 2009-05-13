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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;

/**
 * Attachment for a WsdlMockResponse
 * 
 * @author Ole.Matzura
 */

public class MockFileAttachment extends FileAttachment<WsdlMockResponse>
{
	public MockFileAttachment( AttachmentConfig config, WsdlMockResponse mockResponse )
	{
		super( mockResponse, config );
	}

	public MockFileAttachment( File file, boolean cache, WsdlMockResponse response ) throws IOException
	{
		super( response, file, cache, response.getConfig().addNewAttachment() );
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		if( getPart() == null || getModelItem().getAttachmentPart( getPart() ) == null )
			return AttachmentType.UNKNOWN;
		else
			return getModelItem().getAttachmentPart( getPart() ).getAttachmentType();
	}

	public AttachmentEncoding getEncoding()
	{
		if( getModelItem().isEncodeAttachments() )
		{
			return getModelItem().getAttachmentEncoding( getPart() );
		}
		else
		{
			return AttachmentEncoding.NONE;
		}
	}
}
