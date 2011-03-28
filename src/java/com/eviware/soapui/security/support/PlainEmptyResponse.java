package com.eviware.soapui.security.support;

import java.net.URL;
import java.util.Vector;

import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringsMap;

public class PlainEmptyResponse implements WsdlResponse,HttpResponse 
{

	private TestRequest request;
	private String content = "";
	private String contentAsXml = "";
	private String contentType;

	public PlainEmptyResponse( TestRequest testRequest, String response )
	{
		this.request = testRequest;
		this.content = this.contentAsXml = response;
	}

	public WsdlRequest getRequest()
	{
		return ( WsdlRequest )request;
	}

	public String getRequestContent()
	{
		return request.getRequestContent();
	}

	public void setContentType( String contentType )
	{
		this.contentType = contentType;
	}

	public SSLInfo getSSLInfo()
	{
		return null;
	}

	public void setResponseContent( String content )
	{
		this.content = content;
	}

	public Attachment[] getAttachments()
	{
		return null;
	}

	public Attachment[] getAttachmentsForPart( String arg0 )
	{
		return null;
	}

	public String getContentAsString()
	{
		return content;
	}

	public long getContentLength()
	{
		return content.length();
	}

	public StringToStringsMap getRequestHeaders()
	{
		return new StringToStringsMap();
	}

	public StringToStringsMap getResponseHeaders()
	{
		return new StringToStringsMap();
	}

	public long getTimeTaken()
	{
		return 0;
	}

	public long getTimestamp()
	{
		return 0;
	}

	@SuppressWarnings( "unchecked" )
	public Vector getWssResult()
	{
		return new Vector();
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return content.getBytes();
	}

	public String getProperty( String name )
	{
		return null;
	}

	public void setProperty( String name, String value )
	{
	}

	public String[] getPropertyNames()
	{
		return new String[0]; 
	}

	public String getContentType()
	{
		return contentType;
	}

	public URL getURL()
	{
		return null;
	}

	public RequestMethod getMethod()
	{
		return null; 
	}

	public String getHttpVersion()
	{
		return null; 
	}

	public int getStatusCode()
	{
		return 200;
	}

	public String getContentAsXml()
	{
		return contentAsXml;
	}

	public void setContentAsXml( String contentAsXml )
	{
		this.contentAsXml = contentAsXml;
	}
}
