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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.io.IOException;
import java.net.URI;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;

public interface ExtendedHttpMethod extends HttpRequest, HttpUriRequest
{
	public long getMaxSize();

	public void setMaxSize( long maxSize );

	public long getResponseReadTime();

	public long getResponseReadTimeNanos();

	public void initStartTime();

	public long getTimeTaken();

	public long getStartTime();

	public SSLInfo getSSLInfo();

	public String getResponseCharSet();

	public String getResponseContentType();

	public String getMethod();

	public void setDumpFile( String dumpFile );

	public void setFailed( Throwable t );

	public boolean isFailed();

	public Throwable getFailureCause();

	public boolean hasResponse();

	public byte[] getDecompressedResponseBody() throws IOException;

	public void setDecompress( boolean decompress );

	public void setURI( URI uri );

	public void setHttpResponse( org.apache.http.HttpResponse httpResponse );

	public org.apache.http.HttpResponse getHttpResponse();

	public boolean hasHttpResponse();

	public byte[] getResponseBody() throws IOException;

	public String getResponseBodyAsString() throws IOException;

	public HttpEntity getRequestEntity();

	public void afterReadResponse( SSLSession session );

	public void afterWriteRequest();

	public SoapUIMetrics getMetrics();
}
