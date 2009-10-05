package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;

public abstract class AbstractResponse<T extends Request> implements Response
{
	private StringToStringMap properties = new StringToStringMap();
	private final T request;
	
	public AbstractResponse( T request )
	{
		this.request = request;
	}
	
	@Override
	public Attachment[] getAttachments()
	{
		return null;
	}

	@Override
	public Attachment[] getAttachmentsForPart( String partName )
	{
		return null;
	}

	@Override
	public long getContentLength()
	{
		return getContentAsString().length();
	}

	@Override
	public String getProperty( String name )
	{
		return properties.get( name );
	}

	@Override
	public String[] getPropertyNames()
	{
		return properties.getKeys();
	}

	@Override
	public byte[] getRawRequestData()
	{
		return null;
	}

	@Override
	public byte[] getRawResponseData()
	{
		return null;
	}

	@Override
	public T getRequest()
	{
		return request;
	}

	@Override
	public StringToStringMap getRequestHeaders()
	{
		return null;
	}

	@Override
	public StringToStringMap getResponseHeaders()
	{
		return null;
	}

	@Override
	public void setProperty( String name, String value )
	{
		properties.put( name, value );
	}
}
