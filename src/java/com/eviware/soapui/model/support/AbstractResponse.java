package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public abstract class AbstractResponse<T extends Request> implements Response
{
	private StringToStringMap properties = new StringToStringMap();
	private final T request;

	public AbstractResponse( T request )
	{
		this.request = request;
	}

	public Attachment[] getAttachments()
	{
		return null;
	}

	public Attachment[] getAttachmentsForPart( String partName )
	{
		return null;
	}

	public long getContentLength()
	{
		return getContentAsString().length();
	}

	public String getProperty( String name )
	{
		return properties.get( name );
	}

	public String[] getPropertyNames()
	{
		return properties.getKeys();
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return null;
	}

	public T getRequest()
	{
		return request;
	}

	public String getContentAsXml()
	{
		return getContentAsString();
	}

	public StringToStringsMap getRequestHeaders()
	{
		return null;
	}

	public StringToStringsMap getResponseHeaders()
	{
		return null;
	}

	public void setProperty( String name, String value )
	{
		properties.put( name, value );
	}
}
