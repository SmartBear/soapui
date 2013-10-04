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

package com.eviware.soapui.impl.wsdl.support.wsmc;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.IAfterRequestInjection;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

public class WsmcInjection implements IAfterRequestInjection
{

	private static final String WSMC_ACTION = "http://docs.oasis-open.org/ws-rx/wsmc/200702/MakeConnection";
	private static final String WSMC_NAMESPACE = "http://docs.oasis-open.org/ws-rx/wsmc/200702";

	private String endpoint;
	private WsdlOperation operation;
	private SoapVersion soapVersion;
	private String uuid;

	public WsmcInjection( String endpoint, WsdlOperation operation, SoapVersion soapVersion, String uuid )
	{
		this.endpoint = endpoint;
		this.operation = operation;
		this.soapVersion = soapVersion;
		this.uuid = uuid;
	}

	public String executeAfterRequest()
	{

		/*
		 * HttpRequestConfig httpRequestConfig = ( HttpRequestConfig )(
		 * XmlObject.Factory.newInstance() .changeType( HttpRequestConfig.type )
		 * ); httpRequestConfig.setEndpoint( endpoint );
		 * 
		 * WsaConfigConfig wsaConfigConfig = ( WsaConfigConfig )(
		 * XmlObject.Factory.newInstance() .changeType( WsaConfigConfig.type ) );
		 * WsaContainer wsaContainer = new WsaContainerImpl();
		 * wsaContainer.setOperation( operation ); WsaConfig wsaConfig = new
		 * WsaConfig( wsaConfigConfig, wsaContainer );
		 * 
		 * WsrmConfigConfig wsrmConfigConfig = ( WsrmConfigConfig )(
		 * XmlObject.Factory.newInstance() .changeType( WsrmConfigConfig.type ) );
		 * WsrmConfig wsrmConfig = new WsrmConfig( wsrmConfigConfig, null );
		 * 
		 * WsaRequest makeConnectionRequest = new WsaRequest( httpRequestConfig,
		 * wsaConfig, wsrmConfig, false ); makeConnectionRequest.setOperation(
		 * operation );
		 * 
		 * String content = SoapMessageBuilder.buildEmptyMessage( soapVersion );
		 * 
		 * makeConnectionRequest.getWsaConfig().setWsaEnabled( true );
		 * makeConnectionRequest.getWsaConfig().setAction( WSMC_ACTION );
		 * makeConnectionRequest.getWsaConfig().setAddDefaultTo( true );
		 * makeConnectionRequest.getWsaConfig().setGenerateMessageId( true );
		 * 
		 * try { XmlObject object = XmlObject.Factory.parse( content ); XmlCursor
		 * cursor = object.newCursor();
		 * 
		 * cursor.toFirstContentToken(); cursor.toFirstChild();
		 * cursor.toNextSibling();
		 * 
		 * cursor.toNextToken(); cursor.insertNamespace( "wsmc", WSMC_NAMESPACE );
		 * 
		 * cursor.beginElement( "MakeConnection", WSMC_NAMESPACE );
		 * cursor.beginElement( "Address", WSMC_NAMESPACE ); cursor.insertChars(
		 * WsaUtils.getNamespace(
		 * makeConnectionRequest.getWsaConfig().getVersion() ) + "/anonymous" +
		 * "?id=" + uuid );
		 * 
		 * cursor.dispose();
		 * 
		 * makeConnectionRequest.getOperation().setAction( "" );
		 * makeConnectionRequest.setRequestContent( object.xmlText() );
		 * 
		 * WsaUtils wsaUtils = new WsaUtils( object.xmlText(), soapVersion,
		 * makeConnectionRequest.getOperation(), new
		 * DefaultPropertyExpansionContext( makeConnectionRequest ) ); content =
		 * wsaUtils.addWSAddressingRequest( makeConnectionRequest );
		 * 
		 * makeConnectionRequest.setRequestContent( content ); } catch(
		 * XmlException e ) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * try { makeConnectionRequest.submit( new WsdlSubmitContext( null ), true
		 * );
		 * 
		 * } catch( SubmitException e1 ) { SoapUI.logError( e1 ); }
		 */
		return null;
	}

}
