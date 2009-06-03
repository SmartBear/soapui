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

import org.w3c.dom.Document;

import com.eviware.soapui.config.EndpointConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy.EndpointDefaults;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;

public class WssRequestFilter extends AbstractWssRequestFilter implements RequestFilter
{
	public final static String INCOMING_WSS_PROPERTY = "WssRequestFilter#IncomingWss";

	public void filterWsdlRequest( SubmitContext context, WsdlRequest wsdlRequest )
	{
		WssContainer wssContainer = wsdlRequest.getOperation().getInterface().getProject().getWssContainer();
		OutgoingWss outgoingWss = wssContainer.getOutgoingWssByName( wsdlRequest.getOutgoingWss() );

		DefaultEndpointStrategy des = ( DefaultEndpointStrategy )wsdlRequest.getOperation().getInterface().getProject()
				.getEndpointStrategy();
		EndpointDefaults endpointDefaults = des.getEndpointDefaults( wsdlRequest.getEndpoint() );
		if( StringUtils.hasContent( endpointDefaults.getOutgoingWss() )
				&& ( outgoingWss == null || endpointDefaults.getMode() != EndpointConfig.Mode.COMPLEMENT ) )
		{
			outgoingWss = wssContainer.getOutgoingWssByName( endpointDefaults.getOutgoingWss() );
		}

		if( outgoingWss != null )
		{
			try
			{
				Document wssDocument = getWssDocument( context );
				outgoingWss.processOutgoing( wssDocument, context );
				updateWssDocument( context, wssDocument );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		IncomingWss incomingWss = wssContainer.getIncomingWssByName( wsdlRequest.getIncomingWss() );

		if( StringUtils.hasContent( endpointDefaults.getIncomingWss() )
				&& ( incomingWss == null || endpointDefaults.getMode() != EndpointConfig.Mode.COMPLEMENT ) )
		{
			incomingWss = wssContainer.getIncomingWssByName( endpointDefaults.getIncomingWss() );
		}

		if( incomingWss != null )
		{
			context.setProperty( INCOMING_WSS_PROPERTY, incomingWss );
		}
	}
}
