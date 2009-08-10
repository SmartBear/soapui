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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import java.io.StringWriter;
import java.util.Vector;

import org.w3c.dom.Document;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.filters.WssRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.xml.XmlUtils;

public class WsdlSinglePartHttpResponse extends SinglePartHttpResponse implements WsdlResponse
{
	private Vector<Object> wssResult;

	public WsdlSinglePartHttpResponse( WsdlRequest wsdlRequest, ExtendedHttpMethod postMethod, String requestContent,
			PropertyExpansionContext context )
	{
		super( wsdlRequest, postMethod, requestContent, context );

		processIncomingWss( wsdlRequest, context );
	}

	private void processIncomingWss( WsdlRequest wsdlRequest, PropertyExpansionContext context )
	{
		IncomingWss incomingWss = ( IncomingWss )context.getProperty( WssRequestFilter.INCOMING_WSS_PROPERTY );
		if( incomingWss != null )
		{
			try
			{
				Document document = XmlUtils.parseXml( getResponseContent() );
				wssResult = incomingWss.processIncoming( document, context );
				if( wssResult != null && wssResult.size() > 0 )
				{
					StringWriter writer = new StringWriter();
					XmlUtils.serializePretty( document, writer );
					setResponseContent( writer.toString() );
				}
			}
			catch( Exception e )
			{
				if( wssResult == null )
					wssResult = new Vector<Object>();
				wssResult.add( e );
			}
		}
	}

	public Vector<?> getWssResult()
	{
		return wssResult;
	}

	@Override
	public WsdlRequest getRequest()
	{
		return ( WsdlRequest )super.getRequest();
	}
}
