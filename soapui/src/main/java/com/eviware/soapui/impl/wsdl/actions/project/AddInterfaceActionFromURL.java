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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a WsdlInterface to a WsdlProject from a URL
 *
 * @author Ole.Matzura
 */

public class AddInterfaceActionFromURL extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "AddInterfaceActionFromURL";

    public AddInterfaceActionFromURL() {
        super("Add WSDL from URL", "Adds all interfaces in a specified WSDL URL to the current project");
    }

    public void perform(WsdlProject project, Object param) {
        String url = UISupport.prompt("Enter WSDL URL", "Add WSDL from URL", "");
        if (url == null) {
            return;
        }

        try {
            Boolean createRequests = UISupport.confirmOrCancel("Create default requests for all operations",
                    "Import WSDL");
            if (createRequests == null) {
                return;
            }

            Interface[] ifaces = WsdlInterfaceFactory.importWsdl(project, url, createRequests);
            if (ifaces != null && ifaces.length > 0) {
                UISupport.select(ifaces[0]);
            }
        } catch (InvalidDefinitionException ex) {
            ex.show();
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex.getMessage() + ":" + ex.getCause());
        }
    }
}
