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

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.resolvers.ResolverUtils;
import com.eviware.soapui.settings.CommonSettings;

/**
 * RequestFilter that expands properties in request content
 * 
 * @author Ole.Matzura
 */

public class PropertyExpansionRequestFilter extends AbstractRequestFilter
{
	public final static Logger log = Logger.getLogger( PropertyExpansionRequestFilter.class );

	@Override
	public void filterWsdlRequest( SubmitContext context, WsdlRequest request )
	{
		String content = ( String )context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
		if( content == null )
		{
			log.warn( "Missing request content in context, skipping property expansion" );
		}
		else
		{
			content = PropertyExpander.expandProperties( context, content,
					request.getSettings().getBoolean( CommonSettings.ENTITIZE_PROPERTIES ) );

			if( content != null )
			{
				context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, content );
			}
		}
	}

	/**
	 * @deprecated
	 */

	public static String expandProperties( SubmitContext context, String content )
	{
		return PropertyExpander.expandProperties( context, content );
	}

	/**
	 * @deprecated
	 */

	public static String getGlobalProperty( String propertyName )
	{
		return PropertyExpansionUtils.getGlobalProperty( propertyName );
	}

	/**
	 * @deprecated Use
	 *             {@link ResolverUtils#extractXPathPropertyValue(Object,String)}
	 *             instead
	 */
	public static String extractXPathPropertyValue( Object property, String xpath )
	{
		return ResolverUtils.extractXPathPropertyValue( property, xpath );
	}
}
