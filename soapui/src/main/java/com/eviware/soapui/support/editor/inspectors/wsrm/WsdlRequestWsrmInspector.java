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

package com.eviware.soapui.support.editor.inspectors.wsrm;

import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsmc.WsmcInjection;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmContainer;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmSequence;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmUtils;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.xml.XmlInspector;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class WsdlRequestWsrmInspector extends AbstractWsrmInspector implements XmlInspector, SubmitListener {
    private final WsdlRequest request;

    public WsdlRequestWsrmInspector(WsdlRequest request) {
        super(request);

        request.addSubmitListener(this);
        this.request = request;
    }

    public void buildContent(SimpleBindingForm form) {
        form.addSpace(5);
        form.appendCheckBox("wsrmEnabled", "Enable WS-Reliable Messaging", "");
        form.addSpace(5);

        form.appendComboBox("version", "WS-RM Version", new String[]{WsrmVersionTypeConfig.X_1_0.toString(),
                WsrmVersionTypeConfig.X_1_1.toString(), WsrmVersionTypeConfig.X_1_2.toString()},
                "The  property for managing WS-RM version");

        form.appendTextField("ackTo", "Acknowledgment to",
                "The acknowledgment endpoint reference, will be generated if left empty");
        form.appendTextField("offerEndpoint", "Offer endpoint",
                "The endpoint address included in the Offer element");

        form.addSpace(5);
    }

    @Override
    public void release() {
        super.release();
        request.removeSubmitListener(this);
    }

    public void afterSubmit(Submit submit, SubmitContext context) {
        WsrmContainer container = (WsrmContainer) submit.getRequest();
        if (request.getWsrmConfig().isWsrmEnabled() && submit.getResponse() != null) {
            String content = submit.getResponse().getContentAsString();
            XmlOptions options = new XmlOptions();
            try {
                XmlObject xml = XmlUtils.createXmlObject(content);

                String namespaceDeclaration = "declare namespace wsrm='" + request.getWsrmConfig().getVersionNameSpace()
                        + "';";
                XmlObject result[] = xml.selectPath(namespaceDeclaration + "//wsrm:AcknowledgementRange", options);

                if (result.length > 0) {
                    for (XmlObject aResult : result) {
                        String upper = aResult.selectAttribute(null, "Upper").getDomNode().getNodeValue();
                        String lower = aResult.selectAttribute(null, "Lower").getDomNode().getNodeValue();

                        if (lower.equals(upper)) {
                            LogManager.getLogger("wsrm").info(
                                    "Acknowledgment for message " + upper + " received for identifier: "
                                            + request.getWsrmConfig().getSequenceIdentifier());
                        } else {
                            LogManager.getLogger("wsrm").info(
                                    "Acknowledgment for messages " + lower + " to " + upper + " received for identifier: "
                                            + request.getWsrmConfig().getSequenceIdentifier());
                        }
                    }
                }
            } catch (XmlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (container.getWsrmConfig().isWsrmEnabled()) {
            WsdlInterface iface = request.getOperation().getInterface();
            WsrmUtils utils = new WsrmUtils(iface.getSoapVersion());
            utils.closeSequence(request.getEndpoint(), iface.getSoapVersion(), request.getWsrmConfig()
                    .getVersionNameSpace(), request.getWsrmConfig().getUuid(), request.getWsrmConfig()
                    .getSequenceIdentifier(), 1l, request.getOperation());
        }
    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        WsrmContainer container = (WsrmContainer) submit.getRequest();
        if (container.getWsrmConfig().isWsrmEnabled()) {
            WsdlInterface iface = request.getOperation().getInterface();
            WsrmUtils utils = new WsrmUtils(iface.getSoapVersion());

            WsrmSequence sequence = utils.createSequence(request.getEndpoint(), iface.getSoapVersion(), request
                    .getWsrmConfig().getVersionNameSpace(), request.getWsrmConfig().getAckTo(), 0l, request.getOperation(),
                    ((WsdlRequest) submit.getRequest()).getWsaConfig().getTo(), request.getWsrmConfig().getOfferEndpoint());

            request.getWsrmConfig().setSequenceIdentifier(sequence.getIdentifier());
            request.getWsrmConfig().setUuid(sequence.getUuid());

            if (!request.getWsrmConfig().getVersion().equals(WsrmVersionTypeConfig.X_1_0.toString())) {
                WsmcInjection receiveInjection = new WsmcInjection(request.getEndpoint(), request.getOperation(),
                        iface.getSoapVersion(), request.getWsrmConfig().getUuid());
                request.setAfterRequestInjection(receiveInjection);
            }

        }
        return true;
    }

}
