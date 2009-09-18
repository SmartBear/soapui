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

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;

import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.support.types.StringToStringMap;

/*
 * IMPORTANT TODO:this class is NOT finished yet , and it contains lots of dummy data
 * 
 */
public class JMSResponse implements WsdlResponse
{

	String payload;
	Message message;
	Request request;
	long requestStartedTime;

	public JMSResponse(String payload, Message message, Request request, long requestStartedTime)
	{
		this.payload = payload;
		this.message = message;
		this.request = request;
		this.requestStartedTime = requestStartedTime;
	}

	public Attachment[] getAttachments()
	{
		return new Attachment[0];
	}

	public Attachment[] getAttachmentsForPart(String partName)
	{
		return new Attachment[0];
	}

	public String getContentAsString()
	{
		return payload;
	}

	public long getContentLength()
	{
		return payload.length();
	}

	public String getContentType()
	{
		try
		{
			return message.getJMSType();
		}
		catch (JMSException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getProperty(String name)
	{
		// TODO Auto-generated method stub
		try
		{
			return message.getStringProperty(name);
		}
		catch (JMSException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String[] getPropertyNames()
	{
		List<String> propertyNames = new ArrayList<String>();
		Enumeration temp;
		try
		{
			if (message != null)
			{
				temp = message.getPropertyNames();
				while (temp.hasMoreElements())
				{
					propertyNames.add((String) temp.nextElement());
				}
				return propertyNames.toArray(new String[propertyNames.size()]);
			}
			else
			{
				return new String[0];
			}
		}
		catch (JMSException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getRawRequestData()
	{
		return message.toString().getBytes();
	}

	public byte[] getRawResponseData()
	{
		if (message != null)
			return message.toString().getBytes();
		else
			return "".getBytes();
	}

	public String getRequestContent()
	{
		return payload;
	}

	public StringToStringMap getRequestHeaders()
	{
		AbstractHttpRequest temp = (AbstractHttpRequest) request;
		return temp.getRequestHeaders();
	}

	public StringToStringMap getResponseHeaders()
	{
		if (message != null)
		{
			return JMSHeader.getReceivedMessageHeaders(message);
		}
		else
			return new StringToStringMap();
	}

	public long getTimeTaken()
	{
		return Calendar.getInstance().getTimeInMillis() - requestStartedTime;
	}

	public long getTimestamp()
	{
		try
		{
			return message.getJMSTimestamp();
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public void setProperty(String name, String value)
	{
		try
		{
			message.setStringProperty(name, value);
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}

	}

	public String getContentAsXml()
	{
		return payload;
	}

	public String getHttpVersion()
	{
		return null;
	}

	public RequestMethod getMethod()
	{
		return null;
	}

	public SSLInfo getSSLInfo()
	{
		return null;
	}

	public int getStatusCode()
	{
		return 0;
	}

	public URL getURL()
	{
		return null;
	}

	public void setResponseContent(String responseContent)
	{
	}

	public Vector<?> getWssResult()
	{
		return null;
	}

	public WsdlRequest getRequest()
	{
		return (WsdlRequest) request;
	}

}
