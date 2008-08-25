package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import org.apache.commons.httpclient.Header;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToStringMap;

public abstract class BaseHttpResponse implements HttpResponse
{
	private StringToStringMap requestHeaders;
	private StringToStringMap responseHeaders;
	
	private long timeTaken;
	private long timestamp;
	private String contentType;
	private int statusCode;
	private SSLInfo sslInfo;
	private URL url;
	private WeakReference<AbstractHttpRequest<?>> httpRequest;

	public BaseHttpResponse(ExtendedHttpMethod httpMethod, AbstractHttpRequest<?> httpRequest)
	{
		this.httpRequest = new WeakReference<AbstractHttpRequest<?>>(httpRequest);
		
		this.timeTaken = httpMethod.getTimeTaken();
		
		Settings settings = httpRequest.getSettings();
		if (settings.getBoolean(HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN))
		{
			try
			{
				httpMethod.getResponseBody();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			timeTaken += httpMethod.getResponseReadTime();
		}
		
		this.timestamp = System.currentTimeMillis();
		this.contentType = httpMethod.getResponseContentType();
		this.statusCode = httpMethod.getStatusCode();
		this.sslInfo = httpMethod.getSSLInfo();
		
		try
		{
			this.url = new URL( httpMethod.getURI().toString() );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		initHeaders(httpMethod);
	}
	
	protected void initHeaders(ExtendedHttpMethod postMethod)
	{
		requestHeaders = new StringToStringMap();
		Header[] headers = postMethod.getRequestHeaders();
		for( Header header : headers )
		{
			requestHeaders.put( header.getName(), header.getValue() );
		}
		
		responseHeaders = new StringToStringMap();
		headers = postMethod.getResponseHeaders();
		for( Header header : headers )
		{
			responseHeaders.put( header.getName(), header.getValue() );
		}
		
		responseHeaders.put( "#status#", postMethod.getStatusLine().toString() );
	}
	
	public StringToStringMap getRequestHeaders()
	{
		return requestHeaders;
	}

	public StringToStringMap getResponseHeaders()
	{
		return responseHeaders;
	}
	
	public long getTimeTaken()
	{
		return timeTaken;
	}

	public SSLInfo getSSLInfo()
	{
		return sslInfo;
	}

	public long getTimestamp()
	{
		return timestamp;
	}
	
	public String getContentType()
	{
		return contentType;
	}

	public URL getURL()
	{
		return url;
	}

	public AbstractHttpRequest<?> getRequest()
	{
		return httpRequest.get();
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public Attachment[] getAttachments()
	{
		return new Attachment[0];
	}

	public Attachment[] getAttachmentsForPart(String partName)
	{
		return new Attachment[0];
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return null;
	}
}
