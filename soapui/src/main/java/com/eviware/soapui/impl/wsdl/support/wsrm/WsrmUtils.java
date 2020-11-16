/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support.wsrm;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.WsaConfigConfig;
import com.eviware.soapui.config.WsrmConfigConfig;
import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.support.wsa.WsaRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainerImpl;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.UUID;

public class WsrmUtils {
    private static final String WSRM_CREATE_SEQUENCE = "CreateSequence";
    private static final String WSRM_REQUEST_ACK = "AckRequested";
    private static final String WSRM_EXPIRES = "Expires";
    private static final String WSRM_ACKNOWLEDGMENTS_TO = "AcksTo";
    private static final String WSRM_CLOSE_SEQUENCE = "CloseSequence";
    private static final String WSRM_IDENTIFIER = "Identifier";
    private static final String WSRM_LAST_MSG = "LastMsgNumber";
    private static final String WSRM_CREATE_SEQUENCE_ACTION = "/CreateSequence";
    private static final String WSRM_CLOSE_SEQUENCE_ACTION = "/CloseSequence";
    private static final String WSRM_TERMINATE_SEQUENCE_ACTION = "/TerminateSequence";
    private static final String WSRM_REQUEST_ACK_ACTION = "/AckRequested";

    public final static String WSRM_NS_1_0 = "http://schemas.xmlsoap.org/ws/2005/02/rm";
    public final static String WSRM_NS_1_1 = "http://docs.oasis-open.org/ws-rx/wsrm/200702";
    public final static String WSRM_NS_1_2 = "http://docs.oasis-open.org/ws-rx/wsrm/200702";

    private SoapVersion soapVersion;
    private XmlObject xmlContentObject;
    private String content;

