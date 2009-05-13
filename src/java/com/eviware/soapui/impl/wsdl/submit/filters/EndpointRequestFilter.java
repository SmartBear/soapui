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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;

/**
 * RequestFilter that adds SOAP specific headers
 * 
 * @author Ole.Matzura
 */

public class EndpointRequestFilter extends AbstractRequestFilter
{
	@Override
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> request )
	{
		HttpMethod httpMethod = ( HttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		String strURL = request.getEndpoint();
		strURL = PropertyExpansionUtils.expandProperties( context, strURL );
		try
		{
			if( StringUtils.hasContent( strURL ) )
				httpMethod.setURI( new URI( strURL, request.getSettings().getBoolean( HttpSettings.ENCODED_URLS ) ) );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}
}
