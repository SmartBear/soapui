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

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlRequest from its WsdlOperation
 *
 * @author Ole.Matzura
 */

public class DeleteRestRequestAction extends AbstractSoapUIAction<RestRequest> {
    public DeleteRestRequestAction() {
        super("Delete", "Deletes this Request");
    }

    public void perform(RestRequest request, Object param) {
        if (UISupport.confirm("Delete Request [" + request.getName() + "] from Resource ["
                + request.getOperation().getName() + "]", "Delete Request")) {
            RestMethod method = request.getRestMethod();
            method.removeRequest(request);
        }
    }
}
