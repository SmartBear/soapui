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

package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import hermes.Domain;
import hermes.Hermes;

import javax.jms.Message;
import javax.naming.NamingException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * @author nebojsa.tasic
 * 
 */
public class JMSHeader
{
	public static final String JMSCORRELATIONID = "JMSCorrelationID";
	public static final String JMSREPLYTO = "JMSReplyTo";
	public static final String TIMETOLIVE = "TimeToLive";
	public static final String JMSTYPE = "JMSType";
	public static final String JMSPRIORITY = "JMSPriority";
	public static final String DELIVERY_MODE = "DeliveryMode";
	
	private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;
	private long timeTolive = Message.DEFAULT_TIME_TO_LIVE;
	

	


	public  void setMessageHeaders(Message message, Request request, Hermes hermes)
	{
		AbstractHttpRequest temp = (AbstractHttpRequest) request;
		StringToStringMap headersMap = temp.getRequestHeaders();
		try
		{
			//JMSCORRELATIONID
			if (headersMap.containsKey(JMSCORRELATIONID))
			{
				message.setJMSCorrelationID(PropertyExpander.expandProperties(headersMap.get(JMSCORRELATIONID, "")));
			}

			//JMSREPLYTO
			if (headersMap.containsKey(JMSREPLYTO))
			{
				message.setJMSReplyTo(hermes.getDestination(PropertyExpander.expandProperties(headersMap
						.get(JMSREPLYTO, "")), Domain.QUEUE));
			}

			//TIMETOLIVE
			if (headersMap.containsKey(TIMETOLIVE))
			{
				setTimeTolive(Long.parseLong(PropertyExpander.expandProperties(headersMap.get(TIMETOLIVE, "0"))));
			}
			else
			{
				setTimeTolive(Message.DEFAULT_TIME_TO_LIVE);
			}

			//JMSTYPE
			if (headersMap.containsKey(JMSTYPE))
			{
				message.setJMSType(PropertyExpander.expandProperties(headersMap.get(JMSTYPE, "")));
			}

			//JMSPRIORITY
			if (headersMap.containsKey(JMSPRIORITY))
			{
				message
						.setJMSPriority(Integer.parseInt(PropertyExpander.expandProperties(headersMap.get(JMSPRIORITY, ""))));
			}
			else
			{
				message.setJMSPriority(Message.DEFAULT_PRIORITY);
			}
			
			//DELIVERY_MODE
			if (headersMap.containsKey(DELIVERY_MODE))
			{
				setDeliveryMode(Integer.parseInt(PropertyExpander.expandProperties(headersMap.get(DELIVERY_MODE, "0"))));
			}
			else
			{
				setDeliveryMode(Message.DEFAULT_DELIVERY_MODE);
			}
			
			//CUSTOM PROPERTIES
			String keys[]=headersMap.getKeys();
			for(String key:keys){
				if(!key.equals(JMSCORRELATIONID) && !key.equals(JMSREPLYTO) && !key.equals(TIMETOLIVE) && !key.equals(JMSTYPE) && !key.equals(JMSPRIORITY) && !key.equals(DELIVERY_MODE) ){
					message.setStringProperty(key,PropertyExpander.expandProperties(headersMap.get(key)));
				}
			}
		}
		catch (NamingException e)
		{
			SoapUI.logError(e, "Message header JMSReplyTo = "
					+ PropertyExpander.expandProperties(headersMap.get(JMSREPLYTO)) + "destination not exists!");
		}
		catch (Exception e)
		{
			SoapUI.logError(e, "error while seting message header properties!");
		}

	}
	
	public int getDeliveryMode()
	{
		return deliveryMode;
	}


	public void setDeliveryMode(int deliveryMode)
	{
		this.deliveryMode = deliveryMode;
	}


	public long getTimeTolive()
	{
		return timeTolive;
	}


	public void setTimeTolive(long timeTolive)
	{
		this.timeTolive = timeTolive;
	}
}
