package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import java.net.URL;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;

/*
 * IMPORTANT TODO:this class is NOT finished yet
 * 
 */
public class JMSResponse implements Response, HttpResponse, WsdlResponse
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

	@Override
	public Attachment[] getAttachments()
	{

		return null;
	}

	@Override
	public Attachment[] getAttachmentsForPart(String partName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getContentAsString()
	{
		return payload;
	}

	public long getContentLength()
	{
		return payload.length();
	}

	@Override
	public String getContentType()
	{
		return null;
	}

	@Override
	public String getProperty(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRawRequestData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRawResponseData()
	{

		return payload.getBytes();
	}

	// @Override
	// public Request getRequest()
	// {
	// return request;
	// }

	@Override
	public String getRequestContent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringToStringMap getRequestHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringToStringMap getResponseHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeTaken()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTimestamp()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setProperty(String name, String value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getContentAsXml()
	{

		return payload;
	}

	@Override
	public String getHttpVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestMethod getMethod()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SSLInfo getSSLInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatusCode()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public URL getURL()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResponseContent(String responseContent)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Vector<?> getWssResult()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WsdlRequest getRequest()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
