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

import java.util.ArrayList;
import java.util.Iterator;
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
	XmlObject xmlContentObject;
	// element to add every property to
	Element envelopeElement;
	String wsaVersionNameSpace;
	String replyToAnonimus;
	String relatesToReply;
	String content;
	// needed for checking if ws-a already applied before
	XmlObject xmlHeaderObject;
	ArrayList<Node> headerWsaElementList;

	public WsaUtils(String content, SoapVersion soapVersion, Operation operation)
	{
		this.soapVersion = soapVersion;
		this.operation = operation;
		this.content = content;
		try
		{
			xmlContentObject = XmlObject.Factory.parse(content);
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

		Element header = (Element) SoapUtils.getHeaderElement(xmlContentObject, soapVersion, true).getDomNode();

		header.setAttribute("xmlns:wsa", wsaVersionNameSpace);

		XmlObject[] envelope = xmlContentObject.selectChildren(soapVersion.getEnvelopeQName());
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

	public String addWSAddressingRequest(WsaContainer wsaContainer)
	{
		return addWSAddressingRequest(wsaContainer, null);
	}

	public String addWSAddressingRequest (WsaContainer wsaContainer, ExtendedHttpMethod httpMethod) {
		// if ws-a already exists and globally set not to be overriden return existing content
		boolean hasWsaAlready = getExistingWsAddressing(content);
		if (hasWsaAlready)
		{
			if (SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS))
			{
				cleanExistingWsaHeaders(content);
				return createNewWSAddressingRequest(wsaContainer, httpMethod);
			} else {
				return content;
			}
		} else {
			return createNewWSAddressingRequest(wsaContainer, httpMethod);
		}
		
	}
	private String createNewWSAddressingRequest(WsaContainer wsaContainer, ExtendedHttpMethod httpMethod)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer);

			String action = wsaContainer.getWsaConfig().getAction();
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction(wsaContainer.getOperation(), false);
			}

			if (!StringUtils.isNullOrEmpty(action))
			{
				header.appendChild(builder.createWsaChildElement("wsa:Action", envelopeElement, action));
				wsaContainer.getWsaConfig().setAction(action);
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
					header
							.appendChild(builder.createWsaAddressChildElement("wsa:ReplyTo", envelopeElement, replyToAnonimus));
					wsaContainer.getWsaConfig().setReplyTo(replyToAnonimus);
				}
			}

			String relatesTo = wsaContainer.getWsaConfig().getRelatesTo();
			String relationshipType = wsaContainer.getWsaConfig().getRelationshipType();
			if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo))
			{
				header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType,
						relatesTo));
			}

			String msgId = wsaContainer.getWsaConfig().getMessageID();
			if (!StringUtils.isNullOrEmpty(msgId))
			{
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, msgId));
			}
			else if (operation.isRequestResponse() && SoapUI.getSettings().getBoolean(WsaSettings.GENERATE_MESSAGE_ID))
			{
				// if msgId not specified but wsa:msgId mandatory create one
				String generatedMessageId = UUID.randomUUID().toString();
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, generatedMessageId ));
				wsaContainer.getWsaConfig().setMessageID(generatedMessageId);
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
					String defaultTo = httpMethod.getURI().toString();
					header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, defaultTo ));
					wsaContainer.getWsaConfig().setTo(defaultTo);
				}
			}

			content = xmlContentObject.xmlText();
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
		}

		return content;
	}

	/**
	 * Adds ws-a headers to mock response from editor-menu in this case there is
	 * no request included and only values from ws-a inspector, if any, are used
	 * 
	 * @param content
	 * @param wsaContainer
	 * @return
	 */
	public String addWSAddressingMockResponse(WsaContainer wsaContainer)
	{
		return addWSAddressingMockResponse(wsaContainer, null);
	}

	public String addWSAddressingMockResponse (WsaContainer wsaContainer, WsdlMockRequest request) {
		// if ws-a already exists and globally set not to be overriden return existing content
		boolean hasWsaAlready = getExistingWsAddressing(content);
		if (hasWsaAlready)
		{
			if (SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS))
			{
				cleanExistingWsaHeaders(content);
				return createNewWSAddressingMockResponse(wsaContainer, request);
			} else {
				return content;
			}
		} else {
			return createNewWSAddressingMockResponse(wsaContainer, request);
		}
		
	}
	private String createNewWSAddressingMockResponse(WsaContainer wsaContainer, WsdlMockRequest request)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer);

			String action = wsaContainer.getWsaConfig().getAction();
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction(wsaContainer.getOperation(), true);
			}
			if (!StringUtils.isNullOrEmpty(action))
			{
				header.appendChild(builder.createWsaChildElement("wsa:Action", envelopeElement, action));
				wsaContainer.getWsaConfig().setAction(action);
			}

			String replyTo = wsaContainer.getWsaConfig().getReplyTo();
			if (!StringUtils.isNullOrEmpty(replyTo))
			{
				header.appendChild(builder.createWsaAddressChildElement("wsa:ReplyTo", envelopeElement, replyTo));
			}

			Element requestHeader = null;
			if (request != null)
			{
				XmlObject requestXmlObject = request.getRequestXmlObject();
				requestHeader = (Element) SoapUtils.getHeaderElement(requestXmlObject, request.getSoapVersion(), true)
						.getDomNode();

				// request.messageId = mockResponse.relatesTo so get it
				Element msgNode = XmlUtils.getFirstChildElementNS(requestHeader, wsaVersionNameSpace, "MessageID");
				String requestMessageId = null;
				if (msgNode != null)
				{
					requestMessageId = XmlUtils.getElementText(msgNode);
				}

				String relationshipType = wsaContainer.getWsaConfig().getRelationshipType();
				if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(requestMessageId))
				{
					header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType,
							requestMessageId));
					wsaContainer.getWsaConfig().setRelatesTo(requestMessageId);
				}
				else if (wsaContainer instanceof WsdlMockResponse)
				{
					if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE))
					{
						if (!StringUtils.isNullOrEmpty(requestMessageId))
						{
							header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement,
									relatesToReply, requestMessageId));
							wsaContainer.getWsaConfig().setRelationshipType(relatesToReply);
							wsaContainer.getWsaConfig().setRelatesTo(requestMessageId);
						}
					}
				}

				// request.replyTo = mockResponse.to so get it
				Element replyToNode = XmlUtils.getFirstChildElementNS(requestHeader, wsaVersionNameSpace, "ReplyTo");
				String requestReplyToValue = null;
				if (replyToNode != null)
				{
					Element replyToAddresseNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaVersionNameSpace,
							"Address");
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
						header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement,
								requestReplyToValue));
						wsaContainer.getWsaConfig().setTo(requestReplyToValue);
					}
				}
			}
			else
			{
				String to = wsaContainer.getWsaConfig().getTo();
				if (!StringUtils.isNullOrEmpty(to))
				{
					header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, to));
				}

				String relationshipType = wsaContainer.getWsaConfig().getRelationshipType();
				String relatesTo = wsaContainer.getWsaConfig().getRelatesTo();
				if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo))
				{
					header.appendChild(builder
							.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType, relatesTo));
				}
				else if (wsaContainer instanceof WsdlMockResponse)
				{
					if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE) && !StringUtils.isNullOrEmpty(relatesTo))
					{
						header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relatesToReply,
								relatesTo));
					}
				}

			}

			String msgId = wsaContainer.getWsaConfig().getMessageID();
			if (!StringUtils.isNullOrEmpty(msgId))
			{
				header.appendChild(builder.createWsaChildElement("wsa:MessageID", envelopeElement, msgId));
			}

			content = xmlContentObject.xmlText();
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

	private boolean getExistingWsAddressing(String content)
	{
		boolean appliedWsAddressing = false;
		try
		{
			xmlHeaderObject = (XmlObject) SoapUtils.getHeaderElement(xmlContentObject, soapVersion, true);
			String currentWsaVersionNameSpace = ((Element) xmlHeaderObject.getDomNode()).getAttribute("xmlns:wsa");
			NodeList headerElements = ((Element) xmlHeaderObject.getDomNode()).getChildNodes();
			headerWsaElementList = new ArrayList<Node>();
			for (int i = 0; i < headerElements.getLength(); i++)
			{
				Node childNode = headerElements.item(i);
				String namespaceURI = childNode.getNamespaceURI();
				if (!StringUtils.isNullOrEmpty(namespaceURI) && namespaceURI.equals(currentWsaVersionNameSpace))
				{
					headerWsaElementList.add(childNode);
				}
			}
			if (headerWsaElementList.size() > 0)
			{
				appliedWsAddressing = true;
			}
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}
		return appliedWsAddressing;
	}

	private String cleanExistingWsaHeaders(String content)
	{
		Iterator<Node> iter = headerWsaElementList.iterator();
		while (iter.hasNext())
		{
			((Element) xmlHeaderObject.getDomNode()).removeChild((Node) iter.next());

		}

		((Element) xmlHeaderObject.getDomNode()).removeAttribute("xmlns:wsa");

		content = xmlContentObject.xmlText();

		return content;
	}

}
