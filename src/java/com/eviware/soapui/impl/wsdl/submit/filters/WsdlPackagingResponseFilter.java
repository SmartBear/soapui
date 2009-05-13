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

package com.eviware.soapui.impl.wsdl.submit.filters;

import org.apache.commons.httpclient.Header;

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlMimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlSinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public class WsdlPackagingResponseFilter extends AbstractRequestFilter
{
	@Override
	public void afterWsdlRequest( SubmitContext context, WsdlRequest request )
	{
		ExtendedPostMethod postMethod = ( ExtendedPostMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
		String requestContent = ( String )context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
		XmlBeansSettingsImpl settings = request.getSettings();

		// check content-type for multiplart
		Header responseContentTypeHeader = postMethod.getResponseHeader( "Content-Type" );
		Response response = null;

		if( !settings.getBoolean( WsdlRequest.INLINE_RESPONSE_ATTACHMENTS ) && responseContentTypeHeader != null
				&& responseContentTypeHeader.getValue().toUpperCase().startsWith( "MULTIPART" ) )
		{
			response = new WsdlMimeMessageResponse( request, postMethod, requestContent, context );
		}
		else
		{
			response = new WsdlSinglePartHttpResponse( request, postMethod, requestContent, context );
		}

		context.setProperty( BaseHttpRequestTransport.RESPONSE, response );
	}
}
