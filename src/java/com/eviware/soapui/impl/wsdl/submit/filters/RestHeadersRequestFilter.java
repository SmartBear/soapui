/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpMethod;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.StringUtils;

/**
 * RequestFilter that adds SOAP specific headers
 * 
 * @author Ole.Matzura
 */

public class RestHeadersRequestFilter extends AbstractRequestFilter
{
	@Override
	public void filterRestRequest(SubmitContext context, RestRequest request)
	{
		HttpMethod httpMethod = (HttpMethod) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);

		String path = request.getResource().buildPath(context);
		StringBuffer query = new StringBuffer();
		
		XmlBeansRestParamsTestPropertyHolder params = request.getParams();
		for (int c = 0; c < params.getPropertyCount(); c++)
		{
			RestParamProperty param = params.getPropertyAt(c);
			
			String value = PropertyExpansionUtils.expandProperties(context, param.getValue());
			
			switch( param.getStyle() )
			{
			case HEADER : 
				httpMethod.setRequestHeader( param.getName(), value );
				break;
			case QUERY :
				if( query.length() > 0 )
					query.append( '&' );
				
				query.append( URLEncoder.encode(param.getName())).append('=').append( URLEncoder.encode(value ));
				break;
			case TEMPLATE :
				path.replaceAll(param.getName(), URLEncoder.encode(value));
				break;
			case MATRIX :
				path += ";" + param.getName() + "=" + value;
			}
		}
		
		if( query.length() > 1 )
			httpMethod.setQueryString( query.toString() );
		
		httpMethod.setPath(path);

		// init content-type and encoding
		String encoding = request.getEncoding();

		httpMethod.setRequestHeader("Content-Type", request.getMediaType()
				+ (StringUtils.hasContent(encoding) ? "; charset=" + encoding : ""));
	}
}
