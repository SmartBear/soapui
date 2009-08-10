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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.ConnectionWithSocket;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import org.apache.commons.httpclient.*;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;

/**
 * Extended PostMethod that supports limiting of response size and detailed
 * timestamps
 * 
 * @author Ole.Matzura
 */

public final class HttpMethodSupport
{
	private long timeTaken;
	private long startTime;
	private long maxSize;
	private long responseReadTime;

	private byte[] responseBody;

	private SSLInfo sslInfo;
	private String dumpFile;
	private final HttpMethodBase httpMethod;
	private Throwable failureCause;

	public HttpMethodSupport( HttpMethodBase httpMethod )
	{
		this.httpMethod = httpMethod;
	}

	public String getDumpFile()
	{
		return dumpFile;
	}

	public void setDumpFile( String dumpFile )
	{
		this.dumpFile = dumpFile;
	}

	public void afterReadResponse( HttpState arg0, HttpConnection arg1 )
	{
		if( arg1 instanceof ConnectionWithSocket )
		{
			Socket socket = ( ( ConnectionWithSocket )arg1 ).getConnectionSocket();
			if( socket instanceof SSLSocket )
			{
				sslInfo = new SSLInfo( ( SSLSocket )socket );
			}
		}
	}

	public long getMaxSize()
	{
		return maxSize;
	}

	public void setMaxSize( long maxSize )
	{
		this.maxSize = maxSize;
	}

	public void afterWriteRequest( HttpState arg0, HttpConnection arg1 )
	{
		if( startTime == 0 )
			startTime = System.nanoTime();
	}

	public void initStartTime()
	{
		startTime = System.nanoTime();
	}

	public long getTimeTaken()
	{
		if( timeTaken == 0 )
			timeTaken = ( System.nanoTime() - startTime ) / 1000000;

		return timeTaken;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public byte[] getResponseBody() throws IOException
	{
		if( responseBody != null )
			return responseBody;

		long contentLength = httpMethod.getResponseContentLength();
		long now = System.nanoTime();

		InputStream instream = httpMethod.getResponseBodyAsStream();

		if( maxSize == 0 || ( contentLength >= 0 && contentLength <= maxSize ) )
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if( instream != null )
				Tools.writeAll( out, instream );

			responseReadTime = System.nanoTime() - now;
			responseBody = out.toByteArray();

			try
			{
				if( StringUtils.hasContent( dumpFile ) )
				{
					Tools.writeAll( new FileOutputStream( dumpFile ), new ByteArrayInputStream( responseBody ) );
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			if( !SoapUI.getSettings().getBoolean( HttpSettings.DISABLE_RESPONSE_DECOMPRESSION ) )
			{
				String compressionAlg = HttpClientSupport.getResponseCompressionType( httpMethod );
				if( compressionAlg != null )
				{
					try
					{
						responseBody = CompressionSupport.decompress( compressionAlg, responseBody );
					}
					catch( Exception e )
					{
						IOException ioe = new IOException( "Decompression of response failed" );
						ioe.initCause( e );
						throw ioe;
					}
				}
			}
		}
		else
		{
			try
			{
				if( StringUtils.hasContent( dumpFile ) && instream != null )
				{
					FileOutputStream fileOutputStream = new FileOutputStream( dumpFile );
					Tools.writeAll( fileOutputStream, instream );
					responseReadTime = System.nanoTime() - now;
					fileOutputStream.close();
					instream = new FileInputStream( dumpFile );
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			ByteArrayOutputStream outstream = instream == null ? new ByteArrayOutputStream() : Tools.readAll( instream,
					maxSize );

			if( responseReadTime == 0 )
				responseReadTime = System.nanoTime() - now;

			responseBody = outstream.toByteArray();
		}

		// convert to ms
		responseReadTime /= 1000000;

		return responseBody;
	}

	public SSLInfo getSSLInfo()
	{
		return sslInfo;
	}

	public String getResponseContentType()
	{
		Header[] headers = httpMethod.getResponseHeaders( "Content-Type" );
		if( headers != null && headers.length > 0 )
		{
			return headers[0].getElements()[0].getName();
		}

		return null;
	}

	public long getResponseReadTime()
	{
		return responseReadTime;
	}

	/**
	 * Handles charset specified in Content-Encoding headers
	 * 
	 * @return
	 */

	public String getResponseCharset()
	{
		Header header = httpMethod.getResponseHeader( "Content-Type" );
		if( header != null )
		{
			for( HeaderElement headerElement : header.getElements() )
			{
				NameValuePair parameter = headerElement.getParameterByName( "charset" );
				if( parameter != null )
					return parameter.getValue();
			}
		}

		Header contentEncodingHeader = httpMethod.getResponseHeader( "Content-Encoding" );
		if( contentEncodingHeader != null )
		{
			try
			{
				String value = contentEncodingHeader.getValue();
				if( CompressionSupport.getAvailableAlgorithm( value ) == null )
				{
					new String( "" ).getBytes( value );
					return value;
				}
			}
			catch( Exception e )
			{
			}
		}

		return null;
	}

	public Throwable getFailureCause()
	{
		return failureCause;
	}

	public boolean isFailed()
	{
		return failureCause != null;
	}

	public void setFailed( Throwable t )
	{
		this.failureCause = t;
	}

	public boolean hasResponse()
	{
		return responseBody != null;
	}
}