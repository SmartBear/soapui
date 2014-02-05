/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedEntityEnclosingHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpMethodSupport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import javax.net.ssl.SSLSession;
import java.io.IOException;

/**
 * Extended PostMethod that supports limiting of response size and detailed
 * timestamps
 *
 * @author Ole.Matzura
 */

public final class ExtendedGenericMethod extends HttpPost implements ExtendedEntityEnclosingHttpMethod
{
	private HttpMethodSupport httpMethodSupport;
	private IAfterRequestInjection afterRequestInjection;
	private String method;

	public ExtendedGenericMethod( String method )
	{
		this.method = method;
		httpMethodSupport = new HttpMethodSupport();
	}

	public String getDumpFile()
	{
		return httpMethodSupport.getDumpFile();
	}

	public void setDumpFile( String dumpFile )
	{
		httpMethodSupport.setDumpFile( dumpFile );
	}

	public boolean hasResponse()
	{
		return httpMethodSupport.hasResponse();
	}

	public void afterReadResponse( SSLSession session )
	{
		httpMethodSupport.afterReadResponse( session );
	}

	@Override
	public String getResponseCharSet()
	{
		return httpMethodSupport.getResponseCharset();
	}

	public HttpEntity getRequestEntity()
	{
		return super.getEntity();
	}

	public long getMaxSize()
	{
		return httpMethodSupport.getMaxSize();
	}

	public void setMaxSize( long maxSize )
	{
		httpMethodSupport.setMaxSize( maxSize );
	}

	public long getResponseReadTime()
	{
		return httpMethodSupport.getResponseReadTime();
	}

	public long getResponseReadTimeNanos()
	{
		return httpMethodSupport.getResponseReadTimeNanos();
	}

	public void afterWriteRequest()
	{
		httpMethodSupport.afterWriteRequest();
		if( afterRequestInjection != null )
			afterRequestInjection.executeAfterRequest();
	}

	public void initStartTime()
	{
		httpMethodSupport.initStartTime();
	}

	public long getTimeTaken()
	{
		return httpMethodSupport.getTimeTaken();
	}

	public long getStartTime()
	{
		return httpMethodSupport.getStartTime();
	}

	public byte[] getResponseBody() throws IOException
	{
		return httpMethodSupport.getResponseBody();
	}

	public SSLInfo getSSLInfo()
	{
		return httpMethodSupport.getSSLInfo();
	}

	public String getResponseContentType()
	{
		return httpMethodSupport.getResponseContentType();
	}

	public String getMethod()
	{
		return method;
	}

	public void setAfterRequestInjection( IAfterRequestInjection injection )
	{
		afterRequestInjection = injection;
	}

	public Throwable getFailureCause()
	{
		return httpMethodSupport.getFailureCause();
	}

	public boolean isFailed()
	{
		return httpMethodSupport.isFailed();
	}

	public void setFailed( Throwable t )
	{
		httpMethodSupport.setFailed( t );
	}

	public byte[] getDecompressedResponseBody() throws IOException
	{
		return httpMethodSupport.getDecompressedResponseBody();
	}

	public void setDecompress( boolean decompress )
	{
		httpMethodSupport.setDecompress( decompress );
	}

	public void setHttpResponse( HttpResponse httpResponse )
	{
		httpMethodSupport.setHttpResponse( httpResponse );
	}

	public HttpResponse getHttpResponse()
	{
		return httpMethodSupport.getHttpResponse();
	}

	public boolean hasHttpResponse()
	{
		return httpMethodSupport.hasHttpResponse();
	}

	public String getResponseBodyAsString() throws IOException
	{
		byte[] rawdata = getResponseBody();
		if( rawdata != null )
		{
			return EncodingUtil.getString( rawdata, getResponseCharSet() );
		}
		else
		{
			return null;
		}
	}

	public SoapUIMetrics getMetrics()
	{
		return httpMethodSupport.getMetrics();
	}
}
