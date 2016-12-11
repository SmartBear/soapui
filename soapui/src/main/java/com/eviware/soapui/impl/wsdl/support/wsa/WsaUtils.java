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

package com.eviware.soapui.impl.wsdl.support.wsa;

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
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * WS Addressing-related utility-methods..
 * <p/>
 * ws-a Action element is created according to rules specified at {@link}
 * http://www.w3.org/TR/2007/REC-ws-addr-metadata-20070904/#actioninwsdl {@link}
 * http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#actioninwsdl for
 * explicitly using soap action check Global WS-A settings Soap action
 * overides...
 *
 * @author dragica.soldo
 */

public class WsaUtils {

    public static final String WS_A_NAMESPACE_200508 = "http://www.w3.org/2005/08/addressing";
    public static final String WS_A_NAMESPACE_200408 = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    /*
     * see http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#actioninwsdl
     */
    public static final String WS_A_NAMESPACE_200602 = "http://www.w3.org/2006/02/addressing/wsdl";
    public static final String WS_A_NAMESPACE_200605 = "http://www.w3.org/2006/05/addressing/wsdl";
    /*
     * see http://www.w3.org/TR/2007/REC-ws-addr-metadata-20070904/#actioninwsdl
     */
    public static final String WS_A_NAMESPACE_200705 = "http://www.w3.org/2007/05/addressing/metadata";

    public static final String[] wsaNamespaces = {WS_A_NAMESPACE_200705, WS_A_NAMESPACE_200508, WS_A_NAMESPACE_200408,
            WS_A_NAMESPACE_200605, WS_A_NAMESPACE_200602};

    SoapVersion soapVersion;
    WsdlOperation operation;
    WsaBuilder builder;
    XmlObject xmlContentObject;
    // element to add every property to
    Element envelopeElement;
    String wsaVersionNameSpace;

    String wsaPrefix = null;
    String wsaVersionNamespaceOld = null;

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

