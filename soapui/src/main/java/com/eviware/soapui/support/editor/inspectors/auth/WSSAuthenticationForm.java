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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.types.StringList;

public class WSSAuthenticationForm extends BasicAuthenticationForm<WsdlRequest> {
    protected WSSAuthenticationForm(WsdlRequest request) {
        super(request);
    }

    @Override
    protected void populateBasicForm(SimpleBindingForm basicForm) {
        super.populateBasicForm(basicForm);
        StringList outgoingNames = getOutgoingNames(request);
        StringList incomingNames = getIncomingNames(request);

        basicForm.addSpace(GROUP_SPACING);
        basicForm.appendComboBox("outgoingWss", "Outgoing WSS", outgoingNames.toStringArray(),
                "The outgoing WS-Security configuration to use");
        basicForm.appendComboBox("incomingWss", "Incoming WSS", incomingNames.toStringArray(),
                "The incoming WS-Security configuration to use");
    }

    private StringList getIncomingNames(WsdlRequest request) {
        StringList incomingNames = new StringList(request.getOperation().getInterface().getProject()
                .getWssContainer().getIncomingWssNames());
        incomingNames.add("");
        return incomingNames;
    }

    private StringList getOutgoingNames(WsdlRequest request) {
        StringList outgoingNames = new StringList(request.getOperation().getInterface().getProject()
                .getWssContainer().getOutgoingWssNames());
        outgoingNames.add("");
        return outgoingNames;
    }
}
