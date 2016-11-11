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
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds the specified WsdlMockServices local endpoint to a WsdlInterface
 *
 * @author Ole.Matzura
 */

public class AddEndpointToInterfaceAction extends AbstractSoapUIAction<WsdlMockService> {
    public AddEndpointToInterfaceAction() {
        super("Add Endpoint to Interface", "Adds this MockService's endpoint to the selected Interface");
    }

    public void perform(WsdlMockService mockService, Object param) {
        String[] names = ModelSupport.getNames(mockService.getProject().getInterfaceList(),
                new ModelSupport.InterfaceTypeFilter(WsdlInterfaceFactory.WSDL_TYPE));

        String ifaceName = UISupport.prompt("Select Interface to add MockService endpoint to", "Add Endpoint", names,
                null);

        if (ifaceName != null) {
            WsdlProject project = mockService.getProject();
            AbstractInterface<?> iface = project.getInterfaceByName(ifaceName);
            if (iface != null) {
                iface.addEndpoint(mockService.getLocalEndpoint());
                UISupport.showInfoMessage("Add endpoint [" + mockService.getLocalEndpoint() + "] to " + "Interface ["
                        + ifaceName + "]");
            }
        }
    }
}
