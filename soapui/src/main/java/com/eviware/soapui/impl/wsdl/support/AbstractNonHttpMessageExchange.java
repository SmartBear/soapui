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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.submit.AbstractMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringsMap;

public abstract class AbstractNonHttpMessageExchange<T extends ModelItem> extends AbstractMessageExchange<T>
{

	public AbstractNonHttpMessageExchange( T modelItem )
	{
		super( modelItem );
	}

	public Operation getOperation()
	{
		return null;
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return null;
	}

	public Attachment[] getRequestAttachments()
	{
		return null;
	}

	public Attachment[] getRequestAttachmentsForPart( String partName )
	{
		return null;
	}

	public StringToStringsMap getResponseHeaders()
	{
		return new StringToStringsMap();
	}

	public StringToStringsMap getRequestHeaders()
	{
		return null;
	}

	public Attachment[] getResponseAttachments()
	{
		return null;
	}

	public Attachment[] getResponseAttachmentsForPart( String partName )
	{
		return null;
	}

	public boolean hasRawData()
	{
		return false;
	}
}
