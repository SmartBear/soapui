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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.project.EndpointStrategy;

/**
 * RequestFilter for stripping whitespaces
 *
 * @author Ole.Matzura
 */

public class EndpointStrategyRequestFilter extends AbstractRequestFilter {
    public void filterRequest(SubmitContext context, Request wsdlRequest) {
        Operation operation = wsdlRequest.getOperation();
        if (operation != null) {
            EndpointStrategy endpointStrategy = operation.getInterface().getProject().getEndpointStrategy();
            if (endpointStrategy != null) {
                endpointStrategy.filterRequest(context, wsdlRequest);
            }
        }
    }
}
