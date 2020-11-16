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

package com.eviware.soapui.impl.wsdl.support.wsmc;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.WsaConfigConfig;
import com.eviware.soapui.config.WsrmConfigConfig;
import com.eviware.soapui.impl.support.wsa.WsaRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainerImpl;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmConfig;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public class WsmcUtils {
    private static final String WSMC_ACTION = "http://docs.oasis-open.org/ws-rx/wsmc/200702/MakeConnection";
    private static final String WSMC_NAMESPACE = "http://docs.oasis-open.org/ws-rx/wsmc/200702";

    public void sendMakeConnectionRequest(String endpoint, SoapVersion soapVersion, WsdlOperation operation, String uuid) {
        String identifier = null;

        HttpRequestConfig httpRequestConfig = (HttpRequestConfig) (XmlObject.Factory.newInstance()
                .changeType(HttpRequestConfig.type));
        httpRequestConfig.setEndpoint(endpoint);

        WsaConfigConfig wsaConfigConfig = (WsaConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsaConfigConfig.type));
        WsaContainer wsaContainer = new WsaContainerImpl();
        wsaContainer.setOperation(operation);
        WsaConfig wsaConfig = new WsaConfig(wsaConfigConfig, wsaContainer);

        WsrmConfigConfig wsrmConfigConfig = (WsrmConfigConfig) (XmlObject.Factory.newInstance()
                .changeType(WsrmConfigConfig.type));
        WsrmConfig wsrmConfig = new WsrmConfig(wsrmConfigConfig, null);

        WsaRequest makeConnectionRequest = new WsaRequest(httpRequestConfig, wsaConfig, wsrmConfig, false);
        makeConnectionRequest.setOperation(operation);

        String makeConnectionMessageContent = SoapMessageBuilder.buildEmptyMessage(soapVersion);

        makeConnectionRequest.getWsaConfig().setWsaEnabled(true);
        makeConnectionRequest.getWsaConfig().setAction(WSMC_ACTION);

        makeConnectionRequest.getWsaConfig().setTo(
                WsaUtils.getNamespace(makeConnectionRequest.getWsaConfig().getVersion()) + "/anonymous");
        makeConnectionRequest.getWsaConfig().setGenerateMessageId(true);

        try {
            // XmlObject object = XmlObject.Factory.parse(
            // makeConnectionMessageContent );
            XmlObject object = XmlUtils.createXmlObject(makeConnectionMessageContent);
            XmlCursor cursor = object.newCursor();

            cursor.toFirstContentToken();
            cursor.toFirstChild();
            cursor.toNextSibling();

            cursor.toNextToken();
            cursor.insertNamespace("wsmc", WSMC_NAMESPACE);

            cursor.beginElement("MakeConnection", WSMC_NAMESPACE);
            cursor.beginElement("Address", WSMC_NAMESPACE);
            cursor.insertChars(WsaUtils.getNamespace(makeConnectionRequest.getWsaConfig().getVersion())
                    + "/anonymous?id=" + uuid);

            cursor.dispose();

            cursor.dispose();

            WsaUtils wsaUtils = new WsaUtils(object.xmlText(), soapVersion, null, new DefaultPropertyExpansionContext(
                    makeConnectionRequest));
            String content = wsaUtils.addWSAddressingRequest(makeConnectionRequest);

            makeConnectionRequest.setRequestContent(content);

        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            WsdlSubmit wsdlSubmit = makeConnectionRequest.submit(new WsdlSubmitContext(null), true);

            // startSequenceRequest.getWsaConfig().setWsaEnabled(false);
            while (wsdlSubmit.getStatus() != Status.FINISHED) {
                wsdlSubmit.waitUntilFinished();
            }
            Response response = wsdlSubmit.getResponse();
            String responseContent = response.getContentAsString();
            // XmlObject xml = XmlObject.Factory.parse( responseContent );
            XmlObject xml = XmlUtils.createXmlObject(responseContent);
            XmlCursor cursor = xml.newCursor();
            cursor.toFirstContentToken();
            cursor.toFirstChild();
            cursor.toNextSibling();
            cursor.toFirstChild();

            String sequenceIdentifier = cursor.getTextValue();
            LogManager.getLogger("wsrm").info("Sequence response Received, sequence ID: " + sequenceIdentifier);

            // WsmcInjection receiveInjection = new WsmcInjection(request);
            // request.setAfterRequestInjection(receiveInjection);

        } catch (SubmitException e1) {
            SoapUI.logError(e1);
        } catch (XmlException e) {
            SoapUI.logError(e);
        }
    }
}