    public WsaUtils(String content, SoapVersion soapVersion, WsdlOperation operation, PropertyExpansionContext context) {
        this.soapVersion = soapVersion;
        this.operation = operation;
        this.content = content;
        this.context = context;
        try {
            // xmlContentObject = XmlObject.Factory.parse( content );
            xmlContentObject = XmlUtils.createXmlObject(content);
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    private Element getHeader(WsaContainer wsaContainer) throws XmlException {

        // version="2005/08" is default
        wsaVersionNameSpace = WS_A_NAMESPACE_200508;
        if (wsaContainer.getWsaConfig().getVersion().equals(WsaVersionTypeConfig.X_200408.toString())) {
            wsaVersionNameSpace = WS_A_NAMESPACE_200408;
        }
        anonymousAddress = wsaVersionNameSpace + "/anonymous";
        noneAddress = wsaVersionNameSpace + "/none";
        relationshipTypeReply = wsaVersionNameSpace + "/reply";
        unspecifiedMessage = wsaVersionNameSpace + "/unspecified";

        anonymousType = wsaContainer.getOperation().getAnonymous();
        // if optional at operation level, check policy specification on
        // interface
        // level
        if (anonymousType.equals(AnonymousTypeConfig.OPTIONAL.toString())) {
            anonymousType = wsaContainer.getOperation().getInterface().getAnonymous();
        }

        Element header = (Element) SoapUtils.getHeaderElement(xmlContentObject, soapVersion, true).getDomNode();

        wsaPrefix = XmlUtils.findPrefixForNamespace(header, WsaUtils.WS_A_NAMESPACE_200508);
        if (wsaPrefix != null) {
            wsaVersionNamespaceOld = WsaUtils.WS_A_NAMESPACE_200508;
        } else {
            wsaPrefix = XmlUtils.findPrefixForNamespace(header, WsaUtils.WS_A_NAMESPACE_200408);
            if (wsaPrefix != null) {
                wsaVersionNamespaceOld = WsaUtils.WS_A_NAMESPACE_200408;
            } else {
                wsaPrefix = XmlUtils.findPrefixForNamespace(header, WsaUtils.WS_A_NAMESPACE_200508);
                if (wsaPrefix != null) {
                    wsaVersionNamespaceOld = WsaUtils.WS_A_NAMESPACE_200508;
                } else {
                    wsaPrefix = XmlUtils.findPrefixForNamespace(header, WsaUtils.WS_A_NAMESPACE_200408);
                    if (wsaPrefix != null) {
                        wsaVersionNamespaceOld = WsaUtils.WS_A_NAMESPACE_200408;
                    } else {
                        wsaPrefix = "wsa";
                    }
                }
            }
        }
        XmlObject[] envelope = xmlContentObject.selectChildren(soapVersion.getEnvelopeQName());
        envelopeElement = (Element) envelope[0].getDomNode();

        Boolean mustUnderstand = null;
        if (wsaContainer.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.FALSE.toString())) {
            mustUnderstand = false;
        } else if (wsaContainer.getWsaConfig().getMustUnderstand().equals(MustUnderstandTypeConfig.TRUE.toString())) {
            mustUnderstand = true;
        }

        builder = new WsaBuilder(wsaVersionNameSpace, mustUnderstand);

        return header;

    }

    private Node getWsaProperty(Element header, String elementLocalName) {
        NodeList elmList = header.getElementsByTagName(elementLocalName);
        // NodeList elmList = header.getElementsByTagNameNS(namespaceURI,
        // localName);
        Node elm = null;
        if (elmList.getLength() > 0) {
            elm = elmList.item(0);
        }
        return elm;

    }

    private Element removeWsaProperty(boolean overrideExisting, Element header, String elementLocalName) {
        if (overrideExisting) {
            NodeList elmList = header.getElementsByTagName(elementLocalName);
            Node elm = null;
            if (elmList.getLength() > 0) {
                elm = elmList.item(0);
            }
            if (elm != null) {
                header.removeChild(elm);
            }
        }
        return header;
    }

    /**
     * Processing ws-a property
     *
     * @param header           - header element to add wsa to
     * @param override         - indicates if existing parameters should be overriden with new
     *                         values
     * @param elementLocalName - property string to add, for instance: any:Action, or
     *                         any:ReplyTo
     * @param wsaPropValue     - wsa property value, inserted in input box, or default
     *                         generated
     * @param address          - indicates if property is an endpoint reference, i.e. if it has
     *                         <address> tag inside itself
     * @param refParamsContent - the content of ReferenceParameters for specific endpoint
     *                         reference, null if property is an absolute IRI
     */
    private Element processWsaProperty(Element header, boolean override, String elementLocalName, String wsaPropValue,
                                       boolean address, String refParamsContent) {
        boolean existsWsa = getWsaProperty(header, elementLocalName) != null ? true : false;
        if (override) {
            if (existsWsa) {
                header = removeWsaProperty(override, header, elementLocalName);
            }
            if (address) {
                header.appendChild(builder.createWsaAddressChildElement(elementLocalName, envelopeElement, wsaPropValue,
                        refParamsContent));
            } else {
                header.appendChild(builder.createWsaChildElement(elementLocalName, envelopeElement, wsaPropValue));
            }

        } else if (!existsWsa) {
            if (address) {
                header.appendChild(builder.createWsaAddressChildElement(elementLocalName, envelopeElement, wsaPropValue,
                        refParamsContent));
            } else {
                header.appendChild(builder.createWsaChildElement(elementLocalName, envelopeElement, wsaPropValue));
            }
        }
        return header;
    }

    private Element processWsaProperty(Element header, boolean override, String elementLocalName, String wsaPropValue,
                                       boolean address) {
        return processWsaProperty(header, override, elementLocalName, wsaPropValue, address, null);
    }

    private Element processWsaRelatesToProperty(Element header, boolean override, String elementLocalName,
                                                String relationshipType, String relatesTo) {
        boolean existsWsa = getWsaProperty(header, elementLocalName) != null ? true : false;
        if (override) {
            if (existsWsa) {
                header = removeWsaProperty(override, header, elementLocalName);
            }
            header.appendChild(builder.createRelatesToElement(wsaPrefix + ":RelatesTo", envelopeElement,
                    relationshipType, relatesTo));
        } else if (!existsWsa) {
            header.appendChild(builder.createRelatesToElement(wsaPrefix + ":RelatesTo", envelopeElement,
                    relationshipType, relatesTo));
        }
        return header;
    }

    public String removeWSAddressing(WsaContainer wsaContainer) {
        try {
            Element header = getHeader(wsaContainer);
            NodeList headerProps = XmlUtils.getChildElements(header);
            for (int i = 0; i < headerProps.getLength(); i++) {
                Node headerChild = headerProps.item(i);
                if (headerChild.getNamespaceURI().equals(wsaVersionNameSpace)) {
                    header.removeChild(headerChild);
                }
            }
            content = xmlContentObject.xmlText();
        } catch (XmlException e) {
            SoapUI.logError(e);
        }
        return content;
    }

    public String addWSAddressingRequest(WsaContainer wsaContainer) {
        return addWSAddressingRequest(wsaContainer, null);
    }

    public String addWSAddressingRequest(WsaContainer wsaContainer, ExtendedHttpMethod httpMethod) {
        return createNewWSAddressingRequest(wsaContainer, httpMethod,
                SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS));
    }

