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

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Creates an empty WsdlRequest containing a SOAP Envelope and empty Body
 *
 * @author Ole.Matzura
 */

public class CreateEmptyRequestAction extends AbstractAction {
    private final WsdlRequest request;

    public CreateEmptyRequestAction(WsdlRequest request) {
        super("Create empty");
        this.request = request;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/create_empty_request.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Creates an empty SOAP request");
    }

    public void actionPerformed(ActionEvent e) {
        if (UISupport.confirm("Overwrite existing request?", "Create Empty")) {
            WsdlInterface iface = (WsdlInterface) request.getOperation().getInterface();
            request.setRequestContent(iface.getMessageBuilder().buildEmptyMessage());
        }
    }
}
