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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

public abstract class AbstractMessageExchange<T extends ModelItem> implements MessageExchange
{
	protected StringToStringMap properties;
	private String[] messages;
	private T modelItem;

	public AbstractMessageExchange( T modelItem )
	{
		super();
		this.modelItem = modelItem;
	}

	public T getModelItem()
	{
		return modelItem;
	}

	public String getRequestContentAsXml()
	{
		if( hasRequest( true ) && XmlUtils.seemsToBeXml( getRequestContent() ) )
			return getRequestContent();
		else
			return "<not-xml/>";
	}

	public String getResponseContentAsXml()
	{
		if( hasResponse() && XmlUtils.seemsToBeXml( getResponseContent() ) )
			return getResponseContent();
		else
			return null;
	}

	public void addProperty( String name, String value )
	{
		if( properties == null )
			properties = new StringToStringMap();

		properties.put( name, value );
	}

	public String getProperty( String name )
	{
		return properties.get( name );
	}

	public StringToStringMap getProperties()
	{
		return properties;
	}

	public String[] getMessages()
	{
		return messages;
	}

	public void setMessages( String[] messages )
	{
		this.messages = messages;
	}

}