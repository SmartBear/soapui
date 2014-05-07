/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlMockOperation
 *
 * @author Ole.Matzura
 */

public class RenameMockOperationAction extends AbstractSoapUIAction<AbstractMockOperation> {
    public RenameMockOperationAction() {
        super("Rename", "Renames this node");
    }

    public void perform(AbstractMockOperation mockOperation, Object param) {
        String nodeName = mockOperation instanceof RestMockAction ? "RestMockAction" : "MockOperation";
        String name = UISupport.prompt("Specify name of " + nodeName, "Rename " + nodeName, mockOperation.getName());
        if (name == null || name.equals(mockOperation.getName())) {
            return;
        }

        mockOperation.setName(name);
    }

}
