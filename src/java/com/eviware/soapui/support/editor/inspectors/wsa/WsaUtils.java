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

package com.eviware.soapui.support.editor.inspectors.wsa;

import java.util.UUID;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.model.iface.Operation;

/**
 * WS Addressing-related utility-methods..
 * 
 * @author dragica.soldo
 */

public class WsaUtils
{
	SoapVersion soapVersion;
	Operation operation;
	//String content, WsaContainer wsdlRequest, ExtendedHttpMethod httpMethod
	public WsaUtils(SoapVersion soapVersion, Operation operation) {
		this.soapVersion = soapVersion;
		this.operation = operation;
	}
	public String addWSAddressing( String content, WsaContainer wsaContainer) {
		return addWSAddressing(content, wsaContainer, null);
	}
	public String addWSAddressing( String content, WsaContainer wsaContainer, ExtendedHttpMethod httpMethod )
	{
		try
		{
         //version="2005/08" is default
			String wsaVersionNameSpace = "http://www.w3.org/2005/08/addressing";
         if (wsaContainer.getWsaConfig().getVersion().equals(WsaVersionTypeConfig.X_200408.toString()))
			{
         	wsaVersionNameSpace = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
			}

			XmlObject xmlObject = XmlObject.Factory.parse(content);
			Element header = (Element)SoapUtils.getHeaderElement(xmlObject,soapVersion, true).getDomNode();
			
			header.setAttribute("xmlns:wsa", wsaVersionNameSpace);

			XmlObject[] envelope = xmlObject.selectChildren( soapVersion.getEnvelopeQName() );
         Element elm = (Element) envelope[0].getDomNode();
         
         Boolean mustUnderstand = null;
         if (wsaContainer.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.FALSE.toString()))
			{
				mustUnderstand = false;
			} else if (wsaContainer.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.TRUE.toString()))
			{
				mustUnderstand = true;
			}
         
         WsaBuilder builder = new WsaBuilder( wsaVersionNameSpace, mustUnderstand );
         
         header.appendChild(builder.createWsaChildElement("wsa:Action", elm, wsaContainer.getWsaConfig().getAction()));

         String from = wsaContainer.getWsaConfig().getFrom(); 
         if (from != null && from.length() > 0)
			{
         	header.appendChild(builder.createWsaChildElement("wsa:from", elm, from) );
			}
         
         String replyTo = wsaContainer.getWsaConfig().getReplyTo();
         if (replyTo != null && replyTo.length() > 0)
			{
         	header.appendChild(builder.createWsaChildElement("wsa:replyTo", elm, replyTo) );
			} else if (operation.isRequestResponse())
			{
				//TODO if replyTo not specified but wsa:replyTo mandatory ...specify what???
			}
         
         String faultTo = wsaContainer.getWsaConfig().getFaultTo();
         if (faultTo != null && faultTo.length() > 0)
			{
         	header.appendChild(builder.createWsaChildElement("wsa:faultTo", elm, faultTo) );
			} 

         String msgId = wsaContainer.getWsaConfig().getMessageID();
         if (msgId != null && msgId.length() > 0)
			{
            header.appendChild(builder.createWsaChildElement("wsa:MessageID", elm, msgId));
			} else if (operation.isRequestResponse())
			{
				//if msgId not specified but wsa:msgId mandatory create one
            header.appendChild(builder.createWsaChildElement("wsa:MessageID", elm, UUID.randomUUID().toString()));
			}
         
         //only for wsdlRequest
         if (httpMethod != null)
			{
            String to = wsaContainer.getWsaConfig().getTo();
            if (to != null && to.length() == 0)
   			{
               header.appendChild(builder.createWsaChildElement("wsa:to", elm, to) );
   			} else if (operation.isOneWay() || operation.isRequestResponse()) {
   				//if to not specified but wsa:to mandatory get default value
               header.appendChild(builder.createWsaChildElement("wsa:to", elm, httpMethod.getURI().toString()) );
            }
			}
         
         content = xmlObject.xmlText();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		
		return content;
	}
	
	public class WsaBuilder
	{
		private final String wsaVersionNameSpace;
		private final Boolean mustUnderstand;

		public WsaBuilder(String wsaVersionNameSpace, Boolean mustUnderstand)
		{
			// TODO Auto-generated constructor stub
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
