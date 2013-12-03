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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.util.EntityUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;

/**
 * Extended PostMethod that supports limiting of response size and detailed
 * timestamps
 *
 * @author Ole.Matzura
 */

public class HttpMethodSupport
{
	private long timeTaken;
	private long startTime;
	private long maxSize;
	private long responseReadTime;

	private byte[] responseBody;

	private SSLInfo sslInfo;
	private String dumpFile;
	private Throwable failureCause;
	private boolean decompress;
	private org.apache.http.HttpResponse httpResponse;

	private SoapUIMetrics metrics;

	public HttpMethodSupport()
	{
		decompress = !SoapUI.getSettings().getBoolean( HttpSettings.DISABLE_RESPONSE_DECOMPRESSION );
		metrics = new SoapUIMetrics( new HttpTransportMetricsImpl(), new HttpTransportMetricsImpl() );
	}

	public boolean isDecompress()
	{
		return decompress;
	}

	public void setDecompress( boolean decompress )
	{
		this.decompress = decompress;
	}

	public String getDumpFile()
	{
		return dumpFile;
	}

	public void setDumpFile( String dumpFile )
	{
		this.dumpFile = dumpFile;
	}

	public void afterReadResponse( SSLSession session )
	{
		if( session != null )
		{
			sslInfo = new SSLInfo( session );
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

	public void afterWriteRequest()
	{
		if( startTime == 0 )
		{
			startTime = System.nanoTime();
		}
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

	public SSLInfo getSSLInfo()
	{
		return sslInfo;
	}

	public String getResponseContentType()
	{
		if( hasHttpResponse() )
		{
			if( httpResponse.getEntity() != null )
			{
				if( httpResponse.getEntity().getContentType() != null )
				{
					return httpResponse.getEntity().getContentType().getValue();
				}
			}
		}
		return null;
	}

	public long getResponseReadTime()
	{
		return responseReadTime / 1000000;
	}

	public long getResponseReadTimeNanos()
	{
		return responseReadTime;
	}

	public byte[] getDecompressedResponseBody() throws IOException
	{
		if( hasHttpResponse() )
		{
			String compressionAlg = HttpClientSupport.getResponseCompressionType( httpResponse );
			if( compressionAlg != null )
			{
				try
				{
					return CompressionSupport.decompress( compressionAlg, responseBody );
				}
				catch( Exception e )
				{
					IOException ioe = new IOException( "Decompression of response failed" );
					ioe.initCause( e );
					throw ioe;
				}
			}
		}

		return responseBody;
	}

	/**
	 * Handles charset specified in Content-Encoding headers
	 *
	 * @return
	 */

	public String getResponseCharset()
	{
		if( hasHttpResponse() )
		{
			Header header = null;
			if( httpResponse.getEntity() != null )
			{
				header = httpResponse.getEntity().getContentType();
			}
			if( header != null )
			{
				for( HeaderElement headerElement : header.getElements() )
				{
					NameValuePair parameter = headerElement.getParameterByName( "charset" );
					if( parameter != null )
						return parameter.getValue();
				}
			}

			Header contentEncodingHeader = null;
			if( httpResponse.getEntity() != null )
			{
				contentEncodingHeader = httpResponse.getEntity().getContentEncoding();
			}

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

	public org.apache.http.HttpResponse getHttpResponse()
	{
		return httpResponse;
	}

	public void setHttpResponse( org.apache.http.HttpResponse httpResponse )
	{
		this.httpResponse = httpResponse;
	}

	public boolean hasHttpResponse()
	{
		return httpResponse != null;
	}

	public byte[] getResponseBody() throws IOException
	{
		if( responseBody != null )
			return responseBody;

		if( hasHttpResponse() && httpResponse.getEntity() != null )
		{
			long now = System.nanoTime();
			HttpEntity bufferedEntity = new BufferedHttpEntity( httpResponse.getEntity() );
			long contentLength = bufferedEntity.getContentLength();
			if( metrics != null )
			{
				metrics.setContentLength( contentLength );
			}

			InputStream instream = bufferedEntity.getContent();

			try
			{
				if( maxSize == 0 || ( contentLength >= 0 && contentLength <= maxSize ) )
				{
					responseReadTime = System.nanoTime() - now;
					responseBody = EntityUtils.toByteArray( bufferedEntity );

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

					if( decompress && responseBody.length > 0 )
					{
						String compressionAlg = HttpClientSupport.getResponseCompressionType( httpResponse );
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

					ByteArrayOutputStream outstream = instream == null ? new ByteArrayOutputStream() : Tools.readAll(
							instream, maxSize );

					if( responseReadTime == 0 )
					{
						responseReadTime = System.nanoTime() - now;
					}

					responseBody = outstream.toByteArray();
				}
			} finally
			{
				if( instream != null )
				{
					instream.close();
				}
			}
		}

		// convert to ms
		//responseReadTime /= 1000000;

		return responseBody;
	}

	public SoapUIMetrics getMetrics()
	{
		return metrics;
	}

}