    public WsrmUtils(SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    public WsrmUtils(String content, SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
        this.content = content;

        try {
            xmlContentObject = XmlUtils.createXmlObject(content);
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public String createNewWSReliableMessagingRequest(WsdlRequest wsrmContainer,
                                                      String identifier, long msgNumber, String endpoint) {

        try {
            Element header = getHeader();

            header.setAttribute("xmlns:" + "wsrm", wsrmContainer.getWsrmConfig().getVersionNameSpace());

            Element sequence = header.getOwnerDocument().createElementNS(
                    wsrmContainer.getWsrmConfig().getVersionNameSpace(), "Sequence");

            Element identifierElement = sequence.getOwnerDocument().createElementNS(
                    wsrmContainer.getWsrmConfig().getVersionNameSpace(), "Identifier");
            Text txtElm = identifierElement.getOwnerDocument().createTextNode(identifier);
            identifierElement.appendChild(txtElm);
            sequence.appendChild(identifierElement);

            Element messageId = sequence.getOwnerDocument().createElementNS(
                    wsrmContainer.getWsrmConfig().getVersionNameSpace(), "MessageNumber");
            Text txtElm2 = identifierElement.getOwnerDocument().createTextNode(String.valueOf(msgNumber));
            messageId.appendChild(txtElm2);
            sequence.appendChild(messageId);

            header.appendChild(sequence);

            content = xmlContentObject.xmlText();

            wsrmContainer.getWsaConfig().setWsaEnabled(true);
            if (wsrmContainer.getWsaConfig().getAction() == null) {
                wsrmContainer.getWsaConfig().setAddDefaultAction(true);
            }

            wsrmContainer.getWsaConfig().setTo(endpoint);
            wsrmContainer.getWsaConfig().setGenerateMessageId(true);

            WsaUtils wsaUtils = new WsaUtils(content, wsrmContainer.getOperation().getInterface().getSoapVersion(), null,
                    new DefaultPropertyExpansionContext(wsrmContainer));
            content = wsaUtils.addWSAddressingRequest(wsrmContainer);

        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return content;
    }

    private Element getHeader() throws XmlException {
        return (Element) SoapUtils.getHeaderElement(xmlContentObject, soapVersion, true).getDomNode();
    }

    public WsrmSequence createSequence(String endpoint, SoapVersion soapVersion, String wsrmNamespace, String ackTo,
                                       Long expires, WsdlOperation operation, String wsaTo, String offerEndpoint) {
        String uuid = UUID.randomUUID().toString();
        WsaRequest startSequenceRequest = buildStartSequenceRequest(endpoint, soapVersion, wsrmNamespace, ackTo, expires, operation, uuid,
                offerEndpoint);

        try {

            Response response = submitCreateSequenceRequest(uuid, startSequenceRequest);
            String responseContent = response.getContentAsString();
            // XmlObject xml = XmlObject.Factory.parse( responseContent );
            XmlObject xml = XmlUtils.createXmlObject(responseContent);
            XmlCursor cursor = xml.newCursor();
            cursor.toFirstContentToken();
            cursor.toFirstChild();
            cursor.toNextSibling();
            cursor.toFirstChild();
            cursor.toFirstChild();
            String sequenceIdentifier = cursor.getTextValue();
            LogManager.getLogger("wsrm").info("Sequence response Received, sequence ID: " + sequenceIdentifier);

            // WsmcInjection receiveInjection = new WsmcInjection(request);
            // request.setAfterRequestInjection(receiveInjection);

            return new WsrmSequence(sequenceIdentifier.trim(), uuid, soapVersion, wsrmNamespace,
                    operation);
        } catch (SubmitException e1) {
            SoapUI.logError(e1);
            return null;
        } catch (XmlException e) {
            SoapUI.logError(e);
            return null;
        }
    }

    private Response submitCreateSequenceRequest(String uuid, WsaRequest startSequenceRequest) throws SubmitException {
        WsdlSubmit wsdlSubmit = startSequenceRequest.submit(new WsdlSubmitContext(null), true);
        LogManager.getLogger("wsrm").info("StartSequence Request Sent: " + uuid);

        // startSequenceRequest.getWsaConfig().setWsaEnabled(false);
        while (wsdlSubmit.getStatus() != Status.FINISHED) {
            wsdlSubmit.waitUntilFinished();
        }
        return wsdlSubmit.getResponse();
    }

    WsaRequest buildStartSequenceRequest(String endpoint, SoapVersion soapVersion, String wsrmNamespace, String ackTo, Long expires, WsdlOperation operation, String uuid, String offerEndpoint) {
        HttpRequestConfig httpRequestConfig = (HttpRequestConfig) (XmlObject.Factory.newInstance()
                .changeType(HttpRequestConfig.type));
        httpRequestConfig.setEndpoint(endpoint);
        httpRequestConfig.setMediaType(soapVersion.getContentType());

        WsaConfigConfig wsaConfigConfig = (WsaConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsaConfigConfig.type));
        WsaContainer wsaContainer = new WsaContainerImpl();
        wsaContainer.setOperation(operation);
        WsaConfig wsaConfig = new WsaConfig(wsaConfigConfig, wsaContainer);
        wsaConfig.setTo(endpoint);

        WsrmConfigConfig wsrmConfigConfig = (WsrmConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsrmConfigConfig.type));
        WsrmConfig wsrmConfig = new WsrmConfig(wsrmConfigConfig, null);

        WsaRequest startSequenceRequest = new WsaRequest(httpRequestConfig, wsaConfig, wsrmConfig, false);
        startSequenceRequest.setOperation(operation);

        String openSequenceMessageContent = SoapMessageBuilder.buildEmptyMessage(soapVersion);

        startSequenceRequest.getWsaConfig().setWsaEnabled(true);
        startSequenceRequest.getWsaConfig().setAction(wsrmNamespace + WSRM_CREATE_SEQUENCE_ACTION);

        // if (wsaTo == null) {
        // startSequenceRequest.getWsaConfig().setTo(
        // WsaUtils.getNamespace(startSequenceRequest.getWsaConfig()
        // .getVersion())
        // + "/anonymous?id="+ uuid);
        // } else {
        // startSequenceRequest.getWsaConfig().setTo(wsaTo);
        // }
        startSequenceRequest.getWsaConfig().setReplyTo(ackTo);
        startSequenceRequest.getWsaConfig().setGenerateMessageId(true);

        try {
            XmlObject object = XmlUtils.createXmlObject(openSequenceMessageContent);
            XmlCursor cursor = object.newCursor();

            cursor.toFirstContentToken();
            cursor.toFirstChild();
            cursor.toNextSibling();

            cursor.toNextToken();
            cursor.insertNamespace("wsrm", wsrmNamespace);

            cursor.beginElement(WSRM_CREATE_SEQUENCE, wsrmNamespace);
            String wsaNamespace = wsrmNamespace.equals(WSRM_NS_1_0) ? WsaUtils.WS_A_NAMESPACE_200408 : WsaUtils.WS_A_NAMESPACE_200508;
            if (!wsrmNamespace.equals(WSRM_NS_1_0) && !StringUtils.isNullOrEmpty(offerEndpoint)) {
                cursor.beginElement("Offer", wsrmNamespace);

                cursor.beginElement("Endpoint", wsrmNamespace);
                cursor.beginElement("Address", wsaNamespace);
                cursor.insertChars(offerEndpoint);
                cursor.toParent();
                cursor.toParent();
                cursor.beginElement("Identifier", wsrmNamespace);
                cursor.insertChars("urn:soapui:" + uuid);

                cursor.toParent();
                cursor.toParent();
            }

            cursor.beginElement(WSRM_ACKNOWLEDGMENTS_TO, wsrmNamespace);
            cursor.insertNamespace("wsa", wsaNamespace);
            cursor.beginElement("Address", wsaNamespace);
            if (ackTo == null || ackTo.length() < 1) {
                ackTo = WsaUtils.getNamespace(startSequenceRequest.getWsaConfig().getVersion()) + "/anonymous" + "?id="
                        + uuid;
            }
            cursor.insertChars(ackTo);
            // cursor.insertChars(request.getWsrmConfig().getAckTo());

            if (expires != 0) {
                cursor.toParent();

                cursor.beginElement(WSRM_EXPIRES, wsrmNamespace);
                cursor.insertChars(expires.toString());
            }

            cursor.dispose();

            WsaUtils wsaUtils = new WsaUtils(object.xmlText(), soapVersion, null, new DefaultPropertyExpansionContext(
                    startSequenceRequest));
            content = wsaUtils.addWSAddressingRequest(startSequenceRequest);

            startSequenceRequest.setRequestContent(content);

        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return startSequenceRequest;
    }

    public static String getWsrmVersionNamespace(WsrmVersionTypeConfig.Enum wsrmVersion) {
        if (wsrmVersion == WsrmVersionTypeConfig.X_1_0) {
            return WSRM_NS_1_0;
        } else if (wsrmVersion == WsrmVersionTypeConfig.X_1_1) {
            return WSRM_NS_1_1;
        } else {
            return WSRM_NS_1_2;
        }
    }

    public void closeSequence(String endpoint, SoapVersion soapVersion, String wsrmNamespace, String uuid,
                              String identifier, long lastMsgNum, WsdlOperation operation) {

        HttpRequestConfig httpRequestConfig = (HttpRequestConfig) (XmlObject.Factory.newInstance()
                .changeType(HttpRequestConfig.type));
        httpRequestConfig.setEndpoint(endpoint);
        httpRequestConfig.setMediaType(soapVersion.getContentType());

        WsaConfigConfig wsaConfigConfig = (WsaConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsaConfigConfig.type));
        WsaContainer wsaContainer = new WsaContainerImpl();
        wsaContainer.setOperation(operation);
        WsaConfig wsaConfig = new WsaConfig(wsaConfigConfig, wsaContainer);

        WsrmConfigConfig wsrmConfigConfig = (WsrmConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsrmConfigConfig.type));
        WsrmConfig wsrmConfig = new WsrmConfig(wsrmConfigConfig, null);

        if (!wsrmNamespace.equals(WSRM_NS_1_0)) {
            WsaRequest closeSequenceRequest = new WsaRequest(httpRequestConfig, wsaConfig, wsrmConfig, false);
            closeSequenceRequest.setOperation(operation);

            String openSequenceMessageContent = SoapMessageBuilder.buildEmptyMessage(soapVersion);

            closeSequenceRequest.getWsaConfig().setWsaEnabled(true);
            closeSequenceRequest.getWsaConfig().setAction(wsrmNamespace + WSRM_CLOSE_SEQUENCE_ACTION);

            closeSequenceRequest.getWsaConfig().setTo(endpoint);
            closeSequenceRequest.getWsaConfig().setGenerateMessageId(true);

            try {
                // XmlObject object = XmlObject.Factory.parse(
                // openSequenceMessageContent );
                XmlObject object = XmlUtils.createXmlObject(openSequenceMessageContent);
                XmlCursor cursor = object.newCursor();

                cursor.toFirstContentToken();
                cursor.toFirstChild();
                cursor.toNextSibling();

                cursor.toNextToken();
                cursor.insertNamespace("wsrm", wsrmNamespace);

                cursor.beginElement(WSRM_CLOSE_SEQUENCE, wsrmNamespace);

                cursor.beginElement(WSRM_IDENTIFIER, wsrmNamespace);
                cursor.insertChars(identifier);

                cursor.toParent();

                cursor.beginElement(WSRM_LAST_MSG, wsrmNamespace);
                cursor.insertChars(String.valueOf(lastMsgNum));
                cursor.dispose();

                WsaUtils wsaUtils = new WsaUtils(object.xmlText(), soapVersion, null, new DefaultPropertyExpansionContext(
                        closeSequenceRequest));
                content = wsaUtils.addWSAddressingRequest(closeSequenceRequest);

                closeSequenceRequest.setRequestContent(content);

                LogManager.getLogger("wsrm").info("CloseSequence Request Sent for Sequence: " + identifier);

            } catch (XmlException e) {
                SoapUI.logError(e);
            }

            try {

                WsdlSubmit wsdlSubmit = closeSequenceRequest.submit(new WsdlSubmitContext(null), true);
                while (wsdlSubmit.getStatus() != Status.FINISHED) {
                    wsdlSubmit.waitUntilFinished();
                }
                Response response = wsdlSubmit.getResponse();
                String responseContent = response.getContentAsString();
                // XmlObject xml = XmlObject.Factory.parse( responseContent );
                XmlObject xml = XmlUtils.createXmlObject(responseContent);

                XmlOptions options = new XmlOptions();

                String namespaceDeclaration = "declare namespace wsrm='" + wsrmNamespace + "';";
                XmlObject result[] = xml.selectPath(namespaceDeclaration + "//wsrm:AcknowledgementRange", options);

                if (result.length > 0) {
                    for (XmlObject aResult : result) {
                        String upper = aResult.selectAttribute(null, "Upper").getDomNode().getNodeValue();
                        String lower = aResult.selectAttribute(null, "Lower").getDomNode().getNodeValue();

                        if (lower.equals(upper)) {
                            LogManager.getLogger("wsrm").info(
                                    "Acknowledgment for message " + upper + " received for identifier: " + identifier);
                        } else {
                            LogManager.getLogger("wsrm").info(
                                    "Acknowledgment for messages " + lower + " to " + upper + " received for identifier: "
                                            + identifier);
                        }
                    }
                } else {
                    LogManager.getLogger("wsrm").info("No Acknowledgments received for identifier: " + identifier);
                }

            } catch (SubmitException e1) {
                SoapUI.logError(e1);

            } catch (XmlException e) {
                SoapUI.logError(e);
            }
        }

        // The Terminate Sequence Message
        WsaRequest terminateSequenceRequest = new WsaRequest(httpRequestConfig, wsaConfig, wsrmConfig, false);
        terminateSequenceRequest.setOperation(operation);

        String terminateSequenceRequestContent = SoapMessageBuilder.buildEmptyMessage(soapVersion);

        terminateSequenceRequest.getWsaConfig().setWsaEnabled(true);
        terminateSequenceRequest.getWsaConfig().setAction(wsrmNamespace + WSRM_TERMINATE_SEQUENCE_ACTION);

        terminateSequenceRequest.getWsaConfig().setTo(endpoint);
        terminateSequenceRequest.getWsaConfig().setGenerateMessageId(true);

        try {
            // XmlObject object = XmlObject.Factory.parse(
            // terminateSequenceRequestContent );
            XmlObject object = XmlUtils.createXmlObject(terminateSequenceRequestContent);
            XmlCursor cursor = object.newCursor();

            cursor.toFirstContentToken();
            cursor.toFirstChild();
            cursor.toNextSibling();

            cursor.toNextToken();
            cursor.insertNamespace("wsrm", wsrmNamespace);

            cursor.beginElement("TerminateSequence", wsrmNamespace);
            cursor.beginElement(WSRM_IDENTIFIER, wsrmNamespace);
            cursor.insertChars(identifier);

            cursor.dispose();

            // startSequenceRequest.getOperation().setAction("");
            // startSequenceRequest.setRequestContent(object.xmlText());

            WsaUtils wsaUtils = new WsaUtils(object.xmlText(), soapVersion, null, new DefaultPropertyExpansionContext(
                    terminateSequenceRequest));
            terminateSequenceRequestContent = wsaUtils.addWSAddressingRequest(terminateSequenceRequest);

            terminateSequenceRequest.setRequestContent(terminateSequenceRequestContent);

        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            terminateSequenceRequest.submit(new WsdlSubmitContext(null), true);
        } catch (SubmitException e1) {
            SoapUI.logError(e1);
        }
    }

    public void getAcks(String endpoint, SoapVersion soapVersion, String wsrmNamespace, String uuid, String identifier,
                        WsdlOperation operation) {

        HttpRequestConfig httpRequestConfig = (HttpRequestConfig) (XmlObject.Factory.newInstance()
                .changeType(HttpRequestConfig.type));
        httpRequestConfig.setEndpoint(endpoint);
        httpRequestConfig.setMediaType(soapVersion.getContentType());

        WsaConfigConfig wsaConfigConfig = (WsaConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsaConfigConfig.type));
        WsaContainer wsaContainer = new WsaContainerImpl();
        wsaContainer.setOperation(operation);
        WsaConfig wsaConfig = new WsaConfig(wsaConfigConfig, wsaContainer);

        WsrmConfigConfig wsrmConfigConfig = (WsrmConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsrmConfigConfig.type));
        WsrmConfig wsrmConfig = new WsrmConfig(wsrmConfigConfig, null);

