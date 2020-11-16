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

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.editor.xml.XmlInspector;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class WsdlRequestWsrmPiggybackInspector extends AbstractWsrmInspector implements XmlInspector, SubmitListener {

    private final WsdlRequest request;

    protected WsdlRequestWsrmPiggybackInspector(WsdlRequest request) {
        super(request);
        request.addSubmitListener(this);
        this.request = request;
    }

    @Override
    public void release() {
        super.release();
        request.removeSubmitListener(this);
    }

    public void afterSubmit(Submit submit, SubmitContext context) {

        if (request.getWsrmConfig().isWsrmEnabled() && submit.getResponse() != null) {
            String content = submit.getResponse().getContentAsString();
            XmlOptions options = new XmlOptions();
            try {
                // XmlObject xml = XmlObject.Factory.parse( content );
                XmlObject xml = XmlUtils.createXmlObject(content);

                String namespaceDeclaration = "declare namespace wsrm='" + request.getWsrmConfig().getVersionNameSpace()
                        + "';";
                XmlObject result[] = xml.selectPath(namespaceDeclaration + "//wsrm:AcknowledgementRange", options);

                if (result.length > 0) {
                    for (int i = 0; i < result.length; i++) {
                        String upper = result[i].selectAttribute(null, "Upper").getDomNode().getNodeValue();
                        String lower = result[i].selectAttribute(null, "Lower").getDomNode().getNodeValue();

                        if (lower == upper) {
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

    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {

        return true;
    }

}
