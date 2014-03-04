package com.eviware.soapui.impl.support;

import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MockRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MultipartMessageSupport;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

public abstract class AbstractMockRequest implements MockRequest
{
	private StringToStringsMap requestHeaders;
	private String requestContent;
	private MultipartMessageSupport multipartMessageSupport;
	private final HttpServletResponse response;
	private String protocol;
	private String path;
	private final WsdlMockRunContext context;
	private final WsdlMockRunContext requestContext;
	private final HttpServletRequest request;
	private MockRequestDataSource mockRequestDataSource;
	private String actualRequestContent;
	private boolean responseMessage;
	private XmlObject requestXmlObject;

	public AbstractMockRequest( HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context )
	{
		this.request = request;
		this.response = response;
		this.context = context;

		requestContext = new WsdlMockRunContext( context.getMockService(), null );

		requestHeaders = new StringToStringsMap();
		for( Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements(); )
		{
			String header = ( String )e.nextElement();
			String lcHeader = header.toLowerCase();
			if( lcHeader.equals( "soapaction" ) )
				requestHeaders.put( "SOAPAction", request.getHeader( header ) );
			else if( lcHeader.equals( "content-type" ) )
				requestHeaders.put( "Content-Type", request.getHeader( header ) );
			else if( lcHeader.equals( "content-length" ) )
				requestHeaders.put( "Content-Length", request.getHeader( header ) );
			else if( lcHeader.equals( "content-encoding" ) )
				requestHeaders.put( "Content-Encoding", request.getHeader( header ) );
			else
				requestHeaders.put( header, request.getHeader( header ) );
		}

		protocol = request.getProtocol();
		path = request.getPathInfo();
		if( path == null )
			path = "";
	}

	public String getProtocol()
	{
		return protocol;
	}


	public Attachment[] getRequestAttachments()
	{
		return multipartMessageSupport == null ? new Attachment[0] : multipartMessageSupport.getAttachments();
	}

	public String getRequestContent()
	{
		return multipartMessageSupport == null ? requestContent : multipartMessageSupport.getContentAsString();
	}

	public StringToStringsMap getRequestHeaders()
	{
		return requestHeaders;
	}


	public HttpServletResponse getHttpResponse()
	{
		return response;
	}

	public HttpServletRequest getHttpRequest()
	{
		return request;
	}

	public HttpMethod getMethod()
	{
		return HttpMethod.valueOf( request.getMethod() );
	}

	public String getPath()
	{
		return path;
	}

	public WsdlMockRunContext getContext()
	{
		return context;
	}

	public void setOperation( WsdlOperation operation )
	{
		if( multipartMessageSupport != null )
			multipartMessageSupport.setOperation( operation );
	}

	public WsdlMockRunContext getRequestContext()
	{
		return requestContext;
	}

	public byte[] getRawRequestData()
	{
		return mockRequestDataSource == null ? actualRequestContent == null ? requestContent.getBytes()
				: actualRequestContent.getBytes() : mockRequestDataSource.getData();
	}

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public void setActualRequestContent( String actualRequestContent )
	{
		this.actualRequestContent = actualRequestContent;
	}

	public void setMultipartMessageSupport( MultipartMessageSupport multipartMessageSupport )
	{
		this.multipartMessageSupport = multipartMessageSupport;
	}

	public MultipartMessageSupport getMultipartMessageSupport()
	{
		return multipartMessageSupport;
	}

	public void setRequestContent( String requestContent )
	{
		this.requestContent = requestContent;
	}

	public void setMockRequestDataSource( MockRequestDataSource mockRequestDataSource )
	{
		this.mockRequestDataSource = mockRequestDataSource;
	}

	public void setResponseMessage( boolean responseMessage )
	{
		this.responseMessage = responseMessage;
	}

	public boolean isResponseMessage()
	{
		return responseMessage;
	}

	public void setRequestXmlObject( XmlObject requestXmlObject )
	{
		this.requestXmlObject = requestXmlObject;
	}

	public XmlObject getRequestXmlObject() throws XmlException
	{
		if( requestXmlObject == null && StringUtils.hasContent( getRequestContent() ) )
			// requestXmlObject = XmlObject.Factory.parse( getRequestContent() );
			requestXmlObject = XmlUtils.createXmlObject( getRequestContent(), XmlUtils.createDefaultXmlOptions() );

		return requestXmlObject;
	}
}
