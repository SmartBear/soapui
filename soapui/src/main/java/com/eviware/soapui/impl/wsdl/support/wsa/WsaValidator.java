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
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.WsaAssertionConfiguration;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Validating class for WS Addressing implemented according to WSDL 1.1
 * specification
 *
 * @author dragica.soldo
 * @see {@link}http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#WSDL11MEPS
 */
public class WsaValidator {
    WsdlMessageExchange messageExchange;
    Element header;
    String wsaVersionNameSpace;
    StringBuilder cumulativeErrorMsg;
    WsaAssertionConfiguration wsaAssertionConfiguration;

    public WsaValidator(WsdlMessageExchange messageExchange, WsaAssertionConfiguration wsaAssertionConfiguration) {
        this.messageExchange = messageExchange;
        this.wsaAssertionConfiguration = wsaAssertionConfiguration;
        cumulativeErrorMsg = new StringBuilder();
    }

    public static String getWsaVersion(XmlObject contentObject, SoapVersion soapVersion) {
        String wsaVns = null;
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse( content );
            XmlObject[] envS = contentObject.selectChildren(soapVersion.getEnvelopeQName());
            Element envelope = (Element) envS[0].getDomNode();

            Element hdr = (Element) SoapUtils.getHeaderElement(contentObject, soapVersion, true).getDomNode();

            if (!hdr.hasChildNodes()) {
                return null;
            }

            String wsaNameSpace = XmlUtils.findPrefixForNamespace(hdr, WsaUtils.WS_A_NAMESPACE_200508);
            if (wsaNameSpace != null) {
                wsaVns = WsaUtils.WS_A_NAMESPACE_200508;
            } else {
                wsaNameSpace = XmlUtils.findPrefixForNamespace(hdr, WsaUtils.WS_A_NAMESPACE_200408);
                if (wsaNameSpace != null) {
                    wsaVns = WsaUtils.WS_A_NAMESPACE_200408;
                } else {
                    wsaNameSpace = XmlUtils.findPrefixForNamespace(envelope, WsaUtils.WS_A_NAMESPACE_200508);
                    if (wsaNameSpace != null) {
                        wsaVns = WsaUtils.WS_A_NAMESPACE_200508;
                    } else {
                        wsaNameSpace = XmlUtils.findPrefixForNamespace(envelope, WsaUtils.WS_A_NAMESPACE_200408);
                        if (wsaNameSpace != null) {
                            wsaVns = WsaUtils.WS_A_NAMESPACE_200408;
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (XmlException e) {
            SoapUI.logError(e);
        }
        return wsaVns;
    }

    private void validateWsAddressingCommon(String content) {
        if (wsaAssertionConfiguration.isAssertTo()) {
            Element toNode;
            if (wsaVersionNameSpace != null) {
                toNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "To");
                parseToNode(toNode);
            } else {
                toNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200508, "To");
                if (toNode == null) {
                    toNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200408, "To");
                }
                parseToNode(toNode);
            }
        }
        // if fault_to is specified check if anonymous allowed; faultTo is never
        // asserted
        Element faultToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "FaultTo");
        if (faultToNode != null) {
            Element addressNode = XmlUtils.getFirstChildElementNS(faultToNode, wsaVersionNameSpace, "Address");
            if (addressNode != null) {
                String faultToAddressValue = XmlUtils.getElementText(addressNode);
                if (!StringUtils.isNullOrEmpty(faultToAddressValue)) {
                    // check for anonymous
                    if (AnonymousTypeConfig.PROHIBITED.toString().equals(messageExchange.getOperation().getAnonymous())
                            && WsaUtils.isAnonymousAddress(faultToAddressValue, wsaVersionNameSpace)) {
                        cumulativeErrorMsg
                                .append("WS-A InvalidAddressingHeader FaultTo , Anonymous addresses are prohibited. ");
                    } else if (AnonymousTypeConfig.REQUIRED.toString().equals(
                            ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous())
                            && !(WsaUtils.isAnonymousAddress(faultToAddressValue, wsaVersionNameSpace) || WsaUtils
                            .isNoneAddress(faultToAddressValue, wsaVersionNameSpace))) {
                        cumulativeErrorMsg
                                .append("WS-A InvalidAddressingHeader FaultTo , Anonymous addresses are required. ");
                    }
                }
            }
        }

    }

