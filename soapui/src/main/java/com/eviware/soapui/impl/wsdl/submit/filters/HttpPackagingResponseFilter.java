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

import org.apache.http.Header;

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlMimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlSinglePartHttpResponse;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class HttpPackagingResponseFilter extends AbstractRequestFilter
{
	@Override
	public void afterAbstractHttpResponse( SubmitContext context, AbstractHttpRequestInterface<?> request )
	{
		ExtendedHttpMethod httpMethod = ( ExtendedHttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
		String requestContent = ( String )context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );

		// check content-type for multipart
		Header responseContentTypeHeader = null;
		if( httpMethod.hasHttpResponse() )
		{
			Header[] headers = httpMethod.getHttpResponse().getHeaders( "Content-Type" );
			if( headers != null && headers.length > 0 )
				responseContentTypeHeader = headers[0];
		}
		Response response = null;
		if( request instanceof WsdlRequest )
			response = wsdlRequest( context, ( WsdlRequest )request, httpMethod, responseContentTypeHeader, requestContent );
		else if( request instanceof HttpRequestInterface<?> )
			response = httpRequest( context, ( HttpRequestInterface<?> )request, httpMethod, responseContentTypeHeader,
					requestContent );

		context.setProperty( BaseHttpRequestTransport.RESPONSE, response );

	}

	private Response wsdlRequest( SubmitContext context, WsdlRequest request, ExtendedHttpMethod httpMethod,
			Header responseContentTypeHeader, String requestContent )
	{
		if( context.hasProperty( "PreWssProcessedDocument" ) )
			requestContent = String.valueOf( context.getProperty( "PreWssProcessedDocument" ) );

		XmlBeansSettingsImpl settings = request.getSettings();
		if( !settings.getBoolean( WsdlRequest.INLINE_RESPONSE_ATTACHMENTS ) && responseContentTypeHeader != null
				&& responseContentTypeHeader.getValue().toUpperCase().startsWith( "MULTIPART" ) )
		{
			return new WsdlMimeMessageResponse( request, httpMethod, requestContent, context );
		}
		else
		{
			return new WsdlSinglePartHttpResponse( request, httpMethod, requestContent, context );
		}
	}

	private Response httpRequest( SubmitContext context, HttpRequestInterface<?> request, ExtendedHttpMethod httpMethod,
			Header responseContentTypeHeader, String requestContent )
	{
		if( responseContentTypeHeader != null
				&& responseContentTypeHeader.getValue().toUpperCase().startsWith( "MULTIPART" ) )
		{
			return new MimeMessageResponse( request, httpMethod, requestContent, context );
		}
		else
		{
			return new SinglePartHttpResponse( request, httpMethod, requestContent, context );
		}
	}
}
