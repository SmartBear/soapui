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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;

public class MessageExchangeModelItem extends EmptyModelItem
{
	public final static String MESSAGE_EXCHANGE = "messageExchange";
	private MessageExchange messageExchange;

	public MessageExchangeModelItem( String title, MessageExchange messageExchange )
	{
		super( title, null );
		this.messageExchange = messageExchange;
	}

	public MessageExchange getMessageExchange()
	{
		return messageExchange;
	}

	public void setMessageExchange( MessageExchange messageExchange )
	{
		MessageExchange oldExchange = this.messageExchange;
		this.messageExchange = messageExchange;

		propertyChangeSupport.firePropertyChange( MESSAGE_EXCHANGE, oldExchange, messageExchange );
	}

	public boolean hasRawData()
	{
		return messageExchange == null ? false : messageExchange.hasRawData();
	}

	@Override
	public ModelItem getParent()
	{
		return messageExchange == null ? null : messageExchange.getModelItem();
	}

}
