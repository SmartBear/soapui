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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.xmlbeans.XmlBoolean;

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

public class RestParametersRequestFilter extends AbstractRequestFilter
{
	@SuppressWarnings("deprecation")
	@Override
	public void filterRestRequest(SubmitContext context, RestRequest request)
	{
		HttpMethod httpMethod = (HttpMethod) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);

		String path = request.buildPath(context);
		StringBuffer query = new StringBuffer();
		
		XmlBeansRestParamsTestPropertyHolder params = request.getParams();
		for (int c = 0; c < params.getPropertyCount(); c++)
		{
			RestParamProperty param = params.getPropertyAt(c);
			
			String value = PropertyExpansionUtils.expandProperties(context, param.getValue());
			if( !StringUtils.hasContent(value) && !param.getRequired())
				continue;
			
			switch( param.getStyle() )
			{
			case HEADER : 
				httpMethod.setRequestHeader( param.getName(), value );
				break;
			case QUERY :
				if( query.length() > 0 )
					query.append( '&' );
				
				query.append( URLEncoder.encode(param.getName()));
				if( StringUtils.hasContent(value))
					query.append('=').append( URLEncoder.encode(value ));
				break;
			case TEMPLATE :
				path = path.replaceAll( "\\{" + param.getName() + "\\}", URLEncoder.encode(value));
				break;
			case MATRIX :
				if( param.getType().equals(XmlBoolean.type.getName()))
				{
					if( value.toUpperCase().equals("TRUE") || value.equals("1"))
					{
						path += ";" + param.getName();
					}
				}
				else
				{
					path += ";" + param.getName();
					if( StringUtils.hasContent(value) )
					{
						path += "=" + URLEncoder.encode(value);
					}
				}
			}
		}
		
		if( query.length() > 1 )
			httpMethod.setQueryString( query.toString() );
		
		httpMethod.setPath(path);
		String encoding = StringUtils.unquote( request.getEncoding());
		
		if( request.hasRequestBody() && httpMethod instanceof EntityEnclosingMethod )
		{
			String requestContent = request.getRequestContent();
			try
			{
				byte[] content = encoding == null ? requestContent.getBytes() : requestContent.getBytes(encoding);
				((EntityEnclosingMethod)httpMethod).setRequestEntity(new ByteArrayRequestEntity(content));
			}
			catch (UnsupportedEncodingException e)
			{
				((EntityEnclosingMethod)httpMethod).setRequestEntity(new ByteArrayRequestEntity( requestContent.getBytes()));
			}
		}

		// init content-type and encoding
		httpMethod.setRequestHeader("Content-Type", request.getMediaType()
				+ (StringUtils.hasContent(encoding) ? "; charset=" + encoding : ""));
	}
}
