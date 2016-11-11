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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a new WsdlMockOperation to a WsdlMockService
 *
 * @author Ole.Matzura
 */

public class AddNewMockOperationAction extends AbstractSoapUIAction<WsdlMockService> {
    public final static String SOAPUI_ACTION_ID = "AddNewMockOperationAction";

    public AddNewMockOperationAction() {
        super("New MockOperation", "Creates a new MockOperation for this MockService");
    }

    public void perform(WsdlMockService mockService, Object param) {
        List<OperationWrapper> operations = new ArrayList<OperationWrapper>();

        WsdlProject project = mockService.getProject();
        List<AbstractInterface<?>> interfaces = project.getInterfaces(WsdlInterfaceFactory.WSDL_TYPE);

        for (Interface iface : interfaces) {
            for (int i = 0; i < iface.getOperationCount(); i++) {
                if (!mockService.hasMockOperation(iface.getOperationAt(i))) {
                    operations.add(new OperationWrapper((WsdlOperation) iface.getOperationAt(i)));
                }
            }
        }

        if (operations.isEmpty()) {
            UISupport.showErrorMessage("No unique operations to mock in project!");
            return;
        }

        Object result = UISupport.prompt("Select Operation to Mock", "New MockOperation", operations.toArray());
        if (result != null) {
            WsdlOperation operation = ((OperationWrapper) result).getOperation();
            WsdlMockOperation mockOperation = (WsdlMockOperation) mockService.addNewMockOperation(operation);
            WsdlMockResponse mockResponse = mockOperation.addNewMockResponse("Response 1", true);
            UISupport.selectAndShow(mockResponse);
        }
    }

    public class OperationWrapper {
        private final WsdlOperation operation;

        public OperationWrapper(WsdlOperation operation) {
            this.operation = operation;
        }

        public WsdlOperation getOperation() {
            return operation;
        }

        public String toString() {
            return operation.getInterface().getName() + " - " + operation.getName();
        }
    }
}