    private void parseToNode(Element toNode) {
        if (toNode == null) {
            cumulativeErrorMsg.append("WS-A To property is not specified. ");
        } else {
            String toAddressValue = XmlUtils.getElementText(toNode);
            if (StringUtils.isNullOrEmpty(toAddressValue)) {
                cumulativeErrorMsg.append("WS-A To property is empty. ");
            } else {
                // check for anonymous - in case of mock response to=request.replyTo
                if (AnonymousTypeConfig.PROHIBITED.toString().equals(messageExchange.getOperation().getAnonymous())
                        && WsaUtils.isAnonymousAddress(toAddressValue, wsaVersionNameSpace)) {
                    cumulativeErrorMsg.append("WS-A InvalidAddressingHeader To , Anonymous addresses are prohibited. ");
                }
            }
        }
    }

    public void validateWsAddressingRequest() throws AssertionException, XmlException {
        String content = messageExchange.getRequestContent();
        SoapVersion soapVersion = messageExchange.getOperation().getInterface().getSoapVersion();

        // XmlObject xmlObject = XmlObject.Factory.parse( content );
        XmlObject xmlObject = XmlUtils.createXmlObject(content);
        header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();

        wsaVersionNameSpace = getWsaVersion(xmlObject, soapVersion);
        // not checking because of possibility of having wsaVersionNamespace
        // specified inside property tag itself
        // if (wsaVersionNameSpace == null)
        // {
        // throw new AssertionException( new AssertionError( "WS-A not enabled" )
        // );
        // }

        WsdlOperation operation = messageExchange.getOperation();

        if (wsaAssertionConfiguration.isAssertAction()) {
            assertProperty("Wsa:Action", "Action");
        }
        validateWsAddressingCommon(content);
        if (operation.isRequestResponse()) {
            if (wsaAssertionConfiguration.isAssertMessageId()) {
                // MessageId is Mandatory
                assertProperty("Wsa:MessageId", "MessageId");
            }
            if (wsaAssertionConfiguration.isAssertReplyTo()) {
                // ReplyTo is Mandatory
                Element replyToNode;
                String currentTagWsaNs;
                if (!StringUtils.isNullOrEmpty(wsaVersionNameSpace)) {
                    replyToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "ReplyTo");
                    parseReplyToNode(replyToNode, wsaVersionNameSpace);
                } else {
                    replyToNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200508, "ReplyTo");
                    currentTagWsaNs = WsaUtils.WS_A_NAMESPACE_200508;
                    if (replyToNode == null) {
                        replyToNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200408, "ReplyTo");
                        currentTagWsaNs = WsaUtils.WS_A_NAMESPACE_200408;
                    }
                    parseReplyToNode(replyToNode, currentTagWsaNs);
                }
            }
        }
        String cumulativeError = cumulativeErrorMsg.toString();
        if (!StringUtils.isNullOrEmpty(cumulativeError)) {
            throw new AssertionException(new AssertionError(cumulativeError));
        }
    }

    private void parseReplyToNode(Element replyToNode, String wsaNsStr) {
        if (replyToNode == null) {
            cumulativeErrorMsg.append("WS-A ReplyTo property is not specified. ");
        } else {
            Element addressNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaNsStr, "Address");
            if (addressNode == null) {
                cumulativeErrorMsg.append("WS-A ReplyTo Address property is not specified. ");
            } else {
                String replyToAddressValue = XmlUtils.getElementText(addressNode);
                if (StringUtils.isNullOrEmpty(replyToAddressValue)) {
                    cumulativeErrorMsg.append("WS-A ReplyTo Address property is empty. ");
                } else {
                    // check for anonymous
                    if (AnonymousTypeConfig.PROHIBITED.toString().equals(
                            ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous())
                            && WsaUtils.isAnonymousAddress(replyToAddressValue, wsaNsStr)) {
                        cumulativeErrorMsg
                                .append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are prohibited. ");
                    } else if (AnonymousTypeConfig.REQUIRED.toString().equals(
                            ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous())
                            && !(WsaUtils.isAnonymousAddress(replyToAddressValue, wsaNsStr) || WsaUtils.isNoneAddress(
                            replyToAddressValue, wsaNsStr))) {
                        cumulativeErrorMsg
                                .append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are required. ");
                    }
                }
            }
        }
    }

    public void validateWsAddressingResponse() throws AssertionException, XmlException {
        String content = messageExchange.getResponseContent();
        SoapVersion soapVersion = messageExchange.getOperation().getInterface().getSoapVersion();

        // XmlObject requestXmlObject = XmlObject.Factory.parse(
        // messageExchange.getRequestContent() );
        XmlObject requestXmlObject = XmlUtils.createXmlObject(messageExchange.getRequestContent());
        // XmlObject xmlObject = XmlObject.Factory.parse( content );
        XmlObject xmlObject = XmlUtils.createXmlObject(content);
        header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();

        wsaVersionNameSpace = getWsaVersion(xmlObject, soapVersion);

        if (wsaAssertionConfiguration.isAssertAction()) {
            String defaultWsdlAction = WsdlUtils.getDefaultWsaAction(messageExchange.getOperation(), true);
            // Wsa:Action is assertion text, not property tag
            assertProperty("Wsa:Action", "Action", defaultWsdlAction);
        }
        validateWsAddressingCommon(content);

        if (wsaAssertionConfiguration.isAssertRelatesTo()) {
            // RelatesTo is Mandatory
            Element relatesToNode;
            if (!StringUtils.isNullOrEmpty(wsaVersionNameSpace)) {
                relatesToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "RelatesTo");
                parseRelatesToNode(soapVersion, requestXmlObject, relatesToNode);
            } else {
                relatesToNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200508, "RelatesTo");
                if (relatesToNode == null) {
                    relatesToNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200408, "RelatesTo");
                }
                parseRelatesToNode(soapVersion, requestXmlObject, relatesToNode);
            }
        }
        // if fault_to is specified check if anonymous allowed
        Element replyToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "ReplyTo");
        if (replyToNode != null) {
            Element addressNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaVersionNameSpace, "Address");
            if (addressNode != null) {
                String replyToAddressValue = XmlUtils.getElementText(addressNode);
                if (!StringUtils.isNullOrEmpty(replyToAddressValue)) {
                    // check for anonymous
                    if (AnonymousTypeConfig.PROHIBITED.toString().equals(
                            ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous())
                            && WsaUtils.isAnonymousAddress(replyToAddressValue, wsaVersionNameSpace)) {
                        cumulativeErrorMsg
                                .append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are prohibited. ");
                    } else if (AnonymousTypeConfig.REQUIRED.toString().equals(
                            ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous())
                            && !(WsaUtils.isAnonymousAddress(replyToAddressValue, wsaVersionNameSpace) || WsaUtils
                            .isNoneAddress(replyToAddressValue, wsaVersionNameSpace))) {
                        cumulativeErrorMsg
                                .append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are required. ");
                    }
                }
            }
        }
        if (wsaAssertionConfiguration.isAssertReplyToRefParams()) {
            // check if request ReplyTo ReferenceParameters are included in
            // response
            NodeList requestReplyToRefProps = WsdlUtils.getRequestReplyToRefProps(messageExchange,
                    getWsaVersion(requestXmlObject, soapVersion));
            for (int i = 0; i < requestReplyToRefProps.getLength(); i++) {
                Node refProp = requestReplyToRefProps.item(i);
                String refPropName = refProp.getNodeName();
                NodeList existingResponseRefs = XmlUtils.getChildElementsByTagName(header, refPropName);
                if (existingResponseRefs != null && existingResponseRefs.getLength() > 0) {
                    // TODO check if tag is well formed: wsa:IsReferenceParameter
                    continue;
                } else {
                    cumulativeErrorMsg.append("Response does not have request ReferenceProperty " + refPropName + ". ");
                }

            }
        }
        if (wsaAssertionConfiguration.isAssertFaultToRefParams()) {
            // check if request FaultTo ReferenceParameters are included in
            // response
            NodeList requestFaultToRefProps = WsdlUtils.getRequestFaultToRefProps(messageExchange,
                    getWsaVersion(requestXmlObject, soapVersion));
            for (int i = 0; i < requestFaultToRefProps.getLength(); i++) {
                Node refProp = requestFaultToRefProps.item(i);
                String refPropName = refProp.getNodeName();
                NodeList existingResponseRefs = XmlUtils.getChildElementsByTagName(header, refPropName);
                if (existingResponseRefs != null && existingResponseRefs.getLength() > 0) {
                    continue;
                } else {
                    cumulativeErrorMsg.append("Response does not have request ReferenceProperty " + refPropName + ". ");
                }

            }
        }
        String cumulativeError = cumulativeErrorMsg.toString();
        if (!StringUtils.isNullOrEmpty(cumulativeError)) {
            throw new AssertionException(new AssertionError(cumulativeError));
        }
    }

    private void parseRelatesToNode(SoapVersion soapVersion, XmlObject requestXmlObject, Element relatesToNode) {
        if (relatesToNode == null) {
            cumulativeErrorMsg.append("WS-A RelatesTo property is not specified. ");
        } else {
            String relatesToValue = XmlUtils.getElementText(relatesToNode);
            if (StringUtils.isNullOrEmpty(relatesToValue)) {
                cumulativeErrorMsg.append("WS-A RelatesTo property is empty. ");
            } else {
                String requestMsgId = WsdlUtils.getRequestWsaMessageId(messageExchange,
                        getWsaVersion(requestXmlObject, soapVersion));
                if (!relatesToValue.equals(requestMsgId)) {
                    cumulativeErrorMsg.append("WS-A RelatesTo property is not equal to request wsa:MessageId. ");
                }
            }
            /*
			 * When absent, the implied value of this attribute is
			 * "http://www.w3.org/2005/08/addressing/reply". question is does it
			 * have to be present as 'reply' ???
			 */

            // String relationshipType =
            // relatesToNode.getAttribute("RelationshipType");
            // if (StringUtils.isNullOrEmpty(relationshipType))
            // {
            // relationshipType =
            // relatesToNode.getAttributeNS(WsaUtils.WS_A_VERSION_200508,
            // "RelationshipType");
            // if (StringUtils.isNullOrEmpty(relationshipType))
            // {
            // relationshipType =
            // relatesToNode.getAttributeNS(WsaUtils.WS_A_VERSION_200408,
            // "RelationshipType");
            // if (StringUtils.isNullOrEmpty(relationshipType))
            // {
            // cumulativeErrorMsg.append("WS-A RelationshipType is not specified. ");
            // }
            // }
            // }
        }
    }

    /*
     * asserts specific property
     */
    private void assertProperty(String propertyName, String wsaProperty, String expectedValue) {
        Element propertyNode;

        if (wsaVersionNameSpace != null) {
            propertyNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, wsaProperty);
            parsePropertyNode(propertyName, propertyNode, expectedValue);
        } else {
            propertyNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200508, wsaProperty);
            if (propertyNode == null) {
                propertyNode = XmlUtils.getFirstChildElementNS(header, WsaUtils.WS_A_NAMESPACE_200408, wsaProperty);
            }
            parsePropertyNode(propertyName, propertyNode, expectedValue);
        }
    }

    private void assertProperty(String propertyName, String wsaProperty) {
        assertProperty(propertyName, wsaProperty, null);
    }

    private void parsePropertyNode(String propertyName, Element propertyNode, String expectedValue) {
        if (propertyNode == null) {
            cumulativeErrorMsg.append(propertyName + " property is not specified. ");
        } else {
            String actionValue = XmlUtils.getElementText(propertyNode);
            if (StringUtils.isNullOrEmpty(actionValue)) {
                cumulativeErrorMsg.append(propertyName + " property is empty. ");
            } else if (!StringUtils.isNullOrEmpty(expectedValue)) {
                if (!actionValue.equals(expectedValue)) {
                    cumulativeErrorMsg.append(propertyName + " expecting [" + expectedValue + "], actual value is ["
                            + actionValue + "].");
                }
            }
        }
    }
}
