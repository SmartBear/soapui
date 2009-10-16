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
package com.eviware.soapui.support.editor.inspectors.jms;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request;

public class JMSUtil
{
	
	public static final String JMS_ENDPIONT_PREFIX="jms://";
	
	private static boolean checkIfJMS(Request request)
	{
		try
		{
			return request.getEndpoint().startsWith(JMS_ENDPIONT_PREFIX);
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
			String r = ((MessageExchangeModelItem) messageExchange).getMessageExchange().getProperty("Endpoint");
			return r.startsWith(JMS_ENDPIONT_PREFIX);
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
}
