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

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpVersion;
import org.apache.http.entity.AbstractHttpEntity;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;

public class PostPackagingRequestFilter extends AbstractRequestFilter
{

	@Override
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> request )
	{
		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
		Settings settings = request.getSettings();

		// chunking?
		if( httpMethod.getProtocolVersion().equals( HttpVersion.HTTP_1_1 )
				&& httpMethod instanceof HttpEntityEnclosingRequest )
		{
			HttpEntityEnclosingRequest entityEnclosingMethod = ( ( HttpEntityEnclosingRequest )httpMethod );
			long limit = settings.getLong( HttpSettings.CHUNKING_THRESHOLD, -1 );
			HttpEntity requestEntity = entityEnclosingMethod.getEntity();
			if( requestEntity != null && requestEntity instanceof AbstractHttpEntity )
			{
				( ( AbstractHttpEntity )requestEntity ).setChunked( limit >= 0 ? requestEntity.getContentLength() > limit
						: false );
			}
		}
	}
}
