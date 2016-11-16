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
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.ProjectDirProvider;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

/**
 * Adds a WsdlInterface to a WsdlProject from a wsdl file
 *
 * @author Ole.Matzura
 */

public class AddInterfaceActionFromFile extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "AddInterfaceActionFromFile";

    public AddInterfaceActionFromFile() {
        super("Add WSDL from File", "Adds all interfaces in a specified local WSDL file to the current project");
    }

    public void perform(WsdlProject project, Object param) {
        File file = UISupport.getFileDialogs().open(this, "Select WSDL file", ".wsdl", "WSDL Files (*.wsdl)",
                ProjectDirProvider.getProjectFolder(project));
        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();
        if (path == null) {
            return;
        }

        try {
            Boolean createRequests = UISupport.confirmOrCancel("Create default requests for all operations",
                    "Import WSDL");
            if (createRequests == null) {
                return;
            }

            Interface[] ifaces = WsdlInterfaceFactory
                    .importWsdl(project, file.toURI().toURL().toString(), createRequests);
            if (ifaces.length > 0) {
                UISupport.select(ifaces[0]);
            }
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex.getMessage() + ":" + ex.getCause());
        }
    }
}
