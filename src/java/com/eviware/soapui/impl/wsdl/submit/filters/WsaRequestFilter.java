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
		if( wsdlRequest != null && !((WsdlRequest)wsdlRequest).isWsAddressing())
			return;
		
		String content = (String) context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
		if( content == null )
		{
			log.warn( "Missing request content in context, skipping ws-addressing" );
		}
		else
		{
			content = addWSAddressing(content, (WsdlRequest) wsdlRequest);
			if( content != null )
				context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, content );
		}
	}
	public static String addWSAddressing( String content, WsdlRequest wsdlRequest )
	{
		try
		{
			SoapVersion soapVersion = wsdlRequest.getOperation().getInterface().getSoapVersion();
			
//			Definition definition = wsdlRequest.getOperation().getInterface().getWsdlContext().getDefinition();
//			
//			
//			definition.getTargetNamespace();
//			
//			wsdlRequest.getOperation().getInterface().getBinding().getPortType().getQName();
//			
//			BindingOperation bindingOperation = wsdlRequest.getOperation().getBindingOperation();
			
         //version="2005/08" is default
			String versionNameSpace = "http://www.w3.org/2005/08/addressing";
         if (wsdlRequest.getWsaConfig().getVersion().equals(WsaVersionTypeConfig.X_200408.toString()))
			{
				versionNameSpace = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
			}

			XmlObject xmlObject = XmlObject.Factory.parse(content);
			Element header = (Element)SoapUtils.getHeaderElement(xmlObject,soapVersion, true).getDomNode();
			header.setAttribute("xmlns:wsa", versionNameSpace);

			XmlObject[] envelope = xmlObject.selectChildren( soapVersion.getEnvelopeQName() );
         Element elm = (Element) envelope[0].getDomNode();
         
//         if (!wsdlRequest.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.NONE.toString()))
//			{
//            int muValue = 0;
//            if (wsdlRequest.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.TRUE.toString()))
//				{
//					muValue = 1;
//				}
//            Element mustUnderstandElm = elm.getOwnerDocument().createElementNS("http://www.w3schools.com/transaction/","m:Trans");
//            mustUnderstandElm.setAttribute("soap:mustUnderstand", "" + muValue);
//            header.appendChild(mustUnderstandElm);
//			}
         boolean mustUnderstand = false;
         String mustUnderstandValue = "";
         if (!wsdlRequest.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.NONE.toString()))
			{
				mustUnderstand = true;
				mustUnderstandValue = 
					wsdlRequest.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.TRUE.toString()) ? "1" : "0";
			}
         String soapVersionNamespace = wsdlRequest.getOperation().getInterface().getSoapVersion().getEnvelopeNamespace();
         
         Element wsActionElm = elm.getOwnerDocument().createElementNS(versionNameSpace,"wsa:Action");
         Text actTxtElm = elm.getOwnerDocument().createTextNode(wsdlRequest.getWsaConfig().getAction());
         if (mustUnderstand)
			{
         	wsActionElm.setAttributeNS(soapVersionNamespace, "mustUnderstand", mustUnderstandValue);
         	//("soap:mustUnderstand", "" + mustUnderstandValue);
			}
         wsActionElm.appendChild(actTxtElm);
         header.appendChild(wsActionElm);

         String from = wsdlRequest.getWsaConfig().getFrom(); 
         if (from != null && !from.isEmpty())
			{
            Element wsFromElm = elm.getOwnerDocument().createElementNS(versionNameSpace,"wsa:From");
            Text fromTxtElm = elm.getOwnerDocument().createTextNode(from);
            if (mustUnderstand)
   			{
            	wsFromElm.setAttribute("soap:mustUnderstand", "" + mustUnderstandValue);
   			}
            wsFromElm.appendChild(fromTxtElm);
            header.appendChild(wsFromElm);
			}
         
         String replyTo = wsdlRequest.getWsaConfig().getReplyTo();
         if (replyTo != null && !replyTo.isEmpty())
			{
            Element wsAddressElm = elm.getOwnerDocument().createElementNS(versionNameSpace,"wsa:Address");
            Element wsReplyToElm = elm.getOwnerDocument().createElementNS(versionNameSpace,"wsa:ReplyTo");
            if (mustUnderstand)
   			{
            	wsReplyToElm.setAttribute("soap:mustUnderstand", "" + mustUnderstandValue);
   			}
            Text replyToTxtElm = elm.getOwnerDocument().createTextNode(replyTo);
            wsAddressElm.appendChild(replyToTxtElm);
            wsReplyToElm.appendChild(wsAddressElm);
            header.appendChild(wsReplyToElm);
			}
         
         String faultTo = wsdlRequest.getWsaConfig().getFaultTo();
         if (faultTo != null && !faultTo.isEmpty())
			{
            Element wsAddressElm = elm.getOwnerDocument().createElementNS(versionNameSpace,"wsa:Address");
            Element wsFaultToElm = elm.getOwnerDocument().createElementNS(versionNameSpace,"wsa:FaultTo");
            if (mustUnderstand)
   			{
            	wsFaultToElm.setAttribute("soap:mustUnderstand", "" + mustUnderstandValue);
   			}
            Text faultToTxtElm = elm.getOwnerDocument().createTextNode(faultTo);
            wsAddressElm.appendChild(faultToTxtElm);
            wsFaultToElm.appendChild(wsAddressElm);
            header.appendChild(wsFaultToElm);
			}

         String msgId = wsdlRequest.getWsaConfig().getFaultTo();
         if (msgId != null && !msgId.isEmpty())
			{
				Element wsaMsgIdElm = elm.getOwnerDocument().createElement("wsa:MessageID");
				//String msgId = UUID.randomUUID().toString();
				Text wsaMsgIdTxtElm = elm.getOwnerDocument().createTextNode(msgId);
	         if (mustUnderstand)
				{
	         	wsaMsgIdElm.setAttribute("soap:mustUnderstand", "" + mustUnderstandValue);
				}
				wsaMsgIdElm.appendChild(wsaMsgIdTxtElm);
				header.appendChild(wsaMsgIdElm);
			}
         
         content = xmlObject.xmlText();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		
		
		return content;
	}

}

