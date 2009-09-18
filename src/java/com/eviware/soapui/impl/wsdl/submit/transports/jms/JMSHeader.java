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

import java.util.Enumeration;

import javax.jms.JMSException;
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
	public static final String JMSDELIVERYMODE = "JMSDeliveryMode";
	public static final String JMSEXPIRATION = "JMSExpiration";
	public static final String JMSMESSAGEID = "JMSMessageID";
	public static final String JMSTIMESTAMP = "JMSTimestamp";
	public static final String JMSREDELIVERED = "JMSRedelivered";
	public static final String JMSDESTINATION = "JMSDestination";

	private long timeTolive = Message.DEFAULT_TIME_TO_LIVE;

	public void setMessageHeaders(Message message, Request request, Hermes hermes)
	{
		AbstractHttpRequest temp = (AbstractHttpRequest) request;
		StringToStringMap headersMap = temp.getRequestHeaders();
		try
		{
			// JMSCORRELATIONID
			if (headersMap.containsKey(JMSCORRELATIONID))
			{
				message.setJMSCorrelationID(PropertyExpander.expandProperties(headersMap.get(JMSCORRELATIONID, "")));
			}

			// JMSREPLYTO
			if (headersMap.containsKey(JMSREPLYTO))
			{
				message.setJMSReplyTo(hermes.getDestination(PropertyExpander.expandProperties(headersMap
						.get(JMSREPLYTO, "")), Domain.QUEUE));
			}

			// TIMETOLIVE
			if (headersMap.containsKey(TIMETOLIVE))
			{
				setTimeTolive(Long.parseLong(PropertyExpander.expandProperties(headersMap.get(TIMETOLIVE, "0"))));
			}
			else
			{
				setTimeTolive(Message.DEFAULT_TIME_TO_LIVE);
			}

			// JMSTYPE
			if (headersMap.containsKey(JMSTYPE))
			{
				message.setJMSType(PropertyExpander.expandProperties(headersMap.get(JMSTYPE, "")));
			}

			// JMSPRIORITY
			if (headersMap.containsKey(JMSPRIORITY))
			{
				message.setJMSPriority(Integer.parseInt(PropertyExpander.expandProperties(headersMap.get(JMSPRIORITY,
						String.valueOf(Message.DEFAULT_PRIORITY)))));
			}
			else
			{
				message.setJMSPriority(Message.DEFAULT_PRIORITY);
			}

			// JMSDELIVERYMODE
			if (headersMap.containsKey(JMSDELIVERYMODE))
			{
				message.setJMSDeliveryMode(Integer.parseInt(PropertyExpander.expandProperties(headersMap.get(
						JMSDELIVERYMODE, String.valueOf(Message.DEFAULT_DELIVERY_MODE)))));
			}
			else
			{
				message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);
			}

			// CUSTOM PROPERTIES
			String keys[] = headersMap.getKeys();
			for (String key : keys)
			{
				if (!key.equals(JMSCORRELATIONID) && !key.equals(JMSREPLYTO) && !key.equals(TIMETOLIVE)
						&& !key.equals(JMSTYPE) && !key.equals(JMSPRIORITY) && !key.equals(JMSDELIVERYMODE))
				{
					message.setStringProperty(key, PropertyExpander.expandProperties(headersMap.get(key)));
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

	public long getTimeTolive()
	{
		return timeTolive;
	}

	public void setTimeTolive(long timeTolive)
	{
		this.timeTolive = timeTolive;
	}

	public static StringToStringMap getReceivedMessageHeaders(Message message)
	{
		StringToStringMap headermap = new StringToStringMap();
		try
		{
			headermap.put(JMSDESTINATION, String.valueOf(message.getJMSDestination()));
			headermap.put(JMSDELIVERYMODE, String.valueOf(message.getJMSDeliveryMode()));
			headermap.put(JMSEXPIRATION, String.valueOf(message.getJMSExpiration()));
			headermap.put(JMSMESSAGEID, String.valueOf(message.getJMSMessageID()));
			headermap.put(JMSPRIORITY, String.valueOf(message.getJMSPriority()));
			headermap.put(JMSTIMESTAMP, String.valueOf(message.getJMSTimestamp()));
			headermap.put(JMSCORRELATIONID, String.valueOf(message.getJMSCorrelationID()));
			headermap.put(JMSREPLYTO, String.valueOf(message.getJMSReplyTo()));
			headermap.put(JMSTYPE, String.valueOf(message.getJMSType()));
			headermap.put(JMSREDELIVERED, String.valueOf(message.getJMSRedelivered()));

			Enumeration properties = message.getPropertyNames();
			while (properties.hasMoreElements())
			{
				String key = (String) properties.nextElement();
				headermap.put(key, message.getStringProperty(key));
			}

		}
		catch (JMSException e)
		{
			SoapUI.logError(e);
		}
		return headermap;
	}
}
