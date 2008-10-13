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
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
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
	public static final String WSAM_NAMESPACE = "http://www.w3.org/2007/05/addressing/metadata";

	SoapVersion soapVersion;
	WsdlOperation operation;
	WsaBuilder builder;
	XmlObject xmlContentObject;
	// element to add every property to
	Element envelopeElement;
	String wsaVersionNameSpace;
	String anonymousType;
	String anonymousAddress;
	String noneAddress;
	String relationshipTypeReply;
	// used for mock response relates to if request.messageId not specified
	String unspecifiedMessage;
	String content;
	// needed for checking if ws-a already applied before
	XmlObject xmlHeaderObject;
	ArrayList<Node> headerWsaElementList;
	private final PropertyExpansionContext context;

	public WsaUtils(String content, SoapVersion soapVersion, WsdlOperation operation, PropertyExpansionContext context)
	{
		this.soapVersion = soapVersion;
		this.operation = operation;
		this.content = content;
		this.context = context;
		try
		{
			xmlContentObject = XmlObject.Factory.parse(content);
		}
		catch (Exception e)
		{
			SoapUI.logError(e);
		}
	}

	private Element addWsAddressingCommon(WsaContainer wsaContainer, boolean overrideExisting) throws XmlException
	{

		// version="2005/08" is default
		wsaVersionNameSpace = WS_A_VERSION_200508;
		if (wsaContainer.getWsaConfig().getVersion().equals(WsaVersionTypeConfig.X_200408.toString()))
		{
			wsaVersionNameSpace = WS_A_VERSION_200408;
		}
		anonymousAddress = wsaVersionNameSpace + "/anonymous";
		noneAddress = wsaVersionNameSpace + "/none";
		relationshipTypeReply = wsaVersionNameSpace + "/reply";
		unspecifiedMessage = wsaVersionNameSpace + "/unspecified";

		anonymousType = wsaContainer.getOperation().getAnonymous();
		// if optional at operation level, check policy specification on interface
		// level
		if (anonymousType.equals(AnonymousTypeConfig.OPTIONAL.toString()))
		{
			anonymousType = wsaContainer.getOperation().getInterface().getAnonymous();
		}

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

		String from = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getFrom());
		if (!StringUtils.isNullOrEmpty(from))
		{
			header = processWsaProperty(header, overrideExisting, "wsa:From", from, true);
		}
		String faultTo = PropertyExpansionUtils.expandProperties(context,wsaContainer.getWsaConfig().getFaultTo());
		if (!StringUtils.isNullOrEmpty(faultTo))
		{
			header = processWsaProperty(header, overrideExisting, "wsa:FaultTo", faultTo, true);
		}
		return header;

	}

	private Node getWsaProperty(Element header, String elementLocalName) {
		NodeList elmList = header.getElementsByTagName(elementLocalName);
		Node elm = null;
		if (elmList.getLength() > 0)
		{
			elm = elmList.item(0);
		}
		return elm;
		
	}
	private Element removeWsaProperty(boolean overrideExisting, Element header, String elementLocalName)
	{
		if (overrideExisting)
		{
			NodeList elmList = header.getElementsByTagName(elementLocalName);
			Node elm = null;
			if (elmList.getLength() > 0)
			{
				elm = elmList.item(0);
			}
			if (elm != null)
			{
				header.removeChild(elm);
			}
		}
		return header;
	}
	private Element processWsaProperty(Element header, boolean override, String elementLocalName, String wsaPropValue, boolean address) {
		boolean existsWsa = getWsaProperty(header, elementLocalName)!= null ? true: false;
		if (override)
		{
			if (existsWsa)
			{
				header = removeWsaProperty(override, header, elementLocalName);
			}
			if (address)
			{
				header.appendChild(builder.createWsaAddressChildElement(elementLocalName, envelopeElement, wsaPropValue));
			} else {
				header.appendChild(builder.createWsaChildElement(elementLocalName, envelopeElement, wsaPropValue));
			}
			
		} else if (!existsWsa)
		{
			if (address)
			{
				header.appendChild(builder.createWsaAddressChildElement(elementLocalName, envelopeElement, wsaPropValue));
			} else {
				header.appendChild(builder.createWsaChildElement(elementLocalName, envelopeElement, wsaPropValue));
			}
		}
		return header;
	}

	private Element processWsaRelatesToProperty(Element header, boolean override, String elementLocalName, String relationshipType,
			String relatesTo) {
		boolean existsWsa = getWsaProperty(header, elementLocalName)!= null ? true: false;
		if (override)
		{
			if (existsWsa)
			{
				header = removeWsaProperty(override, header, elementLocalName);
			}
			header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType,
					relatesTo));
		} else if (!existsWsa)
		{
			header.appendChild(builder.createRelatesToElement("wsa:RelatesTo", envelopeElement, relationshipType,
					relatesTo));
		}
		return header;
	}

	public String addWSAddressingRequest(WsaContainer wsaContainer)
	{
		return addWSAddressingRequest(wsaContainer, null);
	}

	public String addWSAddressingRequest(WsaContainer wsaContainer, ExtendedHttpMethod httpMethod)
	{
			return createNewWSAddressingRequest(wsaContainer, httpMethod, SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS));
	}

	private String createNewWSAddressingRequest(WsaContainer wsaContainer, ExtendedHttpMethod httpMethod, boolean override)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer, override);

			String action = PropertyExpansionUtils.expandProperties(context,wsaContainer.getWsaConfig().getAction());
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction(wsaContainer.getOperation(), false);
			}

			if (!StringUtils.isNullOrEmpty(action))
			{
				header = processWsaProperty(header, override, "wsa:Action", action, false);
			}

			String replyTo = PropertyExpansionUtils.expandProperties(context,wsaContainer.getWsaConfig().getReplyTo());
			if (AnonymousTypeConfig.REQUIRED.toString().equals(anonymousType))
			// TODO check if WsaSettings.USE_DEFAULT_REPLYTO is needed considering
			// anonymous added
			// && SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_REPLYTO))
			{
				header = processWsaProperty(header, override, "wsa:ReplyTo", anonymousAddress, true);
			}
			else if (!StringUtils.isNullOrEmpty(replyTo))
			{
				if (!(AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType) && isAnonymousAddress(replyTo,wsaVersionNameSpace)))
				{
					header = processWsaProperty(header, override, "wsa:ReplyTo", replyTo, true);
				}
			}
			else if (operation.isRequestResponse())
			{
				//for request-response replyTo is mandatory, set it to none if anonymous prohibited
				if (!AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType))
				{
					header = processWsaProperty(header, override, "wsa:ReplyTo", anonymousAddress, true);
				} else {
					header = processWsaProperty(header, override, "wsa:ReplyTo", noneAddress, true);
				}
			}

			String relatesTo = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getRelatesTo());
			String relationshipType = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getRelationshipType());
			if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo))
			{
				header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipType, relatesTo);
			}

			if (wsaContainer.getWsaConfig().isGenerateMessageId() )
			{
				String generatedMessageId = UUID.randomUUID().toString();
				header = processWsaProperty(header, override, "wsa:MessageID", generatedMessageId, false);
			} else {
				String msgId = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getMessageID());
				if (!StringUtils.isNullOrEmpty(msgId))
				{
					header = processWsaProperty(header, override, "wsa:MessageID", msgId, false);
				}
			}

			if (wsaContainer.getWsaConfig().isAddDefaultTo() )
			{
				String defaultTo = httpMethod.getURI().toString();
				header = processWsaProperty(header, override, "wsa:To", defaultTo, false);
			} else {
				String to = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getTo());
				if (!StringUtils.isNullOrEmpty(to))
				{
					header = processWsaProperty(header, override, "wsa:To", to, false);
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
	 * @param wsaContainer
	 * @return
	 */
	public String addWSAddressingMockResponse(WsaContainer wsaContainer)
	{
		return addWSAddressingMockResponse(wsaContainer, null);
	}

	public String addWSAddressingMockResponse(WsaContainer wsaContainer, WsdlMockRequest request)
	{
		return createWSAddressingMockResponse(wsaContainer, request, SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS));
	}

	private String createWSAddressingMockResponse(WsaContainer wsaContainer, WsdlMockRequest request, boolean override)
	{
		try
		{
			Element header = addWsAddressingCommon(wsaContainer, override);

			String action = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getAction());
			if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_ACTION) && StringUtils.isNullOrEmpty(action))
			{
				action = WsdlUtils.getDefaultWsaAction(wsaContainer.getOperation(), true);
			}
			if (!StringUtils.isNullOrEmpty(action))
			{
				header = processWsaProperty(header, override, "wsa:Action", action, false);
			}

			if (AnonymousTypeConfig.REQUIRED.toString().equals(anonymousType))
			{
				header = processWsaProperty(header, override, "wsa:ReplyTo", anonymousAddress, true);
			}
			else
			{
				String replyTo = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getReplyTo());
				if (!StringUtils.isNullOrEmpty(replyTo))
				{
					header = processWsaProperty(header, override, "wsa:ReplyTo", replyTo, true);
				}
			}

			Element requestHeader = null;
			if (request != null)
			{
				XmlObject requestXmlObject = request.getRequestXmlObject();
				
				String requestWsaVersionNameSpace = WsaValidator.getWsaVersion(requestXmlObject, request.getSoapVersion());
				
				requestHeader = (Element) SoapUtils.getHeaderElement(requestXmlObject, request.getSoapVersion(), true)
						.getDomNode();

				// request.messageId = mockResponse.relatesTo so get it
				Element msgNode = XmlUtils.getFirstChildElementNS(requestHeader, requestWsaVersionNameSpace, "MessageID");
				String requestMessageId = null;
				if (msgNode != null)
				{
					requestMessageId = XmlUtils.getElementText(msgNode);
				}

				String relationshipType = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getRelationshipType());
				if (!StringUtils.isNullOrEmpty(relationshipType))
				{
					if (!StringUtils.isNullOrEmpty(requestMessageId))
					{
						header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipType, requestMessageId);
					}
					else if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATES_TO))
					{
						// if request.messageId not specified use unspecifiedMessage
						header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipType, unspecifiedMessage);
					}
				}
				else if (wsaContainer instanceof WsdlMockResponse)
				{
					if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE))
					{
						if (!StringUtils.isNullOrEmpty(requestMessageId))
						{
							header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipTypeReply, requestMessageId);
						}
						else if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATES_TO))
						{
							// if request.messageId not specified use
							// unspecifiedMessage
							header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipTypeReply, unspecifiedMessage);
						}
					}
				}

				// request.replyTo = mockResponse.to so get it
				Element replyToNode = XmlUtils.getFirstChildElementNS(requestHeader, requestWsaVersionNameSpace, "ReplyTo");
				String requestReplyToValue = null;
				if (replyToNode != null)
				{
					Element replyToAddresseNode = XmlUtils.getFirstChildElementNS(replyToNode, requestWsaVersionNameSpace,
							"Address");
					if (replyToAddresseNode != null)
					{
						requestReplyToValue = XmlUtils.getElementText(replyToAddresseNode);
					}
				}

				String to = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getTo());
				if (!StringUtils.isNullOrEmpty(to))
				{
					if (!(AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType) && isAnonymousAddress(to,wsaVersionNameSpace)))
					{
						header = processWsaProperty(header, override, "wsa:To", to, false);
					}
				}
				else
				{
					// if to not specified but wsa:to mandatory get default value
					if (!StringUtils.isNullOrEmpty(requestReplyToValue))
					{
						// if anonymous prohibited than default anonymous should not
						// be added
						if (!(AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType) && isAnonymousAddress(requestReplyToValue,wsaVersionNameSpace)))
						{
							header = processWsaProperty(header, override, "wsa:To", requestReplyToValue, false);
						}
					}
				}
			}
			else
			{
				String to = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getTo());
				if (!StringUtils.isNullOrEmpty(to))
				{
//					header = removeWsaProperty(override, header, "wsa:To");
//					header.appendChild(builder.createWsaAddressChildElement("wsa:To", envelopeElement, to));
					header = processWsaProperty(header, override, "wsa:To", to, false);
				}

				String relationshipType = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getRelationshipType());
				String relatesTo = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getRelatesTo());
				if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo))
				{
					header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipType, relatesTo);
				}
				else if (wsaContainer instanceof WsdlMockResponse)
				{
					if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE))
					{
						if (!StringUtils.isNullOrEmpty(relatesTo))
						{
							header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipTypeReply, relatesTo);
						} else if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATES_TO))
						{
							header = processWsaRelatesToProperty(header, override, "wsa:RelatesTo", relationshipTypeReply, unspecifiedMessage);
						}
					}
				}

			}

			if (wsaContainer.getWsaConfig().isGenerateMessageId())
			{
				String generatedMessageId = UUID.randomUUID().toString();
				header = processWsaProperty(header, override, "wsa:MessageID", generatedMessageId, false);
			} else {
				String msgId = PropertyExpansionUtils.expandProperties(context, wsaContainer.getWsaConfig().getMessageID());
				if (!StringUtils.isNullOrEmpty(msgId))
				{
					header = processWsaProperty(header, override, "wsa:MessageID", msgId, false);
				}
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
			Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, elementName);
			wsaElm.setAttribute("RelationshipType", relationshipType);
			Text txtElm = addToElement.getOwnerDocument().createTextNode(relatesTo);
			if (mustUnderstand != null)
			{
				wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
			}
			wsaElm.appendChild(txtElm);
			return wsaElm;
		}
	}

	public static boolean isAnonymousAddress(String address, String wsaVersionNamespace)
	{
		return (address.equals(wsaVersionNamespace + "/anonymous")) ? true : false;
	}

	public static boolean isNoneAddress(String address, String wsaVersionNamespace)
	{
		return (address.equals(wsaVersionNamespace + "/none")) ? true : false;
	}

}
