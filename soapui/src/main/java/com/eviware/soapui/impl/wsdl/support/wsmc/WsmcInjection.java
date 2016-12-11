/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.support.wsmc;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.IAfterRequestInjection;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

public class WsmcInjection implements IAfterRequestInjection {

    private static final String WSMC_ACTION = "http://docs.oasis-open.org/ws-rx/wsmc/200702/MakeConnection";
    private static final String WSMC_NAMESPACE = "http://docs.oasis-open.org/ws-rx/wsmc/200702";

    private String endpoint;
    private WsdlOperation operation;
    private SoapVersion soapVersion;
    private String uuid;

    public WsmcInjection(String endpoint, WsdlOperation operation, SoapVersion soapVersion, String uuid) {
        this.endpoint = endpoint;
        this.operation = operation;
        this.soapVersion = soapVersion;
        this.uuid = uuid;
    }

    public String executeAfterRequest() {

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
