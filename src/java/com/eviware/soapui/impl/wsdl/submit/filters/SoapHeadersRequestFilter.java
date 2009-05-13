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

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.iface.SubmitContext;

/**
 * RequestFilter that adds SOAP specific headers
 * 
 * @author Ole.Matzura
 */

public class SoapHeadersRequestFilter extends AbstractRequestFilter
{
	public void filterWsdlRequest( SubmitContext context, WsdlRequest wsdlRequest )
	{
		HttpMethod postMethod = ( HttpMethod )context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );

		WsdlInterface wsdlInterface = ( WsdlInterface )wsdlRequest.getOperation().getInterface();

		// init content-type and encoding
		String encoding = System.getProperty( "soapui.request.encoding", wsdlRequest.getEncoding() );

		SoapVersion soapVersion = wsdlInterface.getSoapVersion();
		String soapAction = wsdlRequest.isSkipSoapAction() ? null : wsdlRequest.getOperation().getAction();

		postMethod.setRequestHeader( "Content-Type", soapVersion.getContentTypeHttpHeader( encoding, soapAction ) );

		if( !wsdlRequest.isSkipSoapAction() )
		{
			String soapActionHeader = soapVersion.getSoapActionHeader( soapAction );
			if( soapActionHeader != null )
				postMethod.setRequestHeader( "SOAPAction", soapActionHeader );
		}
	}
}
