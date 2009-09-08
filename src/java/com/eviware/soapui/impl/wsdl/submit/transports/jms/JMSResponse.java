package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
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

	private final static Logger log = Logger.getLogger(JMSResponse.class);

	String payload;
	Message message;
	Request request;

	public JMSResponse(String payload, Message message, Request request)
	{
		this.payload = payload;
		this.message = message;
		this.request = request;
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
			temp = message.getPropertyNames();
			while (temp.hasMoreElements())
			{
				propertyNames.add((String) temp.nextElement());
			}
			return propertyNames.toArray(new String[propertyNames.size()]);
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

		return payload.getBytes();
	}

	public byte[] getRawResponseData()
	{

		return payload.getBytes();
	}

	// 
	// public Request getRequest()
	// {
	// return request;
	// }

	public String getRequestContent()
	{
		return payload;
	}

	public StringToStringMap getRequestHeaders()
	{

		return new StringToStringMap();
	}

	public StringToStringMap getResponseHeaders()
	{
		return new  StringToStringMap();
	}

	public long getTimeTaken()
	{
		return 0;
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
		// TODO Auto-generated method stub
		return null;
	}

	public RequestMethod getMethod()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public SSLInfo getSSLInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getStatusCode()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public URL getURL()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setResponseContent(String responseContent)
	{
		// TODO Auto-generated method stub

	}

	public Vector<?> getWssResult()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public WsdlRequest getRequest()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
