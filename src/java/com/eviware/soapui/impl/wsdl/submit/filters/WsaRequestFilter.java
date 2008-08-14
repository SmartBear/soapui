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

import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.iface.SubmitContext;

/**
 * RequestFilter that expands properties in request content
 * 
 * @author Ole.Matzura
 */

public class WsaRequestFilter extends AbstractRequestFilter
{
	public final static Logger log = Logger.getLogger(WsaRequestFilter.class);
	
	public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> wsdlRequest)
	{
		if( !(wsdlRequest instanceof WsdlRequest) || !((WsdlRequest)wsdlRequest).isWsAddressing())
			return;
		
		String content = (String) context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
		if( content == null )
		{
			log.warn( "Missing request content in context, skipping ws-addressing" );
		}
		else
		{
			ExtendedHttpMethod httpMethod = (ExtendedHttpMethod) context.getProperty( BaseHttpRequestTransport.HTTP_METHOD );
			content = addWSAddressing(content, (WsdlRequest) wsdlRequest, httpMethod);
			if( content != null )
				context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, content );
		}
	}
	public static String addWSAddressing( String content, WsdlRequest wsdlRequest, ExtendedHttpMethod httpMethod )
	{
		try
		{
			SoapVersion soapVersion = wsdlRequest.getOperation().getInterface().getSoapVersion();
			
         //version="2005/08" is default
			String wsaVersionNameSpace = "http://www.w3.org/2005/08/addressing";
         if (wsdlRequest.getWsaConfig().getVersion().equals(WsaVersionTypeConfig.X_200408.toString()))
			{
         	wsaVersionNameSpace = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
			}

			XmlObject xmlObject = XmlObject.Factory.parse(content);
			Element header = (Element)SoapUtils.getHeaderElement(xmlObject,soapVersion, true).getDomNode();
			
			header.setAttribute("xmlns:wsa", wsaVersionNameSpace);

			XmlObject[] envelope = xmlObject.selectChildren( soapVersion.getEnvelopeQName() );
         Element elm = (Element) envelope[0].getDomNode();
         
         Boolean mustUnderstand = null;
         if (wsdlRequest.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.FALSE.toString()))
			{
				mustUnderstand = false;
			} else if (wsdlRequest.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.TRUE.toString()))
			{
				mustUnderstand = true;
			}
         
         WsaBuilder builder = new WsaBuilder( header, soapVersion, wsaVersionNameSpace, mustUnderstand );
         
         header.appendChild(builder.createWsaChildElement("wsa:Action", elm, wsdlRequest.getWsaConfig().getAction()));

         String from = wsdlRequest.getWsaConfig().getFrom(); 
         if (from != null && !from.isEmpty())
			{
         	header.appendChild(builder.createWsaChildElement("wsa:from", elm, from) );
			}
         
         String replyTo = wsdlRequest.getWsaConfig().getReplyTo();
         if (replyTo != null && !replyTo.isEmpty())
			{
         	header.appendChild(builder.createWsaChildElement("wsa:replyTo", elm, replyTo) );
			} else if (wsdlRequest.getOperation().isRequestResponse())
			{
				//TODO if replyTo not specified but wsa:replyTo mandatory ...specify what???
			}
         
         String faultTo = wsdlRequest.getWsaConfig().getFaultTo();
         if (faultTo != null && !faultTo.isEmpty())
			{
         	header.appendChild(builder.createWsaChildElement("wsa:faultTo", elm, faultTo) );
			} 

         String msgId = wsdlRequest.getWsaConfig().getMessageID();
         if (msgId != null && !msgId.isEmpty())
			{
            header.appendChild(builder.createWsaChildElement("wsa:MessageID", elm, msgId));
			} else if (wsdlRequest.getOperation().isRequestResponse())
			{
				//if msgId not specified but wsa:msgId mandatory create one
            header.appendChild(builder.createWsaChildElement("wsa:MessageID", elm, UUID.randomUUID().toString()));
			}
         
         String to = wsdlRequest.getWsaConfig().getMessageID();
         if (to != null && to.isEmpty())
			{
            header.appendChild(builder.createWsaChildElement("wsa:to", elm, to) );
			} else if (wsdlRequest.getOperation().isOneWay() || wsdlRequest.getOperation().isRequestResponse()) {
				//if to not specified but wsa:to mandatory get default value
            header.appendChild(builder.createWsaChildElement("wsa:to", elm, httpMethod.getURI().toString()) );
         }
         
         content = xmlObject.xmlText();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		
		return content;
	}
	
	public static class WsaBuilder
	{
		private final SoapVersion soapVersion;
		private final String wsaVersionNameSpace;
		private final Boolean mustUnderstand;

		public WsaBuilder(Element header, SoapVersion soapVersion, String wsaVersionNameSpace, Boolean mustUnderstand)
		{
			// TODO Auto-generated constructor stub
			this.soapVersion = soapVersion;
			this.wsaVersionNameSpace = wsaVersionNameSpace;
			this.mustUnderstand = mustUnderstand;
		}
		
		public Element createWsaChildElement(String elementName, Element addToElement,   String wsaProperty ) {
			Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace,elementName);
	      Text txtElm = addToElement.getOwnerDocument().createTextNode(wsaProperty);
	      if (mustUnderstand != null)
			{
	      	wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
			}
	      wsaElm.appendChild(txtElm);
//	      header.appendChild(wsFromElm);
	      return wsaElm;
		}
		public Element createWsaAddressChildElement(String elementName, Element addToElement,   String wsaProperty ) {
	      Element wsAddressElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace,"wsa:Address");
			Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace,elementName);
	      Text txtElm = addToElement.getOwnerDocument().createTextNode(wsaProperty);
	      if (mustUnderstand != null)
			{
	      	wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
			}
	      wsAddressElm.appendChild(txtElm);
	      wsaElm.appendChild(wsAddressElm);
	      return wsaElm;
		}
	}
	
}

