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

package com.eviware.soapui.impl.wsdl.submit;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;

/**
 * MessageExchange for WSDL-based exchanges
 * 
 * @author ole.matzura
 */

public abstract class AbstractRestMessageExchange<T extends ModelItem> extends AbstractMessageExchange<T> implements
		RestMessageExchange
{
	public AbstractRestMessageExchange( T modelItem )
	{
		super( modelItem );
	}

	public boolean hasResponse()
	{
		String responseContent = getResponseContent();
		return responseContent != null && responseContent.trim().length() > 0;
	}

	public Attachment[] getResponseAttachmentsForPart( String name )
	{
		List<Attachment> result = new ArrayList<Attachment>();

		for( Attachment attachment : getResponseAttachments() )
		{
			if( attachment.getPart().equals( name ) )
				result.add( attachment );
		}

		return result.toArray( new Attachment[result.size()] );
	}

	public Attachment[] getRequestAttachmentsForPart( String name )
	{
		List<Attachment> result = new ArrayList<Attachment>();

		for( Attachment attachment : getRequestAttachments() )
		{
			if( attachment.getPart().equals( name ) )
				result.add( attachment );
		}

		return result.toArray( new Attachment[result.size()] );
	}

	public boolean hasRequest( boolean ignoreEmpty )
	{
		String requestContent = getRequestContent();
		return !( requestContent == null || ( ignoreEmpty && requestContent.trim().length() == 0 ) );
	}

	public boolean hasRawData()
	{
		return false;
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return null;
	}
}
