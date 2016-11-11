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

package com.eviware.soapui.impl.wsdl.actions.request;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Recreates a WsdlRequest from its WsdlOperations schema definition
 *
 * @author Ole.Matzura
 */

public class RecreateRequestAction extends AbstractAction {
    private final WsdlRequest request;

    public RecreateRequestAction(WsdlRequest request) {
        super("Recreate request");
        this.request = request;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/recreate_request.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Recreates a default request from the schema");
    }

    public void actionPerformed(ActionEvent e) {
        boolean createOptional = request.getSettings().getBoolean(
                WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS);
        if (!createOptional) {
            Boolean create = UISupport.confirmOrCancel("Create optional elements in schema?", "Create Request");
            if (create == null) {
                return;
            }

            createOptional = create.booleanValue();
        }

        WsdlOperation wsdlOperation = (WsdlOperation) request.getOperation();
        String req = wsdlOperation.createRequest(createOptional);
        if (req == null) {
            UISupport.showErrorMessage("Request creation failed");
            return;
        }

        if (request.getRequestContent() != null && request.getRequestContent().trim().length() > 0) {
            if (UISupport.confirm("Keep existing values", "Recreate Request")) {
                req = SoapUtils.transferSoapHeaders(request.getRequestContent(), req, wsdlOperation.getInterface()
                        .getSoapVersion());

                req = XmlUtils.transferValues(request.getRequestContent(), req);
            }
        }

        request.setRequestContent(req);
    }
}
