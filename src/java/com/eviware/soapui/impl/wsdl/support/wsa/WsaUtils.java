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

package com.eviware.soapui.impl.wsdl.support.wsa;

import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * WS Addressing-related utility-methods..
 * 
 * @author dragica.soldo
 */

public class WsaUtils
{
	public static final String WS_A_VERSION_200508 = "http://www.w3.org/2005/08/addressing";
	public static final String WS_A_VERSION_200408 = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

	SoapVersion soapVersion;
	Operation operation;
	WsaBuilder builder;
	XmlObject xmlObject;
	// element to add every property to
	Element envelopeElement;
	String wsaVersionNameSpace;
	String replyToAnonimus;
	String relatesToReply;
	String content;
	

	// String content, WsaContainer wsdlRequest, ExtendedHttpMethod httpMethod
	public WsaUtils(String content,SoapVersion soapVersion, Operation operation) 
	{
		this.soapVersion = soapVersion;
		this.operation = operation;
		this.content = content;
		try
		{
			xmlObject = XmlObject.Factory.parse(content);
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
		}
	}

	public Element addWsAddressingCommon(WsaContainer wsaContainer) throws XmlException
	{

		// version="2005/08" is default
		wsaVersionNameSpace = WS_A_VERSION_200508;
		if (wsaContainer.getWsaConfig().getVersion().equals(WsaVersionTypeConfig.X_200408.toString()))
		{
			wsaVersionNameSpace = WS_A_VERSION_200408;
		}
		replyToAnonimus = wsaVersionNameSpace + "/anonymous";
		relatesToReply = wsaVersionNameSpace + "/reply";

		Element header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();

		header.setAttribute("xmlns:wsa", wsaVersionNameSpace);

		XmlObject[] envelope = xmlObject.selectChildren(soapVersion.getEnvelopeQName());
		envelopeElement = (Element) envelope[0].getDomNode();

		Boolean mustUnderstand = null;
		if (wsaContainer.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.FALSE.toString()))
		{
			mustUnderstand = false;
		}
		else if (wsaContainer.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.TRUE.toString()))
		{
			mustUnderstand = true;
		}

		builder = new WsaBuilder(wsaVersionNameSpace, mustUnderstand);


		String from = wsaContainer.getWsaConfig().getFrom();
		if (!StringUtils.isNullOrEmpty(from))
		{
			header.appendChild(builder.createWsaAddressChildElement("wsa:From", envelopeElement, from));
		}
		String faultTo = wsaContainer.getWsaConfig().getFaultTo();
		if (!StringUtils.isNullOrEmpty(faultTo))
		{
			header.appendChild(builder.createWsaAddressChildElement("wsa:FaultTo", envelopeElement, faultTo));
		}

		return header;

	}
	public String addWSAddressingRequest (WsaContainer wsaContainer)
	{
		return addWSAddressingRequest(wsaContainer, null);
	}

	public String addWSAddressingRequest(WsaContainer wsaContainer, ExtendedHttpMethod httpMethod)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer);

			String action = wsaContainer.getWsaConfig().getAction();
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction( wsaContainer.getOperation(), false );
			}
			
			if (!StringUtils.isNullOrEmpty(action))
			{
				header.appendChild(builder.createWsaChildElement("wsa:Action", envelopeElement, action ));
			}

			String replyTo = wsaContainer.getWsaConfig().getReplyTo();
			if (!StringUtils.isNullOrEmpty(replyTo))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:ReplyTo", envelopeElement, replyTo));
			}
			else if (operation.isRequestResponse())
			{
				if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_REPLYTO))
				{
					header.appendChild(builder.createWsaAddressChildElement("wsa:ReplyTo", envelopeElement, replyToAnonimus));
				}
			}

			String relatesTo = wsaContainer.getWsaConfig().getRelationshipType();
			String relationshipType = wsaContainer.getWsaConfig().getRelationshipType();
			if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo))
			{
				header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType, relatesTo));
			}

			String msgId = wsaContainer.getWsaConfig().getMessageID();
			if (!StringUtils.isNullOrEmpty(msgId))
			{
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, msgId));
			}
			else if (operation.isRequestResponse())
			{
				// if msgId not specified but wsa:msgId mandatory create one
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, UUID.randomUUID().toString()));
			}

			String to = wsaContainer.getWsaConfig().getTo();
			if (!StringUtils.isNullOrEmpty(to))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, to));
			}
			else if (operation.isOneWay() || operation.isRequestResponse())
			{
				if (httpMethod != null)
				{
					// if to not specified but wsa:to mandatory get default value
					header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, httpMethod.getURI().toString()));
				}
			}

			content = xmlObject.xmlText();
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
		}

		return content;
	}
	/**
	 * Adds ws-a headers to mock response from editor-menu
	 * in this case there is no request included and only values from ws-a inspector, if any, are used 
	 * @param content
	 * @param wsaContainer
	 * @return
	 */
	public String addWSAddressingMockResponse(WsaContainer wsaContainer)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer);

			String action = wsaContainer.getWsaConfig().getAction();
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction( wsaContainer.getOperation(), true );
			}
			if (!StringUtils.isNullOrEmpty(action) )
			{
				header.appendChild(builder.createWsaChildElement("wsa:Action", envelopeElement, action ));
			}

			String replyTo = wsaContainer.getWsaConfig().getReplyTo();
			if (!StringUtils.isNullOrEmpty(replyTo))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:ReplyTo", envelopeElement, replyTo));
			}

			String to = wsaContainer.getWsaConfig().getTo();
			if (!StringUtils.isNullOrEmpty(to))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, to));
			}

			String msgId = wsaContainer.getWsaConfig().getMessageID();
			if (!StringUtils.isNullOrEmpty(msgId))
			{
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, msgId));
			}

			String relationshipType = wsaContainer.getWsaConfig().getRelationshipType();
			if (!StringUtils.isNullOrEmpty(relationshipType) )
			{
				header
						.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType, ""));
			}
			else if (wsaContainer instanceof WsdlMockResponse)
			{
				if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE))
				{
						header
						.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relatesToReply, ""));
				}
			}
			content = xmlObject.xmlText();
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}

		return content;
	}

	public String addWSAddressingMockResponse(WsaContainer wsaContainer, WsdlMockRequest request)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer);

			String action = wsaContainer.getWsaConfig().getAction();
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction( wsaContainer.getOperation(), true );
			}
			if (!StringUtils.isNullOrEmpty(action) )
			{
				header.appendChild(builder.createWsaChildElement("wsa:Action", envelopeElement, action ));
			}

			String replyTo = wsaContainer.getWsaConfig().getReplyTo();
			if (!StringUtils.isNullOrEmpty(replyTo))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:ReplyTo", envelopeElement, replyTo));
			}

			XmlObject requestXmlObject = request.getRequestXmlObject();
			Element requestHeader = (Element) SoapUtils.getHeaderElement(requestXmlObject, request.getSoapVersion(), true)
					.getDomNode();

			// request.messageId = mockResponse.relatesTo so get it
			Element msgNode = XmlUtils.getFirstChildElementNS(requestHeader, wsaVersionNameSpace, "MessageID");
			String requestMessageId = null;
			if (msgNode != null)
			{
				requestMessageId = XmlUtils.getElementText(msgNode);
			}

			// request.replyTo = mockResponse.to so get it
			Element replyToNode = XmlUtils.getFirstChildElementNS(requestHeader, wsaVersionNameSpace, "ReplyTo");
			String requestReplyToValue = null;
			if (replyToNode != null)
			{
				Element replyToAddresseNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaVersionNameSpace, "Address");
				if (replyToAddresseNode != null)
				{
					requestReplyToValue = XmlUtils.getElementText(replyToAddresseNode);
				}
			}
			
			String to = wsaContainer.getWsaConfig().getTo();
			if (!StringUtils.isNullOrEmpty(to))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, to));
			}
			else
			{
				// if to not specified but wsa:to mandatory get default value
				if (!StringUtils.isNullOrEmpty(requestReplyToValue))
				{
					header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, requestReplyToValue));
				}
			}

			String relationshipType = wsaContainer.getWsaConfig().getRelationshipType();
			if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(requestMessageId))
			{
				header
						.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType, requestMessageId));
			}
			else if (wsaContainer instanceof WsdlMockResponse)
			{
				if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE))
				{
					if (!StringUtils.isNullOrEmpty(requestMessageId))
					{
						header
						.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relatesToReply, requestMessageId));
					}
				}
			}

			String msgId = wsaContainer.getWsaConfig().getMessageID();
			if (!StringUtils.isNullOrEmpty(msgId))
			{
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, msgId));
			}

			content = xmlObject.xmlText();
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
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

		public Element createWsaChildElement(String elementName, Element addToElement, String wsaProperty)
		{
			Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, elementName);
			Text txtElm = addToElement.getOwnerDocument().createTextNode(wsaProperty);
			if (mustUnderstand != null)
			{
				wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
			}
			wsaElm.appendChild(txtElm);
			return wsaElm;
		}

		public Element createWsaAddressChildElement(String elementName, Element addToElement, String wsaProperty)
		{
			Element wsAddressElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, "wsa:Address");
			Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, elementName);
			Text txtElm = addToElement.getOwnerDocument().createTextNode(wsaProperty);
			if (mustUnderstand != null)
			{
				wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
			}
			wsAddressElm.appendChild(txtElm);
			wsaElm.appendChild(wsAddressElm);
			return wsaElm;
		}

		public Element createRelatesToElement(String elementName, Element addToElement, String relationshipType,
				String relatesTo)
		{
			Element wsAddressElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, "wsa:Address");
			Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, elementName);
			wsaElm.setAttribute("RelationshipType", relationshipType);
			Text txtElm = addToElement.getOwnerDocument().createTextNode(relatesTo);
			if (mustUnderstand != null)
			{
				wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
			}
			wsAddressElm.appendChild(txtElm);
			wsaElm.appendChild(wsAddressElm);
			return wsaElm;
		}
	}
	public boolean hasWsAddressing(String content)
	{
		boolean hasWsa = false;
		try
		{
//			XmlObject xmlObject = XmlObject.Factory.parse(content);
			Element header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();
			if (header.hasAttribute("xmlns:wsa")) {
				hasWsa = true;
			}
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}
		return hasWsa;
	}
	private String cleanExistingWsaHeaders(String content) 
	{
		try
		{
//			XmlObject xmlObject = XmlObject.Factory.parse(content);
			Element header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();
			String currentWsaVersionNameSpace = header.getAttribute("xmlns:wsa");
			if (!StringUtils.isNullOrEmpty(currentWsaVersionNameSpace))
			{
				NodeList headerElements = header.getChildNodes();
				while (header.hasChildNodes())
				{
					Node childNode = headerElements.item(0);
					if (childNode.getNamespaceURI().equals(currentWsaVersionNameSpace))
					{
						header.removeChild(childNode);
					}
					
				}
				header.removeAttribute("xmlns:wsa");
			}
			
			content = xmlObject.xmlText();
			
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}
		return content;
	}
	public String overrideExistingRequestHeaders(String content, WsdlRequest wsdlrequest)
	{
		cleanExistingWsaHeaders(content);
		return addWSAddressingRequest(wsdlrequest);
	}
	public String overrideExistingMockresponseHeaders(String content, WsdlMockResponse wsdlMockResponse)
	{
		cleanExistingWsaHeaders(content);
		return addWSAddressingMockResponse(wsdlMockResponse);
	}

}
