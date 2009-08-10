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

package com.eviware.soapui.impl.wsdl.support.wsmc;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.WsaConfigConfig;
import com.eviware.soapui.config.WsrmConfigConfig;
import com.eviware.soapui.impl.support.wsa.WsaRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainerImpl;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmConfig;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;

public class WsmcUtils
{
	private static final String WSMC_ACTION = "http://docs.oasis-open.org/ws-rx/wsmc/200702/MakeConnection";
	private static final String WSMC_NAMESPACE = "http://docs.oasis-open.org/ws-rx/wsmc/200702";

	public void sendMakeConnectionRequest( String endpoint, SoapVersion soapVersion, WsdlOperation operation, String uuid )
	{
		String identifier = null;

		HttpRequestConfig httpRequestConfig = ( HttpRequestConfig )( XmlObject.Factory.newInstance()
				.changeType( HttpRequestConfig.type ) );
		httpRequestConfig.setEndpoint( endpoint );

		WsaConfigConfig wsaConfigConfig = ( WsaConfigConfig )( XmlObject.Factory.newInstance()
				.changeType( WsaConfigConfig.type ) );
		WsaContainer wsaContainer = new WsaContainerImpl();
		wsaContainer.setOperation( operation );
		WsaConfig wsaConfig = new WsaConfig( wsaConfigConfig, wsaContainer );

		WsrmConfigConfig wsrmConfigConfig = ( WsrmConfigConfig )( XmlObject.Factory.newInstance()
				.changeType( WsrmConfigConfig.type ) );
		WsrmConfig wsrmConfig = new WsrmConfig( wsrmConfigConfig, null );

		WsaRequest makeConnectionRequest = new WsaRequest( httpRequestConfig, wsaConfig, wsrmConfig, false );
		makeConnectionRequest.setOperation( operation );

		String makeConnectionMessageContent = SoapMessageBuilder.buildEmptyMessage( soapVersion );

		makeConnectionRequest.getWsaConfig().setWsaEnabled( true );
		makeConnectionRequest.getWsaConfig().setAction( WSMC_ACTION );

		makeConnectionRequest.getWsaConfig().setTo(
				WsaUtils.getNamespace( makeConnectionRequest.getWsaConfig().getVersion() ) + "/anonymous" );
		makeConnectionRequest.getWsaConfig().setGenerateMessageId( true );

		try
		{
			XmlObject object = XmlObject.Factory.parse( makeConnectionMessageContent );
			XmlCursor cursor = object.newCursor();

			cursor.toFirstContentToken();
			cursor.toFirstChild();
			cursor.toNextSibling();

			cursor.toNextToken();
			cursor.insertNamespace( "wsmc", WSMC_NAMESPACE );

			cursor.beginElement( "MakeConnection", WSMC_NAMESPACE );
			cursor.beginElement( "Address", WSMC_NAMESPACE );
			cursor.insertChars( WsaUtils.getNamespace( makeConnectionRequest.getWsaConfig().getVersion() )
					+ "/anonymous?id=" + uuid );

			cursor.dispose();

			cursor.dispose();

			WsaUtils wsaUtils = new WsaUtils( object.xmlText(), soapVersion, null, new DefaultPropertyExpansionContext(
					makeConnectionRequest ) );
			String content = wsaUtils.addWSAddressingRequest( makeConnectionRequest );

			makeConnectionRequest.setRequestContent( content );

		}
		catch( XmlException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{

			WsdlSubmit wsdlSubmit = makeConnectionRequest.submit( new WsdlSubmitContext( null ), true );

			// startSequenceRequest.getWsaConfig().setWsaEnabled(false);
			while( wsdlSubmit.getStatus() != Status.FINISHED )
			{
				wsdlSubmit.waitUntilFinished();
			}
			Response response = wsdlSubmit.getResponse();
			String responseContent = response.getContentAsString();
			XmlObject xml = XmlObject.Factory.parse( responseContent );
			XmlCursor cursor = xml.newCursor();
			cursor.toFirstContentToken();
			cursor.toFirstChild();
			cursor.toNextSibling();
			cursor.toFirstChild();

			String sequenceIdentifier = cursor.getTextValue();
			Logger.getLogger( "wsrm" ).info( "Sequence response Received, sequence ID: " + sequenceIdentifier );

			// WsmcInjection receiveInjection = new WsmcInjection(request);
			// request.setAfterRequestInjection(receiveInjection);

		}
		catch( SubmitException e1 )
		{
			SoapUI.logError( e1 );
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
		}
	}
}
