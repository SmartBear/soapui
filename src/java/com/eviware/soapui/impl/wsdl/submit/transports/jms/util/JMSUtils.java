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
package com.eviware.soapui.impl.wsdl.submit.transports.jms.util;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSEndpoint;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Request;

public class JMSUtils
{

	

	private static boolean checkIfJMS(Request request)
	{
		try
		{
			return request.getEndpoint().startsWith(JMSEndpoint.JMS_ENDPIONT_PREFIX);
		}
		catch (NullPointerException e)
		{
			SoapUI.logError(e);
		}
		return false;
	}

	private static boolean checkIfJMS(MessageExchangeModelItem messageExchange)
	{
		try
		{
			MessageExchange me = ((MessageExchangeModelItem) messageExchange).getMessageExchange();
			if (me != null)
			{
				String r = me.getProperty("Endpoint");
				return r != null && r.startsWith(JMSEndpoint.JMS_ENDPIONT_PREFIX);
			}
			else
			{
				return false;
			}
		}
		catch (NullPointerException e)
		{
			SoapUI.logError(e);
		}
		return false;
	}

	public static boolean checkIfJMS(ModelItem modelItem)
	{
		if (modelItem instanceof Request)
		{
			return checkIfJMS((Request) modelItem);
		}
		else
		{
			if (modelItem instanceof MessageExchangeModelItem)
			{
				return checkIfJMS((MessageExchangeModelItem) modelItem);
			}
		}
		return false;
	}

	public static String extractMapMessagePayloadToString(MapMessage mapMessage) throws JMSException
	{
		StringBuffer sb = new StringBuffer();

		Enumeration<?> mapNames = mapMessage.getMapNames();

		while (mapNames.hasMoreElements())
		{
			String key = (String) mapNames.nextElement();
			String value = mapMessage.getString(key);
			sb.append(key + ": " + value);
		}

		return sb.toString();
	}
}