    private String createNewWSAddressingRequest(WsaContainer wsaContainer, ExtendedHttpMethod httpMethod,
                                                boolean override) {
        try {
            Element header = getHeader(wsaContainer);

            if (override || wsaVersionNamespaceOld == null) {
                header.setAttribute("xmlns:" + wsaPrefix, wsaVersionNameSpace);
            }

            String action = null;
            if (wsaContainer.getWsaConfig().isAddDefaultAction()) {
                action = WsdlUtils.getDefaultWsaAction(wsaContainer.getOperation(), false);
            } else {
                action = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getAction());
            }
            if (!StringUtils.isNullOrEmpty(action)) {
                header = processWsaProperty(header, override, wsaPrefix + ":Action", action, false);
            }

            String replyTo = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getReplyTo());
            String replyToRefParams = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                    .getReplyToRefParams());
            if (AnonymousTypeConfig.REQUIRED.toString().equals(anonymousType))
            // TODO check if WsaSettings.USE_DEFAULT_REPLYTO is needed
            // considering
            // anonymous added
            // &&
            // SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_REPLYTO))
            {
                header = processWsaProperty(header, override, wsaPrefix + ":ReplyTo", anonymousAddress, true,
                        replyToRefParams);
            } else if (!StringUtils.isNullOrEmpty(replyTo)) {
                if (!(AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType) && isAnonymousAddress(replyTo,
                        wsaVersionNameSpace))) {
                    header = processWsaProperty(header, override, wsaPrefix + ":ReplyTo", replyTo, true, replyToRefParams);
                }
            }
            // TODO removed option for the purpose of wstf testing(echo 1.6 for
            // instance needs to have faultTo and no replyTo)
            // see how to handle this if needed (commented by Dragica 20.10.08.)
            // else if (operation.isRequestResponse())
            // {
            // //for request-response replyTo is mandatory, set it to none if
            // anonymous prohibited
            // if
            // (!AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType))
            // {
            // header = processWsaProperty(header, override, wsaPrefix +
            // ":ReplyTo", anonymousAddress, true, replyToRefParams);
            // } else {
            // header = processWsaProperty(header, override, wsaPrefix +
            // ":ReplyTo", noneAddress, true, replyToRefParams);
            // }
            // }

            String from = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getFrom());
            if (!StringUtils.isNullOrEmpty(from)) {
                header = processWsaProperty(header, override, wsaPrefix + ":From", from, true);
            }
            String faultTo = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getFaultTo());
            String faultToRefParams = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                    .getFaultToRefParams());
            if (!StringUtils.isNullOrEmpty(faultTo)) {
                header = processWsaProperty(header, override, wsaPrefix + ":FaultTo", faultTo, true, faultToRefParams);
            }

            String relatesTo = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getRelatesTo());
            String relationshipType = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                    .getRelationshipType());
            if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo)) {
                header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo", relationshipType,
                        relatesTo);
            }

            if (wsaContainer.getWsaConfig().isGenerateMessageId()) {
                String generatedMessageId = "uuid:" + UUID.randomUUID().toString();
                header = processWsaProperty(header, override, wsaPrefix + ":MessageID", generatedMessageId, false);
            } else {
                String msgId = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getMessageID());
                if (!StringUtils.isNullOrEmpty(msgId)) {
                    header = processWsaProperty(header, override, wsaPrefix + ":MessageID", msgId, false);
                }
            }

            if (httpMethod != null && wsaContainer.getWsaConfig().isAddDefaultTo()) {
                String defaultTo = httpMethod.getURI().toString();
                header = processWsaProperty(header, override, wsaPrefix + ":To", defaultTo, false);
            } else {
                String to = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getTo());
                if (!StringUtils.isNullOrEmpty(to)) {
                    header = processWsaProperty(header, override, wsaPrefix + ":To", to, false);
                }
            }
            content = xmlContentObject.xmlText();
        } catch (Exception e) {
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
    public String addWSAddressingMockResponse(WsaContainer wsaContainer) {
        return addWSAddressingMockResponse(wsaContainer, null);
    }

    public String addWSAddressingMockResponse(WsaContainer wsaContainer, WsdlMockRequest request) {
        return createWSAddressingMockResponse(wsaContainer, request,
                SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS));
    }

    private String createWSAddressingMockResponse(WsaContainer wsaContainer, WsdlMockRequest request, boolean override) {
        try {
            Element header = getHeader(wsaContainer);

            if (override || wsaVersionNamespaceOld == null) {
                header.setAttribute("xmlns:" + wsaPrefix, wsaVersionNameSpace);
            }

            String action = null;
            if (wsaContainer.getWsaConfig().isAddDefaultAction()) {
                action = WsdlUtils.getDefaultWsaAction(wsaContainer.getOperation(), true);
            } else {
                action = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getAction());
            }
            if (!StringUtils.isNullOrEmpty(action)) {
                header = processWsaProperty(header, override, wsaPrefix + ":Action", action, false);
            }

            if (AnonymousTypeConfig.REQUIRED.toString().equals(anonymousType)) {
                header = processWsaProperty(header, override, wsaPrefix + ":ReplyTo", anonymousAddress, true);
            } else {
                String replyTo = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getReplyTo());
                String replyToRefParams = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                        .getReplyToRefParams());
                if (!StringUtils.isNullOrEmpty(replyTo)) {
                    header = processWsaProperty(header, override, wsaPrefix + ":ReplyTo", replyTo, true, replyToRefParams);
                }
            }

            Element requestHeader = null;
            if (request != null) {
                XmlObject requestXmlObject = request.getRequestXmlObject();

                String requestWsaVersionNameSpace = WsaValidator.getWsaVersion(requestXmlObject, request.getSoapVersion());

                requestHeader = (Element) SoapUtils.getHeaderElement(requestXmlObject, request.getSoapVersion(), true)
                        .getDomNode();

                // request.messageId = mockResponse.relatesTo so get it
                Element msgNode = XmlUtils.getFirstChildElementNS(requestHeader, requestWsaVersionNameSpace, "MessageID");
                String requestMessageId = null;
                if (msgNode != null) {
                    requestMessageId = XmlUtils.getElementText(msgNode);
                }

                String from = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getFrom());
                if (!StringUtils.isNullOrEmpty(from)) {
                    header = processWsaProperty(header, override, wsaPrefix + ":From", from, true);
                }
                String faultTo = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getFaultTo());
                String faultToRefParams = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                        .getFaultToRefParams());
                if (!StringUtils.isNullOrEmpty(faultTo)) {
                    header = processWsaProperty(header, override, wsaPrefix + ":FaultTo", faultTo, true, faultToRefParams);
                }

                String relationshipType = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                        .getRelationshipType());
                if (!StringUtils.isNullOrEmpty(relationshipType)) {
                    if (!StringUtils.isNullOrEmpty(requestMessageId)) {
                        header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo", relationshipType,
                                requestMessageId);
                    } else if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATES_TO)) {
                        // if request.messageId not specified use
                        // unspecifiedMessage
                        header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo", relationshipType,
                                unspecifiedMessage);
                    }
                } else if (wsaContainer instanceof WsdlMockResponse) {
                    if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE)) {
                        if (!StringUtils.isNullOrEmpty(requestMessageId)) {
                            header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo",
                                    relationshipTypeReply, requestMessageId);
                        } else if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATES_TO)) {
                            // if request.messageId not specified use
                            // unspecifiedMessage
                            header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo",
                                    relationshipTypeReply, unspecifiedMessage);
                        }
                    }
                }

                // request.replyTo = mockResponse.to so get it
                Element replyToNode = XmlUtils
                        .getFirstChildElementNS(requestHeader, requestWsaVersionNameSpace, "ReplyTo");
                String requestReplyToValue = null;
                if (replyToNode != null) {
                    Element replyToAddresseNode = XmlUtils.getFirstChildElementNS(replyToNode, requestWsaVersionNameSpace,
                            "Address");
                    if (replyToAddresseNode != null) {
                        requestReplyToValue = XmlUtils.getElementText(replyToAddresseNode);
                    }
                }

                String to = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getTo());
                if (!StringUtils.isNullOrEmpty(to)) {
                    if (!(AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType) && isAnonymousAddress(to,
                            wsaVersionNameSpace))) {
                        header = processWsaProperty(header, override, wsaPrefix + ":To", to, false);
                    }
                } else {
                    // if to not specified but wsa:to mandatory get default
                    // value
                    if (!StringUtils.isNullOrEmpty(requestReplyToValue)) {
                        // if anonymous prohibited than default anonymous should
                        // not
                        // be added
                        if (!(AnonymousTypeConfig.PROHIBITED.toString().equals(anonymousType) && isAnonymousAddress(
                                requestReplyToValue, wsaVersionNameSpace))) {
                            header = processWsaProperty(header, override, wsaPrefix + ":To", requestReplyToValue, false);
                        }
                    }
                }
            } else {
                String to = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getTo());
                if (!StringUtils.isNullOrEmpty(to)) {
                    // header = removeWsaProperty(override, header, wsaPrefix +
                    // ":To");
                    // header.appendChild(builder.createWsaAddressChildElement(wsaPrefix
                    // + ":To", envelopeElement, to));
                    header = processWsaProperty(header, override, wsaPrefix + ":To", to, false);
                }

                String relationshipType = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig()
                        .getRelationshipType());
                String relatesTo = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getRelatesTo());
                if (!StringUtils.isNullOrEmpty(relationshipType) && !StringUtils.isNullOrEmpty(relatesTo)) {
                    header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo", relationshipType,
                            relatesTo);
                } else if (wsaContainer instanceof WsdlMockResponse) {
                    if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE)) {
                        if (!StringUtils.isNullOrEmpty(relatesTo)) {
                            header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo",
                                    relationshipTypeReply, relatesTo);
                        } else if (SoapUI.getSettings().getBoolean(WsaSettings.USE_DEFAULT_RELATES_TO)) {
                            header = processWsaRelatesToProperty(header, override, wsaPrefix + ":RelatesTo",
                                    relationshipTypeReply, unspecifiedMessage);
                        }
                    }
                }

            }

            if (wsaContainer.getWsaConfig().isGenerateMessageId()) {
                String generatedMessageId = "uuid:" + UUID.randomUUID().toString();
                header = processWsaProperty(header, override, wsaPrefix + ":MessageID", generatedMessageId, false);
            } else {
                String msgId = PropertyExpander.expandProperties(context, wsaContainer.getWsaConfig().getMessageID());
                if (!StringUtils.isNullOrEmpty(msgId)) {
                    header = processWsaProperty(header, override, wsaPrefix + ":MessageID", msgId, false);
                }
            }

            content = xmlContentObject.xmlText();
        } catch (XmlException e) {
            SoapUI.logError(e);
        }

        return content;
    }

    public class WsaBuilder {
        private final String wsaVersionNameSpace;
        private final Boolean mustUnderstand;

        public WsaBuilder(String wsaVersionNameSpace, Boolean mustUnderstand) {
            // TODO Auto-generated constructor stub
            this.wsaVersionNameSpace = wsaVersionNameSpace;
            this.mustUnderstand = mustUnderstand;
        }

        public Element createWsaChildElement(String elementName, Element addToElement, String wsaProperty) {
            Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, elementName);
            Text txtElm = addToElement.getOwnerDocument().createTextNode(wsaProperty);
            if (mustUnderstand != null) {
                wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
            }
            wsaElm.appendChild(txtElm);
            return wsaElm;
        }

        public Element createWsaAddressChildElement(String elementName, Element addToElement, String wsaProperty,
                                                    String refParamsContent) {
            Document document = addToElement.getOwnerDocument();
            Element wsAddressElm = document.createElementNS(wsaVersionNameSpace, wsaPrefix + ":Address");
            Element wsaElm = document.createElementNS(wsaVersionNameSpace, elementName);
            Text propertyContent = document.createTextNode(wsaProperty);
            if (mustUnderstand != null) {
                wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
            }
            wsAddressElm.appendChild(propertyContent);
            wsaElm.appendChild(wsAddressElm);

            try {
                if (refParamsContent != null) {
                    // Text propertyRefParamsContent =
                    // document.createTextNode(refParamsContent);
                    Element refParamsElm = document
                            .createElementNS(wsaVersionNameSpace, wsaPrefix + ":ReferenceParameters");
                    refParamsContent = "<dummy>" + refParamsContent + "</dummy>";
                    Node xx = document.importNode(XmlUtils.parseXml(refParamsContent).getDocumentElement(), true);
                    NodeList xxList = xx.getChildNodes();

                    // refParamsElm.appendChild(propertyRefParamsContent);
                    for (int i = 0; i < xxList.getLength(); i++) {
                        refParamsElm.appendChild(xxList.item(i));
                    }
                    wsaElm.appendChild(refParamsElm);
                }
            } catch (DOMException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return wsaElm;
        }

        public Element createRelatesToElement(String elementName, Element addToElement, String relationshipType,
                                              String relatesTo) {
            Element wsaElm = addToElement.getOwnerDocument().createElementNS(wsaVersionNameSpace, elementName);
            wsaElm.setAttribute("RelationshipType", relationshipType);
            Text txtElm = addToElement.getOwnerDocument().createTextNode(relatesTo);
            if (mustUnderstand != null) {
                wsaElm.setAttributeNS(soapVersion.getEnvelopeNamespace(), "mustUnderstand", mustUnderstand ? "1" : "0");
            }
            wsaElm.appendChild(txtElm);
            return wsaElm;
        }
    }

    public static boolean isAnonymousAddress(String address, String wsaVersionNamespace) {
        return (address.equals(wsaVersionNamespace + "/anonymous")) ? true : false;
    }

    public static boolean isNoneAddress(String address, String wsaVersionNamespace) {
        return (address.equals(wsaVersionNamespace + "/none")) ? true : false;
    }

    public static String getNamespace(String Version) {
        if (Version.equals(WsaVersionTypeConfig.X_200408.toString())) {
            return WS_A_NAMESPACE_200408;
        }
        return WS_A_NAMESPACE_200508;
    }
}
