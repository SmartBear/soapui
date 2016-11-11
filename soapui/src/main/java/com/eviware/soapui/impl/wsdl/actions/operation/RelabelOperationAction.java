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

package com.eviware.soapui.impl.wsdl.actions.operation;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Changes the label of a WsdlOperation as shown in SoapUI
 *
 * @author Ole.Matzura
 */

public class RelabelOperationAction extends AbstractSoapUIAction<WsdlOperation> {
    public RelabelOperationAction() {
        super("Relabel", "Relabel this operation");
    }

    public void perform(WsdlOperation operation, Object param) {
        String name = UISupport.prompt("Specify label for operation\n(will not change underlying wsdl operation name)",
                "Relabel Operation", operation.getName());
        if (name == null || name.equals(operation.getName())) {
            return;
        }

        operation.setName(name);
    }
}
