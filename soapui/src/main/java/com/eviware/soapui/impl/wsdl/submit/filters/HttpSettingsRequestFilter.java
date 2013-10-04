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

package com.eviware.soapui.impl.wsdl.submit.filters;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;

/**
 * RequestFilter that applies SoapUI HTTP-settings to the current request
 * 
 * @author Ole.Matzura
 */

public class HttpSettingsRequestFilter extends AbstractRequestFilter
{
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> httpRequest )
	{
		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		// set maxsize
		Settings settings = httpRequest.getSettings();

		// close connections?
		if( settings.getBoolean( HttpSettings.CLOSE_CONNECTIONS ) )
		{
			httpMethod.setHeader( "Connection", "close" );
		}

		// close connections?
		if( settings.getBoolean( HttpSettings.EXPECT_CONTINUE ) && httpMethod instanceof HttpEntityEnclosingRequest )
		{
			httpMethod.getParams().setParameter( CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.TRUE );
		}

		// compress request?
		String compressionAlg = settings.getString( HttpSettings.REQUEST_COMPRESSION, "None" );
		if( !"None".equals( compressionAlg ) )
			httpMethod.setHeader( "Content-Encoding", compressionAlg );

		// accept compressed responses?
		if( settings.getBoolean( HttpSettings.RESPONSE_COMPRESSION ) )
		{
			httpMethod.setHeader( "Accept-Encoding", CompressionSupport.getAvailableAlgorithms( "," ) );
		}

		String httpVersion = settings.getString( HttpSettings.HTTP_VERSION, "1.1" );
		if( httpVersion.equals( HttpSettings.HTTP_VERSION_1_1 ) )
		{
			httpMethod.getParams().setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1 );
		}
		else if( httpVersion.equals( HttpSettings.HTTP_VERSION_1_0 ) )
		{
			httpMethod.getParams().setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0 );
		}
		else if( httpVersion.equals( HttpSettings.HTTP_VERSION_0_9 ) )
		{
			httpMethod.getParams().setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1 );
		}

		// max size..
		long maxSize = httpRequest.getMaxSize();
		if( maxSize == 0 )
			maxSize = settings.getLong( HttpSettings.MAX_RESPONSE_SIZE, 0 );
		if( maxSize > 0 )
			httpMethod.setMaxSize( maxSize );

		// follow redirects is false; handled in transport
		httpMethod.getParams().setParameter( ClientPNames.HANDLE_REDIRECTS, false );

		// apply global settings
		HttpClientSupport.applyHttpSettings( httpMethod, settings );

		String timeout = context.expand( httpRequest.getTimeout() );
		if( StringUtils.hasContent( timeout ) )
		{
			try
			{
				HttpConnectionParams.setSoTimeout( httpMethod.getParams(), Integer.parseInt( timeout ) );
			}
			catch( NumberFormatException e )
			{
				SoapUI.logError( e );
			}
		}
	}
}