        WsaRequest startSequenceRequest = new WsaRequest(httpRequestConfig, wsaConfig, wsrmConfig, false);
        startSequenceRequest.setOperation(operation);

        String openSequenceMessageContent = SoapMessageBuilder.buildEmptyMessage(soapVersion);

        startSequenceRequest.getWsaConfig().setWsaEnabled(true);
        startSequenceRequest.getWsaConfig().setAction(wsrmNamespace + WSRM_REQUEST_ACK_ACTION);

        startSequenceRequest.getWsaConfig().setTo(endpoint);
        startSequenceRequest.getWsaConfig().setGenerateMessageId(true);

        try {
            // XmlObject object = XmlObject.Factory.parse(
            // openSequenceMessageContent );
            XmlObject object = XmlUtils.createXmlObject(openSequenceMessageContent);
            XmlCursor cursor = object.newCursor();

            cursor.toFirstContentToken();
            cursor.toFirstChild();
            cursor.toNextSibling();

            cursor.toNextToken();
            cursor.insertNamespace("wsrm", wsrmNamespace);

            cursor.beginElement(WSRM_REQUEST_ACK, wsrmNamespace);

            cursor.beginElement(WSRM_IDENTIFIER, wsrmNamespace);
            cursor.insertChars(identifier);

            cursor.dispose();

            WsaUtils wsaUtils = new WsaUtils(object.xmlText(), soapVersion, null, new DefaultPropertyExpansionContext(
                    startSequenceRequest));
            content = wsaUtils.addWSAddressingRequest(startSequenceRequest);

            startSequenceRequest.setRequestContent(content);

            // WsmcInjection wsmcInjection = new WsmcInjection(endpoint,
            // operation, soapVersion, uuid);
            // startSequenceRequest.setAfterRequestInjection(wsmcInjection);

            LogManager.getLogger("wsrm").info("Acknowledgments Requested for Sequence: " + identifier);

        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            WsdlSubmit wsdlSubmit = startSequenceRequest.submit(new WsdlSubmitContext(null), true);

            while (wsdlSubmit.getStatus() != Status.FINISHED) {
                wsdlSubmit.waitUntilFinished();
            }
            Response response = wsdlSubmit.getResponse();
            String responseContent = response.getContentAsString();
            // XmlObject xml = XmlObject.Factory.parse( responseContent );
            XmlObject xml = XmlUtils.createXmlObject(responseContent);
            XmlObject result = xml.selectPath("Envelope/Header/SequenceAcknowledgment")[0];
        } catch (SubmitException e1) {
            SoapUI.logError(e1);

        } catch (XmlException e) {
            SoapUI.logError(e);
        }
    }
}
