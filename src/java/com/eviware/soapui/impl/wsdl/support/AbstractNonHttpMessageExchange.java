package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.submit.AbstractMessageExchange;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringMap;

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

	public StringToStringMap getResponseHeaders()
	{
		return null;
	}

	public StringToStringMap getRequestHeaders()
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
