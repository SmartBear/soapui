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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Creates a request from the specified TestStepResult
 *
 * @author Ole.Matzura
 */

public class CreateRequestAction extends AbstractAction {
    private final WsdlTestRequestStepResult result;

    public CreateRequestAction(WsdlTestStepResult result) {
        this.result = (WsdlTestRequestStepResult) result;

        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/create_request_from_result.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Creates a new request from this result");
    }

    public void actionPerformed(ActionEvent e) {
        WsdlTestRequestStep step = (WsdlTestRequestStep) result.getTestStep();
        String name = UISupport.prompt("Specify name of request", "Create Request", "Result from " + step.getName());

        if (name != null) {
            WsdlOperation operation = (WsdlOperation) step.getTestRequest().getOperation();
            WsdlRequest request = operation.addNewRequest(name);
            request.setRequestContent(result.getRequestContent());
            request.setDomain(result.getDomain());
            request.setEncoding(result.getEncoding());
            request.setEndpoint(result.getEndpoint());
            request.setPassword(result.getPassword());
            request.setUsername(result.getUsername());

            UISupport.showDesktopPanel(request);
        }
    }
}
