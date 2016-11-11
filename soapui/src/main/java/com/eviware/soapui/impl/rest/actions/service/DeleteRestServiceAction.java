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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.actions.iface.RemoveInterfaceAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlRequest from its WsdlOperation
 *
 * @author Ole.Matzura
 */

public class DeleteRestServiceAction extends AbstractSoapUIAction<RestService> {
    public DeleteRestServiceAction() {
        super("Delete", "Deletes this Service");
    }

    public void perform(RestService service, Object param) {
        if (RemoveInterfaceAction.hasRunningDependingTests(service)) {
            UISupport.showErrorMessage("Cannot remove Service due to running depending tests");
            return;
        }

        if (UISupport.confirm("Delete Service [" + service.getName() + "] from Project?", "Delete Service")) {
            if (RemoveInterfaceAction.hasDependingTests(service)) {
                if (!UISupport.confirm("Service has depending TestSteps which will also be removed. Remove anyway?",
                        "Remove Service")) {
                    return;
                }
            }

            service.getProject().removeInterface(service);
        }
    }
}
