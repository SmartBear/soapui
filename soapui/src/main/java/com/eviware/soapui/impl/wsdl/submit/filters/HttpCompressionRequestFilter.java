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

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;

public class HttpCompressionRequestFilter extends AbstractRequestFilter
{
	private final static Logger log = Logger.getLogger( HttpCompressionRequestFilter.class );

	@Override
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> httpRequest )
	{
		Settings settings = httpRequest.getSettings();
		String compressionAlg = settings.getString( HttpSettings.REQUEST_COMPRESSION, "None" );
		if( !"None".equals( compressionAlg ) )
		{
			try
			{
				ExtendedHttpMethod method = ( ExtendedHttpMethod )context
						.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
				if( method instanceof HttpEntityEnclosingRequest )
				{
					HttpEntity requestEntity = ( ( HttpEntityEnclosingRequest )method ).getEntity();
					if( requestEntity != null )
					{
						ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
						requestEntity.writeTo( tempOut );

						byte[] compressedData = CompressionSupport.compress( compressionAlg, tempOut.toByteArray() );
						( ( HttpEntityEnclosingRequest )method ).setEntity( new ByteArrayEntity( compressedData ) );
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
}
